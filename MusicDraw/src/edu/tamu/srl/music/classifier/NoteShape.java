package edu.tamu.srl.music.classifier;

public class NoteShape extends IShape {

	public NoteShape(ShapeName shapeName, IStroke head, HeadType headType, int position) {
		
		super(shapeName, head);
		
		myHeadBox = head.getBoundingBox();
		
		myPosition = position;
		
		myHeadType = headType;
		myStemType = StemType.NONE;
		myFlagType = FlagType.NONE;
		myAccidentalType = AccidentalType.NONE;
		myDotType = DotType.NONE;
	}
	
	public void addStem(IStroke stem, StemType stemType) {
	
		// set the stem's bounding box
		myStemBox = stem.getBoundingBox();
		
		// add the 
		myStrokes.add(stem);
		myStemType = stemType;
	}
	
	public BoundingBox getHeadBox() { return myHeadBox; }
	public BoundingBox getStemBox() { return myStemBox; }
	
	public int position() { return myPosition; }
	public HeadType headType() { return myHeadType; }
	public StemType stemType() { return myStemType; }
	public FlagType flagType() { return myFlagType; }
	public AccidentalType accidentalType() { return myAccidentalType; }
	public DotType dotType() { return myDotType; }
	
	public boolean hasStem() { return myStemType != StemType.NONE; }
	public boolean hasFlag() { return myFlagType != FlagType.NONE; }
	public boolean hasAccidental() { return myAccidentalType != AccidentalType.NONE; }
	public boolean hasDot() { return myDotType != DotType.NONE; }
	
	public enum HeadType { FILLED, EMPTY }
	public enum StemType { UPWARD, DOWNWARD, NONE }
	public enum FlagType { CURVED, STRAIGHT, NONE }
	public enum AccidentalType { FLAT, SHARP, NATURAL, NONE }
	private enum DotType { DOT, NONE }

	
	
	private BoundingBox myHeadBox;
	private BoundingBox myStemBox;
	
	private int myPosition;
	private HeadType myHeadType;
	private StemType myStemType;
	private FlagType myFlagType;
	private AccidentalType myAccidentalType;
	private DotType myDotType;
}