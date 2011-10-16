package proj.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class JsonParser {

	public String getJsonRespon(String jsonPath) {
		String jsonString = "";

		try {

			File jsonFile = new File(jsonPath);
			// oldJsonPath = jsonPath;

			FileReader in = new FileReader(jsonFile);
			BufferedReader stdin = new BufferedReader(in);

			String jsonString1 = null;
			while (((jsonString1 = stdin.readLine()) != null)) {
				jsonString = jsonString + jsonString1;
			}
			in.close();

		} catch (Exception e) {
			// TODO: handle exception
		}

		return jsonString;

	}

}
