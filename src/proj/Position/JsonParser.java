package proj.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.os.Environment;
import android.util.Log;

public class JsonParser {

	private final static File SDCardRoot = Environment
			.getExternalStorageDirectory();
	private static String oldJsonPath = "";
	public static String jsonString = "";

	public static String getJsonRespon(String jsonPath) {

		if (jsonPath.equals(oldJsonPath)) {
			// Return the previous Json string if they equal
			return jsonString;

		} else {

			try {

				File jsonFile = new File(SDCardRoot + "/" + jsonPath);
				oldJsonPath = "/" + jsonPath;

				FileReader in = new FileReader(jsonFile);
				BufferedReader stdin = new BufferedReader(in);

				String jsonString1 = null;
				while (((jsonString1 = stdin.readLine()) != null)) {
					jsonString = jsonString + jsonString1;
				}
				in.close();
				return jsonString;
			} catch (Exception e) {
				// TODO: handle exception
			}

			return jsonString;
		}

	}

}
