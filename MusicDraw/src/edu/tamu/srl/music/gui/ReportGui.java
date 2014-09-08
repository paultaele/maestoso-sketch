package edu.tamu.srl.music.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import edu.tamu.srl.music.xml.lesson.XmlQuestion;

public class ReportGui implements Runnable {

	public ReportGui(List<XmlQuestion> questions, int[] responses) {
		
		myQuestions = questions;
		myResponses = responses;
	}
	
	public void run() {
		
		// initialize the main frame
        myFrame = new JFrame();
        myFrame.setLayout(new BorderLayout());
        myFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        myFrame.getContentPane().setBackground(Color.white);
        
        //
        generateFrame();
        
		// handle the main frame's actions
		myFrame.setVisible(true);										// enable visibility
		myFrame.setLocationRelativeTo(null);							// enable centering of screen
		myFrame.setResizable(true);										// enable resizing
		myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);			// do nothing on closing
		myFrame.toFront();
		myFrame.repaint();
	}
	
	private void generateFrame() {
		
		//
		GridBagConstraints gbc = new GridBagConstraints();
		int gridY = 0;
		
		//
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.white);
		
		// add title panel
		mainPanel.add(createTitlePanel(), BorderLayout.NORTH);
		
		//
		mainPanel.add(createReportPanel(), BorderLayout.WEST);
		
		// TODO
		JPanel superPanel = new JPanel();
		superPanel.setLayout(new BoxLayout(superPanel, BoxLayout.PAGE_AXIS));
		superPanel.add(mainPanel);
		JScrollPane scrollPane = new JScrollPane(superPanel);
		myFrame.add(scrollPane);
	}
	
	private JPanel createReportPanel() {
		
		//
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.white);
		
		//
		panel.add(createSummaryReportPanel(myResponses), BorderLayout.NORTH);
		
		//
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout());
		subPanel.setBackground(Color.white);
		subPanel.add(createQuestionsReportPanel(), BorderLayout.NORTH);
		panel.add(subPanel, BorderLayout.WEST);
		
		//
		return panel;
	}
	
	private JPanel createQuestionsReportPanel() {
		
		//
		GridBagConstraints gbc = new GridBagConstraints();
		int gridY = 0;
		
		//
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(Color.white);
		
		//
		for (int i = 0; i < myResponses.length; ++i) {
			gbc.gridy = gridY++;
			panel.add(createQuestionReportPanel(myQuestions.get(i), myResponses[i]), gbc);
		}
		
		//
		return panel;
	}
	
	private JPanel createQuestionReportPanel(XmlQuestion question, int response) {
		
		// initialize the grid bag constraints
		GridBagConstraints gbc = new GridBagConstraints();
		int gridX = 0;
		
		//
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(Color.white);
		
		//
		gbc.gridx = gridX++;
		panel.add(createCorrectImagePanel(response), gbc);

		
		//
		gbc.gridx = gridX++;
		panel.add(createQuestionTextPanel(question.getQuestionNumber(), question.getQuestionText()), gbc);
		
		//
		gbc.gridx = gridX++;
		panel.add(createQuestionImagePanel(question.getImageFile().getAbsolutePath()), gbc);
		
//		//
//		gbc.gridx = gridX++;
//		panel.add(new JLabel("#" + question.getQuestionNumber()), gbc);
		
		//
		return panel;
	}
	
	private JPanel createQuestionTextPanel(int number, String text) {
		
		//
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		
		//
		String fixedText = MaestosoSketchGui.getFixedText("" + number + ". " + text, QUESTION_TEXT_LENGTH);
		JLabel label = new JLabel();
		label.setFont(QUESTION_FONT);
		label.setBackground(Color.white);
		label.setText(fixedText);
		
		//
		panel.add(label);
		return panel;
	}
	
	private JPanel createCorrectImagePanel(int response) {
		
		//
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		
		//
		String imagePath = "";
		if (response > 0)
			imagePath = FeedbackGui.BOX_YES_IMAGE_FILE_PATH;
		else
			imagePath = FeedbackGui.BOX_NO_IMAGE_FILE_PATH;
		
		//
		ImageIcon imageIcon = new ImageIcon(FeedbackGui.resizeImage(imagePath, FeedbackGui.BOX_WIDTH, FeedbackGui.BOX_HEIGHT));
		JLabel imageLabel = new JLabel(imageIcon);
		imageLabel.setBackground(Color.white);
		
		//
		panel.add(imageLabel);
		return panel;
	}
	
	private JPanel createQuestionImagePanel(String imagePath) {
		
		//
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		
		//
		ImageIcon imageIcon = new ImageIcon(FeedbackGui.resizeImage(imagePath, MaestosoSketchGui.PREVIEW_IMAGE_WIDTH_SMALL, MaestosoSketchGui.PREVIEW_IMAGE_HEIGHT_SMALL));
		JLabel imageLabel = new JLabel(imageIcon);
		imageLabel.setBackground(Color.white);
		
		//
		panel.add(imageLabel);
		return panel;
	}
	
	private JPanel createSummaryReportPanel(int[] responses) {
		
		//
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.white);
		
		//
		JPanel summary = createSummaryTextPanel(responses);
		JPanel grade = createGradePanel(responses);
		
		//
		panel.add(summary, BorderLayout.LINE_START);
		panel.add(grade, BorderLayout.LINE_END);
		return panel;
	}
	
	private JPanel createSummaryTextPanel(int[] responses) {
		
		//
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.white);
		panel.setBorder(EMPTY_BORDER);
		
		//
		int numTotal = responses.length;
		int numCorrect = 0;
		for (int response : responses) {
			if (response > 0)
				++numCorrect;
		}
		
		//
		String text = "Of the <b>" + numTotal + "</b> problems in this lesson, you have answered <b>" + numCorrect + "</b> problems correctly.";
		String fixedText = MaestosoSketchGui.getFixedText(text, SUMMARY_TEXT_LENGTH);
		JLabel label = new JLabel();
		label.setBackground(Color.white);
		label.setText(PRE_HTML + text + POST_HTML);
		label.setFont(SUMMARY_FONT);
		
		//
		panel.add(label);
		return panel;
	}
	
	private JPanel createGradePanel(int[] responses) {
		
		//
		JPanel panel = new JPanel();
		panel.setBorder(BLACK_BORDER);
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.white);
		
		//
		int numTotal = responses.length;
		int numCorrect = 0;
		for (int response : responses) {
			if (response > 0)
				++numCorrect;
		}
		double ratio = (double)numCorrect/(double)numTotal*100;
		String ratioText = new DecimalFormat("#.#").format(ratio) + "%";
		
		//
		JLabel textLabel = new JLabel();
		textLabel.setBackground(Color.white);
