package proj.Position;

import java.io.File;

import android.os.Environment;

public class MakeDirHelper {
	public void make(String filePath, String fileName) {
		File SDCardRoot = Environment.getExternalStorageDirectory();
		File makeFilePath = new File(SDCardRoot + filePath + fileName);

		makeFilePath.mkdir();
	}
}
