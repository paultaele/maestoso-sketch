package edu.tamu.srl.music.classifier;

import java.util.List;

public class StaffShape extends IShape {

	public StaffShape(List<IStroke> strokes, ShapeName shapeName, double topY, double lineInterval) {
		
		// set the shape name and strokes
		super(shapeName, strokes);

		myLineInterval = lineInterval;
		myLineYs = new double[NUM_LINES];
		for (int i = 0; i < myLineYs.length; ++i)
			myLineYs[i] = topY + myLineInterval*i;
		
		myPositionZero = myLineYs[0] - (myLineInterval / 2.0);
	}
	
	public int getLineY(int lineNumber) {
		
		return (int)myLineYs[lineNumber];
	}
	
	public int getLineInterval() {
		
		return (int)myLineInterval;
	}
	
	public int getStaffPosition(double targetY) {
		
		//
		double halfInterval = myLineInterval * 0.5;
		double currentY = myPositionZero;
		int currentPos = 0;
		
		//
		if (targetY >= currentY) {
			
			while (currentY < targetY) {
				
				currentY += halfInterval;
				++currentPos;
			}
			
			if (currentY - targetY < targetY - (currentY - halfInterval))
				return currentPos;
			else
				return currentPos - 1;
		}
		
		else if (targetY < currentY) {
			
			while (currentY > targetY) {
				
				currentY -= halfInterval;
				--currentPos;
			}
			
			if (targetY - currentY < (currentY + halfInterval) - targetY)
				return currentPos;
			else
				return currentPos + 1;
		}
		
		return -1;
	}

	public double getStaffPositionY(int position) {
		
		double halfInterval = myLineInterval * 0.5;
		
		return myPositionZero + halfInterval*position;
	}
	
	private double myLineInterval;
	private double[] myLineYs;
	private double myPositionZero;
	
	public static final int NUM_LINES = 5;
	public static final int NUM_POSITIONS = 10;
}