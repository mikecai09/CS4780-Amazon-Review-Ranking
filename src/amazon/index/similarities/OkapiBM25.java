package amazon.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class OkapiBM25 extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
        float k1 = (float)1.2;
        float k2 = 750;
        float b = (float)0.75;
        float N = stats.getNumberOfDocuments();
        float df = stats.getDocFreq();
        float qtermFreq = 1;
        float score = (float)Math.log((N - df + 0.5)/df+0.5);
        score*=((k1+1)*termFreq)/(k1*(1-b+b*(docLength/stats.getAvgFieldLength()))+termFreq);
        score*=((k2+1)*qtermFreq)/(k2+qtermFreq);
        return score;    }

    @Override
    public String toString() {
        return "Okapi BM25";
    }

}
