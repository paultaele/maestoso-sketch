package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.srl.music.xml.XmlPoint;
import edu.tamu.srl.music.xml.XmlSketch;
import edu.tamu.srl.music.xml.XmlSketchParser;
import edu.tamu.srl.music.xml.XmlStroke;

public abstract class AbstractShapeClassifier implements IShapeClassifier {

	public abstract boolean classify(List<IShape> originals);

	public abstract List<IShape> getResult();
	
	protected List<List<Point2D.Double>> getStrokes(List<IShape> shapes) {
		
		List<List<Point2D.Double>> strokes = new ArrayList<List<Point2D.Double>>();
		
		for (IShape shape : shapes) {
			
			strokes.add(shape.getStrokes().get(0).getPoints());
		}
		
		return strokes;
	}
	
	protected boolean isLine(List<Point2D.Double> points) {
		
		// check if staff line is a line
		Point2D.Double pointStart = points.get(0);
		Point2D.Double pointEnd = points.get(points.size()-1);
		double distance = distance(pointStart, pointEnd);
		double pathDistance = pathDistance(points);
		double lineRatio = distance / pathDistance;
		
		// check for linearity
		if (lineRatio < 0.95 || lineRatio > 1.05)
			return false;
		
		return true;
	}
	
	protected double distance(Point2D.Double p1, Point2D.Double p2) {
		
		return Math.sqrt( (p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y) );
	}

	protected double pathDistance(List<Point2D.Double> points) {
		
		double distance = 0.0;
		
		for (int i = 1; i < points.size() - 1; ++i) {
			
			distance += distance(points.get(i-1), points.get(i));
		}
		
		return distance;
	}
	
	protected List<IShape> myShapes;
	
	protected boolean ENABLE_OUTPUT = false;
}
