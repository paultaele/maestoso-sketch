package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.xml.sketch.XmlPoint;
import edu.tamu.srl.music.xml.sketch.XmlSketch;
import edu.tamu.srl.music.xml.sketch.XmlSketchParser;
import edu.tamu.srl.music.xml.sketch.XmlStroke;

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
		
		return isLine(points, LINE_RATIO_FLOOR, LINE_RATIO_CEILING);
	}
	
	protected boolean isLine(List<Point2D.Double> points, double floor, double ceiling) {
		
		// check if staff line is a line
		Point2D.Double pointStart = points.get(0);
		Point2D.Double pointEnd = points.get(points.size()-1);
		double distance = distance(pointStart, pointEnd);
		double pathDistance = pathDistance(points);
		double lineRatio = distance / pathDistance;
		
		// check for linearity
		if (lineRatio < floor || lineRatio > ceiling)
			return false;
		
		return true;
	}
	
	protected double distance(Point2D.Double p1, Point2D.Double p2) {
		
		double distance = Math.sqrt( (p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y) );
		
		return distance;
	}

	protected double pathDistance(List<Point2D.Double> points) {
		
		double distance = 0.0;
		for (int i = 1; i < points.size(); ++i)
			distance += distance(points.get(i-1), points.get(i));
		
		return distance;
	}
	
	protected List<IShape> cloneShapes(List<IShape> originals) {
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape original : originals) {
			shapes.add(original);
		}
		return shapes;
	}

	protected List<IShape> extractRawShapes(List<IShape> shapes) {
		List<IShape> rawShapes = new ArrayList<IShape>();
		Iterator<IShape> iterator = shapes.iterator();
		while (iterator.hasNext()) {
			IShape shape = iterator.next();
			if (shape.getShapeName() == ShapeName.RAW) {
				rawShapes.add(shape);
				iterator.remove();
			}
		}
		return rawShapes;
	}
	
	protected StaffShape getStaffShape(List<IShape> shapes) {
		IShape staff = null;
		for (IShape s : shapes) {
			if (s.getShapeName() == IShape.ShapeName.STAFF) {
				staff = s;
				break;
			}
		}
		StaffShape staffShape = (StaffShape)staff;
		return staffShape;
	}
	
	
	
	protected List<IShape> myShapes;
	
	public static final double LINE_RATIO_FLOOR = 0.90;
	public static final double LINE_RATIO_CEILING = 1.10;
//	public static final Color DEBUG_COLOR = new Color(128, 0, 128);
	public static final Color CLEAN_STROKE_DISPLAY_COLOR = Color.black;
	public static final Color TRANSITION_STROKE_DISPLAY_COLOR = Color.gray;
}
