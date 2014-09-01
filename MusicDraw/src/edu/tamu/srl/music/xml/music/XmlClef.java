package edu.tamu.srl.music.xml.music;

public class XmlClef {
	
	public XmlClef(Type type) {
		
		myType = type;
	}
	
	public Type getType() {
		
		return myType;
	}
	
	public enum Type { TREBLE, BASS }
	
	private Type myType;
}
