package runners;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;

import amazon.analyzer.DocAnalyzer;
import amazon.index.Indexer;
import amazon.index.Searcher;
import amazon.searcher.DocSearcher;

public class Main {

	//The main entrance to test various functions 
	public static void main(String[] args) {
		try {
			long currentTime = System.currentTimeMillis();
			DocAnalyzer analyzer = new DocAnalyzer("data/models/en-token.bin");
			analyzer.LoadDirectory("data/amazon", ".json");
			long timeElapsed = System.currentTimeMillis() - currentTime;
			System.out.println(timeElapsed/1000);
//			HashMap<String, Integer> dict = analyzer.getCorpus().getDictionary(); 
//			PrintStream standard = System.out;
//			PrintStream o = new PrintStream(new File("P.txt")); 
//	        System.setOut(o); 
//
//	        dict = sortByValue(dict);
//			for(String key:dict.keySet()) {
//				System.out.println(key+"   "+dict.get(key));
//			}
//			
//			String[] query = {"general chicken","fried chicken","BBQ sandwiches","mashed potatoes","Grilled Shrimp Salad","lamb Shank","Pepperoni pizza","brussel sprout salad","FRIENDLY STAFF","Grilled Cheese"};

//			
			//using brute-force strategy to scan through the whole corpus
//			DocSearcher bruteforceSearcher = new DocSearcher(analyzer.getCorpus(), "data/models/en-token.bin");
//			for(String q:query) {
//				bruteforceSearcher.search(q);
//			}
//			System.out.println("----------------------");

			//create inverted index
//			Indexer.index("data/indices", analyzer.getCorpus());
////			search in the inverted index
//
//			PrintStream o = new PrintStream(new File("Index.txt"));
//	        System.setOut(o);
//
//			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("data/indices")));//using your own index path
//			Terms terms = MultiFields.getTerms(reader, "content");//get reference to all the indexed terms in the content field
//			TermsEnum termsEnum = terms.iterator(null);
//			ArrayList<Integer> docFreq = new ArrayList<>();
//			ArrayList<Long> TTF = new ArrayList<>();
////
//			while (termsEnum.next()!=null) {//iterate through all terms
//			    docFreq.add(termsEnum.docFreq());
//			    TTF.add(termsEnum.totalTermFreq());
//			}
//			Collections.sort(docFreq, Collections.reverseOrder());
//			Collections.sort(TTF, Collections.reverseOrder());
////	        System.setOut(standard);
//			long timeElapsed = System.currentTimeMillis() - currentTime;
//			System.out.println(timeElapsed/1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> map){ 
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
	       // Defined Custom Comparator here
        // Sort the list 
		    Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
		        public int compare(Map.Entry<String, Integer> o1,  
		                           Map.Entry<String, Integer> o2) 
		        { 
		            return (o2.getValue()).compareTo(o1.getValue()); 
		        } 
		    }); 
	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap<String, Integer> sortedHashMap = new LinkedHashMap<String, Integer>();
	       for (Iterator<Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
	              Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
}
