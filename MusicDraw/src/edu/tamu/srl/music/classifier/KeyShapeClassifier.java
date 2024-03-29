package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.SketchPanel;

public class KeyShapeClassifier
	extends AbstractShapeClassifier implements IShapeClassifier {

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
		
		// create and run the classifier
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> rawStrokes = getStrokes(rawShapes);
		List<Template> templates = Template.getTemplates(DATA_DIR_NAME);
		Pair result = classifier.classify(rawStrokes, templates);

		if (SketchPanel.DISPLAY_SHAPE_SCORES) {
			System.out.println("### TESTING FOR KEY ###");
			System.out.println("SCORE: " + result.score());
		}
		
		// case: the result's score exceeds the minimum threshold
		// therefore, the stroke(s) have high enough confidence to be that shape
		if (result.score() > MIN_SCORE_THRESHOLD) {
			
			// get the shape name
			ShapeName newShapeName = getShapeName(result.shape());
			
			// get the stroke
			List<IStroke> newStrokes = new ArrayList<IStroke>();
			for (IShape rawShape : rawShapes) {
				IStroke newStroke = rawShape.getStrokes().get(0);
				newStrokes.add(newStroke);
			}
			
			// get the staff position
			IShape staff = null;
			for (IShape s : shapes) {
				if (s.getShapeName() == IShape.ShapeName.STAFF) {
					staff = s;
					break;
				}
			}
			StaffShape staffShape = (StaffShape)staff;
			IShape temp = new IShape(ShapeName.RAW, newStrokes);
			int position = staffShape.getPosition(temp.getBoundingBox().centerY());
			
			// set the shape
			IShape newShape = new KeyShape(newShapeName, newStrokes, position);
			
			// check if the shape falls in any special case
			// if the shape is a special case, then it's not that shape
			boolean isSpecialCase = isSpecialCase(newShape);
			if (isSpecialCase)
				return false;
			else
				newShape.setColor(Color.blue); // TEMP
			
			// set the shape's image
			setImage(newShape, shapes);
			
			// add the shape to the list of shapes
			shapes.add(newShape);
			myShapes = shapes;
			return true;
		}
		
		return false;
	}
	
	public List<IShape> getResult() {

		return myShapes;
	}
	
	private void setImage(IShape shape, List<IShape> shapes) {
		
		KeyShape keyShape = (KeyShape)shape;
		
		// get the staff as reference for setting the shape
		IShape staff = null;
		for (IShape s : shapes) {
			if (s.getShapeName() == IShape.ShapeName.STAFF) {
				staff = s;
				break;
			}
		}
		StaffShape staffShape = (StaffShape)staff;

		// get the buffered image's width and height
		BufferedImage bufferedImage = IShape.getImage(keyShape.getShapeName().name());
		double x = 0.0;
		double y = 0.0;
		double originalWidth = bufferedImage.getWidth();
		double originalHeight = bufferedImage.getHeight();
		double height = staffShape.getLineInterval() * 2.0;
		double width = (height*originalWidth)/originalHeight;
		
		// set the shape's offset to line up with the staff lines
		double shapeOffset = staffShape.getLineInterval();
		if (shape.getShapeName() == ShapeName.KEY_FLAT)
			shapeOffset *= 0.9;
		else if (shape.getShapeName() == ShapeName.KEY_SHARP)
			shapeOffset *= 1.1;
		x = shape.getBoundingBox().minX();
		y = staffShape.getPositionY(keyShape.getPosition()) - shapeOffset;

		//
		IImage image = new IImage(bufferedImage, x, y, width, height);
		shape.addImage(image);
	}
	
	private boolean isSpecialCase(IShape shape) {
		
		// line test
		// note: no beat shape is a singleton line
		if (shape.getStrokes().size() == 1) {
			
			List<Point2D.Double> points = shape.getStrokes().get(0).getPoints();
			if (isLine(points))
				return true;
		}
		
		// sharp test
		if (shape.getShapeName() == ShapeName.KEY_SHARP) {
			
			if (shape.getStrokes().size() != 4)
				return true;
		}
		
		// flat test
		if (shape.getShapeName() == ShapeName.KEY_FLAT) {
			
			if (shape.getStrokes().size() == 1) {
				
				// ???
				IStroke flatStroke = shape.getStrokes().get(0);
				Point2D.Double lastPoint = flatStroke.get(flatStroke.size() - 1);
				Point2D.Double point = null;
				boolean isLastPointFarRight = true;
				for (int i = 0; i < flatStroke.size() - 1; ++i) {
					
					point = flatStroke.get(i);
					if (point.x > lastPoint.x) {
						isLastPointFarRight = false;
						break;
					}
				}
				if (isLastPointFarRight)
					return true;
			}
		}
		
		// shape is not a special case
		return false;
	}
	
	private ShapeName getShapeName(String shapeName) {
		
		if (shapeName.equals("KeyFlat"))
			return ShapeName.KEY_FLAT;
		if (shapeName.equals("KeySharp"))
			return ShapeName.KEY_SHARP;
		
		return ShapeName.RAW;
	}

	
	
	
	public static final String DATA_DIR_NAME = "key";
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/key/";
	public static final double MIN_SCORE_THRESHOLD = 0.70;
}
