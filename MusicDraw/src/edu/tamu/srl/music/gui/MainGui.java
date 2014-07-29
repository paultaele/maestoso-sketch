		package edu.tamu.srl.music.gui;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.tamu.srl.music.classifier.IShape;
import edu.tamu.srl.music.classifier.Template;

/**
 * The main GUI for the application.
 * 
 * @author Paul Taele
 *
 */
public class MainGui implements Runnable {
	
	/**
	 * The main method for running the application.
	 * @param args The console arguments.
	 */
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new MainGui());
	}
	
	/**
	 * Runs the methods to create the main frame.
	 */
	public void run() {
    	
        createFrame();
    }
	
	/**
	 * Creates the main frame.
	 */
	private void createFrame() {
		
		// set the dimensions of the main frame relative to a set ratio of
		// the screen's size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		FRAME_WIDTH = (int)(screenSize.width * FRAME_RATIO);
		FRAME_HEIGHT = (int)(screenSize.height * FRAME_RATIO);
		
		// initialize the main frame
        myFrame = new JFrame();
        
        // load templates and images from disk
        Template.loadTemplates();
        IShape.loadImages();
        
        // create the main panel and add it to the main frame
        createMainPanel();
		myFrame.add(myMainPanel);
        
		// handle the main frame's actions
		myFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);					// set the size of the frame
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			// enable closing
		myFrame.setVisible(true);										// enable visibility
		myFrame.setLocationRelativeTo(null);							// enable centering of screen
		myFrame.setResizable(false);									// disable resizing
		myFrame.setTitle(APPLICATION_TITLE);							// set application title
        myFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);	// do nothing on closing
        myFrame.addWindowListener(new WindowAdapter() {					// handle post-closing actions
            public void windowClosing(WindowEvent event) {
                exitProcedure();
            }
        });
    }
	
	/**
	 * Handles application after user closes the main frame.
	 */
	public void exitProcedure() {
		
        myFrame.dispose();
        System.exit(0);
    }
	
	/**
	 * Creates the main panel.
	 */
	private void createMainPanel() {
		
		// initialize and set up the main panel's layout
        myMainPanel = new JPanel();
        myMainPanel.setLayout(new BorderLayout());
        
        // create the text panel and add it to the main panel
        createTextPanel();
        myMainPanel.add(myTextPanel, BorderLayout.NORTH);
        
        // create the draw panel and add it to the main panel
		createDrawPanel();
        myMainPanel.add(myDrawPanel, BorderLayout.CENTER);
		
        // create the interaction panel and add it to the main panel
        createInteractionPanel();
        myMainPanel.add(myInteractionPanel, BorderLayout.SOUTH);
    }
	
	/**
	 * Creates the text panel.
	 */
	private void createTextPanel() {
		
		// WORK IN PROGRESS
		myTextPanel = new JPanel();
		myTextPanel.setLayout(new BorderLayout());
		
		JLabel textLabel = new JLabel("???");
		myTextPanel.add(textLabel);
	}
	
	/**
	 * Creates the draw panel.
	 */
	private void createDrawPanel() {
		
		// create the draw panel
		 myDrawPanel = new SketchPanel(this);
	}
	
	/**
	 * Creates the interaction panel.
	 */
	private void createInteractionPanel() {
		
		// initialize the interaction panel and set up its layout
		myInteractionPanel = new JPanel();
		myInteractionPanel.setLayout(new BorderLayout());
		
		// create the colors panel and add it to the interaction panel
		createColorsPanel();
		myInteractionPanel.add(myColorsPanel, BorderLayout.NORTH);
		
		// create the edit panel and add it to the interaction panel
		createEditPanel();
		myInteractionPanel.add(myEditPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Creates the colors panel.
	 */
	private void createColorsPanel() {
		
		// initialize the colors panel
		myColorsPanel = new JPanel();
		
		// initialize the image icons
		Icon iconB  = new ImageIcon(IMAGES_DIR_PATHNAME + "blue.gif");
		Icon iconM  = new ImageIcon(IMAGES_DIR_PATHNAME + "magenta.gif");
		Icon iconR  = new ImageIcon(IMAGES_DIR_PATHNAME + "red.gif");
		Icon iconBl = new ImageIcon(IMAGES_DIR_PATHNAME + "black.gif");
		Icon iconG  = new ImageIcon(IMAGES_DIR_PATHNAME + "green.gif");
		
		// initialize the buttons
		JButton redButton = new JButton(iconR);
		JButton blackButton = new JButton(iconBl);
		JButton magentaButton = new JButton(iconM);
		JButton blueButton = new JButton(iconB);
		JButton greenButton = new JButton(iconG);
		
		// set the sizes of the buttons
		blackButton.setPreferredSize(new Dimension(16, 16));
		magentaButton.setPreferredSize(new Dimension(16, 16));
		redButton.setPreferredSize(new Dimension(16, 16));
		blueButton.setPreferredSize(new Dimension(16, 16));
		greenButton.setPreferredSize(new Dimension(16,16));
		
		// create the red button and set the icon we created for red
		redButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myDrawPanel.setPaintRed();
			}

		});
		
		// create the black button and set the icon we created for black
		blackButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				myDrawPanel.setPaintBlack();
			}
		});
		
		// create the magenta button and set the icon we created for magenta
		magentaButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				myDrawPanel.setPaintMagenta();
			}
		});
		
		// create the blue button and set the icon we created for blue
		blueButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				myDrawPanel.setPaintBlue();
			}
		});
		
		// create the green button and set the icon we created for green
		greenButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				myDrawPanel.setPaintGreen();
			}
		});
		
		// add the buttons to the colors panel
		myColorsPanel.add(greenButton);
		myColorsPanel.add(blueButton);
		myColorsPanel.add(magentaButton);
		myColorsPanel.add(blackButton);
		myColorsPanel.add(redButton);
	}
	
	/**
	 * Creates the edit panel.
	 */
	private void createEditPanel() {
		
		// initialize the edit panel
		myEditPanel = new JPanel();
		
		// initialize the buttons
		JButton clearButton = new JButton("Clear");
		JButton undoButton = new JButton("Undo");
		JButton saveButton = new JButton("Save");
		JButton checkButton = new JButton("Check");
		
		// create the clear button and add clear functionality
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myDrawPanel.clear();
			}
		});
		
		// create the undo button and add undo functionality
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myDrawPanel.undo();
			}
		});
		
		// create the save button and add save functionality
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myDrawPanel.save();
			}
		});
		
		// create the check button and add check functionality
		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myDrawPanel.check();
			}
		});
		
		// add the buttons to the panel
		myEditPanel.add(clearButton);
		myEditPanel.add(undoButton);
		myEditPanel.add(saveButton);
		myEditPanel.add(checkButton);
	}
	
	/**
	 * Gets the text panel.
	 * 
	 * @return
	 */
	public JPanel getTextPanel() {
		
		return myTextPanel;
	}
	
	
	
	private JFrame myFrame;
	private JPanel myMainPanel;
	private JPanel myTextPanel;
	private SketchPanel myDrawPanel;
	private JPanel myInteractionPanel;
	private JPanel myEditPanel;
	private JPanel myColorsPanel;
	
	public static int FRAME_WIDTH;
	public static int FRAME_HEIGHT;
	public static final double FRAME_RATIO = 0.8;
	public static final
		String IMAGES_DIR_PATHNAME = "src/edu/tamu/srl/music/images/";
	public static final
		String APPLICATION_TITLE = "Maestoso Sketch";
}