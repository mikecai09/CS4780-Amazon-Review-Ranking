package amazon.index;

public class ResultDoc {
    private int _id;
    private String _rank;

    private int _image = 0; //Image
    private double _overall = 0;
    private int _vote = 0; //vote
    private boolean _verified = false;
    private String _reviewerID = "";
    private String _asin = "";
    private String _reviewerName="";
    private String _summary = "";
    private String _reviewText = "";
    private long _unixreviewtime = 0;
    private float score = 0;
    private float relevancy = 0;
    private float senti = 0;
    private float len = 0;

    public ResultDoc(int id) {
        _id = id;
    }

    public int id() {
        return _id;
    }

    public String rank(){
        return _rank;
    }

    public void rank(String r){
        _rank = r;
    }

    public int image(){return _image;}

    public void image(int img){_image = img;}

    public double overall(){return _overall;}

    public void overall(double ovrall){_overall = ovrall;};

    public int vote(){return _vote;};

    public void vote(int v){_vote = v;};

    public boolean verified(){return _verified;}

    public void verified(boolean v){_verified = v;}

    public String reviewerID(){return _reviewerID;}

    public void reviewerID(String id){_reviewerID = id;}

    public String asin(){return _asin;}

    public void asin(String n){_asin = n;}

    public String reviewerName(){return _reviewerName;}

    public void reviewerName(String name){_reviewerName = name;}

    public String summary(){return _summary;}

    public void summary(String s){_summary = s;}

    public String reviewText(){return _reviewText;}

    public void reviewText(String t){_reviewText = t;}

    public float relevancy(){return relevancy;}

    public void relevancy(float t){relevancy = t;}
    
    public float len(){return len;}

    public void len(float t){len = t;}

    public long reviewTime(){return _unixreviewtime;}

    public void reviewTime(long t){_unixreviewtime = t;}
    public float score(){
        return score;
    }
    public float senti(){return senti;}

    public void senti(float s){senti = s;}

    public void score(float _score){
        score = _score;
    }
}
