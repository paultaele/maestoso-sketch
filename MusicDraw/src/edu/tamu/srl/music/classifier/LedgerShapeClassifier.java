package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;

public class LedgerShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	public boolean classify(List<IShape> originals) {
		
		// clone the list of original shapes
		// extract all raw shapes
		// get the staff as reference for setting the shape
		List<IShape> shapes = cloneShapes(originals);
		List<IShape> rawShapes = extractRawShapes(shapes);
		StaffShape staffShape = getStaffShape(shapes);
		List<IShape> newShapes = null;
		
		//
		newShapes = hasLedgerLine(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}

		return false;
	}

	public List<IShape> getResult() {

		return myShapes;
	}

	private List<IShape> hasLedgerLine(List<IShape> rawShapes, List<IShape> originals, StaffShape staffShape) {
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		// get raw stroke
		IShape rawShape = rawShapes.get(0);
		IStroke rawStroke = rawShape.getStrokes().get(0);
		List<Point2D.Double> rawPoints = rawStroke.getPoints();
		
		// line test
		if (!isLine(rawPoints))
			return null;
		
		// horizontal test
		Point2D.Double firstPoint = rawPoints.get(0);
		Point2D.Double lastPoint = rawPoints.get(rawPoints.size()-1);
		double xDiff = firstPoint.x - lastPoint.x;
		double yDiff = firstPoint.y - lastPoint.y;
		double angle = Math.abs(Math.toDegrees(Math.atan2(yDiff, xDiff)));
		if (Math.abs(180 - angle) > HORIZONTAL_ANGLE_MAX_RATIO_THRESHOLD 
				&& Math.abs(angle ) > HORIZONTAL_ANGLE_MAX_RATIO_THRESHOLD)
			return null;
		
		// create a temporary beautified line
		double midY = Math.abs(firstPoint.y + lastPoint.y) / 2;
		firstPoint = new Point2D.Double(firstPoint.x, midY);
		lastPoint = new Point2D.Double(lastPoint.x, midY);
		List<Point2D.Double> cleanPoints = new ArrayList<Point2D.Double>();
		cleanPoints.add(firstPoint);
		cleanPoints.add(lastPoint);
		IStroke cleanStroke = new IStroke(cleanPoints, rawStroke.getColor());
		
		// external staff test
		double topY = staffShape.getLineY(0);
		double bottomY = staffShape.getLineY(staffShape.NUM_LINES-1);
		if (topY < midY && midY < bottomY)
			return null;
		
		// length test
		double interval = staffShape.getLineInterval();	// diameter
		double length = pathDistance(cleanPoints);
		if (length < interval * LEDGER_LINE_LENGTH_MIN_RATIO
				|| interval * LEDGER_LINE_LENGTH_MAX_RATIO < length)
			return null;
		
		//
		rawStroke.setColor(DEBUG_COLOR);
		IShape ledgerShape = null;
		if (midY < topY)
			ledgerShape = new IShape(ShapeName.UPPER_LINE, rawStroke);
		else
			ledgerShape = new IShape(ShapeName.LOWER_LINE, rawStroke);
		List<IShape> shapes = cloneShapes(originals);
		shapes.add(ledgerShape);
		return shapes;
	}

	private double HORIZONTAL_ANGLE_MAX_RATIO_THRESHOLD = 10.0;
	public static final double LEDGER_LINE_LENGTH_MIN_RATIO = 1;
	public static final double LEDGER_LINE_LENGTH_MAX_RATIO = 2;
}