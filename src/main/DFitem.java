package main;

public class DFitem implements Comparable{
    private int termId;
	private String term;
	private int freq;
	
	
	
public DFitem(int id,String t,int f){
	this.termId=id;
	this.term=t;
	this.freq=f;
}
public DFitem(String t,int f){
	
	this.term=t;
	this.freq=f;
}
public void addFreq(){
	this.freq+=1;
}
public int getID(){
	return this.termId;
}
public int getFreq(){
	return this.freq;
}
public String getTerm(){
	return this.term;
}
@Override
public int compareTo(Object o) {
	// TODO Auto-generated method stub
	return this.getTerm().compareTo(((DFitem)o).getTerm());
		
}
}

