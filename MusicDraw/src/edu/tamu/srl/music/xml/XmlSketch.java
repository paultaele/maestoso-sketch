package edu.tamu.srl.music.xml;

import java.util.List;

public class XmlSketch {

	public XmlSketch(String shape, List<XmlStroke> strokes) {
		
		myShape = shape;
		myStrokes = strokes;
	}
	
	public String getShape() {
		
		return myShape;
	}
	
	public List<XmlStroke> getStrokes() {
		
		return myStrokes;
	}
	
	private String myShape;
	private List<XmlStroke> myStrokes;
}

