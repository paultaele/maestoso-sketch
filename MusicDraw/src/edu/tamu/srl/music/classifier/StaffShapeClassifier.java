package edu.tamu.srl.music.classifier;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.srl.music.classifier.IShape.ShapeName;
import edu.tamu.srl.music.gui.MusicDrawGui;

public class StaffShapeClassifier implements IShapeClassifier {

	public boolean classify(List<IShape> originals) {
		
		//
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape original : originals)
			shapes.add(original);
		
		// look for five staff lines in the list of shapes
		int staffLineCount = 0;
		int[] staffLineIndices = new int[5];
		for (int i = 0; i < shapes.size(); ++i) {
			
			// get the current shape
			IShape shape = shapes.get(i);
			
			// case: the current shape is a staff line
			if (shape.getShapeName() == ShapeName.STAFF_LINE) {
				
				// add the index to the list of staff line indices
				staffLineIndices[staffLineCount] = i;
				++staffLineCount;
			}
			
			// case: five staff lines found
			if (staffLineCount == 5) {
				
				// end the loop
				break;
			}
		}

		// case: there are five staff lines
		if (staffLineCount == 5) {
			
			// extract the staff lines separately
			java.util.List<IShape> staffLines = new java.util.ArrayList<IShape>();
			for (int index : staffLineIndices)
				staffLines.add(shapes.get(index));

			// remove the staff lines from the list of all shapes
			for (IShape staffLine : staffLines)
				shapes.remove(staffLine);
			
			//
			IShape priorStaff = null;
			boolean hasStaff = false;
			for (IShape shape : shapes) {
				if (shape.getShapeName().equals(ShapeName.STAFF)) {
					hasStaff = true;
					priorStaff = shape;
				}
			}
			
			//
			double topY = Double.POSITIVE_INFINITY;
			double bottomY = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < staffLines.size(); ++i) {
				
				IStroke currentStroke = staffLines.get(i).getStrokes().get(0);
				double currentY = currentStroke.getPoints().get(0).y;
				
				if (currentY < topY)
					topY = currentY;
				if (currentY > bottomY)
					bottomY = currentY;
			}
			
			//
			double lineInterval = 0.0;
			java.util.List<IStroke> beautifiedStaffLineStrokes = new java.util.ArrayList<IStroke>();
			if (!hasStaff) {
				
				lineInterval = (bottomY - topY) / (staffLines.size()-1);
			}
			else {
				
				lineInterval = ((StaffShape)priorStaff).getLineInterval();
				double middleY = (topY + bottomY)/2;
				topY = middleY - 2*lineInterval;
				bottomY = middleY + 2*lineInterval;
			}
			
			// add beautified staff lines to the staff shape
			for (int i = 0; i < staffLines.size(); ++i) {
				
				// set up the current beautified staff line
				Color strokeColor = staffLines.get(i).getStrokes().get(0).getColor();
				IStroke beautifiedStaffLineStroke = new IStroke(strokeColor);
				Point2D.Double leftPoint = new Point2D.Double(0, topY + lineInterval*i);
//				Point2D.Double p2 = new Point2D.Double(MusicDrawGui.getWidth(), topY + lineInterval*i);
				Point2D.Double rightPoint = new Point2D.Double(StaffLineShapeClassifier.HORIZONTAL_DISTANCE, topY + lineInterval*i);
				beautifiedStaffLineStroke.add(leftPoint);
				beautifiedStaffLineStroke.add(rightPoint);
				
				// add beautified staff line to list of beautified staff lines
				beautifiedStaffLineStrokes.add(beautifiedStaffLineStroke);
			}
			
			// add staff to list of shapes
			IShape staff = new StaffShape(beautifiedStaffLineStrokes, IShape.ShapeName.STAFF, topY, lineInterval);
			shapes.add(staff);
			
			//
			myShapes = shapes;
			
			return true;
		}
		
		return false;
	}

	public List<IShape> getResult() {
		
		return myShapes;
	}

	
	
	private List<IShape> myShapes;
}