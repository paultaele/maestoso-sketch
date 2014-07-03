package edu.tamu.srl.music.classifier;

import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeType;

public class ShapeClassifier {

	public List<IShape> classify(List<IShape> shapes) {

		// 
		boolean containsStaff = containsStaff(shapes);
		boolean containsClef = containsClef(shapes);
		boolean containsNumber = containsNumber(shapes);
		boolean containsTimeSignature = containsTimeSignature(shapes);
				
		
		//
		boolean isClassified = false;
		IShapeClassifier classifier = null;
		
		// test for staff line and staff
		if (!containsStaff) {
			
			// test for staff line
			classifier = new StaffLineShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified) {

				// test for staff
				List<IShape> newShapes = classifier.getResult();
				classifier = new StaffShapeClassifier();
				isClassified = classifier.classify(newShapes);
				if (isClassified)
					return classifier.getResult();
				else
					return newShapes;
			}
		}
		
		// test for clef
		if (containsStaff && !containsClef) {
				
			// test for staff line
			classifier = new ClefShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
		}
		
		//
		if (containsStaff && containsClef && !containsTimeSignature) {
			
			// test for accidentals
			if (!containsNumber) {
//				
//				
			}
			
			// test for numbers
			classifier = new NumberShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified) {
			
				// temp
//				return classifier.getResult();
				
				// test for time signature
				List<IShape> newShapes = classifier.getResult();
				classifier = new TimeSignatureShapeClassifier();
				isClassified = classifier.classify(newShapes);
				if (isClassified)
					return classifier.getResult();
				else
					return newShapes;
			}
		}
		
		
		//
		return shapes;
	}
	
	public static boolean contains(List<IShape> shapes, IShape.ShapeType shapeType) {
		
		for (IShape shape : shapes)
			if (shape.getShapeType() == shapeType)
				return true;
		
		return false;
	}
	
	private boolean containsStaff(List<IShape> shapes) {
		
		return ShapeClassifier.contains(shapes, ShapeType.STAFF);
	}
	
	private boolean containsClef(List<IShape> shapes) {
		
		return ShapeClassifier.contains(shapes, ShapeType.TREBLE_CLEF) 
				|| ShapeClassifier.contains(shapes, ShapeType.BASS_CLEF);
	}
	
	private boolean containsNumber(List<IShape> shapes) {
		
		for (IShape shape : shapes)
			if (isNumber(shape))
				return true;
		
		return false;
	}
	
	private boolean containsTimeSignature(List<IShape> shapes) {
		
		int numberCount = 0;
		for (IShape shape : shapes)
			if (isNumber(shape))
				++numberCount;
		
		return numberCount >= 2;
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
}
