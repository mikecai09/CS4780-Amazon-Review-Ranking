package amazon.index;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import structures.Corpus;
import structures.ReviewDoc;

public class Indexer {

    /**
     * Creates the initial index files on disk
     *
     * @param indexPath
     * @return
     * @throws IOException
     */
    private static IndexWriter setupIndex(String indexPath) throws IOException {
    	File path = new File(indexPath);
    	if (path.exists()) {
    		System.err.println("[Error]You need to first delete this folder!");
    		return null;
    	}
    	
        Analyzer analyzer = new SpecialAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
                analyzer);
        config.setOpenMode(OpenMode.CREATE);
        config.setRAMBufferSizeMB(2048.0);

        FSDirectory dir;
        IndexWriter writer = null;
        dir = FSDirectory.open(new File(indexPath));
        writer = new IndexWriter(dir, config);

        return writer;
    }

    /**
     * @param indexPath
     *            Where to create the index
     * @param corpus
     *            The prefix of all the paths in the fileList
     * @throws IOException
     */
    public static void index(String indexPath, Corpus corpus)
            throws IOException {

        System.out.println("Creating Lucene index...");

        FieldType _contentFieldType = new FieldType();
        _contentFieldType.setIndexed(true);
        _contentFieldType.setStored(true);

        Properties properties = new Properties();
        properties.setProperty("annotators","tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP nlp = new StanfordCoreNLP(properties);  
        
        IndexWriter writer = setupIndex(indexPath);
        int i;
        
        for(i=0; i<corpus.getCorpusSize(); i++) {
        	ReviewDoc review = corpus.getDoc(i);
        
            Document doc = new Document();
            doc.add(new Field("content", review.getReviewText()+" "+review.getSummary(), _contentFieldType));//the content field is searchable
            doc.add(new Field("reviewerID", review.getReviewerID(), _contentFieldType));//the author field is searchable
            doc.add(new Field("asin", review.getAsin(), _contentFieldType));//the author field is searchable
            doc.add(new Field("reviewText", review.getReviewText(), _contentFieldType));//the content field is searchable
            doc.add(new Field("summary", review.getSummary(), _contentFieldType));//the content field is searchable
            doc.add(new Field("image", ""+review.getImage(), _contentFieldType));//the content field is searchable
            doc.add(new Field("overall", ""+review.getOverall(), _contentFieldType));//the content field is searchable
            doc.add(new Field("vote", review.getVote(), _contentFieldType));//the content field is searchable
            doc.add(new Field("verified", ""+review.isVerified(), _contentFieldType));//the content field is searchable
            doc.add(new Field("reviewTime", ""+review.getUnixreviewtime(), _contentFieldType));//the content field is searchable
            doc.add(new Field("reviewerName", ""+review.getReviewerName(), _contentFieldType));//the content field is searchable
            
            String txt = doc.getField("content").stringValue();
            Annotation annotation = nlp.process(txt);
            float sent = 0,count = 0;
            for(CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            	Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            	//SimpleMatrix simpleMatrix = RNNCoreAnnotations.getPredictions(tree);
            	sent += RNNCoreAnnotations.getPredictedClass(tree);
            	count++;
            }
            sent /= count;
            doc.add(new Field("Sentiment", ""+sent, _contentFieldType));
            
            writer.addDocument(doc);

            if (i % 1 == 0)
                System.out.println(" -> indexed " + i + " docs...");
        }
        System.out.println(" -> indexed " + i + " total docs.");

        writer.close();
    }
}
