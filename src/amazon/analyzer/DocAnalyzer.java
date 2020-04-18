package amazon.analyzer;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import opennlp.tools.util.InvalidFormatException;
import structures.Corpus;
import structures.ReviewDoc;

public class DocAnalyzer extends TextAnalyzer {
	
	Corpus m_corpus;
	SimpleDateFormat m_dateParser; // to parse Yelp date format: 2014-05-22
	
	public DocAnalyzer(String tokenizerModel) throws InvalidFormatException, FileNotFoundException, IOException {
		super(tokenizerModel);
		m_corpus = new Corpus();
		m_dateParser = new SimpleDateFormat("yyyy-MM-dd");
	}
	
	public Corpus getCorpus() {
		return m_corpus;
	}
	
	// sample code for demonstrating how to recursively load files in a directory
	// Adding a specific asin number that we are examining
	public void LoadDirectory(String folder, String suffix, String asin) {
		File dir = new File(folder);
		int size = m_corpus.getCorpusSize();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				analyzeDocument(f.getAbsolutePath(), asin);
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix, asin);
		}
		size = m_corpus.getCorpusSize() - size;
		System.out.println("Loading " + size + " review documents from " + folder);
	}
	
	// sample code for demonstrating how to read a file from disk in Java
	JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			
			while((line=reader.readLine())!=null) {
				buffer.append(line);
			}
			reader.close();
			
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	void analyzeDocument(String path, String asin) {
		BufferedReader br;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader(path));
			;
			while ((sCurrentLine = br.readLine()) != null) {
				try {
					JSONObject review = new JSONObject(sCurrentLine);
					if(asin==null||review.getString("asin").equals(asin)) {
						ReviewDoc doc = new ReviewDoc(review.getString("asin"), review.getString("reviewerID"));
						try {
							doc.setImage(review.getStringArray("image").length);
						} catch (JSONException e) {
							doc.setImage(0);
						}
						doc.setOverall(review.getDouble("overall"));
						try {
							doc.setVote(review.getString("vote"));
						} catch (JSONException e) {
							doc.setVote("0");
						}
						doc.setVerified(review.getBoolean("verified"));
						try {
							doc.setReviewerName(review.getString("reviewerName"));
						} catch (JSONException e) {
							doc.setReviewerName("");
						}
						try{
							doc.setReviewText(review.getString("reviewText"));
						}
						catch(JSONException e){
							doc.setReviewText("");
						}
						try{
							doc.setSummary(review.getString("summary"));
						}
						catch(JSONException e){
							doc.setSummary("");
						}
						if((doc.getReviewText()+doc.getSummary()).length() != 0){
							doc.setBoW(tokenize((doc.getReviewText()+" "+doc.getSummary())));
						}
						else{
							doc.setBoW(tokenize(""));
						}
//						doc.setBoW(new String[]{review.getString("reviewerID")});
						doc.setUnixreviewtime(review.getLong("unixReviewTime"));

						m_corpus.addDoc(doc);
					}
				}
				catch(JSONException e){
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
