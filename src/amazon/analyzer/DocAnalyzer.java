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
	public void LoadDirectory(String folder, String suffix) {
		File dir = new File(folder);
		int size = m_corpus.getCorpusSize();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				analyzeDocument(f.getAbsolutePath());
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
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
	
	void analyzeDocument(String path) {
		BufferedReader br;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader(path));
			;
			while ((sCurrentLine = br.readLine()) != null) {
				try {
					JSONObject review = new JSONObject(sCurrentLine);
					ReviewDoc doc = new ReviewDoc(review.getString("asin"), review.getString("reviewerID"));
					try{
						doc.setImage(review.getStringArray("image").length);
					}
					catch (JSONException e){
						doc.setImage(0);
					}
//					System.out.println(doc.getReviewerID());
					doc.setOverall(review.getDouble("overall"));
					try {
						doc.setVote(review.getString("vote"));
					}
					catch(JSONException e){
						doc.setVote("");
					}
					doc.setVerified(review.getBoolean("verified"));
					try{
						doc.setReviewerName(review.getString("reviewerName"));
					}
					catch (JSONException e){
						doc.setReviewerName("");
					}
					try {
						doc.setReviewerText(review.getString("reviewerText"));
					}
					catch (JSONException e){
						doc.setReviewerText("");
					}
					try{
						doc.setSummary(review.getString("summary"));
					}
					catch (JSONException e){
						doc.setSummary("");
					}
					doc.setUnixreviewtime(review.getLong("unixReviewTime"));

//					doc.setBoW(tokenize(content));

					m_corpus.addDoc(doc);

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
