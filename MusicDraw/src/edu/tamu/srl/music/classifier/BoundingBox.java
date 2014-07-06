package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.util.List;

public class BoundingBox {
	
	public BoundingBox(List<Point2D.Double> points) {
		
		createBoundingBox(points);
	}
	
	private void createBoundingBox(List<Point2D.Double> points) {
		
		myMinX = Double.MAX_VALUE;
		myMinY = Double.MAX_VALUE;
		myMaxX = Double.MIN_VALUE;
		myMaxY = Double.MIN_VALUE;
		
		for (Point2D.Double point : points) {
			
			if (point.x < myMinX)
				myMinX = point.x;
			else if (point.x > myMaxX)
				myMaxX = point.x;
			
			if (point.y < myMinY)
				myMinY = point.y;
			else if (point.y > myMaxY)
				myMaxY = point.y;
		}
		
		myCenterX = width() / 2.0;
		myCenterY = height() / 2.0;
	}
	
	public BoundingBox(double minX, double minY, double maxX, double maxY) {
		
		myMinX = minX;
		myMinY = minY;
		myMaxX = maxX;
		myMaxY = maxY;
	}
	
	public double width() {
		
		return myMaxX - myMinX;
	}
	
	public double height() {
		
		return myMaxY - myMinY;
	}
	
	public double minX() {
		
		return myMinX;
	}
	
	public double minY() {
		
		return myMinY;
	}
	
	public double maxX() {
		
		return myMaxX;
	}
	
	public double maxY() {
		
		return myMaxY;
	}
	
	public double centerX() {
		
		return myCenterX;
	}
	
	public double centerY() {
		
		return myCenterY;
	}
	
	private double myMinX;
	private double myMinY;
	private double myMaxX;
	private double myMaxY;
	private double myCenterX;
	private double myCenterY;
}