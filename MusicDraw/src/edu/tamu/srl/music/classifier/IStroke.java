package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IStroke  {

	public IStroke() {
		
		myPoints = new java.util.ArrayList<Point2D.Double>();
		myColor = Color.black;
	}
	
	public IStroke(Color color) {
	
		myColor = color;
		myPoints = new ArrayList<Point2D.Double>();
		
	}
	
	public IStroke(List<Point2D.Double> points, Color color) {
		
		myColor = color;
		myPoints = points;
	}
	
	public void add(Point2D.Double point) {
		
		myPoints.add(point);
	}
	
	public Point2D.Double get(int index) {
		
		return myPoints.get(index);
	}
	
	public java.util.List<Point2D.Double> get() {
		
		return myPoints;
	}
	
	public Color getColor() {
		
		return myColor;
	}
	
	public void setColor(Color color) {
		
		myColor = color;
	}
	
	public int size() {
		
		return myPoints.size();
	}
	
	public List<Point2D.Double> getPoints() {
		
		return myPoints;
	}
	
	
	
	private List<Point2D.Double> myPoints;
	private Color myColor;
}