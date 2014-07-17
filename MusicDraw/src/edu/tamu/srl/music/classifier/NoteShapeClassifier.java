package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
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
		
		
		return false;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}

	private List<IShape> hasFilledNoteHead(List<IShape> rawShapes, List<IShape> shapes, StaffShape staffShape) {
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		// minimum distance test
		// input's length is at least greater than circumference of an interval circle
		IShape scribble = rawShapes.get(0);
		List<Point2D.Double> points = scribble.getStrokes().get(0).getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		double minLength = 2.0 * Math.PI * interval; 	// circumference = 2*pi*d
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
		
		// need more tests?
		// ???
		
		// create filled rest
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		scribbleStroke.setColor(Color.red); // TODO : remove later?
		ShapeName shapeName = ShapeName.NOTE;
		IShape temp = new IShape(ShapeName.RAW, scribbleStroke);
		int position = staffShape.getStaffPosition(temp.getBoundingBox().centerY());
		IShape filledHead = new NoteShape(shapeName, scribbleStroke, NoteShape.HeadType.FILLED, position);
		shapes.add(filledHead);
		
		return shapes;
	}

	
	
	public static final double NOTE_HEAD_BOX_MIN_RATIO = 0.6;
	public static final double NOTE_HEAD_BOX_MAX_RATIO = 1.2;
}