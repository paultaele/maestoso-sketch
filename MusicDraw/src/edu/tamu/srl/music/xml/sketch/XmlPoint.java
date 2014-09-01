package edu.tamu.srl.music.xml.sketch;

public class XmlPoint {
	
	public XmlPoint(double x, double y) {
		
		myX = x;
		myY = y;
	}
	
	public double X() {
		
		return myX;
	}
	
	public double Y() {
		
		return myY;
	}
	
	private double myX, myY;
}