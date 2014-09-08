package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;

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
	
	public IStroke copy() {
		
		// initialize copy
		IStroke copy = new IStroke();
		
		// copy points
		copy.myPoints = new ArrayList<Point2D.Double>();
		for (Point2D.Double point : this.myPoints)
			copy.myPoints.add(point);
		
		// copy color
		copy.myColor = this.myColor;
		
		// copy bounding box
		if (myBoundingBox != null)
			copy.myBoundingBox = this.myBoundingBox.copy();
		
		return copy;
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
	
	public BoundingBox getBoundingBox() {
		
		if (myBoundingBox == null) {
			
			IShape temp = new IShape(ShapeName.RAW, this);
			myBoundingBox = temp.getBoundingBox();
		}
		
		return myBoundingBox;
	}
	
	
	
	private List<Point2D.Double> myPoints;
	private Color myColor;
	private BoundingBox myBoundingBox;
}