package edu.tamu.srl.music.xml.music;

public class XmlComponent {

	public XmlComponent(Type type) {
		
		this(type, 0, Accidental.NONE, 0, Bar.NONE);
	}
	
	public XmlComponent(Type type, int position, Accidental accidental, double duration, Bar bar) {
		
		myType = type;
		myPosition = position;
		myAccidental = accidental;
		myDuration = duration;
		myBar = bar;
	}
	
	public void setPosition(int position) { myPosition = position; }
	public void setAccidental(Accidental accidental) { myAccidental = accidental; }
	public void setDuration(double duration) { myDuration = duration; }
	public void setBar(Bar bar) { myBar = bar; }
	
	public Type getType() {
		
		return myType;
	}
	
	public int getPosition() {
		
		if (myType == Type.NOTE)
			return myPosition;
		
		return 0;
	}
	
	public Accidental getAccidental() {
		
		if (myType == Type.NOTE)
			return myAccidental;
		
		return Accidental.NONE;
	}
	
	public double getDuration() {
		
		if (myType == Type.NOTE || myType == Type.REST)
			return myDuration;
		
		return 0;
	}
	
	public Bar getBar() {
		
		if (myType == Type.BAR)
			return myBar;
		
		return Bar.NONE;
	}
	
	public enum Type { NOTE, REST, BAR }

	public enum Accidental { SHARP, FLAT, NATURAL, NONE }
	
	public enum Bar { SINGLE, DOUBLE, NONE }
	
	
	
	private Type myType;
	private int myPosition;					// note
	private Accidental myAccidental;		// note
	private double myDuration;				// note and rest
	private Bar myBar;						// bar
}