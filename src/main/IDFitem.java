package main;

public class IDFitem implements Comparable{
	private int termId;
    private double tfidfscore;
	public IDFitem(int id,double tfidf){
		this.termId=id;
		
		this.tfidfscore=tfidf;
	}
	public int getId(){
		return this.termId;
	}
	public double getscore(){
		return this.tfidfscore;
	}
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		if (this.getId()>((IDFitem)o).getId()){
			return 1;
		}else {
			return -1;
		}
	}
}
