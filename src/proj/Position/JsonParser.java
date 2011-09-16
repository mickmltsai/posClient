package proj.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.JSONObject;

import android.os.Environment;

public class JsonParser {

	static File SDCardRoot = Environment.getExternalStorageDirectory();
	static String oldJsonPath = null;

	public static String getJsonRespon(String jsonPath) {
		
		if (jsonPath.equals(jsonPath)) {
			
			try {
				
		
			File SD = Environment.getExternalStorageDirectory();
			File test = new File(SD + "/Position/" + Global.MapId + "/"
					+ Global.MapId + ".json");

			FileReader in = new FileReader(test);
			BufferedReader stdin = new BufferedReader(in);

			String jsonString = "";
			String jsonString1 = null;
			while (((jsonString1 = stdin.readLine()) != null)) {
				jsonString = jsonString + jsonString1;
			}
			in.close();

			JSONObject jsonObj = new JSONObject(jsonString);
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			
			return null;
		} else {

			return oldJsonPath;
		}

	}

}
