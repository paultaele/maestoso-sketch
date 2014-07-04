package edu.tamu.srl.music.classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;

public class TimeSignatureShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	public TimeSignatureShapeClassifier() {
		
		myShapes = null;
	}
	
	@Override
	public boolean classify(List<IShape> originals) {

		// clone the list of original shapes
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape original : originals)
			shapes.add(original);
		
		// count the number of numbers
		double numberCount = 0;
		// extract all raw shapes
		List<IShape> numberShapes = new ArrayList<IShape>();
		Iterator<IShape> iterator = shapes.iterator();
		while (iterator.hasNext()) {
			
			IShape shape = iterator.next();
			if (isNumber(shape)) {
				
				numberShapes.add(shape);
				iterator.remove();
				++numberCount;
			}
		}
		if (numberCount < 2)
			return false;
		
		// get the staff
		StaffShape staffShape = null;
		for (IShape shape : shapes) {

			if (shape.getShapeName() == ShapeName.WHOLE_STAFF)
			{
				staffShape = (StaffShape) shape;
				break;
			}
		}
		
		// TODO
		// NOTE: IShape(ShapeType, IStroke, File)
		// get the image of the top number
		
		
		// NOTE: IShape(ShapeType, IStroke, File)
		// get the image of the bottom number
		
		
		
		return false;
	}

	@Override
	public List<IShape> getResult() {
		
		return myShapes;
	}

	private boolean isNumber(IShape shape) {
		
		if (shape.getShapeName() == ShapeName.ONE)
			return true;
		if (shape.getShapeName() == ShapeName.TWO)
			return true;
		if (shape.getShapeName() == ShapeName.THREE)
			return true;
		if (shape.getShapeName() == ShapeName.FOUR)
			return true;
		if (shape.getShapeName() == ShapeName.FIVE)
			return true;
		if (shape.getShapeName() == ShapeName.SIX)
			return true;
		if (shape.getShapeName() == ShapeName.SEVEN)
			return true;
		if (shape.getShapeName() == ShapeName.EIGHT)
			return true;
		if (shape.getShapeName() == ShapeName.NINE)
			return true;
		
		return false;
	}
	
	private List<IShape> myShapes;
}
