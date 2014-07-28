package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.SketchPanel;

public class RestShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	public boolean classify(List<IShape> originals) {
		
		// clone the list of original shapes
		// extract all raw shapes
		// get the staff as reference for setting the shape
		List<IShape> shapes = cloneShapes(originals);
		List<IShape> rawShapes = extractRawShapes(shapes);
		StaffShape staffShape = getStaffShape(shapes);
		
		// filled rest test
		List<IShape> shapesWithFilledRest = hasFilledRest(rawShapes, shapes);
		if (shapesWithFilledRest != null) {
			
			myShapes = shapesWithFilledRest;
			return true;
		}
		
		// dots test
		List<IShape> shapesWithDot = hasDot(rawShapes, shapes, staffShape);
		if (shapesWithDot != null) {
			
			myShapes = shapesWithDot;
			return true;
		}
		
		// create and run the classifier
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> strokes = getStrokes(rawShapes);
		List<Template> templates = Template.getTemplates(DATA_DIR_NAME);
		Pair result = classifier.classify(strokes, templates);
		
		// TODO
		if (SketchPanel.DISPLAY_SHAPE_SCORES) {
			System.out.println("### TESTING REST ###"); // TODO
			System.out.println("SCORE: " + result.score());
		}
		
		// case: the result's score exceeds the minimum threshold
		// therefore, the stroke(s) have high enough confidence to be that shape
		if (result.score() > MIN_SCORE_THRESHOLD) {
			
			// set the IShape object
			ShapeName newShapeName = getShapeName(result.shape());
			List<IStroke> newStrokes = new ArrayList<IStroke>();
			for (IShape rawShape : rawShapes) {
				
				IStroke newStroke = rawShape.getStrokes().get(0);
				newStrokes.add(newStroke);
			}
			IShape newShape = new RestShape(newShapeName, newStrokes);
			
			// check if the shape falls in any special case
			// if the shape is a special case, then it's not that shape
			boolean isSpecialCase = isSpecialCase(newShape, shapes);
			if (isSpecialCase)
				return false;
			else
				newShape.setColor(Color.orange); // TEMP
			
			// set the shape's image
			if (newShape.getShapeName() != ShapeName.UPPER_BRACKET
					&& newShape.getShapeName() != ShapeName.LOWER_BRACKET) {
				
				setImage(newShape, shapes);
			}
			
			//
			shapes.add(newShape);
			myShapes = shapes;
			return true;
		}
				
		
		return false;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private List<IShape> hasFilledRest(List<IShape> rawShapes, List<IShape> originalShapes) {
		
		// case: singleton test
		if (rawShapes.size() > 1)
			return null;
		
		// get scribble candidate
		IShape scribble = rawShapes.get(0);
		
		// copy original shapes
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape originalShape : originalShapes)
			shapes.add(originalShape);
		
		// get bracket
		boolean hasBracket = false;
		IShape bracket = null;
		Iterator<IShape> iterator = shapes.iterator();
		while (iterator.hasNext()) {
			
			IShape shape = iterator.next();
			if (shape.getShapeName() == ShapeName.UPPER_BRACKET
					|| shape.getShapeName() == ShapeName.LOWER_BRACKET) {
				
				bracket = shape;
				iterator.remove();
				hasBracket = true;
				break;
			}
		}
		if (!hasBracket)
			return null;
		
		// bracket contains scribble test
		// TODO: more stringent test
		if (!bracket.getBoundingBox().contains(scribble.getBoundingBox().center())) {
		
			return null;
		}
		
		// sufficiently filled test
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		int numContained = 0;
		int totalContained = scribbleStroke.size();
		for (Point2D.Double point : scribbleStroke.getPoints()) {
			if (bracket.getBoundingBox().contains(point))
				++numContained;
		}
		if (((double)numContained/(double)totalContained) < FILLED_REST_RATIO_FLOOR)
			return null;
		
		// create filled rest
		List<IStroke> strokes = new ArrayList<IStroke>();
		strokes.add(scribble.getStrokes().get(0));
		strokes.add(bracket.getStrokes().get(0));
		IShape.ShapeName shapeName = null;
		if (bracket.getShapeName() == ShapeName.LOWER_BRACKET)
			shapeName = ShapeName.WHOLE_REST;
		else if (bracket.getShapeName() == ShapeName.UPPER_BRACKET)
			shapeName = ShapeName.HALF_REST;
		IShape filledRest = new RestShape(shapeName, strokes);
		shapes.add(filledRest);
		
		// set the image and clear the strokes
		setImage(filledRest, shapes);
		
