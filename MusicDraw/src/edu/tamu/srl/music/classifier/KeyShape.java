package edu.tamu.srl.music.classifier;

import java.util.List;

public class KeyShape extends IShape {

	public KeyShape(ShapeName shapeName, List<IStroke> strokes, int position) {
		super(shapeName, strokes);
		
		myPosition = position;
	}

	public int getPosition() {
		
		return myPosition;
	}
	
	private int myPosition;
}
