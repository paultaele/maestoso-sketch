package edu.tamu.srl.music.classifier;

import java.util.List;

public class KeyShape extends IShape {

	public KeyShape(ShapeName shapeName, List<IStroke> strokes, int staffPosition) {
		super(shapeName, strokes);
		
		myStaffPosition = staffPosition;
	}

	public int getStaffPosition() {
		
		return myStaffPosition;
	}
	
	private int myStaffPosition;
}