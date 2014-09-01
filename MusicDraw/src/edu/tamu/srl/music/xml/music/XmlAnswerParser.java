package edu.tamu.srl.music.xml.music;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlAnswerParser extends DefaultHandler {

	public XmlAnswerParser(File file) {
		
		this(file.getAbsolutePath());
	}
	
	public XmlAnswerParser(String filePath) {
		
		myXmlFilePath = filePath;
		
		parseDocument();
	}
	
	public void parseDocument() {
		
		// parse
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
        	
        	SAXParser parser = factory.newSAXParser();
        	parser.parse(myXmlFilePath, this);
        	
        } catch (ParserConfigurationException e) {
        	System.out.println("ParserConfig error");
        } catch (SAXException e) {
        	System.out.println("SAXException : xml not well formed");
        } catch (IOException e) {
        	System.out.println("IO error");
        }
	}
	
	public void startElement(String s, String s1, String element, Attributes attributes) throws SAXException {
		
		if (element.equalsIgnoreCase("answer")) {
			myIsAnswer = true;
			myAnswer = null;
			myStaffList = new ArrayList<XmlStaff>();
			
			if (OUTPUT_TO_CONSOLE) System.out.println("<answer>");
		}
		else if (element.equalsIgnoreCase("staff")) {
			myIsStaff = true;
			myStaff = null;
			myKeySignature = new ArrayList<XmlKey>();
			myTimeSignature = new ArrayList<XmlTime>();
			myComponents = new ArrayList<XmlComponent>();
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t<staff>");
		}
		else if (element.equalsIgnoreCase("clef")) {
			myIsClef = true;
			myClef = null;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t<clef>");
		}
		else if (element.equalsIgnoreCase("keySignature")) {
			myIsKeySignature = true;

			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t<keySignature>");
		}
		else if (element.equalsIgnoreCase("key")) {
			myIsKey = true;
			myKey = null;
			myKeyType = null;
			myPosition = 0;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t\t<key>");
		}
		else if (element.equalsIgnoreCase("timeSignature")) {
			myIsTimeSignature = true;

			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t<timeSignature>");
		}
		else if (element.equalsIgnoreCase("time")) {
			myIsTime = true;
			myTime = null;
			myTimeType = null;
			myValue = 0;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t\t<time>");
		}
		else if (element.equalsIgnoreCase("type")) {
			myIsType = true;
			
			if (myIsClef || myIsComponent) {
				if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t<type>");
			}
			else if (myIsTime || myIsKey) {
				if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t\t<type>");
			}
		}
		else if (element.equalsIgnoreCase("position")) {
			myIsPosition = true;
			
			if (myIsComponent) {
				if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t<position>");
			}
			else if (myIsKey) {
				if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t\t<position>");
			}
		}
		else if (element.equalsIgnoreCase("value")) {
			myIsValue = true;
			
			if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t\t<value>");
		}
		else if (element.equalsIgnoreCase("component")) {
			myIsComponent = true;
			myComponent = null;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t<component>");
		}
		else if (element.equalsIgnoreCase("accidental")) {
			myIsAccidental = true;
			
			if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t<accidental>");
		}
		else if (element.equalsIgnoreCase("duration")) {
			myIsDuration = true;
			
			if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t<duration>");
		}
		else if (element.equalsIgnoreCase("bar")) {
			myIsBar = true;
			
			if (OUTPUT_TO_CONSOLE) System.out.print("\t\t\t<bar>");
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		
		String content = String.copyValueOf(ch, start, length).trim();
		
		if (myIsClef) {
			
			if (myIsType) {
				myClefType = null;
				if (content.equalsIgnoreCase("treble"))
					myClefType = XmlClef.Type.TREBLE;
				else if (content.equalsIgnoreCase("bass"))
					myClefType = XmlClef.Type.BASS;
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myClefType.toString().toLowerCase());
			}
		}
		else if (myIsKey) {
			
			if (myIsType) {
				if (content.equalsIgnoreCase("flat"))
					myKeyType = XmlKey.Type.FLAT;
				else if (content.equalsIgnoreCase("sharp"))
					myKeyType = XmlKey.Type.SHARP;
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myKeyType.toString().toLowerCase());
			}
			else if (myIsPosition) {
				myPosition = Integer.parseInt(content);
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myPosition);
			}
		}
		else if (myIsTime) {
			
			if (myIsType) {
				if (content.equalsIgnoreCase("top"))
					myTimeType = XmlTime.Type.TOP;
				else if (content.equalsIgnoreCase("bottom"))
					myTimeType = XmlTime.Type.BOTTOM;
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myTimeType.toString().toLowerCase());
			}
			else if (myIsValue) {
				myValue = Integer.parseInt(content);
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myValue);
			}
		}
		else if (myIsComponent) {
			
			if (myIsType) {
				if (content.equalsIgnoreCase("note"))
					myComponentType = XmlComponent.Type.NOTE;
				else if (content.equalsIgnoreCase("rest"))
					myComponentType = XmlComponent.Type.REST;
				else if (content.equalsIgnoreCase("bar"))
					myComponentType = XmlComponent.Type.BAR;
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myComponentType.toString().toLowerCase());
			}
			else if (myIsPosition) {
				myPosition = Integer.parseInt(content);
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myPosition);
			}
			else if (myIsAccidental) {
				if (content.equalsIgnoreCase("flat"))
					myAccidentalType = XmlComponent.Accidental.FLAT;
				else if (content.equalsIgnoreCase("sharp"))
					myAccidentalType = XmlComponent.Accidental.SHARP;
				else if (content.equalsIgnoreCase("natural"))
					myAccidentalType = XmlComponent.Accidental.NATURAL;
				else if (content.equalsIgnoreCase("none"))
					myAccidentalType = XmlComponent.Accidental.NONE;
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myAccidentalType.toString().toLowerCase());
			}
			else if (myIsDuration) {
				myDuration = Double.parseDouble(content);
				
				if (OUTPUT_TO_CONSOLE) System.out.print(myDuration);
			}
			else if (myIsBar) {
				if (content.equalsIgnoreCase("single"))
					myBarType = XmlComponent.Bar.SINGLE;
				else if (content.equalsIgnoreCase("double"))
					myBarType = XmlComponent.Bar.DOUBLE;
				else if (content.equalsIgnoreCase("none"))
					myBarType = XmlComponent.Bar.NONE;

				if (OUTPUT_TO_CONSOLE) System.out.print(myBarType.toString().toLowerCase());
			}
		}
	}
	
	public void endElement(String s, String s1, String element) throws SAXException {
		
		if (element.equalsIgnoreCase("answer")) {
			myIsAnswer = false;
			myAnswer = new XmlAnswer(myStaffList);
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</answer>");
		}
		else if (element.equalsIgnoreCase("staff")) {
			myIsStaff = true;
			myStaff = new XmlStaff(myClef, myKeySignature, myTimeSignature, myComponents);
			myStaffList.add(myStaff);
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t</staff>");
		}
		else if (element.equalsIgnoreCase("clef")) {
			myIsClef = false;
			myClef = new XmlClef(myClefType);
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t</clef>");
		}
		else if (element.equalsIgnoreCase("keySignature")) {
			myIsKeySignature = false;

			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t</keySignature>");
		}
		else if (element.equalsIgnoreCase("timeSignature")) {
			myIsTimeSignature = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t</timeSignature>");
		}
		else if (element.equalsIgnoreCase("time")) {
			myIsTime = false;
			myTime = new XmlTime(myTimeType, myValue);
			myTimeSignature.add(myTime);
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t\t</time>");
		}
		else if (element.equalsIgnoreCase("key")) {
			myIsKey = false;
			myKey = new XmlKey(myKeyType, myPosition);
			myKeySignature.add(myKey);
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t\t</key>");
		}
		else if (element.equalsIgnoreCase("type")) {
			myIsType = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</type>");
		}
		else if (element.equalsIgnoreCase("value")) {
			myIsValue = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</value>");
		}
		else if (element.equalsIgnoreCase("position")) {
			myIsPosition = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</position>");
		}
		else if (element.equalsIgnoreCase("component")) {
			myIsComponent = false;
			myComponent = new XmlComponent(myComponentType, myPosition, myAccidentalType, myDuration, myBarType);
			myComponents.add(myComponent);
			
			if (OUTPUT_TO_CONSOLE) System.out.println("\t\t</component>");
		}
		else if (element.equalsIgnoreCase("accidental")) {
			myIsAccidental = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</accidental>");
		}
		else if (element.equalsIgnoreCase("duration")) {
			myIsDuration = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</duration>");
		}
		else if (element.equalsIgnoreCase("bar")) {
			myIsBar = false;
			
			if (OUTPUT_TO_CONSOLE) System.out.println("</bar>");
		}
	}
	
	public XmlAnswer getAnswer() {
		
		return myAnswer;
	}
	
	
	
	private String myXmlFilePath;
	
	private XmlAnswer myAnswer;
	private List<XmlStaff> myStaffList;
	private XmlStaff myStaff;
	private XmlClef myClef;
	private List<XmlKey> myKeySignature;
	private List<XmlTime> myTimeSignature;
	private List<XmlComponent> myComponents;
	private XmlKey myKey;
	private XmlTime myTime;
	private int myValue;
	private int myPosition;
	private double myDuration;
	private XmlComponent myComponent;
	
	private XmlClef.Type myClefType;
	private XmlTime.Type myTimeType;
	private XmlKey.Type myKeyType;
	private XmlComponent.Type myComponentType;
	private XmlComponent.Accidental myAccidentalType;
	private XmlComponent.Bar myBarType;
	
	private boolean myIsAnswer;
	private boolean myIsStaff;
	private boolean myIsClef;
	private boolean myIsKeySignature;
	private boolean myIsTimeSignature;
	private boolean myIsKey;
	private boolean myIsPosition;
	private boolean myIsTime;
	private boolean myIsType;
	private boolean myIsValue;
	private boolean myIsComponent;
	private boolean myIsDuration;
	private boolean myIsAccidental;
	private boolean myIsBar;
	
	public static final boolean OUTPUT_TO_CONSOLE = false;
}
