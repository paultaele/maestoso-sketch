package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeType;

public class NumberShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

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
			if (shape.getShapeType() == ShapeType.RAW) {
				
				rawShapes.add(shape);
				iterator.remove();
			}
		}
		
		//
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> strokes = getStrokes(rawShapes);
		List<Template> templates = getTemplates(DATA_DIR_PATHNAME);
		Pair result = classifier.classify(strokes, templates);

		//
		if (result.score() > MIN_SCORE_THRESHOLD) {
			
			//
			ShapeType newShapeType = getShapeType(result.shape());
			List<IStroke> newStrokes = new ArrayList<IStroke>();
			for (IShape rawShape : rawShapes) {

				IStroke newStroke = rawShape.getStrokes().get(0);
				newStroke.setColor(Color.red); // TEMP
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

	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private ShapeType getShapeType(String shapeName) {
		
		if (shapeName.equals("1"))
			return ShapeType.ONE;
		if (shapeName.equals("2"))
			return ShapeType.TWO;
		if (shapeName.equals("3"))
			return ShapeType.THREE;
		if (shapeName.equals("4"))
			return ShapeType.FOUR;
		if (shapeName.equals("5"))
			return ShapeType.FIVE;
		if (shapeName.equals("6"))
			return ShapeType.SIX;
		if (shapeName.equals("7"))
			return ShapeType.SEVEN;
		if (shapeName.equals("8"))
			return ShapeType.EIGHT;
		if (shapeName.equals("9"))
			return ShapeType.NINE;
		
		return ShapeType.RAW;
	}
	
	
	
	private List<IShape> myShapes;
	
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/number/";
	public static final double MIN_SCORE_THRESHOLD = 0.75;
}
