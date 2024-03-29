package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeGroup;
import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.classifier.NoteShape.AccidentalType;
import edu.tamu.srl.music.classifier.NoteShape.HeadType;
import edu.tamu.srl.music.classifier.NoteShape.StemType;
import edu.tamu.srl.music.gui.SketchPanel;

public class NoteShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

	public boolean classify(List<IShape> originals) {
		
		// clone the list of original shapes
		// extract all raw shapes
		// get the staff as reference for setting the shape
		List<IShape> shapes = cloneShapes(originals);
		List<IShape> rawShapes = extractRawShapes(shapes);
		StaffShape staffShape = getStaffShape(shapes);
		List<IShape> newShapes = null;
		
		// 1. Filled note head test.
		newShapes = hasFilledNoteHead(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// 2. Empty note head test.
		newShapes = hasEmptyNoteHead(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// 3. Stem test.
		newShapes = hasStem(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// 4. Flag test.
		newShapes = hasFlag(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// 5. Beam test.
		newShapes = hasBeam(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// 6. Dot test.
		newShapes = hasDot(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		// 7. Accidental test.
		newShapes = hasAccidental(rawShapes, shapes, staffShape);
		if (newShapes != null) {
			myShapes = newShapes;
			return true;
		}
		
		return false;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private List<IShape> hasAccidental(List<IShape> originalRawShapes, List<IShape> originalShapes, StaffShape staffShape) {
		
		// clone the original and raw shapes
		List<IShape> shapes = cloneShapes(originalShapes);
		List<IShape> rawShapes = cloneShapes(originalRawShapes);
		
		// get all notes without accidentals
		List<NoteShape> noteShapes = new ArrayList<NoteShape>();
		ShapeName shapeName = null;
		for (IShape shape : shapes) {

			// get the current shape name
			shapeName = shape.getShapeName();
			
			// see if the shape name matches a note shape
			NoteShape noteShape = null;
			if (shapeName == ShapeName.NOTE) {
				
				noteShape = (NoteShape)shape;
				
				if (!noteShape.hasAccidental())
					noteShapes.add(noteShape);
			}
		}
		
		// non-zero shapes test
		if (noteShapes.size() == 0)
			return null;
		
		// extract out the ledger lines into the raw shapes list
		Iterator<IShape> iterator = shapes.iterator();
		while (iterator.hasNext()) {
			IShape currentShape = iterator.next();
			if (currentShape.getShapeGroup() == ShapeGroup.LEDGER) {
				iterator.remove();
				rawShapes.add(currentShape);
			}
		}
		
		// 
		double rawLengths = 0.0;
		double interval = staffShape.getLineInterval();
		for (IShape rawShape : originalRawShapes) {
			
			List<Point2D.Double> shapePoints = rawShape.getStrokes().get(0).getPoints();
			rawLengths += pathDistance(shapePoints);
		}
		if (rawLengths < interval * ACCIDENTAL_LENGTH_MIN_RATIO)
			return null;
		
		// create and run the classifier
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> pointsLists = getStrokes(originalRawShapes);
		List<Template> templates = Template.getTemplates(DATA_DIR_NAME);
		Pair result = classifier.classify(pointsLists, templates);
		
		// console debugging output
		if (SketchPanel.DISPLAY_SHAPE_SCORES) {
			System.out.println("### TESTING REST ###"); // TODO
			System.out.println("SCORE: " + result.score());
		}
		
		// visual test
		// determines if shape is visually an accidental
		AccidentalType accidentalType = AccidentalType.NONE;
		if (result.score() > MIN_SCORE_THRESHOLD) {
			
			// see if it passes special cases
			if (result.shape().equals("Sharp")) {					// sharp should only have 4 strokes
				if (pointsLists.size() == 4)
					accidentalType = AccidentalType.SHARP;
			}
			else if (result.shape().endsWith("Flat")) {				// flat should not have more than 2 strokes
				if (pointsLists.size() <= 2)
					accidentalType = AccidentalType.FLAT;
			}
			else if (result.shape().equals("Natural")) {
				
				boolean isNatural = true;
				if (pointsLists.size() != 2)							// natural should only have 2 strokes
					isNatural = false;
				for (List<Point2D.Double> points : pointsLists)	{		// each stroke in natural should not be a line
					if (isLine(points)) {
						isNatural = false;
						break;
					}
				}
				if (isNatural)
					accidentalType = AccidentalType.NATURAL;
			}
		}
		if (accidentalType == AccidentalType.NONE)
			return null;
		
		// set the placeholder accidental IShape
		List<IStroke> scribbleStrokes = new ArrayList<IStroke>();
		for (List<Point2D.Double> points : pointsLists)
			scribbleStrokes.add(new IStroke(points, Color.black));
		IShape accidental = new IShape(ShapeName.RAW, scribbleStrokes);
		
		// find the closest image
		NoteShape randomNoteShape = noteShapes.get(0);
		Point2D.Double accidentalPoint = accidental.getBoundingBox().right();
		double minDistance = java.lang.Double.MAX_VALUE;				// initialize the minimum distance
		NoteShape closestNoteShape = null;
		Point2D.Double closestNotePoint = null;
		for (NoteShape noteShape : noteShapes) {
			
			// get the current note's bounding box and edge point
			BoundingBox noteBox = noteShape.getBoundingBox();
			Point2D.Double notePoint = notePoint = noteBox.left();
			
			// get the current distance and see if it's the closest note shape
			double currDistance = Math.abs(notePoint.x - accidentalPoint.x);
			if (currDistance < minDistance) {
				minDistance = currDistance;
				closestNoteShape = noteShape;
				closestNotePoint = notePoint;
			}
		}
		
		// distance test
		if (minDistance > interval)
			return null;
		
		// set image
		BufferedImage bufferedImage = IShape.getImage(accidentalType.name());
		double originalWidth = bufferedImage.getWidth();
		double originalHeight = bufferedImage.getHeight();
		double height = staffShape.getLineInterval() * 2.0;
		double width = (height*originalWidth)/originalHeight;
		double xOffset = interval*0.5;
		double yOffset = accidentalType == AccidentalType.FLAT ? height*0.75 : height*0.5;
		double x = closestNotePoint.x - width - xOffset;
		double y = closestNotePoint.y - yOffset;
		IImage image = new IImage(bufferedImage, x, y, width, height);
		closestNoteShape.addAccidental(accidentalType, image, scribbleStrokes);
		
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
		
		// get all non-dotted notes
		List<NoteShape> allNoteShapes = new ArrayList<NoteShape>();
		ShapeName shapeName = null;
		for (IShape shape : shapes) {

			// get the current shape name
			shapeName = shape.getShapeName();
			
			// see if the shape name matches a note shape
			NoteShape noteShape = null;
			if (shapeName == ShapeName.NOTE) {
				
				noteShape = (NoteShape)shape;
				
				if (!noteShape.hasDot())
					allNoteShapes.add(noteShape);
			}
		}
		
		// 1. non-zero shapes test
		if (allNoteShapes.size() == 0)
			return null;
		
		// 2. singleton raw shape test
		// input should only be a single raw shape
		if (rawShapes.size() > 1)
			return null;
		
		// 3. dot size test
		// see if the shape is actually a dot
		double interval = staffShape.getLineInterval();	// diameter
		double length = pathDistance(dotPoints);
		if (length > MAX_DOT_LENGTH || dotStroke.size() > MAX_DOT_NUM_POINTS)
			return null;
		
		// 4. same level test
		// the dot has to be at the same level position as the note within half a staff line interval
		double dotHeight = dotShape.getBoundingBox().centerY();
		List<NoteShape> levelNoteShapes = new ArrayList<NoteShape>();
		for (NoteShape noteShape : allNoteShapes) {
			
			int notePosition = noteShape.position();
			double upperHeight = staffShape.getPositionY(notePosition-1);
			double lowerHeight = staffShape.getPositionY(notePosition+1);
			
			if (upperHeight < dotHeight && dotHeight < lowerHeight)
				levelNoteShapes.add(noteShape);
		}
		if (levelNoteShapes.size() == 0)
			return null;
		
		// valid dot position test
		List<NoteShape> validNoteShapes = new ArrayList<NoteShape>();
		for (NoteShape noteShape : levelNoteShapes) {
			
			// case: stem notes
			if ((noteShape.headType() == HeadType.FILLED || noteShape.headType() == HeadType.EMPTY ) && noteShape.hasStem()) {
				
				// case: upward stem (dot on left)
				if (noteShape.stemType() == StemType.UPWARD) {
					if (dotShape.getBoundingBox().center().x < noteShape.getHeadBox().left().x) {
						validNoteShapes.add(noteShape);
					}
				}
				
				// case: downward stem (dot on right)
				else if (noteShape.stemType() == StemType.DOWNWARD) {
					if (noteShape.getHeadBox().right().x < dotShape.getBoundingBox().center().x)
						validNoteShapes.add(noteShape);
				}
			}
			
			// case: whole notes
			else if (noteShape.headType() == HeadType.EMPTY) {
				if (noteShape.getHeadBox().right().x < dotShape.getBoundingBox().center().x)
					validNoteShapes.add(noteShape);
			}
		}
		if (validNoteShapes.size() == 0)
			return null;
		
		// find the corresponding note
		double minDistance = java.lang.Double.MAX_VALUE;
		NoteShape closestNoteShape = null;
		Point2D.Double closestNotePoint = null;
		boolean isDotRight = false;
		for (NoteShape noteShape : validNoteShapes) {

			// get the current note's bounding box and edge point
			BoundingBox noteBox = noteShape.getBoundingBox();
			Point2D.Double notePoint = null;
			
			//
			if (noteShape.hasStem() && noteShape.stemType() == StemType.DOWNWARD) {
				notePoint = noteBox.right();
				isDotRight = true;
			}
			else if (noteShape.hasStem() && noteShape.stemType() == StemType.UPWARD) {
				notePoint = noteBox.left();
				isDotRight = false;
			}
			else if (!noteShape.hasStem() && noteShape.headType() == HeadType.EMPTY) {
				notePoint = noteBox.right();
				isDotRight = true;
			}
			
			// get the current distance and see if it's the closest note shape
			double currDistance = Math.abs(notePoint.x - dotPoint.x);
			if (currDistance < minDistance) {
				minDistance = currDistance;
				closestNoteShape = noteShape;
				closestNotePoint = notePoint;
			}
		}
		
		// 5. distance test
		if (minDistance > interval)
			return null;
		
		//
		double xOffset = isDotRight ? interval/3 : -interval/3;
		double yOffset = interval*0.25;
		double dotX = closestNotePoint.x + xOffset;
		double dotY = closestNotePoint.y + yOffset;
		List<Point2D.Double> cleanDotPoints = NoteShapeClassifier.getFilledNoteStroke(dotX, dotY, DOT_SIZE);
		IStroke cleanDotStroke = new IStroke(cleanDotPoints, CLEAN_STROKE_DISPLAY_COLOR);
		closestNoteShape.addDot(cleanDotStroke);
		
		// remove and re-add rest shape so undoing removes the newly dotted shape
		shapes.remove(closestNoteShape);
		shapes.add(closestNoteShape);
		
		//
		return shapes;
	}
	
	private List<IShape> hasBeam(List<IShape> rawShapes, List<IShape> originals, StaffShape staffShape) {
		
		// non-multiple notes test
		// do not recognize stems if there are no multiple notes (i.e., greater than 1 note)
		List<IShape> shapes = cloneShapes(originals);
		int noteCount = 0;
		for (IShape shape : shapes) {
			if (shape.getShapeName() == ShapeName.NOTE)
				++noteCount;
		}
		if (noteCount < 2)
			return null;
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		// get all the notes that have stems and do not already have a flag or beam
		List<NoteShape> candidateNotes = new ArrayList<NoteShape>();
		for (IShape shape : shapes) {

			NoteShape candidateNote = null;
			if (shape.getShapeName() == ShapeName.NOTE) {
				
				candidateNote = (NoteShape)shape;
				
				if (candidateNote.hasStem() && !candidateNote.hasBeam() && !candidateNote.hasFlag() && candidateNote.headType() == HeadType.FILLED)
					candidateNotes.add(candidateNote);
			}
		}
		
		// multiple candidate notes test
		if (candidateNotes.size() < 2)
			return null;
		
		//
		IShape scribble = rawShapes.get(0);
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		List<Point2D.Double> scribblePoints = scribbleStroke.getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		
		//
		Point2D.Double beamFirstPoint = scribblePoints.get(0);
		Point2D.Double beamLastPoint = scribblePoints.get(scribblePoints.size() - 1);
		Point2D.Double beamLeftPoint = beamFirstPoint.x < beamLastPoint.x ? beamFirstPoint : beamLastPoint;
		Point2D.Double beamRightPoint = beamFirstPoint.x > beamLastPoint.x ? beamFirstPoint : beamLastPoint;
		NoteShape leftNote = null;
		NoteShape rightNote = null;
		for (NoteShape candidateNote : candidateNotes) {
			
			Point2D.Double stemEndpoint = candidateNote.getStemEndpoint();
				
			if (distance(beamLeftPoint, stemEndpoint) < interval * STEM_BEAM_COINCIDENT_MAX_RATIO)
				leftNote = candidateNote;
			else if (distance(beamRightPoint, stemEndpoint) < interval * STEM_BEAM_COINCIDENT_MAX_RATIO)
				rightNote = candidateNote;
		}
		if (leftNote == null || rightNote == null)
			return null;

		// beautify beam
		double stemHeight = leftNote.getStemBox().height();
		double leftOffset, rightOffset = 0.0;
		Point2D.Double leftStemEndpoint = leftNote.getStemEndpoint();
		Point2D.Double rightStemEndpoint = rightNote.getStemEndpoint();
		List<Point2D.Double> beamPoints = new ArrayList<Point2D.Double>();
		Point2D.Double left, right;
		for (double i = 0; i < FLAG_BEAM_HEIGHT_RATIO; ++i) {
			
			leftOffset = rightOffset  = stemHeight * i/100.0;
			if (leftNote.stemType() == StemType.UPWARD)
				leftOffset *= -1;
			if (rightNote.stemType() == StemType.UPWARD)
				rightOffset *= -1;
			
			left = new Point2D.Double(leftStemEndpoint.x, leftStemEndpoint.y + leftOffset);
			right = new Point2D.Double(rightStemEndpoint.x, rightStemEndpoint.y + rightOffset);
			beamPoints.add(left);
			beamPoints.add(right);
		}
		IStroke beamStroke = new IStroke(beamPoints, scribbleStroke.getColor());
		leftNote.addBeam(beamStroke, scribbleStroke, null, rightNote);
		rightNote.addBeam(beamStroke, scribbleStroke, leftNote, null);
		
		//
		shapes.remove(leftNote);
		shapes.remove(rightNote);
		shapes.add(leftNote);
		shapes.add(rightNote);
		beamStroke.setColor(CLEAN_STROKE_DISPLAY_COLOR);
		return shapes;
	}
	
	private List<IShape> hasFlag(List<IShape> rawShapes, List<IShape> originalShapes, StaffShape staffShape) {
		
		// non-zero notes test
		// do not recognize stems if there are no notes
		List<IShape> shapes = cloneShapes(originalShapes);
		int noteCount = 0;
		for (IShape shape : shapes) {
			if (shape.getShapeName() == ShapeName.NOTE)
				++noteCount;
		}
		if (noteCount == 0)
			return null;
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		IShape scribble = rawShapes.get(0);
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		List<Point2D.Double> scribblePoints = scribbleStroke.getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		double length = pathDistance(scribblePoints);
		
		// length test
		// should be of length between interval and double interval
		if (length < interval || (interval*2) < length)
			return null;
		
		// far left endpoint test
		// 1. check if first or last endpoint is far left
		// 2. check if that endpoint is the farthest left
		Point2D.Double firstFlagEndpoint = scribblePoints.get(0);
		Point2D.Double lastFlagEndpoint = scribblePoints.get(scribblePoints.size() - 1);
		Point2D.Double leftFlagEndpoint = firstFlagEndpoint.x < lastFlagEndpoint.x ? firstFlagEndpoint : lastFlagEndpoint;
		Point2D.Double rightFlagEndpoint = firstFlagEndpoint.x > lastFlagEndpoint.x ? firstFlagEndpoint : lastFlagEndpoint;
		for (Point2D.Double currentScribblePoint : scribblePoints) {
			if (currentScribblePoint.x < leftFlagEndpoint.x)
				return null;
		}
		
		// range test
		// should not go past the width of the interval
		if (rightFlagEndpoint.x - leftFlagEndpoint.x > interval )
			return null;
		
		// coincident test
		NoteShape note = null;
		double coincidentThreshold = interval * STEM_FLAG_COINCIDENT_MAX_RATIO;
		Iterator<IShape> iterator = shapes.iterator();
		boolean foundNote = false;
		while (iterator.hasNext()) { // iterate through each shape
			
			IShape shape = iterator.next();
			
			if (shape.getShapeName() == ShapeName.NOTE) {
				
				note = (NoteShape)shape;
				
				// skip note if it already has a flag or a stem
				if (!note.hasStem() || note.hasFlag() || note.hasBeam() || note.headType() == HeadType.EMPTY)
					continue;
				
				Point2D.Double stemEndpoint = null;
				if (note.stemType() == NoteShape.StemType.DOWNWARD)
					stemEndpoint = note.getStemBox().bottom();
				else
					stemEndpoint = note.getStemBox().top();
				
				if (distance(leftFlagEndpoint, stemEndpoint) > coincidentThreshold)
					continue;
				
				foundNote = true;
				break;
			}
		}
		if (!foundNote)
			return null;
		// inward flag test
		// flag should point inward towards note head
		if (note.stemType() == StemType.DOWNWARD && leftFlagEndpoint.y < rightFlagEndpoint.y)
			return null;
		else if (note.stemType() == StemType.UPWARD && leftFlagEndpoint.y > rightFlagEndpoint.y)
			return null;
		
		// create beautified flag
		BoundingBox stemBox = note.getStemBox();
		Point2D.Double flagLeftPoint = null;
		Point2D.Double flagRightPoint = null;
		if (note.stemType() == StemType.DOWNWARD) {
			flagLeftPoint = new Point2D.Double(stemBox.centerX(), stemBox.maxY());
			flagRightPoint = new Point2D.Double(stemBox.centerX()+interval*0.66, stemBox.maxY()-interval);
		}
		else if (note.stemType() == StemType.UPWARD) {
			flagLeftPoint = new Point2D.Double(stemBox.centerX(), stemBox.minY());
			flagRightPoint = new Point2D.Double(stemBox.centerX()+interval*0.66, stemBox.minY()+interval);
		}
		
		double offset = 0.0;
		List<Point2D.Double> flagPoints = new ArrayList<Point2D.Double>();
//		flagPoints.add(flagLeftPoint);
//		flagPoints.add(flagRightPoint);
		Point2D.Double left, right = null;
		for (int i = 0; i < FLAG_BEAM_HEIGHT_RATIO; ++i) {
			
			offset = note.getStemBox().height() * i/100.0;
			if (note.stemType() == StemType.UPWARD)
				offset *= -1;
			
			left = new Point2D.Double(flagLeftPoint.x, flagLeftPoint.y + offset);
			right = new Point2D.Double(flagRightPoint.x, flagRightPoint.y + offset);
			flagPoints.add(left);
			flagPoints.add(right);
		}
		
		IStroke flagStroke = new IStroke(flagPoints, scribbleStroke.getColor());
		
		//
		note.addFlag(flagStroke, scribbleStroke);
		flagStroke.setColor(CLEAN_STROKE_DISPLAY_COLOR);
		
		//
		shapes.remove(note);
		shapes.add(note);
		return shapes;
	}
	
	private List<IShape> hasStem(List<IShape> rawShapes, List<IShape> originals, StaffShape staffShape) {
		
		// non-zero notes test
		// do not recognize stems if there are no notes
		List<IShape> shapes = cloneShapes(originals);
		int noteCount = 0;
		for (IShape shape : shapes) {
			if (shape.getShapeName() == ShapeName.NOTE)
				++noteCount;
		}
		if (noteCount == 0)
			return null;
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		IShape scribble = rawShapes.get(0);
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		List<Point2D.Double> scribblePoints = scribbleStroke.getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		
		// line test
		if (!isLine(scribblePoints))
			return null;
		
		// minimum stem distance test
		// input's length is at least greater than circumference of an interval circle
		double minLength = interval * STEM_LENGTH_MIN_RATIO;
		double maxLength = interval * STEM_LENGTH_MAX_RATIO;
		double length = pathDistance(scribblePoints);
		if (length < minLength || maxLength < length)
			return null;
		
		//
		Point2D.Double stemStartPoint = scribblePoints.get(0);
		Point2D.Double stemEndPoint = scribblePoints.get(scribblePoints.size()-1);
		Point2D.Double stemMidPoint = new Point2D.Double((stemStartPoint.x+stemEndPoint.x)/2, (stemStartPoint.y+stemEndPoint.y)/2);
		
		// vertical test
		double xDiff = stemEndPoint.x - stemStartPoint.x;
		double yDiff = stemEndPoint.y - stemStartPoint.y;
		double angle = Math.abs(Math.toDegrees(Math.atan2(yDiff, xDiff)));
		if (angle < VERTICAL_ANGLE_MIN_THRESHOLD || VERTICAL_ANGLE_MAX_THRESHOLD < angle)
			return null;
		
		// coincident test
		NoteShape note = null;
		boolean isDownwardStem = false;
		double coincidentThreshold = interval * STEM_HEAD_COINCIDENT_MAX_RATIO;
		Iterator<IShape> iterator = shapes.iterator();
		boolean foundNote = false;
		while (iterator.hasNext()) { // iterate through each shape
			
			// get the current shape
			IShape shape = iterator.next();
			
			// case: shape is a note
			if (shape.getShapeName() == ShapeName.NOTE) {

				// get the note and its left and right bounding points
				NoteShape candidateNote = (NoteShape)shape;
				BoundingBox candidateNoteBox = candidateNote.getBoundingBox();
				Point2D.Double noteLeftPoint = candidateNoteBox.left();
				Point2D.Double noteRightPoint = candidateNoteBox.right();
				
				// get stem's top and bottom point
				Point2D.Double stemTopPoint = null;
				Point2D.Double stemBottomPoint = null;
				if (stemStartPoint.y < stemEndPoint.y) {
					stemTopPoint = stemStartPoint;
					stemBottomPoint = stemEndPoint;
				}
				else {
					stemTopPoint = stemEndPoint;
					stemBottomPoint = stemStartPoint;
				}
				
				// set the corresponding note and stem points
				Point2D.Double notePoint = null;
				Point2D.Double stemPoint = null;
				if (distance(stemMidPoint, noteLeftPoint) < distance(stemMidPoint, noteRightPoint)) {
					notePoint = candidateNoteBox.left();
					stemPoint = stemTopPoint;
					isDownwardStem = true;
				}
				else {
					notePoint = candidateNoteBox.right();
					stemPoint = stemBottomPoint;
					isDownwardStem = false;
				}
				if (distance(notePoint, stemPoint) > coincidentThreshold)
					continue;
				
				// get the note and stop iterating
				note = candidateNote;
				foundNote = true;
				break;
			}
		}
		if (!foundNote)
			return null;
		
		// no stem test
		if (note.hasStem())
			return null;
		
		// create beautified stem
		BoundingBox noteBox = note.getBoundingBox();
		double stemX, stemTopY, stemBottomY;
		double stemLength = interval * STEM_LENGTH_RATIO;
		stemX = isDownwardStem ? noteBox.minX() : noteBox.maxX();
		stemTopY = isDownwardStem ? noteBox.centerY() : noteBox.centerY() - stemLength;
		stemBottomY = isDownwardStem ? noteBox.centerY() + stemLength : noteBox.centerY();
		Point2D.Double stemTopPoint = new Point2D.Double(stemX, stemTopY);
		Point2D.Double stemBottomPoint = new Point2D.Double(stemX, stemBottomY);
		List<Point2D.Double> stemPoints = new ArrayList<Point2D.Double>();
		stemPoints.add(stemTopPoint);
		stemPoints.add(stemBottomPoint);
		IStroke stemStroke = new IStroke(stemPoints, scribbleStroke.getColor());
		
		//
		NoteShape.StemType stemType = isDownwardStem ? NoteShape.StemType.DOWNWARD : NoteShape.StemType.UPWARD;
		note.addStem(stemStroke, scribbleStroke, stemType);
		stemStroke.setColor(CLEAN_STROKE_DISPLAY_COLOR);
		return shapes;
	}

	private List<IShape> hasFilledNoteHead(List<IShape> rawShapes, List<IShape> shapes, StaffShape staffShape) {
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		//
		IShape scribble = rawShapes.get(0);
		List<Point2D.Double> points = scribble.getStrokes().get(0).getPoints();
		double interval = staffShape.getLineInterval();	// diameter
		
		// minimum distance test
		// input's length is at least greater than circumference of an interval circle
		double minLength = 2.0 * Math.PI * interval; 	// 2*circumference = 2*pi*d
		double length = pathDistance(points);
		if (length < minLength)
			return null;
		
		// bounding box test
		double width = scribble.getBoundingBox().width();
		double height = scribble.getBoundingBox().height();
		double minInterval = interval * NOTE_HEAD_BOX_MIN_RATIO;
		double maxInterval = interval * NOTE_HEAD_BOX_MAX_RATIO;
		if (width < minInterval || height < minInterval || maxInterval < width || maxInterval < height)
			return null;
		
		// create filled note head
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		scribbleStroke.setColor(CLEAN_STROKE_DISPLAY_COLOR);
		
		// add the note shape to the list of shapes
		IShape noteShape = createNoteShape(scribbleStroke, shapes, NoteShape.HeadType.FILLED, staffShape);
		shapes.add(noteShape);
		return shapes;
	}
	
	private IShape createNoteShape(IStroke scribbleStroke, List<IShape> shapes, NoteShape.HeadType headType, StaffShape staffShape) {
		
		// 1. Get the points of interest of the scribble stroke.
		double interval = staffShape.getLineInterval();
		Point2D.Double noteTop = scribbleStroke.getBoundingBox().top();
		Point2D.Double noteCenter = scribbleStroke.getBoundingBox().center();
		Point2D.Double noteBottom = scribbleStroke.getBoundingBox().bottom();
		
		// 2. Determine if the scribble's center lies outside the staff lines.
		int position = staffShape.getPosition(noteCenter.y);
		double positionX = 0.0;
		double positionY = 0.0;
		int numLines = 0;
		List<IShape> lines = null;
		ShapeName ledgerType = null;
		List<IStroke> originalLedgerLines = new ArrayList<IStroke>();
		if (StaffShape.TOP_LEDGER_MIN_POSITION < position && position < StaffShape.BOTTOM_LEDGER_MIN_POSITION) {
			positionX = scribbleStroke.getBoundingBox().centerX();
			positionY = staffShape.getPositionY(position);
		}
		else {
			
			// get the ledger line type
			ledgerType = position <= StaffShape.TOP_LEDGER_MIN_POSITION ? ShapeName.UPPER_LINE : ShapeName.LOWER_LINE;
			
			// get all ledger lines that belong to the note head
			lines = new ArrayList<IShape>();
			Iterator<IShape> iterator = shapes.iterator();
			while (iterator.hasNext()) {
				
				// find all shapes of a certain ledger line type
				IShape shape = iterator.next();
				if (shape.getShapeName() == ledgerType) {
				
					Point2D.Double lineLeftPoint = shape.getBoundingBox().left();
					Point2D.Double lineRightPoint = shape.getBoundingBox().right();
					originalLedgerLines.add(shape.getStrokes().get(0));
					
					// add note's ledger lines to list
					if (lineLeftPoint.x < noteCenter.x && noteCenter.x < lineRightPoint.x) {
						lines.add(shape);
						iterator.remove();
					}
				}
			}
			
			// 1. count the number of ledger lines
			numLines = lines.size();
			if (numLines == 0) {
				
				positionX = scribbleStroke.getBoundingBox().centerX();
				if (ledgerType == ShapeName.UPPER_LINE)
					positionY = staffShape.getPositionY(StaffShape.TOP_LEDGER_MIN_POSITION+1);
				else
					positionY = staffShape.getPositionY(StaffShape.BOTTOM_LEDGER_MIN_POSITION-1);
			}
			else {
				
				// 2. find the furtherest ledger line
				IShape farLine = lines.get(0);
				for (IShape line : lines) {
	
					if (ledgerType == ShapeName.UPPER_LINE) {
						if (line.getBoundingBox().center().y < farLine.getBoundingBox().center().y)
							farLine = line;
					}
					else {
						if (line.getBoundingBox().center().y > farLine.getBoundingBox().center().y)
							farLine = line;
					}
				}
				
				// 3. determine if note center is above, below, or at furtherest ledger line
				int offset = 0;
				Point2D.Double farLineCenter = farLine.getBoundingBox().center();
				double topDistance = distance(noteTop, farLineCenter);
				double centerDistance = distance(noteCenter, farLineCenter);
				double bottomDistance = distance(noteBottom, farLineCenter);
				if (topDistance < centerDistance && topDistance < bottomDistance)
					offset = 1;
				else if (bottomDistance < topDistance && bottomDistance < centerDistance)
					offset = -1;
				else
					offset = 0;
				
				// 4. set the note to this y position and keep the current x's position
				if (ledgerType == ShapeName.UPPER_LINE) {
					position = -numLines*2+NUM_LINE_POSITION_OFFSET+offset;
				}
				else {
					position = numLines*2 + (StaffShape.BOTTOM_LEDGER_MIN_POSITION - NUM_LINE_POSITION_OFFSET)+offset;
				}
				positionX = scribbleStroke.getBoundingBox().centerX();
				positionY = staffShape.getPositionY(position);
			}
		}
		
		// get the beautified note stroke
		ShapeName shapeName = ShapeName.NOTE;
		List<Point2D.Double> notePoints = null;
		if (headType == HeadType.FILLED)
			notePoints = getFilledNoteStroke(positionX, positionY, interval);
		else if (headType == HeadType.EMPTY)
			notePoints = getEmptyNoteStroke(positionX, positionY, interval);
		IStroke noteStroke = new IStroke(notePoints, scribbleStroke.getColor());
		NoteShape noteShape = new NoteShape(shapeName, noteStroke, headType, position, scribbleStroke);
		
		// add the ledger lines (if any)
		if (numLines > 0) {
			
			List<IStroke> lineStrokes = new ArrayList<IStroke>();
			double x = noteShape.getBoundingBox().center().x;
			double leftX = x - interval*0.75;
			double rightX = x + interval*0.75;
			int startPosition = ledgerType == ShapeName.UPPER_LINE ? StaffShape.TOP_LEDGER_MIN_POSITION : StaffShape.BOTTOM_LEDGER_MIN_POSITION;
			for (int i = 0; i < numLines; ++i) {

				int positionOffset = 2 * i;
				if (ledgerType == ShapeName.UPPER_LINE)
					positionOffset *= -1;
				double y = staffShape.getPositionY(startPosition + positionOffset);
				
				// set the line's stroke
				Point2D.Double leftPoint = new Point2D.Double(leftX, y);
				Point2D.Double rightPoint = new Point2D.Double(rightX, y);
				List<Point2D.Double> linePoints = new ArrayList<Point2D.Double>();
				linePoints.add(leftPoint);
				linePoints.add(rightPoint);
				IStroke lineStroke = new IStroke(linePoints, CLEAN_STROKE_DISPLAY_COLOR);
				lineStrokes.add(lineStroke);
				
				// add the line strokes to the note
				noteShape.addLedgerLines(lineStrokes, originalLedgerLines);
			}
		}
		
		return noteShape;
	}

	private List<IShape> hasEmptyNoteHead(List<IShape> rawShapes, List<IShape> shapes, StaffShape staffShape) { 
		
		// singleton raw shape test
		// input should only be a single raw shape (stroke), which is the filled note head shape
		if (rawShapes.size() > 1)
			return null;
		
		//
		IShape scribble = rawShapes.get(0);
		List<Point2D.Double> points = scribble.getStrokes().get(0).getPoints();
		double interval = staffShape.getLineInterval();		// diameter
		
		// circumference distance test
		// input's length is roughly the circumference of an interval circle
		double circumference = Math.PI * interval; 	// circumference = 2*pi*r = pi*d
		double lowerLimit = circumference * NOTE_HEAD_BOX_MIN_RATIO;
		double upperLimit = circumference * NOTE_HEAD_BOX_MAX_RATIO;
		double length = pathDistance(points);
		
		if (length < lowerLimit || upperLimit < length)
			return null;
		
		// bounding box test
		double width = scribble.getBoundingBox().width();
		double height = scribble.getBoundingBox().height();
		double minInterval = interval * NOTE_HEAD_BOX_MIN_RATIO;
		double maxInterval = interval * NOTE_HEAD_BOX_MAX_RATIO;
		if (width < minInterval || height < minInterval || maxInterval < width || maxInterval < height)
			return null;

		// closed shape test
		Point2D.Double startPoint = points.get(0);
		Point2D.Double endPoint = points.get(points.size()-1);
		if (distance(startPoint, endPoint) > interval * CLOSED_SHAPE_MIN_RATIO)
			return null;
		
		// create empty note head
		IStroke scribbleStroke = scribble.getStrokes().get(0);
		scribbleStroke.setColor(CLEAN_STROKE_DISPLAY_COLOR);
		
		// add the note shape to the list of shapes
		IShape noteShape = createNoteShape(scribbleStroke, shapes, NoteShape.HeadType.EMPTY, staffShape);
		shapes.add(noteShape);
		return shapes;
	}
	
	public static List<Point2D.Double> getEmptyNoteStroke(double positionX, double positionY, double diameter) {
		
		// reduce the beautified stroke's diameter (visual hack)
		diameter *= VISUALIZED_NOTE_HEAD_RATIO;
		
		double radius = diameter / 2.0;
		
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		double a = positionX;
		double b = positionY;
		double r = radius;
		double pi = Math.PI;
		double x, y;
		Point2D.Double currentPoint;
		for (int i = 0; i <= 32; ++i) {
			
			x = a + r*Math.cos(i*pi/8);
			y = b + r*Math.sin(i*pi/8);
			currentPoint = new Point2D.Double(x, y);
			points.add(currentPoint);
		}
		
		return points;
	}
	
	public static List<Point2D.Double> getFilledNoteStroke(double positionX, double positionY, double diameter) {
		
		// reduce the beautified stroke's diameter (visual hack)
		diameter *= VISUALIZED_NOTE_HEAD_RATIO;
		
		//
		List<List<Point2D.Double>> pointsList = new ArrayList<List<Point2D.Double>>();
		for (double i = 0.0; i < 1.0; i+=0.1)
			pointsList.add(getEmptyNoteStroke(positionX, positionY, diameter*i));
		
		//
		List<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
		for (List<Point2D.Double> points : pointsList)
			newPoints.addAll(points);
		
		return newPoints;
	}
	
	
	
	public static final double MIN_SCORE_THRESHOLD = 0.75;
	public static final double NOTE_HEAD_BOX_MIN_RATIO = 0.4;
	public static final double NOTE_HEAD_BOX_MAX_RATIO = 1.4;
	public static final double STEM_LENGTH_MIN_RATIO = 2;
	public static final double STEM_LENGTH_MAX_RATIO = 4;
	public static final double ACCIDENTAL_LENGTH_MIN_RATIO = 2.5;
	public static final double VERTICAL_ANGLE_MIN_THRESHOLD = 80.0;
	public static final double VERTICAL_ANGLE_MAX_THRESHOLD = 100.0;
	public static final double CLOSED_SHAPE_MIN_RATIO = 0.3;
	public static final double VISUALIZED_NOTE_HEAD_RATIO = 0.8;
	public static final double STEM_HEAD_COINCIDENT_MAX_RATIO = 0.4;
	public static final double STEM_FLAG_COINCIDENT_MAX_RATIO = 0.4;
	public static final double STEM_BEAM_COINCIDENT_MAX_RATIO = 0.4;
	public static final double STEM_LENGTH_RATIO = 3;
	public static final int NUM_LINE_POSITION_OFFSET = 2;
	public static final int MAX_DOT_NUM_POINTS = 3;
	public static final double MAX_DOT_LENGTH = 5.0;
	public static final int FLAG_BEAM_HEIGHT_RATIO = 10;
	public static final int DOT_SIZE = 5;
	public static final String DATA_DIR_NAME = "accidental";
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/accidental/";
}