package runners;

import java.io.IOException;

import edu.virginia.cs.analyzer.DocAnalyzer;
import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.Searcher;
import edu.virginia.cs.searcher.DocSearcher;

public class Main {

	//The main entrance to test various functions 
	public static void main(String[] args) {
//		try {
//			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
//			analyzer.LoadDirectory("data/yelp", ".json");
//			
			String query = "general chicken";
//			
//			//using brute-force strategy to scan through the whole corpus
//			DocSearcher bruteforceSearcher = new DocSearcher(analyzer.getCorpus(), "data/models/en-token.bin");
//			bruteforceSearcher.search(query);
			
			//create inverted index
//			Indexer.index("data/indices", analyzer.getCorpus());
			
			//search in the inverted index
			Searcher indexSearcher = new Searcher("data/indices");
			indexSearcher.search(query);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
