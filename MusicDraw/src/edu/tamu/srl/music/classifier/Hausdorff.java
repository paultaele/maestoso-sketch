package edu.tamu.srl.music.classifier;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Hausdorff {

	public Pair classify(List<List<Point2D.Double>> strokes, List<Template> templates) {
		
		// resample the strokes
		strokes = resample(strokes, N);
		for (Template template : templates)
			template.setPoints(resample(template.getStrokes(), N));
		
		// scale the points
		strokes = scaleTo(strokes, SIZE);
		for (Template template : templates)
			template.setPoints(scaleTo(template.getStrokes(), SIZE));
		
		// translate the point
		strokes = translateTo(strokes, K);
		for (Template template : templates)
			template.setPoints(translateTo(template.getStrokes(), K));
		
		//
		Template Tmin = templates.get(0);
		Template Tmax = templates.get(0);
		double minD = Double.MAX_VALUE;
		double minS = Double.MAX_VALUE;
		double maxD = Double.MIN_VALUE;
		double maxS = Double.MIN_VALUE;
		for (int i = 0; i < templates.size(); ++i) {
			
			Template T = templates.get(i);
			double d = distance(strokes, T.getStrokes());
			double s = 1.0 - Math.abs((1.0 - d) / (Math.sqrt(SIZE*SIZE + SIZE*SIZE))) / 10.0;
			
			if (d < minD) {
				Tmin = T;
				minD = d;
				minS = s;
			}
			if (d > maxD) {
				Tmax = T;
				maxD = d;
				maxS = s;
			}
		}
//		System.out.println("min: " + Tmin.getShape() + ": d=" + minD + " , s=" + minS);
//		System.out.println("max: " + Tmax.getShape() + ": d=" + maxD + " , s=" + maxS);
		
		return new Pair(Tmin, minS);
	}
	
	private List<List<Point2D.Double>> resample(List<List<Point2D.Double>> strokes, int n) {
		
		// set the variable for point spacing
		// initialize the variable for total distance
		// initialize list for new strokes
		final double I = pathLength(strokes) / (n-1);
		double D = 0.0;
		List<List<Point2D.Double>> newStrokes = new ArrayList<List<Point2D.Double>>();
		
		// iterate through each stroke points in a list of strokes
		for (List<Point2D.Double> points : strokes) {
			
			// initialize list of resampled stroke points
			// add the first stroke point
			List<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
			newPoints.add(points.get(0));
			
			//
			for (int i = 1; i < points.size(); ++i) {
				
				double d = distance(points.get(i-1), points.get(i));
				if (D+d >= I) {
					
					double qx = points.get(i-1).x + ((I-D)/d)*(points.get(i).x - points.get(i-1).x);
					double qy = points.get(i-1).y + ((I-D)/d)*(points.get(i).y - points.get(i-1).y);
					Point2D.Double q = new Point2D.Double(qx, qy);
					newPoints.add(q);
					points.add(i, q);
					D = 0.0;
				}
				else {
					
					D += d;
				}
			}
			D = 0.0;
			
			newStrokes.add(newPoints);
		}
		
		return newStrokes;
	}
	
	private List<List<Point2D.Double>> scaleTo(List<List<Point2D.Double>> strokes, int size) {
		
		List<List<Point2D.Double>> newStrokes = new ArrayList<List<Point2D.Double>>();
		
		//
		List<Point2D.Double> allPoints = new ArrayList<Point2D.Double>();
		for (List<Point2D.Double> points : strokes) {
			
			for (Point2D.Double p : points)
				allPoints.add(p);
		}
		BoundingBox B = boundingBox(allPoints);
		
		//
		for (List<Point2D.Double> points : strokes) {
			
			List<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
			
			for (Point2D.Double p : points) {
				
				double qx = p.x * size / B.width();
				double qy = p.y * size / B.height();
				Point2D.Double q = new Point2D.Double(qx, qy);
				newPoints.add(q);
			}
			
			newStrokes.add(newPoints);
		}
		
		return newStrokes;
	}
	
	private List<List<Point2D.Double>> translateTo(List<List<Point2D.Double>> strokes, Point2D.Double k) {
		
		List<List<Point2D.Double>> newStrokes = new ArrayList<List<Point2D.Double>>();
		
		//
		List<Point2D.Double> allPoints = new ArrayList<Point2D.Double>();
		for (List<Point2D.Double> points : strokes) {
			
			for (Point2D.Double p : points)
				allPoints.add(p);
		}
		Point2D.Double c = centroid(allPoints);

		for (List<Point2D.Double> points : strokes) {
					
			List<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
			
			for (Point2D.Double p : points) {
				
				double qx = p.x + k.x - c.x;
				double qy = p.y + k.y - c.y;
				Point2D.Double q = new Point2D.Double(qx, qy);
				newPoints.add(q);
			}
			
			newStrokes.add(newPoints);
		}
		
		return newStrokes;
	}
	
	private double pathLength(List<List<Point2D.Double>> A) {
		
		double d = 0.0;
		for (List<Point2D.Double> points : A) {
			
			d += pathLengthPoints(points);
		}
		
		return d;
	}
	
	private double pathLengthPoints(List<Point2D.Double> A) {
		
		double d = 0.0;
		for (int i = 1; i < A.size(); ++i) {
			
			d += distance(A.get(i-1), A.get(i));
		}
		
		return d;
	}
	
	private double distance(Point2D.Double a, Point2D.Double b) {
	
		double distance = Math.sqrt( (b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y) );
		
		return distance;
	}
	
	private double distance(List<List<Point2D.Double>> A, List<List<Point2D.Double>> B) {
		
		//
		List<Point2D.Double> original = new ArrayList<Point2D.Double>();
		List<Point2D.Double> template = new ArrayList<Point2D.Double>();
		for (List<Point2D.Double> points : A)
			for (Point2D.Double point : points)
				original.add(point);
		for (List<Point2D.Double> points : B)
			for (Point2D.Double point : points)
				template.add(point);
		
		double d = 0.0;
		for (Point2D.Double point : original) {
			
			double minD = Double.MAX_VALUE;
			for (Point2D.Double other : template) {
				
				double currD = distance(point, other);
				if (currD < minD)
					minD = currD;
			}
			d += minD;
		}
		
		return d;
	}
	
	private BoundingBox boundingBox(List<Point2D.Double> points) {
		
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		for (Point2D.Double p : points) {
			
			if (p.x < minX)
				minX = p.x;
			
			else if (p.x > maxX)
				maxX = p.x;
			
			if (p.y < minY)
				minY = p.y;
			
			else if (p.y > maxY)
				maxY = p.y;
		}
		
		return new BoundingBox(minX, minY, maxX, maxY);
	}

	private Point2D.Double centroid(List<Point2D.Double> points) {
		
		double meanX = 0.0;
		double meanY = 0.0;
		for (Point2D.Double point : points) {
			meanX += point.x;
			meanY += point.y;
		}
		meanX /= points.size();
		meanY /= points.size();
		
		return new Point2D.Double(meanX, meanY);
	}
	
	
	
	public static final int N = 64;
	public static final int SIZE = 250;
	public static final Point2D.Double K = new Point2D.Double(0.0, 0.0);
}