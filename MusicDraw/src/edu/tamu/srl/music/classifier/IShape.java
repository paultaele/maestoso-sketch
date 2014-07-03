package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IShape {
	
	public IShape(ShapeType shapeType, IStroke stroke) {
		
		this(shapeType, stroke, null);
	}
	
	public IShape(ShapeType shapeType, List<IStroke> strokes) {
		
		this(shapeType, strokes, null);
	}
	
	public IShape(ShapeType shapeType, IStroke stroke, File imageFile) {
		
		myShapeType = shapeType;
		myStrokes = new ArrayList<IStroke>();
		myStrokes.add(stroke);
		myImageFile = imageFile;
		myImageX = 0;
		myImageY = 0;
		
		if (imageFile != null)
			myHasImage = true;
		else
			myHasImage = false;
		
		myBoundingBox = new BoundingBox(stroke.getPoints());
	}
	
	public IShape(ShapeType shapeType, List<IStroke> strokes, File imageFile) {
		
		myShapeType = shapeType;
		myStrokes = strokes;
		myImageFile = imageFile;
		myImageX = 0;
		myImageY = 0;
		
		if (imageFile != null)
			myHasImage = true;
		else
			myHasImage = false;
		
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for (IStroke stroke : strokes)
			for (Point2D.Double point : stroke.getPoints())
				points.add(point);
		myBoundingBox = new BoundingBox(points);
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
	
	public enum ShapeType {
		
		RAW,
		STAFF_LINE, STAFF,
		TREBLE_CLEF, BASS_CLEF,
		ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
		SHARP, FLAT
	}

	
	
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