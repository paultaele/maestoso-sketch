package edu.tamu.srl.music.classifier;

import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.classifier.IShape.ShapeGroup;

public class ShapeClassifier {

	public List<IShape> classify(List<IShape> shapes) {

		// 
		boolean containsWholeStaff = contains(shapes, ShapeName.WHOLE_STAFF);
		boolean containsClef = contains(shapes, ShapeGroup.CLEF);
		boolean containsBeat = contains(shapes, ShapeGroup.BEAT);
		boolean containsTimeSignature = contains(shapes, ShapeGroup.BEAT, 2);
		boolean canWriteMusic = containsWholeStaff && containsClef && containsTimeSignature;
				
		//
		boolean isClassified = false;
		IShapeClassifier classifier = null;
		
		// test for staff line and staff
		if (!containsWholeStaff) {
			
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
		if (containsWholeStaff && !containsClef) {
			
			// test for staff line
			classifier = new ClefShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
		}
		
		// test for keys and beats
		if (containsWholeStaff && containsClef && !containsTimeSignature) {
			
			// test for keys
			if (!containsBeat) {
				
				classifier = new KeyShapeClassifier();
				isClassified = classifier.classify(shapes);
				if (isClassified)
					return classifier.getResult();
			}
			
			// test for beats
			classifier = new BeatShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
		}
		
		if (canWriteMusic) {
			
			// test for notes
			classifier = new NoteShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
			
			// test for rests
			classifier = new RestShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
			
			// test for bar lines
			classifier = new BarShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
			
			// test for ledger lines
			classifier = new LedgerShapeClassifier();
			isClassified = classifier.classify(shapes);
			if (isClassified)
				return classifier.getResult();
		}
		
		//
		return shapes;
	}
	
	public static boolean contains(List<IShape> shapes, IShape.ShapeName shapeName) {
		
		return contains(shapes, shapeName, 1);
	}
	
	public static boolean contains(List<IShape> shapes, IShape.ShapeGroup shapeType) {
		
		return contains(shapes, shapeType, 1);
	}
	
	public static boolean contains(List<IShape> shapes, IShape.ShapeName shapeName, int totalCount) {
		
		int count = 0;
		for (IShape shape : shapes) {
		
			if (shape.getShapeName() == shapeName) {
				
				++count;
				if (count == totalCount)
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean contains(List<IShape> shapes, IShape.ShapeGroup shapeType, int totalCount) {
		
		int count = 0;
		for (IShape shape : shapes) {
		
			if (shape.getShapeGroup() == shapeType) {
				
				++count;
				if (count == totalCount)
					return true;
			}
		}
		
		return false;
	}
}
