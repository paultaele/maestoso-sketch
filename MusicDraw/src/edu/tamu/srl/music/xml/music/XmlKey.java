package edu.tamu.srl.music.xml.music;

public class XmlKey {

	public XmlKey(Type type, int position) {
		
		myType = type;
		myPosition = position;
	}
	
	public Type getType() {
		
		return myType;
	}
	
	public int getPosition() {
		
		return myPosition;
	}
	
	public enum Type { FLAT, SHARP }
	
	private Type myType;
	private int myPosition;
}
