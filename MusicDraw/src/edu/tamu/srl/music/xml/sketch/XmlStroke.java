package edu.tamu.srl.music.xml.sketch;

import java.util.List;

public class XmlStroke {
	
	public XmlStroke(List<XmlPoint> points) {
		
		myPoints = points;
	}
	
	public List<XmlPoint> toPoints() {
		
		return myPoints;
	}
	
	private List<XmlPoint> myPoints;
}