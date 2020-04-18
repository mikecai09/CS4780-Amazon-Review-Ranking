/**
 * 
 */
package structures;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Hongning Wang
 * Basic data structure for a Yelp review document
 */
public class ReviewDoc {
	int m_image; //Image
	double m_overall; //numerical rating of the review
	String m_vote; //vote
	boolean m_verified;
	String m_reviewerID;
	String m_asin;
	String m_reviewerName;
	String m_reviewText;
	String m_summary;
	long m_unixreviewtime;
	
	/**
	 * INSTRUCTOR'S NOTE: Your bag-of-word representation of this document
	 * HashMap is an example, and you are free to use any reasonable data structure for this purpose
	 */
	HashMap<String, Integer> m_BoW; //word -> frequency
	
	public ReviewDoc(String asin, String ID) {
		m_asin = asin;
		m_reviewerID = ID;
	}

	public int getImage() {
		return m_image;
	}

	public void setImage(int m_image) {
		this.m_image = m_image;
	}

	public double getOverall() {
		return m_overall;
	}

	public void setOverall(double m_overall) {
		this.m_overall = m_overall;
	}

	public String getVote() {
		return m_vote;
	}

	public void setVote(String m_vote) {
		this.m_vote = m_vote;
	}

	public boolean isVerified() {
		return m_verified;
	}

	public void setVerified(boolean m_verified) {
		this.m_verified = m_verified;
	}

	public String getReviewerID() {
		return m_reviewerID;
	}

	public void setReviewerID(String m_reviewerID) {
		this.m_reviewerID = m_reviewerID;
	}

	public String getAsin() {
		return m_asin;
	}

	public void setAsin(String m_asin) {
		this.m_asin = m_asin;
	}

	public String getReviewerName() {
		return m_reviewerName;
	}

	public void setReviewerName(String m_reviewerName) {
		this.m_reviewerName = m_reviewerName;
	}

	public String getReviewText() {
		return m_reviewText;
	}

	public void setReviewText(String m_reviewerText) {
		this.m_reviewText = m_reviewerText;
	}

	public String getSummary() {
		return m_summary;
	}

	public void setSummary(String m_summary) {
		this.m_summary = m_summary;
	}

	public long getUnixreviewtime() {
		return m_unixreviewtime;
	}

	public void setUnixreviewtime(long m_unixreviewtime) {
		this.m_unixreviewtime = m_unixreviewtime;
	}

	public HashMap<String, Integer> getBoW(){
		return m_BoW;
	}
	
	public void setBoW(String[] tokens) {
		/**
		 * INSTRUCTOR'S NOTE: please construct your bag-of-word representation of this document based on the processed document content
		 */
		m_BoW = new HashMap<>();
		for(String token:tokens) {
			m_BoW.put(token, m_BoW.getOrDefault(token, 0)+1);
		}
	}
	
	//check whether the document contains the query term
	public boolean contains(String term) {
		return m_BoW.containsKey(term);
	}
	
	//return the frequency of query term in this document 
	public int counts(String term) {
		if (contains(term))
			return m_BoW.get(term);
		else 
			return 0;
	}
}
