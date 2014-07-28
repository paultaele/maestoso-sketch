package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.util.List;

public class NoteShape extends IShape {

	public NoteShape(ShapeName shapeName, IStroke head, HeadType headType, int position) {
		
		super(shapeName, head);
		
		myHeadBox = head.getBoundingBox();
		
		myPosition = position;
		
		myHeadType = headType;
		myStemType = StemType.NONE;
		myAccidentalType = AccidentalType.NONE;
		myHasFlag = false;
		myHasDot = false;
	}
	
	public void addStem(IStroke stemStroke, StemType stemType) {
	
		myStemBox = stemStroke.getBoundingBox();
		
		myStrokes.add(stemStroke);
		myStemType = stemType;
	}
	
	public void addFlag(IStroke flagStroke) {
		
		myStrokes.add(flagStroke);
		myHasFlag = true;
	}
	
	public void addBeam(IStroke stroke, NoteShape leftNote, NoteShape rightNote) {
		
		if (myLeftNote != null)
			myLeftNote = leftNote;
		if (myRightNote != null)
			myRightNote = rightNote;
		if (leftNote != null && rightNote == null)
			myStrokes.add(stroke);
	}
	
	public void addDot(IStroke stroke) {
		
		myStrokes.add(stroke);
		myHasDot = true;
	}
	
	public void addLedgerLines(List<IStroke> lines) {
		
		for (IStroke line : lines)
			myStrokes.add(line);
	}
	
	public Point2D.Double getStemEndpoint() {
		
		if (!hasStem())
			return null;
		
		if (myStemEndpoint == null)
			myStemEndpoint = myStemType == StemType.DOWNWARD ? myStemBox.bottom() : myStemBox.top();
			
		return myStemEndpoint;
	}
	
	public BoundingBox getHeadBox() { return myHeadBox; }
	public BoundingBox getStemBox() { return myStemBox; }
	
	public int position() { return myPosition; }
	public HeadType headType() { return myHeadType; }
	public StemType stemType() { return myStemType; }
	public AccidentalType accidentalType() { return myAccidentalType; }
	public DotType dotType() { return myDotType; }
	public NoteShape leftNote() { return myLeftNote; }
	public NoteShape rightNote() { return myRightNote; }
	
	public boolean hasStem() { return myStemType != StemType.NONE; }
	public boolean hasBeam() { return (myLeftNote != null) || (myRightNote != null); }
	public boolean hasAccidental() { return myAccidentalType != AccidentalType.NONE; }
	public boolean hasDot() { return myHasDot; }
	public boolean hasFlag() { return myHasFlag; }
	
	public enum HeadType { FILLED, EMPTY }
	public enum StemType { UPWARD, DOWNWARD, NONE }
	public enum AccidentalType { FLAT, SHARP, NATURAL, NONE }
	private enum DotType { DOT, NONE }

	

	private BoundingBox myHeadBox;
	private BoundingBox myStemBox;
	
	private boolean myHasFlag;
	private boolean myHasDot;
	
	private int myPosition;
	private HeadType myHeadType;
	private StemType myStemType;
	private AccidentalType myAccidentalType;
	private DotType myDotType;
	private NoteShape myLeftNote;
	private NoteShape myRightNote;
	private Point2D.Double myStemEndpoint;
}