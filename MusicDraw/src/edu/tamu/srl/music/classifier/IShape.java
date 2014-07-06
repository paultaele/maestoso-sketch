package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IShape {
	
	public IShape(ShapeName shapeName, IStroke stroke) {
		
		myShapeName = shapeName;
		myShapeType = getShapeType(shapeName);
		myStrokes = new ArrayList<IStroke>();
		myStrokes.add(stroke);
		myImageFile = null;
		myImageX = 0;
		myImageY = 0;
		myHasImage = false;
		
		myBoundingBox = new BoundingBox(stroke.getPoints());
	}
	
	public IShape(ShapeName shapeName, List<IStroke> strokes) {
		
		myShapeName = shapeName;
		myShapeType = getShapeType(shapeName);
		myStrokes = strokes;
		myImageFile = null;
		myImageX = 0;
		myImageY = 0;
		myHasImage = false;
		
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for (IStroke stroke : strokes)
			for (Point2D.Double point : stroke.getPoints())
				points.add(point);
		myBoundingBox = new BoundingBox(points);
	}
	
	private ShapeType getShapeType(ShapeName shapeName) {
		
		if (shapeName == ShapeName.STAFF_LINE
				|| shapeName == ShapeName.WHOLE_STAFF)
			return ShapeType.STAFF;
		
		else if (shapeName == ShapeName.TREBLE_CLEF
				|| shapeName == ShapeName.BASS_CLEF)
			return ShapeType.CLEF;
		
		else if (shapeName == ShapeName.TWO
				|| shapeName == ShapeName.THREE
				|| shapeName == ShapeName.FOUR
				|| shapeName == ShapeName.FIVE
				|| shapeName == ShapeName.SIX
				|| shapeName == ShapeName.SEVEN
				|| shapeName == ShapeName.EIGHT
				|| shapeName == ShapeName.NINE)
			return ShapeType.BEAT;
		
		else if (shapeName == ShapeName.SHARP
				|| shapeName == ShapeName.FLAT)
			return ShapeType.ACCIDENTAL;
		
		else
			return ShapeType.NONE;
	}
	
	public ShapeName getShapeName() {
		
		return myShapeName;
	}
	
	public ShapeType getShapeType() {
		
		return myShapeType;
	}
	
	public List<IStroke> getStrokes() {
		
		return myStrokes;
	}
	
	public File getImageFile() {
		
		return myImageFile;
	}
	
	public void setImageFile(File imageFile) {
		
		myHasImage = true;
		myImageFile = imageFile;
	}
	
	public boolean hasImage() {
		
		return myHasImage;
	}
	
	public boolean hasTransformed() {
		
		return myHasTransformed;
	}
	
	public int getImageWidth() {
		
		return myImageWidth;
	}
	
	public int getImageHeight() {
		
		return myImageHeight;
	}
	
	public int getImageX() {
		
		return myImageX;
	}
	
	public int getImageY() {
		
		return myImageY;
	}
	
	public void setImageWidth(int imageWidth) {
		
		myImageWidth = imageWidth;
	}
	
	public void setImageHeight(int imageHeight) {
		
		myImageHeight = imageHeight;
	}
	
	public void setImageX(int imageX) {
		
		myImageX = imageX;
	}
	
	public void setImageY(int imageY) {
		
		myImageY = imageY;
	}
	
	public BoundingBox getBoundingBox() {
		
		return myBoundingBox;
	}
	
	public enum ShapeName {
		
		RAW,
		STAFF_LINE, WHOLE_STAFF,
		TREBLE_CLEF, BASS_CLEF,
		TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
		SHARP, FLAT
	}
	
	public enum ShapeType {
		
		NONE,
		STAFF,
		CLEF,
		BEAT,
		ACCIDENTAL
	}

	
	
	private ShapeName myShapeName;
	private ShapeType myShapeType;
	private List<IStroke> myStrokes;
	private File myImageFile;
	private boolean myHasImage;
	private boolean myHasTransformed;
	private int myImageWidth;
	private int myImageHeight;
	private int myImageX;
	private int myImageY;
	private BoundingBox myBoundingBox;
	
	public static final String IMAGE_DIR_PATHNAME = "src/edu/tamu/srl/music/images/";
}