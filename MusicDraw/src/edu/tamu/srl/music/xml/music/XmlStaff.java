package edu.tamu.srl.music.xml.music;

import java.util.List;

public class XmlStaff {
	
	public XmlStaff(XmlClef clef, List<XmlKey> keySignature, List<XmlTime> timeSignature, List<XmlComponent> components) {
		
		myClef = clef;
		myKeySignature = keySignature;
		myTimeSignature = timeSignature;
		myComponents = components;
	}
	
	public XmlClef getClef() { return myClef; }
	public List<XmlKey> getKeySignature() { return myKeySignature; }
	public List<XmlTime> getTimeSignature() { return myTimeSignature; }
	public List<XmlComponent> getComponents() { return myComponents; }
	
	
	
	private XmlClef myClef;
	private List<XmlKey> myKeySignature;
	private List<XmlTime> myTimeSignature;
	private List<XmlComponent> myComponents;
}
