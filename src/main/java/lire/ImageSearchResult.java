package lire;

import java.io.File;
import java.io.Serializable;

public class ImageSearchResult implements Serializable {
	private String imagePath;
	private float score;
	private File file;

	public ImageSearchResult() {
	}

	public ImageSearchResult(String imagePath, float score) {
		this.imagePath = imagePath;
		this.score = score;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
