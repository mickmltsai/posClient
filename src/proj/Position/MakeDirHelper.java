package proj.Position;

import java.io.File;

public class MakeDirHelper {
	public void make(String filePath, String fileName) {

		File makeFilePath = new File(filePath + fileName);

		makeFilePath.mkdir();
	}
}
