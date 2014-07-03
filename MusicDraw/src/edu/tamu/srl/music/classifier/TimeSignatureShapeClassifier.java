package edu.tamu.srl.music.classifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeType;

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

			if (shape.getShapeType() == ShapeType.STAFF)
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
		
		if (shape.getShapeType() == ShapeType.ONE)
			return true;
		if (shape.getShapeType() == ShapeType.TWO)
			return true;
		if (shape.getShapeType() == ShapeType.THREE)
			return true;
		if (shape.getShapeType() == ShapeType.FOUR)
			return true;
		if (shape.getShapeType() == ShapeType.FIVE)
			return true;
		if (shape.getShapeType() == ShapeType.SIX)
			return true;
		if (shape.getShapeType() == ShapeType.SEVEN)
			return true;
		if (shape.getShapeType() == ShapeType.EIGHT)
			return true;
		if (shape.getShapeType() == ShapeType.NINE)
			return true;
		
		return false;
	}
	
	private List<IShape> myShapes;
}
