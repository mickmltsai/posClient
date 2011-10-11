package proj.Position;

import java.io.File;

public class LookHelper {
	public Boolean look(String filePath, String fileName) {

		File lookFilePath = new File(filePath + fileName);

		return lookFilePath.exists();
	}
}