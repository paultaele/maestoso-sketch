package edu.tamu.srl.music.xml;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// http://www.javacodegeeks.com/2012/01/xml-parsing-using-saxparser-with.html
public class XmlSketchParser extends DefaultHandler {

	public XmlSketchParser(String xmlFilePath) {
		
		//
		myXmlFilePath = xmlFilePath;
		myShape = "";
		mySketch = null;
		
		//
		parseDocument();
	}
	
	private void parseDocument() {
		
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
	
	@Override
	public void startElement(String s, String s1, String element, Attributes attributes) throws SAXException {
		
		if (element.equalsIgnoreCase("sketch")) {
			
			myShape = attributes.getValue("shape");
			myStrokes = new ArrayList<XmlStroke>();
		}
		
		if (element.equalsIgnoreCase("stroke")) {
			
			myCurrentPoints = new ArrayList<XmlPoint>();
		}
		
		if (element.equalsIgnoreCase("point")) {
			
			double x = Double.parseDouble(attributes.getValue("x"));
			double y = Double.parseDouble(attributes.getValue("y"));
			
			myCurrentPoints.add(new XmlPoint(x, y));
		}
	}
	
	@Override
	public void endElement(String s, String s1, String element) throws SAXException {

		if (element.equalsIgnoreCase("sketch")) {
			
			mySketch = new XmlSketch(myShape, myStrokes);
		}
		
		if (element.equalsIgnoreCase("stroke")) {
			
			myStrokes.add(new XmlStroke(myCurrentPoints));
			myCurrentPoints = null;
		}
	}
	
	public XmlSketch getSketch() {
		
		return mySketch;
	}
	
	private String myXmlFilePath;
	private List<XmlStroke> myStrokes;
	private String myShape;
	private XmlSketch mySketch;
	
	private List<XmlPoint> myCurrentPoints;
}
