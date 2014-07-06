package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.util.List;

public class Template {
	
	public Template(String shape, List<List<Point2D.Double>> strokes) {
		
		myShape = shape;
		myStrokes = strokes;
	}
	
	public List<List<Point2D.Double>> getStrokes() {
		
		return myStrokes;
	}
	
	public void setPoints(List<List<Point2D.Double>> strokes) {
		
		myStrokes = strokes;
	}
	
	public String getShape() {
		
		return myShape;
	}
	
	private List<List<Point2D.Double>> myStrokes;
	private String myShape;
}