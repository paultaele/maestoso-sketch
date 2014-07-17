package edu.tamu.srl.music.classifier;

import java.awt.image.BufferedImage;

public class IImage {

	public IImage(BufferedImage image, double x, double y, double width, double height) {
		
		myImage = image;
		myX = x;
		myY = y;
		myHeight = height;
		myWidth = width;
	}
	
	public BufferedImage image() {
		return myImage;
	}
	
	public double x() {
		return myX;
	}
	
	public double y() {
		return myY;
	}
	
	public double height() {
		return myHeight;
	}
	
	public double width() {
		return myWidth;
	}
	
	
	
	private BufferedImage myImage;
	private double myX;
	private double myY;
	private double myWidth;
	private double myHeight;
}
