package edu.tamu.srl.music.xml;

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

public class XmlLessonParser extends DefaultHandler  {

	public XmlLessonParser(File xmlFile) {
		
		this(xmlFile.getAbsolutePath());
	}

	public XmlLessonParser(String xmlFilePath) {
		
		myXmlFilePath = xmlFilePath;
		myXmlQuestions = new ArrayList<XmlQuestion>();
		
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
	
	public void startElement(String s, String s1, String element, Attributes attributes) throws SAXException {
		
		if (element.equalsIgnoreCase("title")) {
			myIsTitle = true;
		}
		else if (element.equalsIgnoreCase("number")) {
			myIsNumber = true;
		}
		else if (element.equalsIgnoreCase("text")) {
			myIsQuestion = true;
		}
		else if (element.equalsIgnoreCase("hint")) {
			myIsHint = true;
		}
		else if (element.equalsIgnoreCase("answer")) {
			myIsAnswer = true;
		}
		else if (element.equalsIgnoreCase("image")) {
			myIsImage = true;
		}
		else if (element.equalsIgnoreCase("criteria")) {
			myIsCriteria = true;
			myGradingCriteria = new boolean[XmlQuestion.NUM_GRADING_CRITERIA];
			myCriterionCount = 0;
		}
		else if (element.equalsIgnoreCase("criterion")) {
			myIsCriterion = true;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		
		String content = String.copyValueOf(ch, start, length).trim();
		
		if (myIsTitle) {
			myTitle = content;
		}
		else if (myIsNumber) {
			myQuestionNumber = Integer.parseInt(content);
		}
		else if (myIsQuestion) {
			myQuestionText = content;
		}
		else if (myIsHint) {
			myHintText = content;
		}
		else if (myIsAnswer) {
			if (content.equals("null"))
				myAnswerFile = null;
			else
				myAnswerFile = new File(content);
		}
		else if (myIsImage) {
			if (content.equals("null"))
				myImageFile = null;
			else
				myImageFile = new File(content);
		}
		else if (myIsCriterion) {
			
			if (content.equals("true"))
				myGradingCriteria[myCriterionCount] = true;
			else
				myGradingCriteria[myCriterionCount] = false;
			
			++myCriterionCount;
		}
	}
	
	public void endElement(String s, String s1, String element) throws SAXException {
		
		if (element.equalsIgnoreCase("title")) {
			myIsTitle = false;
		}
		else if (element.equalsIgnoreCase("number")) {
			myIsNumber = false;
		}
		else if (element.equalsIgnoreCase("text")) {
			myIsQuestion = false;
		}
		else if (element.equalsIgnoreCase("hint")) {
			myIsHint = false;
		}
		else if (element.equalsIgnoreCase("answer")) {
			myIsAnswer = false;
		}
		else if (element.equalsIgnoreCase("image")) {
			myIsImage = false;
		}
		else if (element.equalsIgnoreCase("criteria")) {
			myIsCriteria = false;
		}
		else if (element.equalsIgnoreCase("criterion")) {
			myIsCriterion = false;
		}
		else if (element.equalsIgnoreCase("question")) {
			XmlQuestion xmlQuestion = new XmlQuestion(myQuestionNumber, myQuestionText, myHintText, myAnswerFile, myImageFile, myGradingCriteria);
			myXmlQuestions.add(xmlQuestion);
		}
		else if (element.equalsIgnoreCase("lesson")) {
			
			myXmlLesson = new XmlLesson(myTitle, myXmlQuestions);
		}
	}
	
	public XmlLesson getXmlLesson() {
		
		return myXmlLesson;
	}
	
	private String myXmlFilePath;
	private XmlLesson myXmlLesson;
	private String myTitle;
	private int myQuestionNumber;
	private String myQuestionText;
	private String myHintText;
	private File myAnswerFile;
	private File myImageFile;
	private boolean[] myGradingCriteria;
	private boolean myIsTitle;
	private boolean myIsNumber;
	private boolean myIsQuestion;
	private boolean myIsHint;
	private boolean myIsAnswer;
	private boolean myIsImage;
	private boolean myIsCriteria;
	private boolean myIsCriterion;
	private int myCriterionCount;
	private List<XmlQuestion> myXmlQuestions;
}
