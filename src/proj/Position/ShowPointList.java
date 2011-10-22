package proj.Position;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ntu.com.google.zxing.client.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShowPointList extends Activity {

	private TextView title;
	private ListView list;
	private JSONArray jsonObjArray;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pointlist);

		findViews();
		showTitle();
		listPoint();

	}

	private void findViews() {
		title = (TextView) findViewById(R.id.pointListTiltle);
		list = (ListView) findViewById(R.id.pointListView);
	}

	private void showTitle() {
		try {
			JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));

			jsonObj.getString("title");

			if (jsonObj.getString("title") != null && !jsonObj.getString("title").equals("")) {
				Global.MapTitle = jsonObj.getString("title");
				title.setText(Global.MapTitle + " 之各定位點");
			} else {
				title.setText("本地之各定位點");
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listPoint() {
		ArrayList<String> mStrings = new ArrayList<String>();

		if (Global.MapId == null) {
			title.setText("請先選擇地圖!");
		} else {
			// Adapt point's title
			try {
				JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));
				jsonObjArray = jsonObj.getJSONArray("points");

				for (int i = 0; i < jsonObjArray.length(); i++) {

					mStrings.add(jsonObjArray.getJSONObject(i).getString("title"));
				}

				list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings));

				// set filter
				list.setTextFilterEnabled(true);
				
				// Handle after click point item
				list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						try {

							Global.PointId = jsonObjArray.getJSONObject((int) arg0.getItemIdAtPosition((int) arg3)).getString("pointID");
							Global.PointTitle = jsonObjArray.getJSONObject((int) arg0.getItemIdAtPosition((int) arg3)).getString("title");

							Intent intent = new Intent();
							setResult(RESULT_OK, intent);

							finish();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
