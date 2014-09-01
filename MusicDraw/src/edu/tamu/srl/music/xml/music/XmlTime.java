package edu.tamu.srl.music.xml.music;

public class XmlTime {

	public XmlTime(Type type, int value) {
		
		myType = type;
		myValue = value;
	}
	
	public Type getType() {
		
		return myType;
	}
	
	public int getValue() {
		
		return myValue;
	}
	
	public enum Type { TOP, BOTTOM}
	
	private Type myType;
	private int myValue;
}
