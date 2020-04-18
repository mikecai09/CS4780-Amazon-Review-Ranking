package amazon.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import amazon.index.ResultDoc;
import amazon.index.Searcher;
import amazon.index.similarities.*;

import java.util.*;

public class Evaluate {
	/**
	 * Format for judgements.txt is:
	 * 
	 * line 0: <query 1 text> line 1: <space-delimited list of relevant URLs>
	 * line 2: <query 2 text> line 3: <space-delimited list of relevant URLs>
	 * ...
	 * Please keep all these constants!
	 */

	Searcher _searcher = null;

	public static void setSimilarity(Searcher searcher, String method) {
		if(method == null)
			return;
		else if(method.equals("--ok"))
			searcher.setSimilarity(new OkapiBM25());
		else if(method.equals("--tfidf"))
			searcher.setSimilarity(new TFIDFDotProduct());
		else if(method.equals("--bdp"))
			searcher.setSimilarity(new BooleanDotProduct());
		else
		{
			System.out.println("[Error]Unknown retrieval function specified!");
			printUsage();
			System.exit(1);
		}
	}
    
    public static void printUsage()
    {
        System.out.println("To specify a ranking function, make your last argument one of the following:");        
        System.out.println("\t--ok\tOkapi BM25");
        System.out.println("\t--tfidf\tTFIDF Dot Product");
    }

	public void search(String method, String indexPath, String query) throws IOException {
		_searcher = new Searcher(indexPath);
		setSimilarity(_searcher, method);
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		System.out.println("\nQuery: " + query);

		int i = 1;
		for(ResultDoc rdoc:results){
			System.out.println(i + ". Summary: " + rdoc.summary());
			System.out.println("   Score: "+rdoc.score());
			i++;
		}
	}

//	//Please implement P@K, MRR and NDCG accordingly
//	public void evaluate(String method, String indexPath, String judgeFile) throws IOException {
//		_searcher = new Searcher(indexPath);
//		setSimilarity(_searcher, method);
//
//		BufferedReader br = new BufferedReader(new FileReader(judgeFile));
//		String line = null, judgement = null;
//		int k = 10;
//		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
//		double numQueries = 0.0;
////		List<Double> map = new ArrayList<>();
////		List<Double> prec = new ArrayList<>();
////		List<Double> rr = new ArrayList<>();
////		List<Double> ndcg = new ArrayList<>();
//		while ((line = br.readLine()) != null) {
//			judgement = br.readLine();
//
//			//compute corresponding AP
//			meanAvgPrec += AvgPrec(line, judgement);
//			//compute corresponding P@K
//			p_k += Prec(line, judgement, k);
//			//compute corresponding MRR
//			mRR += RR(line, judgement);
//			//compute corresponding NDCG
//			nDCG += NDCG(line, judgement, k);
//			++numQueries;
//		}
//		br.close();
//
//		System.out.println("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
//		System.out.println("\nP@" + k + ": " + p_k / numQueries);//this is the final P@K performance of your selected ranker
//		System.out.println("\nMRR: " + mRR / numQueries);//this is the final MRR performance of your selected ranker
//		System.out.println("\nNDCG: " + nDCG / numQueries); //this is the final NDCG performance of your selected ranker
//
//	}
//
//	double AvgPrec(String query, String docString) {
//		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
//		if (results.size() == 0)
//			return 0; // no result returned
//
//		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
//		int i = 1;
//		double avgp = 0.0;
//		double numRel = 0;
////		System.out.println("\nQuery: " + query);
//		for (ResultDoc rdoc : results) {
//			if (relDocs.contains(rdoc.title())) {
//				//how to accumulate average precision (avgp) when we encounter a relevant document
//				numRel ++;
//				avgp+=numRel/i;
////				System.out.print("  ");
//			} else {
//				//how to accumulate average precision (avgp) when we encounter an irrelevant document
////				System.out.print("X ");
//			}
////			System.out.println(i + ". " + rdoc.title());
//			++i;
//		}
//
//		//compute average precision here
//		if(numRel==0) {
//			avgp = 0;
//		}
//		else {
//			avgp /= relDocs.size();
//		}
//		System.out.println("Average Precision: " + avgp);
//		return avgp;
//	}
//
//	//precision at K
//	double Prec(String query, String docString, int k) {
//		double p_k = 0.0;
//		//your code for computing precision at K here
//		int numRel = 0;
//		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
//		if (results.size() == 0) {
//			return 0; // no result returned
//		}
//		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
//		for(int i = 0;i<k&&i<results.size();i++) {
//			if(relDocs.contains(results.get(i).title())) {
//				numRel++;
//			}
//		}
//		p_k = (double)numRel/k;
//		System.out.println("Precision@"+k+": " + p_k);
//		return p_k;
//	}
//
//	//Reciprocal Rank
//	double RR(String query, String docString) {
//		double rr = 0;
//		//your code for computing Reciprocal Rank here
//		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
//		if (results.size() == 0) {
//			return 0; // no result returned
//		}
//		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
//		int i = 1;
//		for(ResultDoc rdoc : results) {
//			if(relDocs.contains(rdoc.title())) {
//				rr = 1.0/i;
//				break;
//			}
//			i++;
//		}
//		System.out.println("RR: " + rr);
//		return rr;
//	}
//
//	//Normalized Discounted Cumulative Gain
//	double NDCG(String query, String docString, int k) {
//		double dcg = 0.0;
//		double idcg = 0.0;
//		double ndcg = 0.0;
//
//		//your code for computing Normalized Discounted Cumulative Gain here
//		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
//		if (results.size() == 0) {
//			return 0; // no result returned
//		}
//		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.trim().split("\\s+")));
//		for(int i = 1;i<=k&&i<=results.size();i++) {
//			if(relDocs.contains(results.get(i-1).title())) {
//				dcg += 1.0/(Math.log(1+i)/Math.log(2));
//			}
//		}
//
//		for(int i = 1; i<=k&&i<=relDocs.size();i++) {
//			idcg += 1.0/(Math.log(1+i)/Math.log(2));
//		}
//		if(idcg == 0) {
//			return 0;
//		}
//		ndcg = dcg/idcg;
//		System.out.println("ndcg@"+k+": "+ndcg);
//		return ndcg;
//	}
}