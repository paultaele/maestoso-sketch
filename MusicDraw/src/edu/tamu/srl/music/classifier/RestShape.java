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
	
	public double duration() {
		
		double duration = 0.0;
		
		if (getShapeName() == ShapeName.WHOLE_REST)
			duration = 4.0;
		else if (getShapeName() == ShapeName.HALF_REST)
			duration = 2.0;
		else if (getShapeName() == ShapeName.QUARTER_REST)
			duration = 1.0;
		else if (getShapeName() == ShapeName.EIGHTH_REST)
			duration = 0.5;
		
		if (myHasDot)
			duration += duration/2;
		
		return duration;
	}

	private boolean myHasDot;
}
