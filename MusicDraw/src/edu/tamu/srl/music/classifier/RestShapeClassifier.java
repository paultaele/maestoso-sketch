package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.SketchPanel;

public class RestShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

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
		
		// TODO: scribble check
		
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
			IShape newShape = new IShape(newShapeName, newStrokes);
			
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
				
				newShape.setImageFile(IShape.getImage(newShapeName.name()));
				setLocation(newShape, shapes);
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
	
	private void setLocation(IShape shape, List<IShape> shapes) {
		
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
		double imageHeight = bufferedImage.getHeight();
		double imageWidth = bufferedImage.getWidth();
		
		//
		double xPos = 0.0;
		double yPos = 0.0;
		double newImageWidth = 0.0;
		double newImageHeight = 0.0;
		
		if (shape.getShapeName() == ShapeName.QUARTER_REST) {
			xPos = shape.getBoundingBox().minX();
			yPos = staffShape.getStaffPositionY(3);
					
			newImageHeight = staffShape.getLineInterval() * 3;
			newImageWidth = (newImageHeight*imageWidth)/imageHeight;
		}
		else if (shape.getShapeName() == ShapeName.EIGHTH_REST) {
			xPos = shape.getBoundingBox().minX();
			yPos = staffShape.getStaffPositionY(4);
					
			newImageHeight = staffShape.getLineInterval() * 2;
			newImageWidth = (newImageHeight*imageWidth)/imageHeight;
		}
		else {
			xPos = shape.getBoundingBox().minX();
			yPos = shape.getBoundingBox().minY();
			newImageHeight = imageHeight;
			newImageWidth = imageWidth;
		}
		
		//
		shape.setImageX((int)xPos);
		shape.setImageY((int)yPos);
		shape.setImageWidth((int)newImageWidth);
		shape.setImageHeight((int)newImageHeight);
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
//					|| top > staffShape.getLineY(1)
//					|| bottom < staffShape.getLineY(3)
					|| bottom > staffShape.getLineY(4)
			)
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
//					|| top > staffShape.getLineY(2)
//					|| bottom < staffShape.getLineY(2)
					|| bottom > staffShape.getLineY(4)
			)
				return true;
		}
		
//		// sharp test
//		if (shape.getShapeName() == ShapeName.KEY_SHARP) {
//			
//			if (shape.getStrokes().size() != 4)
//				return true;
//		}
//		
//		// flat test
//		if (shape.getShapeName() == ShapeName.KEY_FLAT) {
//			
//			if (shape.getStrokes().size() == 1) {
//				
//				IStroke flatStroke = shape.getStrokes().get(0);
//				Point2D.Double lastPoint = flatStroke.get(flatStroke.size() - 1);
//				Point2D.Double point = null;
//				boolean isLastPointFarRight = true;
//				for (int i = 0; i < flatStroke.size() - 1; ++i) {
//					
//					point = flatStroke.get(i);
//					if (point.x > lastPoint.x) {
//						isLastPointFarRight = false;
//						break;
//					}
//				}
//				if (isLastPointFarRight)
//					return true;
//			}
//		}
		
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
}