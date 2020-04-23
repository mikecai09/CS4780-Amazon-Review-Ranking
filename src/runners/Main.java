package runners;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;
import amazon.evaluator.Evaluate;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;

import amazon.analyzer.DocAnalyzer;
import amazon.evaluator.*;
import amazon.index.Indexer;
import amazon.index.Searcher;
import amazon.searcher.DocSearcher;

public class Main {
	final static String _dataset = "npl";
	final static String _prefix = "data/";
	final static String asin = "B000165DSM";
//	final static String asin = null;

	//The main entrance to test various functions 
	public static void main(String[] args) {

		try {
			//create inverted index
			long currentTime = System.currentTimeMillis();
//			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
//			analyzer.LoadDirectory("data/amazon", ".json", asin);
//			Indexer.index("data/indices", analyzer.getCorpus());

			Evaluate e = new Evaluate();
			e.search("--tfidf","data/indices","great product music very nice");

			long timeElapsed = System.currentTimeMillis() - currentTime;
			System.out.println(timeElapsed/1000 + " secs");
//			deleteDir(new File("data/indices"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}
}
