package proj.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import ntu.com.google.zxing.client.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

		File rootDir = new File(Global.SDPathRoot + "/" + Global.MapDirName + "/");
		fileList = rootDir.listFiles();

		// Check if there is no map
		int count = 0;
		for (int j = 0; j < fileList.length; j++) {
			if (fileList[j].isDirectory()) {
				count = count + 1;
			}

		}

		if (count == 0) {
			title.setText("無任何地圖");
		} else {
			ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

			ArrayList<String> mStringsTitle = new ArrayList<String>();
			ArrayList<String> mStringsVer = new ArrayList<String>();

			mapIdMapping = new ArrayList<String>();

			// Adapt map's title and ver.
			for (int j = 0; j < fileList.length; j++) {
				if (fileList[j].isDirectory()) {
					try {
						jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + fileList[j].getName() + "/" + fileList[j].getName() + ".json"));

						if (jsonObj.getString("title") == null || jsonObj.getString("title").equals("")) {
							mStringsTitle.add("無地圖名稱");
						} else {
							mStringsTitle.add(jsonObj.getString("title"));
						}
						mStringsVer.add("版本號: "+jsonObj.getString("mapVer"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mapIdMapping.add(fileList[j].getName());
				}

			}

			for (int i = 0; i < mStringsTitle.size(); i++) {
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("mapTitle", mStringsTitle.get(i));
				item.put("mapVer", mStringsVer.get(i));
				data.add(item);
			}

			list.setAdapter(new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, new String[] { "mapTitle", "mapVer" }, new int[] { android.R.id.text1, android.R.id.text2 }));
			// set filter
			list.setTextFilterEnabled(true);
			
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

					finish();
				}
			});

		}

	}
}