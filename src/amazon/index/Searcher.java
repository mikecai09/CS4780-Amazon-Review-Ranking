package amazon.index;

import java.io.File;
import java.io.IOException;
import java.util.*;
import amazon.index.similarities.*;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher
{
    private IndexSearcher indexSearcher;
    private SpecialAnalyzer analyzer;
    private static SimpleHTMLFormatter formatter;
    private static final int numFragments = 4;
    private static final ArrayList<String> defaultField = new ArrayList<String>(){
        {
            add("content");
        }
    };//by default, we will only search in the review content field
    private String userIDX = "data/useridx"; //User index path
    /**
     * Sets up the Lucene index Searcher with the specified index.
     *
     * @param indexPath
     *            The path to the desired Lucene index.
     */
    public Searcher(String indexPath)
    {
        try
        {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            indexSearcher = new IndexSearcher(reader);
            analyzer = new SpecialAnalyzer();
            formatter = new SimpleHTMLFormatter("****", "****");           
            
            indexSearcher.setSimilarity(new BM25Similarity());//using default BM25 formula
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public void setSimilarity(Similarity sim)
    {
        indexSearcher.setSimilarity(sim);
    }

    /**
     * The main search function.
     * @param searchQuery Set this object's attributes as needed.
     * @return
     */
    public SearchResult search(SearchQuery searchQuery) {
        BooleanQuery combinedQuery = new BooleanQuery();
        for(String field: searchQuery.fields())
        {
            QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
            parser.setDefaultOperator(QueryParser.Operator.OR);//all query terms need to present in a matched document
            parser.setAllowLeadingWildcard(true);
            try
            {
                Query textQuery = parser.parse(searchQuery.queryText());
                combinedQuery.add(textQuery, BooleanClause.Occur.MUST);
            }
            catch(ParseException exception)
            {
                exception.printStackTrace();
            }
        }

        return runSearch(combinedQuery, searchQuery);
    }

    /**
     * The simplest search function. Searches the content field and returns a
     * the default number of results.
     *
     * @param queryText
     *            The text to search
     * @return the SearchResult
     */
    public SearchResult search(String queryText)
    {
    	long currentTime = System.currentTimeMillis();
    	SearchResult results = search(new SearchQuery(queryText, defaultField));
    	long timeElapsed = System.currentTimeMillis() - currentTime;
		
		System.out.format("[Info]%d documents returned for query [%s] in %.3f seconds\n", results.numHits(), queryText, timeElapsed/1000.0);
        return results;
    }

    /**
     * Performs the actual Lucene search.
     *
     * @param luceneQuery
     * @param numResults
     * @return the SearchResult
     */
    private SearchResult runSearch(Query luceneQuery, SearchQuery searchQuery)
    {
        try
        {
            TopDocs docs = indexSearcher.search(luceneQuery, searchQuery.fromDoc() + searchQuery.numResults());
            ScoreDoc[] hits = docs.scoreDocs;
//            String title = searchQuery.fields().get(1);
            SearchResult searchResult = new SearchResult(searchQuery, docs.totalHits);

            //This is used to track which documents were retrieved by original query
            HashSet<String> hitdocs = new HashSet<>();
            for(ScoreDoc hit : hits)
            {
                Document doc = indexSearcher.doc(hit.doc);
                ResultDoc rdoc = new ResultDoc(hit.doc);

                String highlighted = null;
                try
                {
                    Highlighter highlighter = new Highlighter(formatter, new QueryScorer(luceneQuery));

                    rdoc.rank(""+(hit.doc+1));
                    rdoc.image(Integer.parseInt(doc.getField("image").stringValue()));
                    rdoc.overall(Double.parseDouble(doc.getField("overall").stringValue()));
                    rdoc.vote(doc.getField("vote").stringValue().equals("")?0:Integer.parseInt(doc.getField("vote").stringValue()));
                    rdoc.verified(Boolean.parseBoolean(doc.getField("verified").stringValue()));
                    rdoc.reviewerID(doc.getField("reviewerID").stringValue());
                    rdoc.asin(doc.getField("asin").stringValue());
//                    rdoc.reviewerName(doc.getField("reviewerName").stringValue());
                    rdoc.summary(doc.getField("summary").stringValue());
                    rdoc.reviewText(doc.getField("reviewText").stringValue());
                    rdoc.reviewTime(Long.parseLong(doc.getField("reviewTime").stringValue()));
                    rdoc.score(Score(hit.score, rdoc.reviewerID(),rdoc.overall(),rdoc.image(), rdoc.vote(), rdoc.verified(), rdoc.reviewTime()));

                    hitdocs.add(rdoc.reviewerID()+rdoc.reviewTime()); //Maybe review time is enough to uniquely identify each review doc
                    String[] snippets = highlighter.getBestFragments(analyzer, searchQuery.fields().get(0), doc.getField("reviewText").stringValue(), numFragments);
                    highlighted = createOneSnippet(snippets);
                }
                catch(InvalidTokenOffsetsException exception)
                {
                    exception.printStackTrace();
                    highlighted = "(no snippets yet)";
                }

                searchResult.addResult(rdoc);
                searchResult.setSnippet(rdoc, highlighted);
            }

            //Calculate score for documents that were not hit
            Query q = new MatchAllDocsQuery();
            TopDocs alldocs = indexSearcher.search(q,searchQuery.numResults());
            hits = alldocs.scoreDocs;
            for(ScoreDoc hit:hits){
                Document doc = indexSearcher.doc(hit.doc);
                String key = doc.getField("reviewerID").stringValue()+Long.parseLong(doc.getField("reviewTime").stringValue());
                if(!hitdocs.contains(key)){ //if the document was not matched by our original query
                    String highlighted = null;
                    ResultDoc rdoc = new ResultDoc(hit.doc);
                    try {
                        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(luceneQuery));

                        rdoc.image(Integer.parseInt(doc.getField("image").stringValue()));
                        rdoc.overall(Double.parseDouble(doc.getField("overall").stringValue()));
                        rdoc.vote(doc.getField("vote").stringValue().equals("") ? 0 : Integer.parseInt(doc.getField("vote").stringValue()));
                        rdoc.verified(Boolean.parseBoolean(doc.getField("verified").stringValue()));
                        rdoc.reviewerID(doc.getField("reviewerID").stringValue());
                        rdoc.asin(doc.getField("asin").stringValue());
//                    rdoc.reviewerName(doc.getField("reviewerName").stringValue());
                        rdoc.summary(doc.getField("summary").stringValue());
                        rdoc.reviewText(doc.getField("reviewText").stringValue());
                        rdoc.reviewTime(Long.parseLong(doc.getField("reviewTime").stringValue()));
                        rdoc.score(Score(0, rdoc.reviewerID(), rdoc.overall(), rdoc.image(), rdoc.vote(), rdoc.verified(), rdoc.reviewTime()));

                        String[] snippets = highlighter.getBestFragments(analyzer, searchQuery.fields().get(0), doc.getField("reviewText").stringValue(), numFragments);
                        highlighted = createOneSnippet(snippets);
                    }
                    catch(InvalidTokenOffsetsException exception)
                    {
                        exception.printStackTrace();
                        highlighted = "(no snippets yet)";
                    }

                    searchResult.addResult(rdoc);
                    searchResult.setSnippet(rdoc, highlighted);
                }
            }
            return searchResult;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
        return new SearchResult(searchQuery);
    }
    /**
     * Create one string of all the extracted snippets from the highlighter
     * @param snippets
     * @return
     */
    private float Score(float score, String reviewerID, double overall, int image, int vote, boolean verified, long reviewTime){
        SearchResult userInfo = SearchUser(reviewerID);
        /**
         * TODO: Customize score function
         */
        return score;
    }

    private SearchResult SearchUser(String reviewerID){
        try{
            //Create combined query using reviewerID. Note that we only need to search in reviewerID field.
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(userIDX)));
            IndexSearcher useridxSearcher = new IndexSearcher(reader);
            SearchQuery query = new SearchQuery(reviewerID, "reviewerID");

            BooleanQuery combinedQuery = new BooleanQuery();
            QueryParser parser = new QueryParser(Version.LUCENE_46, "reviewerID", analyzer);
            parser.setDefaultOperator(QueryParser.Operator.OR);//all query terms need to present in a matched document
            parser.setAllowLeadingWildcard(true);
            Query textQuery = parser.parse(reviewerID);
            combinedQuery.add(textQuery, BooleanClause.Occur.MUST);

            //Retrieving user information used to calculate score
            TopDocs expertdocs = useridxSearcher.search(combinedQuery,query.numResults());
            ScoreDoc[] hits = expertdocs.scoreDocs;
            SearchResult userinfo = new SearchResult(query, expertdocs.totalHits);

            for(ScoreDoc sdoc : hits) {
                ResultDoc d = new ResultDoc(sdoc.doc);
                Document temp = useridxSearcher.doc(sdoc.doc);

                d.image(Integer.parseInt(temp.getField("image").stringValue()));
                d.overall(Double.parseDouble(temp.getField("overall").stringValue()));
                d.vote(temp.getField("vote").stringValue().equals("")?0:Integer.parseInt(temp.getField("vote").stringValue()));
                d.verified(Boolean.parseBoolean(temp.getField("verified").stringValue()));
                d.reviewerID(temp.getField("reviewerID").stringValue());
            }
            return userinfo;
        }
        catch(IOException | ParseException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    private String createOneSnippet(String[] snippets)
    {
        String result = " ... ";
        for(String s: snippets)
            result += s + " ... ";
        return result;
    }
}
