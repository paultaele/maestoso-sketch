package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;

public class NoteShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	public boolean classify(List<IShape> originals) {
		
		// clone the list of original shapes
		// extract all raw shapes
		// get the staff as reference for setting the shape
		List<IShape> shapes = cloneShapes(originals);
		List<IShape> rawShapes = extractRawShapes(shapes);
		StaffShape staffShape = getStaffShape(shapes);
		List<IShape> newShapes = null;
		
		// filled note head test
		newShapes = hasFilledNoteHead(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// empty note head test
		newShapes = hasEmptyNoteHead(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// stem test
		newShapes = hasStem(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		return false;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private List<IShape> hasStem(List<IShape> rawShapes, List<IShape> originals, StaffShape staffShape) {
		
		// non-zero notes test
		// do not recognize stems if there are no notes
		List<IShape> shapes = cloneShapes(originals);
		int noteCount = 0;
		for (IShape shape : shapes) {
			if (shape.getShapeName() == ShapeName.NOTE)
				++noteCount;
		}
		if (noteCount == 0)
			return null;
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		//
		IShape scribble = rawShapes.get(0);
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		List<Point2D.Double> scribblePoints = scribbleStroke.getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		
		// line test
		if (!isLine(scribblePoints))
			return null;
		
		// minimum stem distance test
		// input's length is at least greater than circumference of an interval circle
		double minLength = interval * STEM_LENGTH_MIN_RATIO;
		double maxLength = interval * STEM_LENGTH_MAX_RATIO;
		double length = pathDistance(scribblePoints);
		if (length < minLength || maxLength < length)
			return null;
		
		//
		Point2D.Double stemStartPoint = scribblePoints.get(0);
		Point2D.Double stemEndPoint = scribblePoints.get(scribblePoints.size()-1);
		Point2D.Double stemMidPoint = new Point2D.Double((stemStartPoint.x+stemEndPoint.x)/2, (stemStartPoint.y+stemEndPoint.y)/2);
		
		// vertical test
		double xDiff = stemEndPoint.x - stemStartPoint.x;
		double yDiff = stemEndPoint.y - stemStartPoint.y;
		double angle = Math.abs(Math.toDegrees(Math.atan2(yDiff, xDiff)));
		if (angle < VERTICAL_ANGLE_MIN_THRESHOLD || VERTICAL_ANGLE_MAX_THRESHOLD < angle)
			return null;
		
		// minimum closeness test
		NoteShape note = null;
		boolean isDownwardStem = false;
		double minCloseness = interval * STEM_TO_HEAD_MIN_CLOSENESS_RATIO;
		Iterator<IShape> iterator = shapes.iterator();
		boolean foundNote = false;
		while (iterator.hasNext()) { // iterate through each shape
			
			// get the current shape
			IShape shape = iterator.next();
			
			// case: shape is a note
			if (shape.getShapeName() == ShapeName.NOTE) {

				// get the note and its left and right bounding points
				NoteShape candidateNote = (NoteShape)shape;
				BoundingBox candidateNoteBox = candidateNote.getBoundingBox();
				Point2D.Double noteLeftPoint = candidateNoteBox.left();
				Point2D.Double noteRightPoint = candidateNoteBox.right();
				
				// get stem's top and bottom point
				Point2D.Double stemTopPoint = null;
				Point2D.Double stemBottomPoint = null;
				if (stemStartPoint.y < stemEndPoint.y) {
					stemTopPoint = stemStartPoint;
					stemBottomPoint = stemEndPoint;
				}
				else {
					stemTopPoint = stemEndPoint;
					stemBottomPoint = stemStartPoint;
				}
				
				// set the corresponding note and stem points
				Point2D.Double notePoint = null;
				Point2D.Double stemPoint = null;
				if (distance(stemMidPoint, noteLeftPoint) < distance(stemMidPoint, noteRightPoint)) {
					notePoint = candidateNoteBox.left();
					stemPoint = stemTopPoint;
					isDownwardStem = true;
				}
				else {
					notePoint = candidateNoteBox.right();
					stemPoint = stemBottomPoint;
					isDownwardStem = false;
				}
				if (distance(notePoint, stemPoint) > minCloseness)
					continue;
				
				// get the note and stop iterating
				note = candidateNote;
				foundNote = true;
				break;
			}
		}
		if (!foundNote)
			return null;
		
		// no stem test
		if (note.hasStem())
			return null;
		
		// create beautified stem
		BoundingBox noteBox = note.getBoundingBox();
		double stemX, stemTopY, stemBottomY;
		double stemLength = interval * STEM_LENGTH_RATIO;
		stemX = isDownwardStem ? noteBox.minX() : noteBox.maxX();
		stemTopY = isDownwardStem ? noteBox.centerY() : noteBox.centerY() - stemLength;
		stemBottomY = isDownwardStem ? noteBox.centerY() + stemLength : noteBox.centerY();
		Point2D.Double stemTopPoint = new Point2D.Double(stemX, stemTopY);
		Point2D.Double stemBottomPoint = new Point2D.Double(stemX, stemBottomY);
		List<Point2D.Double> stemPoints = new ArrayList<Point2D.Double>();
		stemPoints.add(stemTopPoint);
		stemPoints.add(stemBottomPoint);
		IStroke stemStroke = new IStroke(stemPoints, scribbleStroke.getColor());
		
		// TODO : add stem to note
		// * NoteShape note;
		// * IStroke stemStroke
		NoteShape.StemType stemType = isDownwardStem ? NoteShape.StemType.DOWNWARD : NoteShape.StemType.UPWARD;
		note.addStem(stemStroke, stemType);
		stemStroke.setColor(DEBUG_COLOR); // TODO : remove later?
//		IShape stemShape = new IShape(ShapeName.RAW, stemStroke);
		return shapes;
	}

	private List<IShape> hasFilledNoteHead(List<IShape> rawShapes, List<IShape> shapes, StaffShape staffShape) {
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		//
		IShape scribble = rawShapes.get(0);
		List<Point2D.Double> points = scribble.getStrokes().get(0).getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		
		// minimum distance test
		// input's length is at least greater than circumference of an interval circle
		double minLength = 2.0 * Math.PI * interval; 	// 2*circumference = 2*pi*d
		double length = pathDistance(points);
		if (length < minLength)
			return null;
		
		// bounding box test
		double width = scribble.getBoundingBox().width();
		double height = scribble.getBoundingBox().height();
		double minInterval = interval * NOTE_HEAD_BOX_MIN_RATIO;
		double maxInterval = interval * NOTE_HEAD_BOX_MAX_RATIO;
		if (width < minInterval || height < minInterval || maxInterval < width || maxInterval < height)
			return null;
		
		// create filled note head
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		scribbleStroke.setColor(DEBUG_COLOR); // TODO : remove later?
		ShapeName shapeName = ShapeName.NOTE;
		int position = staffShape.getPosition(scribbleStroke.getBoundingBox().centerY());
		double positionX = scribbleStroke.getBoundingBox().centerX();
		double positionY = staffShape.getPositionY(position);

		// get the beautified note stroke
		List<Point2D.Double> notePoints = getFilledNoteStroke(positionX, positionY, interval);
		IStroke noteStroke = new IStroke(notePoints, scribbleStroke.getColor());
		IShape noteShape = new NoteShape(shapeName, noteStroke, NoteShape.HeadType.EMPTY, position);
		shapes.add(noteShape);
		return shapes;
	}

	private List<IShape> hasEmptyNoteHead(List<IShape> rawShapes, List<IShape> shapes, StaffShape staffShape) { 
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		//
		IShape scribble = rawShapes.get(0);
		List<Point2D.Double> points = scribble.getStrokes().get(0).getPoints();
		double interval = staffShape.getLineInterval();		// diameter
		
		// circumference distance test
		// input's length is roughly the circumference of an interval circle
		double circumference = Math.PI * interval; 	// circumference = 2*pi*r = pi*d
		double lowerLimit = circumference * NOTE_HEAD_BOX_MIN_RATIO;
		double upperLimit = circumference * NOTE_HEAD_BOX_MAX_RATIO;
		double length = pathDistance(points);
		
		if (length < lowerLimit || upperLimit < length)
			return null;
		
		// bounding box test
		double width = scribble.getBoundingBox().width();
		double height = scribble.getBoundingBox().height();
		double minInterval = interval * NOTE_HEAD_BOX_MIN_RATIO;
		double maxInterval = interval * NOTE_HEAD_BOX_MAX_RATIO;
		if (width < minInterval || height < minInterval || maxInterval < width || maxInterval < height)
			return null;

		// closed shape test
		Point2D.Double startPoint = points.get(0);
		Point2D.Double endPoint = points.get(points.size()-1);
		if (distance(startPoint, endPoint) > interval * CLOSED_SHAPE_MIN_RATIO)
			return null;
		
		// create empty note head
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		scribbleStroke.setColor(DEBUG_COLOR); // TODO : remove later?
		ShapeName shapeName = ShapeName.NOTE;
		IShape temp = new IShape(ShapeName.RAW, scribbleStroke);
		int position = staffShape.getPosition(temp.getBoundingBox().centerY());
		double positionX = temp.getBoundingBox().centerX();
		double positionY = staffShape.getPositionY(position);
		
		// get the beautified note stroke
		List<Point2D.Double> notePoints = getEmptyNoteStroke(positionX, positionY, interval);
		IStroke noteStroke = new IStroke(notePoints, scribbleStroke.getColor());
		IShape noteShape = new NoteShape(shapeName, noteStroke, NoteShape.HeadType.EMPTY, position);
		shapes.add(noteShape);
		return shapes;
	}
	
	private List<Point2D.Double> getEmptyNoteStroke(double positionX, double positionY, double diameter) {
		
		// reduce the beautified stroke's diameter (visual hack)
		diameter *= VISUALIZED_NOTE_HEAD_RATIO;
		
		double radius = diameter / 2.0;
		
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		double a = positionX;
		double b = positionY;
		double r = radius;
		double pi = Math.PI;
		double x, y;
		Point2D.Double currentPoint;
		for (int i = 0; i <= 32; ++i) {
			
			x = a + r*Math.cos(i*pi/8);
			y = b + r*Math.sin(i*pi/8);
			currentPoint = new Point2D.Double(x, y);
			points.add(currentPoint);
		}
		
		return points;
	}
	
	private List<Point2D.Double> getFilledNoteStroke(double positionX, double positionY, double diameter) {
		
		// reduce the beautified stroke's diameter (visual hack)
		diameter *= VISUALIZED_NOTE_HEAD_RATIO;
		
		//
		List<List<Point2D.Double>> pointsList = new ArrayList<List<Point2D.Double>>();
		for (double i = 0.0; i < 1.0; i+=0.1)
			pointsList.add(getEmptyNoteStroke(positionX, positionY, diameter*i));
		
		//
		List<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
		for (List<Point2D.Double> points : pointsList)
			newPoints.addAll(points);
		
		return newPoints;
	}
	
	public static final double NOTE_HEAD_BOX_MIN_RATIO = 0.5;
	public static final double NOTE_HEAD_BOX_MAX_RATIO = 1.4;
	public static final double STEM_LENGTH_MIN_RATIO = 2;
	public static final double STEM_LENGTH_MAX_RATIO = 4;
	public static final double VERTICAL_ANGLE_MIN_THRESHOLD = 80.0;
	public static final double VERTICAL_ANGLE_MAX_THRESHOLD = 100.0;
	public static final double CLOSED_SHAPE_MIN_RATIO = 0.3;
	public static final double VISUALIZED_NOTE_HEAD_RATIO = 0.8;
	public static final double STEM_TO_HEAD_MIN_CLOSENESS_RATIO = 0.4;
	public static final double STEM_LENGTH_RATIO = 2.5;
	public static final Color DEBUG_COLOR = new Color(128, 0, 128);
}