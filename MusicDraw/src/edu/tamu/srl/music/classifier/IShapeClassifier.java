package edu.tamu.srl.music.classifier;

import java.util.List;

public interface IShapeClassifier {

	public boolean classify(List<IShape> originals);
	public List<IShape> getResult();
}
