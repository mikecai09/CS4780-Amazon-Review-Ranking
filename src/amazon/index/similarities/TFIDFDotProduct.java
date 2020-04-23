package amazon.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.util.MathUtil;

public class TFIDFDotProduct extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     *            Term frequency
     *
     * @param docLength
     *
     *            Document length
     */
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {    	
    	double s = 0.5;
    	double tfln = (termFreq)/(termFreq + s + (s*docLength)/(stats.getAvgFieldLength()));
    	double lw = Math.log((stats.getNumberOfDocuments()+1)/(stats.getDocFreq()));
    	return (float)(tfln*lw);
    }

    @Override
    public String toString() {
        return "TF-IDF Dot Product";
    }
}
