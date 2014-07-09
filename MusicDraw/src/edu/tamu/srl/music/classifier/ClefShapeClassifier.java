package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.SketchPanel;

public class ClefShapeClassifier extends AbstractShapeClassifier implements IShapeClassifier {

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
		
		//
		Hausdorff classifier = new Hausdorff();
		List<List<Point2D.Double>> strokes = getStrokes(rawShapes);
//		List<Template> templates = getTemplates(DATA_DIR_PATHNAME);
		List<Template> templates = Template.getTemplates(DATA_DIR_NAME);
		Pair result = classifier.classify(strokes, templates);
		
		// TODO
		if (SketchPanel.DISPLAY_SHAPE_SCORES) {
			System.out.println("### TESTING FOR CLEF ###"); // TODO
			System.out.println("SCORE: " + result.score());
		}
		
		//
		if (result.score() > MIN_SCORE_THRESHOLD) {

			// get new shape type
			ShapeName newShapeName = null;
			BufferedImage newImage = null;
			if (result.shape().equals("TrebleClef")) {
				
				newShapeName = ShapeName.TREBLE_CLEF;
				newImage = IShape.getImage(newShapeName.name());
			}
			else if (result.shape().equals("BassClef")) {
				
				if (strokes.size() != 3)
					return false;
				
				newShapeName = ShapeName.BASS_CLEF;
				newImage = IShape.getImage(newShapeName.name());
			}
			
			// get new strokes
			List<IStroke> newStrokes = new ArrayList<IStroke>();
			for (IShape rawShape : rawShapes) {
				
				IStroke rawStroke = rawShape.getStrokes().get(0);
				rawStroke.setColor(new Color(128, 0, 128)); // TEMP
				newStrokes.add(rawStroke);
			}
			
			// create shape and set its image
			IShape newShape = new IShape(newShapeName, newStrokes);
			newShape.setImageFile(newImage);
			IShape staffShape = null;
			for (IShape shape : shapes) {
				if (shape.getShapeName() == IShape.ShapeName.WHOLE_STAFF) {
					staffShape = shape;
					break;
				}
			}
			setLocation(newShape, (StaffShape)staffShape);
			
			// add clef to list of shapes
			shapes.add(newShape);
			myShapes = shapes;
			
			//
			return true;
		}
		
		return false;
	}
	
	public List<IShape> getResult() {
		
		return myShapes;
	}
	
	private void setLocation(IShape shape, StaffShape staff) {
		
		int staffTopY = (int)staff.getLineY(0);
		int staffBottomY = (int)staff.getLineY(staff.NUM_LINES - 1);
		int staffHeight = staffBottomY - staffTopY;
		int lineInterval = (int) staff.getLineInterval();
		
		BufferedImage bufferedImage = shape.getImageFile();
		int imageWidth = bufferedImage.getWidth();
		int imageHeight = bufferedImage.getHeight();
		
		if (shape.getShapeName() == ShapeName.TREBLE_CLEF) {
			
			shape.setImageX(IMAGE_X_POS);
			shape.setImageY(staffTopY - lineInterval);
			
			int newImageHeight = staffHeight + (int)(lineInterval*1.5);
			int newImageWidth = (newImageHeight*imageWidth)/imageHeight;
			
			shape.setImageWidth(newImageWidth);
			shape.setImageHeight(newImageHeight);
		}
		else if (shape.getShapeName() == ShapeName.BASS_CLEF) {
			
			shape.setImageX(IMAGE_X_POS);
			shape.setImageY(staffTopY);
			
			int newImageHeight = staffHeight - (int)(lineInterval*0.5);
			int newImageWidth = (newImageHeight*imageWidth)/imageHeight;
			
			shape.setImageWidth(newImageWidth);
			shape.setImageHeight(newImageHeight);
		}
	}

	
	
	public static final String DATA_DIR_NAME = "clef";
	public static final String DATA_DIR_PATHNAME = "src/edu/tamu/srl/music/data/clef/";
	public static final double MIN_SCORE_THRESHOLD = 0.70;
	public static final int IMAGE_X_POS = 10;
}