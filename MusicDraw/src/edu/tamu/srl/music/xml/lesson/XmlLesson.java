package edu.tamu.srl.music.xml.lesson;

import java.util.List;

public class XmlLesson {

	public XmlLesson(String title, List<XmlQuestion> questions) {
		
		myTitle = title;
		myQuestions = questions;
	}
	
	public String getTitle() { return myTitle; }
	public List<XmlQuestion> getQuestions() { return myQuestions; }
	
	

	private String myTitle;
	private List<XmlQuestion> myQuestions;
}