		return shapes;
	}
	
	private List<IShape> hasDot(List<IShape> rawShapes, List<IShape> originalShapes, StaffShape staffShape) {
		
		// clone the original shapes
		List<IShape> shapes = cloneShapes(originalShapes);
		
		// get the dot shape
		IShape dotShape = rawShapes.get(0);
		IStroke dotStroke = dotShape.getStrokes().get(0);
		List<Point2D.Double> dotPoints = dotStroke.getPoints();
		Point2D.Double dotPoint = dotShape.getBoundingBox().center();
		
		// get all eligible rest shapes
		List<RestShape> restShapes = new ArrayList<RestShape>();
		ShapeName shapeName = null;
		for (IShape shape : shapes) {

			// get the current shape name
			shapeName = shape.getShapeName();
			
			// see if the shape name matches a rest shape
			if (shapeName == ShapeName.WHOLE_REST || shapeName == ShapeName.HALF_REST
					|| shapeName == ShapeName.QUARTER_REST || shapeName == shapeName.EIGHTH_REST) {
				
				// get the rest image's top point
				IImage restImage = shape.getImages().get(0);
				Point2D.Double restImagePoint = new Point2D.Double(restImage.x() + restImage.width()/2, restImage.y());
				
				// add the shape if the image's right-hand side is left of the dot shape
				if (restImagePoint.x < dotPoint.x)
					restShapes.add((RestShape)shape);
			}
		}
		
		// 1. non-zero completed rests test
		if (restShapes.size() == 0)
			return null;
		
		// 2. singleton raw shape test
		// input should only be a single raw shape
		if (rawShapes.size() > 1)
			return null;
		
		// 3. size test
		// see if the shape is actually a dot
		double interval = staffShape.getLineInterval();	// diameter
		double length = pathDistance(dotPoints);
		if (length > NoteShapeClassifier.MAX_DOT_LENGTH || dotStroke.size() > NoteShapeClassifier.MAX_DOT_NUM_POINTS)
			return null;
		
		// 4. location test
		// see if the shape is between the second and third staff line
		if (dotPoint.y < staffShape.getLineY(1) || staffShape.getLineY(2) < dotPoint.y)
			return null;
		
		// find the corresponding rest
		Point2D.Double closestRestImagePoint = null;					// initialize the closest rest image's position
		RestShape closestRestShape = null;								// initialize the closest rest shape
		double minDistance = java.lang.Double.MAX_VALUE;				// initialize the minimum distance
		for (RestShape restShape : restShapes) {
			
			// get the rest image's top right corner
			IImage restImage = restShape.getImages().get(0);
			Point2D.Double restImagePoint = new Point2D.Double(restImage.x() + restImage.width(), restImage.y());
			
			// get the current distance and see if it's the closest rest shape
			double currDistance = Math.abs(restImagePoint.x - dotPoint.x);
			if (currDistance < minDistance) {
				minDistance = currDistance;
				closestRestShape = restShape;
				closestRestImagePoint = restImagePoint;
			}
		}
		
		// 5. distance test
		if (minDistance > interval)
			return null;
		
		//
		double dotX = closestRestImagePoint.x + interval/2;
		double dotY = staffShape.getLineY(1) + interval/2;
		List<Point2D.Double> cleanDotPoints = NoteShapeClassifier.getFilledNoteStroke(dotX, dotY, 10);
		IStroke cleanDotStroke = new IStroke(cleanDotPoints, DEBUG_COLOR);
		closestRestShape.addDot(cleanDotStroke);
		closestRestShape.toggleDisplayStrokes(true);
		
		// remove and re-add rest shape so undoing removes the newly dotted shape
		shapes.remove(closestRestShape);
		shapes.add(closestRestShape);
		
		//
		return shapes;
	}
	
	
	private void setImage(IShape shape, List<IShape> shapes) {
		
		// get the staff as reference for setting the shape
		IShape staff = null;
		for (IShape s : shapes) {
			if (s.getShapeName() == IShape.ShapeName.WHOLE_STAFF) {
				staff = s;
				break;
			}
		}
		StaffShape staffShape = (StaffShape)staff;
		
		// get the buffered image's width and height
		BufferedImage bufferedImage = IShape.getImage(shape.getShapeName().name());
		double originalHeight = bufferedImage.getHeight();
		double originalWidth = bufferedImage.getWidth();
		
		//
		double x = 0.0;
		double y = 0.0;
		double width = 0.0;
		double height = 0.0;
		
		if (shape.getShapeName() == ShapeName.QUARTER_REST) {
			x = shape.getBoundingBox().minX();
			y = staffShape.getPositionY(3);
					
			height = staffShape.getLineInterval() * 3;
			width = (height*originalWidth)/originalHeight;
		}
		else if (shape.getShapeName() == ShapeName.EIGHTH_REST) {
			x = shape.getBoundingBox().minX();
			y = staffShape.getPositionY(4);
					
			height = staffShape.getLineInterval() * 2;
			width = (height*originalWidth)/originalHeight;
		}
		else if (shape.getShapeName() == ShapeName.WHOLE_REST) {
			x = shape.getBoundingBox().minX();
			y = staffShape.getPositionY(4);
					
			height = staffShape.getLineInterval() * 0.5;
			width = (height*originalWidth)/originalHeight;
		}
		else if (shape.getShapeName() == ShapeName.HALF_REST) {
			x = shape.getBoundingBox().minX();
			y = staffShape.getPositionY(5);
					
			height = staffShape.getLineInterval() * 0.5;
			width = (height*originalWidth)/originalHeight;
		}
		else {
			x = shape.getBoundingBox().minX();
			y = shape.getBoundingBox().minY();
			height = originalHeight;
			width = originalWidth;
		}
		
		// set the image and clear the strokes
		IImage image = new IImage(bufferedImage, x, y, width, height);
		shape.addImage(image);
		shape.clearStrokes();
	}
	
	private boolean isSpecialCase(IShape shape, List<IShape> shapes) {
		
		// get the staff as reference for setting the shape
		IShape staff = null;
		for (IShape s : shapes) {
			if (s.getShapeName() == IShape.ShapeName.WHOLE_STAFF) {
				staff = s;
				break;
			}
		}
		StaffShape staffShape = (StaffShape)staff;
		
		// line test
		// note: no rest shape is a singleton line
		if (shape.getStrokes().size() == 1) {
			
			List<Point2D.Double> points = shape.getStrokes().get(0).getPoints();
			if (isLine(points))
				return true;
		}
		
		// upper/lower bracket test
		if (shape.getShapeName() == ShapeName.UPPER_BRACKET
				|| shape.getShapeName() == ShapeName.LOWER_BRACKET) {
			
			double interval = staffShape.getLineInterval();
			
			// case: ratio test
			if (shape.getBoundingBox().height() > shape.getBoundingBox().width())
				return true;
			
			// case: height test
			if (shape.getBoundingBox().height() > interval * 1.5)
				return true;
			
			// case: width test
			if (shape.getBoundingBox().width() > interval * 2.5)
				return true;
			
			// case: center test
			double line1Y = staffShape.getLineY(1);
			double line2Y = staffShape.getLineY(2);
			double centerY = shape.getBoundingBox().centerY();
			if (centerY < line1Y || centerY > line2Y)
				return true;
		}
		
		// quarter rest test
		if (shape.getShapeName() == ShapeName.QUARTER_REST) {
			
			// case: singleton stroke test
			if (shape.getStrokes().size() > 1)
				return true;
			
			// case: height test
			double top = shape.getBoundingBox().minY();
			double bottom = shape.getBoundingBox().maxY();
			if (top < staffShape.getLineY(0)
					|| bottom > staffShape.getLineY(4))
				return true;
			
			// case: length test
			if (pathDistance(shape.getStrokes().get(0).getPoints()) < staffShape.getLineInterval())
				return true;
		}
		
		// eighth rest test
		if (shape.getShapeName() == ShapeName.EIGHTH_REST) {
			
			// case: singleton stroke test
			if (shape.getStrokes().size() > 1)
				return true;
			
			// case: height test
			double top = shape.getBoundingBox().minY();
			double bottom = shape.getBoundingBox().maxY();
			if (top < staffShape.getLineY(0)
					|| bottom > staffShape.getLineY(4))
				return true;
		}
		
		// shape is not a special case
		return false;
	}
	
	private ShapeName getShapeName(String shapeName) {
		
		if (shapeName.equals("EighthRest"))
			return ShapeName.EIGHTH_REST;
		if (shapeName.equals("LowerBracket"))
			return ShapeName.LOWER_BRACKET;
		if (shapeName.equals("QuarterRest"))
			return ShapeName.QUARTER_REST;
		if (shapeName.equals("UpperBracket"))
			return ShapeName.UPPER_BRACKET;
		
		return ShapeName.RAW;
	}

	public static final String DATA_DIR_NAME = "rest";
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/rest/";
	public static final double MIN_SCORE_THRESHOLD = 0.75;
	public static final double FILLED_REST_RATIO_FLOOR = 0.8;
}