//		textLabel.setFont(QUESTION_FONT);
		textLabel.setText(PRE_HTML + FeedbackGui.space(3) + "<b>Grade:</b>" + POST_HTML);
		
		//
		JLabel percentageLabel = new JLabel();
		percentageLabel.setBackground(Color.white);
		percentageLabel.setFont(GRADE_FONT);
		percentageLabel.setText(PRE_HTML + "<b>" + FeedbackGui.space(1) + ratioText + "</b>" + FeedbackGui.space(1) + POST_HTML);
		
		//
		panel.add(textLabel, BorderLayout.NORTH);
		panel.add(percentageLabel, BorderLayout.SOUTH);
		return panel;
	}
	
	private JPanel createTitlePanel() {
		
		//
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		
		//
		JLabel label = new JLabel();
		label.setBackground(Color.white);
		label.setFont(TITLE_FONT);
		label.setText(PRE_HTML + "<b>Maestoso Sketch &mdash; Report Card</b>" + POST_HTML);
		
		//
		panel.add(label);
		return panel;
	}
	
	
	
	private JFrame myFrame;
	private List<XmlQuestion> myQuestions;
	private int[] myResponses;
	
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;
	public static final int QUESTION_TEXT_LENGTH = 250;
	public static final int SUMMARY_TEXT_LENGTH = 250;
	public static final Font TITLE_FONT = new Font("Sans Serif", Font.PLAIN, 20);
	public static final Font QUESTION_FONT = new Font("Sans Serif", Font.PLAIN, 16);
	public static final Font GRADE_FONT = new Font("Sans Serif", Font.PLAIN, 60);
	public static final Font SUMMARY_FONT = new Font("Sans Serif", Font.PLAIN, 16);
	public static final String PRE_HTML = "<html>";
	public static final String POST_HTML = "</html>";
	public static final Border BLACK_BORDER = BorderFactory.createLineBorder(Color.black);
	public static final EmptyBorder EMPTY_BORDER = new EmptyBorder(10, 10, 10, 10);
}