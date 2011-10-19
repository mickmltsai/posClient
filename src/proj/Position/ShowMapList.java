package proj.Position;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import ntu.com.google.zxing.client.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ShowMapList extends Activity {
	private TextView title;
	private ListView list;
	private File[] fileList;
	private ArrayList<String> mapIdMapping;
	private JSONObject jsonObj;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maplist);

		findViews();
		showTitle();
		listMap();

	}

	private void findViews() {
		title = (TextView) findViewById(R.id.mapListTiltle);
		list = (ListView) findViewById(R.id.mapListView);
	}

	private void showTitle() {
		title.setText("請選擇地圖");
	}

	private void listMap() {
		ArrayList<String> mStrings = new ArrayList<String>();

		mapIdMapping = new ArrayList<String>();

		File rootDir = new File(Global.SDPathRoot + "/" + Global.MapDirName + "/");
		fileList = rootDir.listFiles();

		// Adapt map's title
		for (int j = 0; j < fileList.length; j++) {
			if (fileList[j].isDirectory()) {
				try {
					jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + fileList[j].getName() + "/" + fileList[j].getName() + ".json"));

					if (jsonObj.getString("title") == null || jsonObj.getString("title").equals("")) {
						mStrings.add("無地圖名稱");
					} else {
						mStrings.add(jsonObj.getString("title"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mapIdMapping.add(fileList[j].getName());
			}

		}

		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings));

		// Handle after click map item
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				try {
					jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + mapIdMapping.get(arg2) + "/" + mapIdMapping.get(arg2) + ".json"));
					Global.MapTitle = jsonObj.getString("title");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Global.MapId = mapIdMapping.get(arg2);
				Global.PointId = null;
				Global.PointTitle = null;

				Intent intent = new Intent();
				setResult(RESULT_OK, intent);

				// ↓ Already do in PositionActivity showMapData()↓
				// Copy last downloaded JSON file in to map dir root

				// File srcFile = new File(Global.SDPathRoot + "/" +
				// Global.MapDirName + "/" + mapIdMapping.get(arg2) + "/" +
				// mapIdMapping.get(arg2) + ".json");
				// File dstFile = new File(Global.SDPathRoot + "/" +
				// Global.MapDirName + "/" + "last.json");
				//
				// BufferedInputStream in;
				// try {
				// in = new BufferedInputStream(new FileInputStream(srcFile));
				//
				// BufferedOutputStream out = new BufferedOutputStream(new
				// FileOutputStream(dstFile));
				//
				// byte[] tmp = new byte[1024];
				//
				// while (in.read(tmp) != -1) {
				// out.write(tmp);
				// }
				// in.close();
				// out.close();
				// } catch (FileNotFoundException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				finish();
			}
		});

	}
}