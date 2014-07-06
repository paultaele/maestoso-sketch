package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;

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
		
		// line test
		if (isLine(rawShapes))
			return false;
		
		// right-angle test
//		if (isRightAngle(rawShape))
//			return false;
		
		//
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> strokes = getStrokes(rawShapes);
//		List<Template> templates = getTemplates(DATA_DIR_PATHNAME);
		List<Template> templates = Template.getTemplates(DATA_DIR_NAME);
		Pair result = classifier.classify(strokes, templates);

		// TODO
		if (ENABLE_OUTPUT) {
			System.out.println("### TESTING FOR KEY ###"); // TODO
			System.out.println("SCORE: " + result.score());
		}
		
		if (result.score() > MIN_SCORE_THRESHOLD) {
			
			// case: shape name is key sharp
			if (result.shape().equals("KeySharp")) {
				
				if (strokes.size() != 4)
					return false;
			}
			else if (result.shape().equals("KeyFlat")) {
				;
			}
			
			//
			ShapeName newShapeType = getShapeType(result.shape());
			List<IStroke> newStrokes = new ArrayList<IStroke>();
			for (IShape rawShape : rawShapes) {

				IStroke newStroke = rawShape.getStrokes().get(0);
				newStroke.setColor(Color.blue); // TEMP
				newStrokes.add(newStroke);
			}
			IShape shape = new IShape(newShapeType, newStrokes);
			
			//
			shapes.add(shape);
			myShapes = shapes;
			return true;
		}
		
		return false;
	}

	@Override
	public List<IShape> getResult() {

		return myShapes;
	}
	
	private ShapeName getShapeType(String shapeName) {
		
		if (shapeName.equals("KeyFlat"))
			return ShapeName.KEY_FLAT;
		if (shapeName.equals("KeySharp"))
			return ShapeName.KEY_SHARP;
		
		return ShapeName.RAW;
	}

	
	
	public static final String DATA_DIR_NAME = "key";
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/key/";
	public static final double MIN_SCORE_THRESHOLD = 0.75;
}
