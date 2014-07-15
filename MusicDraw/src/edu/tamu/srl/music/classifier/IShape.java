package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

public class IShape {
	
	public IShape(ShapeName shapeName, IStroke stroke) {
		
		myShapeName = shapeName;
		myShapeGroup = myShapeGroup(shapeName);
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
		myShapeGroup = myShapeGroup(shapeName);
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
	
	private ShapeGroup myShapeGroup(ShapeName shapeName) {
		
		if (shapeName == ShapeName.STAFF_LINE
				|| shapeName == ShapeName.WHOLE_STAFF)
			return ShapeGroup.STAFF;
		
		else if (shapeName == ShapeName.TREBLE_CLEF
				|| shapeName == ShapeName.BASS_CLEF)
			return ShapeGroup.CLEF;
		
		else if (shapeName == ShapeName.TWO
				|| shapeName == ShapeName.THREE
				|| shapeName == ShapeName.FOUR
				|| shapeName == ShapeName.FIVE
				|| shapeName == ShapeName.SIX
				|| shapeName == ShapeName.SEVEN
				|| shapeName == ShapeName.EIGHT
				|| shapeName == ShapeName.NINE)
			return ShapeGroup.BEAT;
		
		else if (shapeName == ShapeName.KEY_SHARP
				|| shapeName == ShapeName.KEY_FLAT)
			return ShapeGroup.KEY;
		
		else if (shapeName == ShapeName.SHARP
				|| shapeName == ShapeName.FLAT)
			return ShapeGroup.ACCIDENTAL;
		
		else if (shapeName == ShapeName.SINGLE_BAR
				|| shapeName == ShapeName.DOUBLE_BAR)
			return ShapeGroup.BAR;
		
		else if (shapeName == ShapeName.LOWER_BRACKET
				|| shapeName == shapeName.UPPER_BRACKET
				|| shapeName == shapeName.WHOLE_REST
				|| shapeName == shapeName.HALF_REST
				|| shapeName == shapeName.QUARTER_REST
				|| shapeName == shapeName.EIGHTH_REST)
			return ShapeGroup.REST;
			
		
		else
			return ShapeGroup.NONE;
	}
	
	public ShapeName getShapeName() {
		
		return myShapeName;
	}
	
	public ShapeGroup getShapeGroup() {
		
		return myShapeGroup;
	}
	
	public List<IStroke> getStrokes() {
		
		return myStrokes;
	}
	
	public BufferedImage getImageFile() {
		
		return myImageFile;
	}
	
	public void setImageFile(BufferedImage imageFile) {
		
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
	
	public void setColor(Color color) {
		
		for (IStroke stroke : myStrokes) {
			
			stroke.setColor(color);
		}
	}
	
	public BoundingBox getBoundingBox() {
		
		return myBoundingBox;
	}
	
	public enum ShapeName {
		
		RAW,
		STAFF_LINE, WHOLE_STAFF,
		KEY_SHARP, KEY_FLAT,
		TREBLE_CLEF, BASS_CLEF,
		TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
		SHARP, FLAT, NATURAL,
		SINGLE_BAR, DOUBLE_BAR,
		LOWER_BRACKET, UPPER_BRACKET, WHOLE_REST, HALF_REST, QUARTER_REST, EIGHTH_REST
	}
	
	public enum ShapeGroup {
		
		NONE,
		STAFF,
		KEY,
		CLEF,
		BEAT,
		ACCIDENTAL,
		BAR,
		REST
	}
	
	public static void loadImages() {
		
		HashMap<String, BufferedImage> map = new HashMap<String, BufferedImage>();
		File directory = new File(IMAGE_DIR_PATHNAME);
		for (File file : directory.listFiles()) {
			
			if (file.isFile() && file.getName().endsWith(".png")) {
				
				String name = file.getName();
				name = name.substring(0, name.length() - 4);
				BufferedImage bufferedImage = null;
				try {
					bufferedImage = ImageIO.read(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				map.put(name, bufferedImage);
			}
		}
		
		myImagesMap = map;
	}
	
	public static BufferedImage getImage(String shapeName) {
		
		BufferedImage bufferedImage = myImagesMap.get(shapeName);
		
		return bufferedImage;
	}

	
	
	private ShapeName myShapeName;
	private ShapeGroup myShapeGroup;
	private List<IStroke> myStrokes;
	private BufferedImage myImageFile;
	private boolean myHasImage;
	private boolean myHasTransformed;
	private int myImageWidth;
	private int myImageHeight;
	private int myImageX;
	private int myImageY;
	private BoundingBox myBoundingBox;
	
	private static HashMap<String, BufferedImage> myImagesMap;
	public static final String IMAGE_DIR_PATHNAME = "src/edu/tamu/srl/music/images/";
}