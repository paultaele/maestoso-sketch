package edu.tamu.srl.music.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.tamu.srl.music.classifier.IShape;
import edu.tamu.srl.music.classifier.IShape.ShapeType;
import edu.tamu.srl.music.classifier.IStroke;
import edu.tamu.srl.music.classifier.ShapeClassifier;

/**
 * The sketch panel class for handling drawing interactions.
 * 
 * @author Paul Taele
 *
 */
@SuppressWarnings("serial")
public class SketchPanel extends JPanel {

	/**
	 * The main constructor for the sketch panel.
	 * 
	 * @param gui The associated GUI object.
	 */
	public SketchPanel(MainGui gui) {
		
		// give access of the associated GUI to the sketch panel
		myGui = gui;
		
		// disable double buffering since the program is not animation-heavy
		// note: double buffering is a technique for drawing graphics that 
		//		 shows no (or less) flickering, tearing, and other artifacts
		setDoubleBuffered(false);
		
		// add a mouse motion listener for when the mouse is pressed
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				
				// get the mouse's initial location
				myPrevX = e.getX();
				myPrevY = e.getY();
				
				// set the color of the current stroke and add the first point
				myCurrStroke = new IStroke(myGraphics2D.getColor());
				myCurrStroke.add(new Point2D.Double(myPrevX, myPrevY));
			}
		});
		
		// add a mouse motion listener for when the mouse is dragged
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				
				// get the mouse's current location
				myCurrX = e.getX();
				myCurrY = e.getY();
				
				// draw a line between the mouse's current and previous location
				if (myGraphics2D != null) {
					
					Shape line = new Line2D.Double(myPrevX, myPrevY, myCurrX, myCurrY);
					myGraphics2D.draw(line);
				}
				
				// set the mouse's current location as the previous location
				myPrevX = myCurrX;
				myPrevY = myCurrY;
				
				// add the current point to the current stroke
				myCurrStroke.add(new Point2D.Double(myPrevX, myPrevY));
				
				// update the sketch panel by adding the included line
				repaint();
			}
		});
		
		// add a mouse listener for when the mouse is released
		addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				
				//
				IShape shape = new IShape(IShape.ShapeType.RAW, myCurrStroke);
				myShapes.add(shape);
				
				//
				ShapeClassifier shapeClassifier = new ShapeClassifier();
				List<IShape> shapes = shapeClassifier.classify(myShapes);
				myShapes = shapes;
				
				// update the sketch panel
				myShapes.add(null);
				undo();
			}
		});
	}
	
	/**
	 * Handles the painting interactions. If there is nothing on the
	 * canvas, then it creates an image the size of the window. It
	 * sets the value of the graphics as the image. It also sets the
	 * rendering, clears the canvas, and then finally draws the image.
	 * 
	 * @param g The graphics.
	 */
	public void paintComponent(Graphics g) {
		
		// case: the canvas' image does not exist
		if(myImage == null) {
			
			// create the canvas' image
			myImage = createImage(getSize().width, getSize().height);
			
			// set the canvas' graphic with the created image
			myGraphics2D = (Graphics2D)myImage.getGraphics();
			
			// set up the canvas' rendering style
			myGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// set up the canvas' stroke style
			myGraphics2D.setStroke(STROKE_STYLE);
			
			// clear the canvas
			clear();
		}
		
		// draw the image onto the canvas
		g.drawImage(myImage, 0, 0, null);
	}
	
	/**
	 * Clears the canvas. It sets the pen color to white, then fills the 
	 * window with white, and then sets the mouse color back to black.
	 */
	public void clear() {
		
		// set the pen color to white
		myGraphics2D.setPaint(Color.white);
		
		// fill the entire canvas to white
		myGraphics2D.fillRect(0, 0, getSize().width, getSize().height);

		// set the pen color to black
		myGraphics2D.setPaint(ACTIVE_COLOR);
		
		// clear the list of shapes
		myShapes = new ArrayList<IShape>();
		
		// update the canvas
		repaint();
	}
	
	/**
	 * Undoes the last action.
	 */
	public void undo() {
		
		// case: there are no shapes that are on the sketch panel
		// immediately exit the undo method
		if (myShapes.size() == 0)
			return;
		
		// make copy of current list of shapes
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape shape : myShapes)
			shapes.add(shape);
		
		// clear the canvas by painting the entire canvas white and
		// removing all shapes in the shapes list
		clear();
		
		// set the original list of shapes to the list of copied shapes
		myShapes = shapes;
		
		// remove last shape from list of shapes
		myShapes.remove(myShapes.size()-1);
		
		// repaint current list of shapes
		repaintShapes(myShapes);
		
	}
	
	/**
	 * Checks the drawing for recognition.
	 */
	public void check() {
		
		;
	}
	
	private void repaintShapes(List<IShape> shapes) {

		// get the active color
		Color activeColor = myGraphics2D.getColor();
		
		// case: 2D graphics is "on"
		if (myGraphics2D != null) {
	    	
			// iterate through each shape
	    	double prevX, prevY, currX, currY;
	    	for (IShape shape : shapes) {
	    		
	    		//
	    		if (shape.hasImage()) {
	    			
	    			try {
	    				
		    			int width = shape.getImageHeight();
		    			int height = shape.getImageHeight();
		    			int xPos = shape.getImageX();
		    			int yPos = shape.getImageY();
		    			
		    			BufferedImage otherImage = ImageIO.read(shape.getImageFile());
		    			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		    			Graphics g = newImage.createGraphics();
		    			g.drawImage(otherImage, 0, 0, width, height, null);
		    			g.dispose();
		    			myGraphics2D.drawImage(newImage, xPos, yPos, null);
	
		    			} catch (IOException e) {
		    			e.printStackTrace();
	    			}
	    		}
	    		
	    		//
	    		else {
	    			
	    			// iterate through each stroke
			    	for (IStroke stroke : shape.getStrokes()) {
				    		
			    		// get the current stroke's points
			    		List<Point2D.Double> points = stroke.getPoints();
			    		myGraphics2D.setColor(stroke.getColor());
				    	
			    		// draw the stroke's path between each stroke point
			    		for (int i = 1; i < points.size(); ++i) {
				    		
				    		prevX = points.get(i-1).x;
				    		prevY = points.get(i-1).y;
				    		currX = points.get(i).x;
				    		currY = points.get(i).y;
				    		Shape line = new Line2D.Double(prevX, prevY, currX, currY);
				    		myGraphics2D.draw(line);
				    	}
			    	}
		    	}
	    	}
	    }
	    
		// update the sketch panel and reset brush to active color
	    repaint();
	    myGraphics2D.setColor(activeColor);
	}
	
	/**
	 * Sets the paint to black.
	 */
	public void setPaintBlack() {
		
		myGraphics2D.setPaint(Color.black);
		ACTIVE_COLOR = Color.black;
		repaint();
	}
	
	/**
	 * Sets the paint to red.
	 */
	public void setPaintRed() {
		
		myGraphics2D.setPaint(Color.red);
		ACTIVE_COLOR = Color.red;
		repaint();
	}
	
	/**
	 * Sets the paint to magenta.
	 */
	public void setPaintMagenta() {
		
		myGraphics2D.setPaint(Color.magenta);
		ACTIVE_COLOR = Color.magenta;
		repaint();
	}
	
	/**
	 * Sets the paint to blue.
	 */
	public void setPaintBlue() {
		
		myGraphics2D.setPaint(Color.blue);
		ACTIVE_COLOR = Color.blue;
		repaint();
	}
	
	/**
	 * Sets the paint to green.
	 */
	public void setPaintGreen() {
		
		myGraphics2D.setPaint(Color.green);
		ACTIVE_COLOR = Color.green;
		repaint();
	}

	
	
	private MainGui myGui;
	
	private Image myImage;							// the image that the user will draw on
	private Graphics2D myGraphics2D;				// the graphic that the user will draw on
	
	private double myCurrX;							// the pen's current x-coordinate
	private double myCurrY;							// the pen's current y-coordinate
	
	private double myPrevX;							// the pen's previous x-coordinate
	private double myPrevY;							// the pen's previous y-coordinate
	
	private IStroke myCurrStroke;
	private List<IShape> myShapes;
	
	private static final float STROKE_WIDTH = 3f;	// the pen's stroke width
	private static final Stroke STROKE_STYLE 		// the mouse's stroke style
		= new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	public static Color ACTIVE_COLOR = Color.black;
}