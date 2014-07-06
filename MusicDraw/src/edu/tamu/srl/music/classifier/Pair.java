package edu.tamu.srl.music.classifier;

public class Pair {
	
	public Pair(Template T, double score) {
		
		myT = T;
		myScore = score;
	}
	
	public Template T() {
		
		return myT;
	}
	
	public String shape() {
		
		return myT.getShape();
	}
	
	public double score() {
		
		return myScore;
	}
	
	private Template myT;
	private double myScore;
}