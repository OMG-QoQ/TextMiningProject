package main;

public class PriorItem implements Comparable {
	
	private int n;
	private double sim;
	private int target;
	public PriorItem(){
		
	}
	public PriorItem(int n,double sim,int tar){
		this.n=n;
		this.sim=sim;
		this.target=tar;
		
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public double getSim() {
		return sim;
	}

	public void setSim(double sim) {
		this.sim = sim;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		if (this.getSim()>((PriorItem)o).getSim()){
			return -1;
		}else if (this.getSim()==((PriorItem)o).getSim()){
			return 0;
		}else {
			return 1;
		}
	}

}
