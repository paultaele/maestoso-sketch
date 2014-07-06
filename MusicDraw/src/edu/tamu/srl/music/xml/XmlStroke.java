package edu.tamu.srl.music.xml;

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