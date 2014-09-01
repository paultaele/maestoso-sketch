package edu.tamu.srl.music.xml.lesson;

import java.io.File;

public class XmlQuestion {
	
	public XmlQuestion(int number, String questionText, File answerFile, File imageFile, boolean[] gradingCriteria) {
		
		myQuestionNumber = number;
		myQuestionText = questionText;
		myAnswerFile = answerFile;
		myImageFile = imageFile;
		myGradingCriteria = gradingCriteria;
	}
	
	public int getQuestionNumber() { return myQuestionNumber; }
	public String getQuestionText() { return myQuestionText; }
	public File getAnswerFile() { return myAnswerFile; }
	public File getImageFile() { return myImageFile; }
	public boolean[] getGradingCriteria() { return myGradingCriteria; }

	public boolean canGradeClef() { return myGradingCriteria[CLEF_INDEX]; }
	public boolean canGradeKey() { return myGradingCriteria[KEY_INDEX]; }
	public boolean canGradeTime() { return myGradingCriteria[TIME_INDEX]; }
	public boolean canGradeNotes() { return myGradingCriteria[NOTES_INDEX]; }
	public boolean canGradeDurations() { return myGradingCriteria[DURATIONS_INDEX]; }
	public boolean canGradeMeasures() { return myGradingCriteria[MEASURES_INDEX]; }
	public boolean canGradeStaff() {
		
		if (!canGradeClef() && !canGradeKey() && !canGradeTime() && !canGradeNotes() && !canGradeDurations() && !canGradeMeasures())
			return true;
		
		return false;
	}
	
	private int myQuestionNumber;
	private String myQuestionText;
	private File myAnswerFile;
	private File myImageFile;
	private boolean[] myGradingCriteria;
	
	
	public static final int NUM_GRADING_CRITERIA = 6;
	public static final int CLEF_INDEX = 0;
	public static final int KEY_INDEX = 1;
	public static final int TIME_INDEX = 2;
	public static final int NOTES_INDEX = 3;
	public static final int DURATIONS_INDEX = 4;
	public static final int MEASURES_INDEX = 5;
}
