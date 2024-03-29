package edu.tamu.srl.music.classifier;

import java.util.List;

public class StaffShape extends IShape {

	public StaffShape(List<IStroke> strokes, ShapeName shapeName, double topY, double lineInterval) {
		
		// set the shape name and strokes
		super(shapeName, strokes);

		//
		myLineInterval = lineInterval;
		myLineYs = new double[NUM_LINES];
		for (int i = 0; i < myLineYs.length; ++i)
			myLineYs[i] = topY + myLineInterval*i;
		
		//
		myPositionZero = myLineYs[0] - myLineInterval;
	}
	
	public int getLineY(int lineNumber) {
		
		return (int)myLineYs[lineNumber];
	}
	
	public int getLineInterval() {
		
		return (int)myLineInterval;
	}
	
	public int getPosition(double targetY) {
		
		//
		double halfInterval = myLineInterval * 0.5;
		double currentY = myPositionZero;
		int currentPos = 0;
		int finalPos = 0;
		
		//
		if (targetY >= currentY) {
			
			while (currentY < targetY) {
				
				currentY += halfInterval;
				++currentPos;
			}
			
			if (currentY - targetY < targetY - (currentY - halfInterval))
				finalPos = currentPos;
			else
				finalPos = currentPos - 1;
		}
		
		else if (targetY < currentY) {
			
			while (currentY > targetY) {
				
				currentY -= halfInterval;
				--currentPos;
			}
			
			if (targetY - currentY < (currentY + halfInterval) - targetY)
				finalPos = currentPos;
			else
				finalPos = currentPos + 1;
		}
		
		return finalPos;
	}

	public double getPositionY(int position) {
		
		double halfInterval = myLineInterval * 0.5;
		double staffPositionY = myPositionZero + halfInterval*position;
		
		return staffPositionY;
	}
	
	private double myLineInterval;
	private double[] myLineYs;
	private double myPositionZero;
	
	public static final int NUM_LINES = 5;
	public static final int NUM_POSITIONS = 10;
	public static final int TOP_LEDGER_MIN_POSITION = 0;
	public static final int BOTTOM_LEDGER_MIN_POSITION = 12;
}