package edu.tamu.srl.music.classifier;

import java.util.List;

public class StaffShape extends IShape {

	public StaffShape(List<IStroke> strokes, ShapeType shapeType, double topY, double lineInterval) {
		
		super(shapeType, strokes);

		myLineInterval = lineInterval;
		myLineYs = new double[NUM_LINES];
		for (int i = 0; i < myLineYs.length; ++i)
			myLineYs[i] = topY + myLineInterval*i;
	}
	
	public int getLineY(int lineNumber) {
		
		return (int)myLineYs[lineNumber];
	}
	
	public int getLineInterval() {
		
		return (int)myLineInterval;
	}
	
	
	
	private double myLineInterval;
	private double[] myLineYs;
	
	public static final int NUM_LINES = 5;
}