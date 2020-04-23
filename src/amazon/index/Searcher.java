package amazon.index;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import amazon.index.similarities.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils.Property;

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
import org.ejml.simple.SimpleMatrix;


public class Searcher
{
    private IndexSearcher indexSearcher;
    private SpecialAnalyzer analyzer;
    private static SimpleHTMLFormatter formatter;
    private static final int numFragments = 4;
//    private StanfordCoreNLP nlp;
    
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
//            Properties properties = new Properties();
//            properties.setProperty("annotators","tokenize, ssplit, parse, sentiment");
//            nlp = new StanfordCoreNLP(properties);       
            
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
            float totSent = 0, totLen = 0, totVote = 0;

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
                    rdoc.relevancy(hit.score);
                    
                    String txt = doc.getField("content").stringValue();
//                    Annotation annotation = nlp.process(txt);
//                    float sent = 0,count = 0;
//                    for(CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//                    	Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
//                    	//SimpleMatrix simpleMatrix = RNNCoreAnnotations.getPredictions(tree);
//                    	sent += RNNCoreAnnotations.getPredictedClass(tree);
//                    	count++;
//                    }
//                    sent /= count;
                    float sent = Float.parseFloat(doc.getField("Sentiment").stringValue());
                    totSent += sent;
                    totLen += txt.length();
                    totVote += rdoc.vote();
                    rdoc.senti(sent);

                    rdoc.len(txt.length());
                    //rdoc.score(Score(hit.score, rdoc.reviewerID(),rdoc.overall(),rdoc.image(), rdoc.vote(), rdoc.verified(), rdoc.reviewTime(), sent));
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
                        rdoc.relevancy(0);

                        String txt = doc.getField("content").stringValue();
//                      Annotation annotation = nlp.process(txt);
//                      float sent = 0,count = 0;
//                      for(CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//                      	Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
//                      	//SimpleMatrix simpleMatrix = RNNCoreAnnotations.getPredictions(tree);
//                      	sent += RNNCoreAnnotations.getPredictedClass(tree);
//                      	count++;
//                      }
//                      sent /= count;
                        float sent = Float.parseFloat(doc.getField("Sentiment").stringValue());
                        totSent += sent;
                        totLen += txt.length();
                        totVote += rdoc.vote();
                        rdoc.senti(sent);
 
                        rdoc.len(txt.length());
                        //rdoc.score(Score(0, rdoc.reviewerID(),rdoc.overall(),rdoc.image(), rdoc.vote(), rdoc.verified(), rdoc.reviewTime(), sent));
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
            searchResult.avgSenti = totSent/hits.length;
            System.out.println("Average sentiment score: " + searchResult.avgSenti);
            searchResult.avgLen = totLen/hits.length;
            searchResult.avgVote = totVote/hits.length;
            for(ResultDoc rdoc: searchResult.getDocs()) {
            	rdoc.score(Score(rdoc.relevancy(), rdoc.reviewerID(), rdoc.image(), rdoc.vote(), rdoc.verified(), rdoc.reviewTime(), rdoc.senti(), searchResult.avgSenti, searchResult.avgLen, rdoc.len(), searchResult.avgVote));
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
    private float Score(float score, String reviewerID, int image, int vote, boolean verified, long reviewTime, float sentiment, float avgSent, float avgLen, float reviewLen, float avgVote){
        SearchResult userInfo = SearchUser(reviewerID);
        /**
         * TODO: Customize score function
         */
        float votes = 0;
        for(ResultDoc i : userInfo.getDocs()) {
        	votes += i.vote();
        }
//        if(userInfo.getDocs().size() != 0)
//        	votes/=userInfo.getDocs().size();
        System.out.println("AvgVote: " + votes);
        float credibility = (float)Math.log(Math.log(votes+1)+1)+1;
        double timeDiff = Math.log((Instant.now().getEpochSecond() - reviewTime)/2592000);
        float is_verified = (float) 0.1;
        if(verified) is_verified = 1;
        
//        System.out.println("timeDiff: " + timeDiff);
        System.out.println("Credi: " + credibility);
        System.out.println("ID: " + reviewerID);
//        System.out.println("Vote: " + 0.05*vote);
//        System.out.println("rele: " + 0.25*score);
//        System.out.println("Length: " + 0.4*(reviewLen/avgLen));
//        System.out.println("Senti: " + 0.3/(1+Math.abs(sentiment-avgSent)) );
        
        
        
        double ret = timeDiff * is_verified * (credibility*(0.4*(reviewLen/avgLen) + 
        		0.3*(2/(1+Math.abs(sentiment-avgSent))) + 0.05*(vote/avgVote) + 0.25*score)
        		+ Math.log(1+image));
        System.out.println("Final Score: " + ret);
        return (float) ret;
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
                d.vote(temp.getField("vote").stringValue().equals("")?0:Integer.parseInt(temp.getField("vote").stringValue().replaceAll(",", "")));
                d.verified(Boolean.parseBoolean(temp.getField("verified").stringValue()));
                d.reviewerID(temp.getField("reviewerID").stringValue());
                
                userinfo.addResult(d);
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
