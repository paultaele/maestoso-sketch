package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.SketchPanel;

public class BeatShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

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
		
		// create and run the classifier
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> strokes = getStrokes(rawShapes);
		List<Template> templates = Template.getTemplates(DATA_DIR_NAME);
		Pair result = classifier.classify(strokes, templates);

		// TODO
		if (SketchPanel.DISPLAY_SHAPE_SCORES) {
			System.out.println("### TESTING BEAT ###"); // TODO
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
				newShape.setColor(Color.red); // TEMP
			
			// set the shape's image
			setImage(newShape, shapes);
			
			//
			shapes.add(newShape);
			myShapes = shapes;
			return true;
		}
		
		return false;
	}
	
	private boolean isSpecialCase(IShape candidate, List<IShape> shapes) {
		
		// line test
		// note: no beat shape is a singleton line
		if (candidate.getStrokes().size() == 1) {
			
			List<Point2D.Double> points = candidate.getStrokes().get(0).getPoints();
			if (isLine(points))
				return true;
		}
		
		// path length test
		IShape staff = null;
		for (IShape shape : shapes) {
			if (shape.getShapeName() == IShape.ShapeName.WHOLE_STAFF) {
				staff = shape;
				break;
			}
		}
		StaffShape staffShape = (StaffShape)staff;
		double pathLength = 0.0;
		for (IStroke stroke : candidate.getStrokes()) {
			pathLength += pathDistance(stroke.getPoints());
		}
		if (pathLength < staffShape.getLineInterval() * 2.0) { // check if path length is less than two staff line intervals
			return true;
		}
		
		return false;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private ShapeName getShapeName(String shapeName) {
		
		if (shapeName.equals("2"))
			return ShapeName.TWO;
		if (shapeName.equals("3"))
			return ShapeName.THREE;
		if (shapeName.equals("4"))
			return ShapeName.FOUR;
		if (shapeName.equals("5"))
			return ShapeName.FIVE;
		if (shapeName.equals("6"))
			return ShapeName.SIX;
		if (shapeName.equals("7"))
			return ShapeName.SEVEN;
		if (shapeName.equals("8"))
			return ShapeName.EIGHT;
		if (shapeName.equals("9"))
			return ShapeName.NINE;
		
		return ShapeName.RAW;
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
		
		// check if there is another beat already drawn to use its location as reference
		boolean hasBeat = false;
		IShape other = null;
		for (IShape s : shapes) {
			
			if (s.getShapeGroup() == IShape.ShapeGroup.BEAT) {
				
				hasBeat = true;
				other = s;
			}
		}
		
		//
		BufferedImage bufferedImage = IShape.getImage(shape.getShapeName().name());
		double originalWidth = bufferedImage.getWidth();
		double originalHeight = bufferedImage.getHeight();
		double width = 0.0;
		double height = 0.0;
		double x = 0.0;
		double y = 0.0;
		
		if (hasBeat) {
			
			x = other.getImages().get(0).x();
			y = staffShape.getLineY(2);
		}
		else {
			
			x = shape.getBoundingBox().minX();
			y = staffShape.getLineY(0);
		}
		height = (int)(staffShape.getLineInterval() * 2);
		width = ((int)height*originalWidth)/originalHeight;
		
		IImage image = new IImage(bufferedImage, x, y, width, height);
		shape.addImage(image);
	}
	
	public static final String DATA_DIR_NAME = "beat";
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/beat/";
	public static final double MIN_SCORE_THRESHOLD = 0.75;
}
