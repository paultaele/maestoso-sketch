package edu.tamu.srl.music.classifier;

public class NoteShape extends IShape {

	public NoteShape(ShapeName shapeName, IStroke stroke, HeadType headType, int position) {
		
		super(shapeName, stroke);
		
		myPosition = position;
		
		myHeadType = headType;
		myStemType = StemType.NONE;
		myFlagType = FlagType.NONE;
		myAccidentalType = AccidentalType.NONE;
		myDotType = DotType.NONE;
	}
	
	
	
	public int position() { return myPosition; }
	public HeadType headType() { return myHeadType; }
	public StemType stemType() { return myStemType; }
	public FlagType flagType() { return myFlagType; }
	public AccidentalType accidentalType() { return myAccidentalType; }
	public DotType dotType() { return myDotType; }
	
	public enum HeadType { FILLED, EMPTY }
	public enum StemType { UPWARD, DOWNWARD, NONE }
	public enum FlagType { CURVED, STRAIGHT, NONE }
	public enum AccidentalType { FLAT, SHARP, NATURAL, NONE }
	private enum DotType { DOT, NONE }
	
	private int myPosition;
	private HeadType myHeadType;
	private StemType myStemType;
	private FlagType myFlagType;
	private AccidentalType myAccidentalType;
	private DotType myDotType;
}