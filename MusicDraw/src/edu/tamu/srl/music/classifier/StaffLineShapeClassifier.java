package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.MainGui;

public class StaffLineShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	public boolean classify(List<IShape> originals) {
		
		// cast list to array
		IShape[] shapes = new IShape[originals.size()];
		for (int i = 0; i < shapes.length; ++i)
			shapes[i] = originals.get(i);
		
		// iterate through each shape
		boolean hasBeenClassified = false;
		for (int i = 0; i < shapes.length; ++i) {
			
			// case: current shape is a raw shape
			if (shapes[i].getShapeName() == ShapeName.RAW) {
				
				// get the raw shape
				IShape shapeLineCandidate = shapes[i];
				
				// classify the shape line candidate
				boolean isClassified = classify(shapeLineCandidate);
				
				//
				if (isClassified) {
					
					myShape = getShape();
					hasBeenClassified = true;
					shapes[i] = myShape;
				}
			}
		}
		
		if (hasBeenClassified) {
			
			myShapes = new ArrayList<IShape>();
			for (IShape shape : shapes)
				myShapes.add(shape);
		}
		
		return hasBeenClassified;
	}
	
	public boolean classify(IShape shape) {

		//
		IStroke stroke = shape.getStrokes().get(0);
		List<Point2D.Double> points = stroke.getPoints();
		
		//
		Point2D.Double pointStart = points.get(0);
		Point2D.Double pointEnd = points.get(points.size()-1);
		double distance = distance(pointStart, pointEnd);
		double pathDistance = pathDistance(points);

		// check if staff line matches entire width of draw application
		double distanceRatio = distance / MainGui.FRAME_WIDTH;
		if ( distanceRatio < 0.9 ||  distanceRatio > 1.1 )
			return false;
			
		// check if staff line is horizontal
		double xDiff = pointEnd.x - pointStart.x;
		double yDiff = pointEnd.y - pointStart.y;
		double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));
		if (angle < -5.0 || angle > 5.0)
			return false;

		// check if staff line is a line
		double lineRatio = distance / pathDistance;
		if (lineRatio < 0.95)
			return false;
		
		//
		List<Point2D.Double> line = beautifyStaffLine(points);
		IStroke lineStroke = new IStroke(line, stroke.getColor());
		myShape = new IShape(IShape.ShapeName.STAFF_LINE, lineStroke);

		return true;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private IShape getShape() {
		
		return myShape;
	}
	
	private List<Point2D.Double> beautifyStaffLine(List<Point2D.Double> points) {
		
		//
		Point2D.Double p1 = points.get(0);
		Point2D.Double p2 = points.get(points.size() - 1);
		double midY = (p1.y + p2.y) / 2.0;
		
		//
		Point2D.Double left = new Point2D.Double(0, midY);
		Point2D.Double right = new Point2D.Double(MainGui.FRAME_WIDTH - 1, midY);
		
		//
		List<Point2D.Double> line = new ArrayList<Point2D.Double>();
		line.add(left);
		line.add(right);
		
		return line;
	}
	
	private double distance(Point2D.Double p1, Point2D.Double p2) {
		
		return Math.sqrt( (p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y) );
	}

	private double pathDistance(List<Point2D.Double> points) {
		
		double distance = 0.0;
		
		for (int i = 1; i < points.size() - 1; ++i) {
			
			distance += distance(points.get(i-1), points.get(i));
		}
		
		return distance;
	}

	
	private IShape myShape;
	private List<IShape> myShapes;
}
