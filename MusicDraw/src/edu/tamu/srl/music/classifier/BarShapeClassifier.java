package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;

public class BarShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	@Override
	public boolean classify(List<IShape> originals) {
		
		// clone the list of original shapes
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape original : originals)
			shapes.add(original);
		
		// extract all raw shapes
		List<IShape> rawShapes = new ArrayList<IShape>();
		Iterator<IShape> iterator = shapes.iterator();
		while (iterator.hasNext()) {
			IShape shape = iterator.next();
			if (shape.getShapeName() == ShapeName.RAW) {
				rawShapes.add(shape);
				iterator.remove();
			}
		}
		
		// get the staff as reference for setting the shape
		IShape staff = null;
		for (IShape s : shapes) {
			if (s.getShapeName() == IShape.ShapeName.STAFF) {
				staff = s;
				break;
			}
		}
		StaffShape staffShape = (StaffShape)staff;
		
		// 
		boolean isBarLine = isBarLine(rawShapes, staffShape);
		if (!isBarLine)
			return false;
		
		// create single bar line
		IShape rawShape = rawShapes.get(0);
		IStroke rawStroke = rawShape.getStrokes().get(0);
		List<Point2D.Double> rawPoints = rawStroke.getPoints();
		List<Point2D.Double> barPoints = new ArrayList<Point2D.Double>();
		barPoints.add(new Point2D.Double(rawPoints.get(0).x, staffShape.getLineY(0)));
		barPoints.add(new Point2D.Double(rawPoints.get(0).x, staffShape.getLineY(staffShape.NUM_LINES - 1)));
		
		// create single bar
		IStroke barStroke = new IStroke(barPoints, rawStroke.getColor());
		IShape barShape = new IShape(ShapeName.SINGLE_BAR, barStroke);
		
		// locate possible double bar
		// note: if no double bars exist, then this method will instead
		// add the barShape to the myShapes list
		shapes = mergeBars(barShape, shapes);
		
		//
		myShapes = shapes;
		return true;
	}
	
	private List<IShape> mergeBars(IShape bar1, List<IShape> originals) {
		
		// clone the list of original shapes
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape original : originals)
			shapes.add(original);
		
		// look for a double bar
		Iterator<IShape> iterator = shapes.iterator();
		double threshold = bar1.getBoundingBox().height() * DOUBLE_BAR_DISTANCE_THRESHOLD_RATIO; //
		boolean hasDoubleBar = false;
		while (iterator.hasNext()) {
			IShape shape = iterator.next();
			if (shape.getShapeName() == ShapeName.SINGLE_BAR) {
				
				double distance = Math.abs(bar1.getBoundingBox().centerX() - shape.getBoundingBox().centerX());
				if (distance < threshold) {
					
					//
					hasDoubleBar = true;
					IShape other = shape;
					iterator.remove();
					
					// 
					double gap = threshold * 0.5;
					if (other.getBoundingBox().minX() < bar1.getBoundingBox().minX())
						gap = -gap;
					Point2D.Double newPoint1 = new Point2D.Double(bar1.getBoundingBox().minX() + gap, other.getBoundingBox().minY());
					Point2D.Double newPoint2 = new Point2D.Double(bar1.getBoundingBox().minX() + gap, other.getBoundingBox().maxY());
					List<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
					newPoints.add(newPoint1);
					newPoints.add(newPoint2);
					IStroke newStroke = new IStroke(newPoints, other.getStrokes().get(0).getColor());
					IShape bar2 = new IShape(ShapeName.SINGLE_BAR, newStroke);
					
					//
					List<IStroke> barStrokes = new ArrayList<IStroke>();
					barStrokes.add(bar1.getStrokes().get(0));
					barStrokes.add(bar2.getStrokes().get(0));
					
					//
					IShape doubleBar = new IShape(ShapeName.DOUBLE_BAR, barStrokes);
					shapes.add(doubleBar);
					break;
				}
			}
		}
		
		if (hasDoubleBar) {
			return shapes;
		}
		else {
			originals.add(bar1);
			return originals;
		}
	}

	@Override
	public List<IShape> getResult() {
		
		return myShapes;
	}

	private boolean isBarLine(List<IShape> rawShapes, StaffShape staffShape) {
		
		// singleton raw shape test
		if (rawShapes.size() != 1)
			return false;
		
		//
		IShape rawShape = rawShapes.get(0);
		IStroke rawStroke = rawShape.getStrokes().get(0);
		List<Point2D.Double> rawPoints = rawStroke.getPoints();
		
		// get the top and bottom points
		Point2D.Double topPoint, bottomPoint;
		if (rawPoints.get(0).y < rawPoints.get(rawPoints.size() - 1).y) {
			topPoint = rawPoints.get(0);
			bottomPoint = rawPoints.get(rawPoints.size() - 1);
		}
		else {
			topPoint = rawPoints.get(rawPoints.size() - 1);
			bottomPoint = rawPoints.get(0);
		}
		
		// line test
		if (!isLine(rawPoints)) {
			return false;
		}
		
		
		// horizontal test
		double deltaX = bottomPoint.x - topPoint.x;
		double deltaY = bottomPoint.y - topPoint.y;
		double degree = Math.atan2(deltaY, deltaX) * 180.0 / Math.PI;
		if (degree < DEGREE_FLOOR || degree > DEGREE_CEILING) {
			return false;
		}
		
		// location/height test
		double interval = staffShape.getLineInterval() * INTERVAL_RATIO;
		Point2D.Double topStaffPoint = new Point2D.Double(topPoint.x, staffShape.getLineY(0));
		Point2D.Double bottomStaffPoint = new Point2D.Double(bottomPoint.x, staffShape.getLineY(staffShape.NUM_LINES - 1));
		if (distance(topPoint, topStaffPoint) > interval || distance(bottomPoint, bottomStaffPoint) > interval) {
			return false;
		}
		
		return true;
	}
	
	
	
	
	public static final double DEGREE_FLOOR = 75.0;
	public static final double DEGREE_CEILING = 105.0;
	public static final double INTERVAL_RATIO = 0.75;
	private static final double DOUBLE_BAR_DISTANCE_THRESHOLD_RATIO = 0.2;
}
