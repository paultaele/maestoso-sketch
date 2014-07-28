package edu.tamu.srl.music.classifier;

import java.util.List;

public class RestShape extends IShape {
	
	public RestShape(ShapeName shapeName, List<IStroke> strokes) {
		
		super(shapeName, strokes);
		
		myHasDot = false;
	}
	
	public void addDot(IStroke stroke) {
		
		myStrokes.add(stroke);
		myHasDot = true;
	}
	
	public boolean hasDot() {
		
		return myHasDot;
	}

	private boolean myHasDot;
}
