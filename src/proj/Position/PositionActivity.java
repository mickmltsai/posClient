package proj.Position;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ntu.com.google.zxing.client.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PositionActivity extends Activity {

	private MyWebView mapView;
	private Button scan;

	private TextView pointTitle;
	private String jsonURL;
	private String mapID;
	private String mapVer;
	private String pointID;
	private String title;

	private Thread thread;
	private ProgressDialog waitDownDialog;

	// Use for download handler message code
	private interface messageCode {
		public static final int DOWNLOAD_OK = 0;
		public static final int DOWNLOAD_FAILED = 1;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViews();
		setListeners();
		makeRootDir();
		pointTitle.setTextColor(Color.RED);
		pointTitle.setTextSize(35);
		// showLastMapData();
		startScan();

	}

	private void findViews() {
		pointTitle = (TextView) findViewById(R.id.plainText);

		mapView = (MyWebView) findViewById(R.id.myWebView1);
		// Enable to zoom in/out
		mapView.getSettings().setBuiltInZoomControls(true);
		// Enable to zoom in/out by double tap
		mapView.getSettings().setUseWideViewPort(true);

		scan = (Button) findViewById(R.id.scan);
	}

	private void setListeners() {
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startScan();
			}
		});
	}

	private void startScan() {
		Intent intent = new Intent("ntu.com.google.zxing.client.android.SCAN");
		intent.setPackage("ntu.com.google.zxing.client.android");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				Boolean isNeedUpdate;

				try {
					String[] contentArray = contents.split("\\?");
					String[] contentPart = contentArray[1].split("&");

					// Parse QR code content and assign value
					jsonURL = contents + "&from=client";

					String[] tmp = contentPart[0].split("=");
					mapID = tmp[1];
					tmp = contentPart[1].split("=");
					mapVer = tmp[1];
					tmp = contentPart[2].split("=");
					pointID = tmp[1];
					tmp = contentPart[3].split("=");
					title = tmp[1];

					// Show title
					pointTitle.setText(title);

					// Assign point id to global variable
					Global.PointId = pointID;

					// Check whether it need to download Json file
					isNeedUpdate = !checkMapVerUpdate(mapID, mapVer);

					// Need to Update
					if (isNeedUpdate) {
						waitDownDialog = new ProgressDialog(PositionActivity.this);
						waitDownDialog.setTitle("下載中!");
						waitDownDialog.setMessage("請稍等...");
						waitDownDialog.show();
						// Start download Json file
						thread = new Thread(new Runnable() {

							public void run() {

								try {

									downloadMapJson(mapID, jsonURL);

									JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/last.json"));

									String map = jsonObj.getString("map");
									DownloadHelper downloader = new DownloadHelper();
									// ======================================================================================
									downloader.downFile(map, Global.MapDirRoot + "/" + mapID, "map.jpg");
									// ======================================================================================
									// ==================================

									// Download point's imgs

									// ==================================
									handler.sendEmptyMessage(messageCode.DOWNLOAD_OK);
								} catch (MalformedURLException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
									handler.sendEmptyMessage(messageCode.DOWNLOAD_FAILED);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						});
						thread.start();
					} else {

						// Do follow by
						// handler.sendEmptyMessage(messageCode.DOWNLOAD_OK); ??
						// But waitDownDialog.dismiss(); in scanResultOk() !!

						showMapData(mapID);

						File SD = Environment.getExternalStorageDirectory();
						File test = new File(SD + "/Position/" + mapID + "/" + mapID + ".json");

						FileReader in = new FileReader(test);
						BufferedReader stdin = new BufferedReader(in);

						String jsonString = "";
						String jsonString1 = null;
						while (((jsonString1 = stdin.readLine()) != null)) {
							jsonString = jsonString + jsonString1;
						}
						in.close();

						JSONObject jsonObj = new JSONObject(jsonString);
						JSONArray jsonObjArray = jsonObj.getJSONArray("points");

						// JSONArray jsonPhoto = jsonObjArray.getJSONObject(
						// Integer.valueOf(Global.PointId)).getJSONArray(
						// "photos");
						JSONObject jsonObjCoordObject;
						// =================================================================================================
						String title = "";
						String desc = "";
						Boolean tag = false;
						for (int i = 0; i < jsonObjArray.length(); i++) {

							jsonObjCoordObject = jsonObjArray.getJSONObject(i);

							if (jsonObjCoordObject.getString("pointID").equals(Global.PointId)) {

								title = jsonObjCoordObject.getString("title");
								desc = jsonObjCoordObject.getString("description");
								tag = true;
								break;
							}
						}

						// =================================================================================================

						LayoutInflater factory = LayoutInflater.from(this);
						final View v1 = factory.inflate(R.layout.contentview, null);

						Builder gg = new AlertDialog.Builder(PositionActivity.this);
						TextView titlet = (TextView) v1.findViewById(R.id.ttt);
						if (tag) {
							gg.setTitle(title);
							// gg.setMessage(desc);

							titlet.setText(desc);

						} else {
							gg.setTitle("抱歉");
							titlet.setText("此位置已被刪除!");
						}

						gg.setPositiveButton("確認", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub

							}
						});

						titlet.setTextSize(20);
						titlet.setTextColor(Color.YELLOW);
						gg.setView(v1);
						gg.show();

					}

				} catch (Exception e) {
					// Handle when scan QR code which is not our form
					showLastMapData();
					// pointTitle.setText("QR code error!");
					pointTitle.setText("條碼錯誤!");
				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel

				// Show AlertDialog for temporarily handle (Need to show last
				// map)
				/*
				 * Builder scanCancelDialog = new
				 * AlertDialog.Builder(PositionActivity.this);
				 * scanCancelDialog.setMessage("請按確認繼續...");
				 * scanCancelDialog.setTitle("掃描取消!").setPositiveButton("確認",
				 * new DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface dialog, int
				 * which) { // TODO Auto-generated method stub
				 * 
				 * } }); scanCancelDialog.show();
				 */
				// Show last map data
				showLastMapData();

			}
		}
	}

	private void makeRootDir() {
		// Make root dir if not in SD card yet

		Boolean isExistent;
		LookHelper looker = new LookHelper();
		isExistent = looker.look(Global.SDPathRoot, Global.MapDirName);

		if (!isExistent) {
			MakeDirHelper maker = new MakeDirHelper();
			maker.make(Global.SDPathRoot, Global.MapDirName);
		}

	}

	private Boolean searchMapDir(String mapId) {
		// Check whether the Map dir exists or not

		LookHelper looker = new LookHelper();

		return looker.look(Global.MapDirRoot, mapId);
	}

	private void downloadMapJson(String mapId, String downHttpUrl) throws IOException {
		// Download the JSON file

		DownloadHelper downloader = new DownloadHelper();

		File SDCardRoot = Environment.getExternalStorageDirectory();
		MakeDirHelper def = new MakeDirHelper();
		def.make(Global.MapDirRoot, mapId);

		downloader.downFile(downHttpUrl, Global.MapDirRoot + "/" + mapId, mapId + ".json");

		// Copy last downloaded JSON file in to map dir root
		File abc = new File(SDCardRoot + "/" + Global.MapDirRoot + "/" + mapId + "" + "/" + mapId + ".json");
		File dstFile = new File(SDCardRoot + "/" + Global.MapDirRoot + "/" + "last.json");

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(abc));
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dstFile));

		byte[] tmp = new byte[1024];

		while (in.read(tmp) != -1) {
			out.write(tmp);
		}
		in.close();
		out.close();

	}

	private Boolean checkMapVerUpdate(String mapId, String mapVer) {
		// Return false when it need to be updated

		Boolean result = false;

		File SD = Environment.getExternalStorageDirectory();
		File test = new File(SD + "/Position/" + mapId + "/" + mapId + ".json");
		try {
			FileReader in = new FileReader(test);
			BufferedReader stdin = new BufferedReader(in);

			String jsonString = "";
			String jsonString1 = null;
			while (((jsonString1 = stdin.readLine()) != null)) {
				jsonString = jsonString + jsonString1;
			}
			in.close();

			JSONObject jsonObj = new JSONObject(jsonString);

			int verNow = jsonObj.getInt("mapVer");

			if (verNow >= Integer.valueOf(mapVer)) {
				result = true;
			}
			// pointTitle.setText(verNow+" "+Integer.valueOf(mapVer));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
			// pointTitle.setText("No last map data!(FileNotFoundException)");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
			// pointTitle.setText("No last map data!(JSONException)");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// pointTitle.setText("No last map data!(IOException)");
		}

		return result;
	}

	private Boolean checkOldMapData(String MapId) {
		// Return true if there is old map data
		LookHelper look = new LookHelper();
		return look.look(Global.MapDirRoot, MapId);
	}

	private void showMapData(String mapId) {

		File SDCardRoot = Environment.getExternalStorageDirectory();
		// Copy last downloaded JSON file in to map dir root
		File abc = new File(SDCardRoot + "/" + Global.MapDirRoot + "/" + mapId + "/" + mapId + ".json");
		File dstFile = new File(SDCardRoot + "/" + Global.MapDirRoot + "/" + "last.json");

		try {

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(abc));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dstFile));

			byte[] tmp = new byte[1024];

			while (in.read(tmp) != -1) {
				out.write(tmp);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			pointTitle.setText("showMapData error!");
		}

		// Enable to zoom in/out

		mapView.getSettings().setBuiltInZoomControls(true);
		// Enable to zoom in/out by double tap
		mapView.getSettings().setUseWideViewPort(true);

		// Bind the image path
		// String data =
		// "<img src = \"file:///android_res/drawable/abc.png\" />";
		// String data =
		// "<img src = \"http:///indoorposition.appspot.com/image/map.jpg\" />";

		// String data = "<img src = \"file:///sdcard/somefile.jpg\" />";
		String data = "<img src = \"file:///sdcard/" + Global.MapDirName + "/" + mapId + "/" + "map.jpg" + "\" />";

		final String mimeType = "text/html";
		final String encoding = "utf-8";

		// Set map id
		Global.MapId = mapId;
		// Show map IMG
		mapView.loadDataWithBaseURL("about:blank", data, mimeType, encoding, "");
		// Update the touch points data (touchevent and onDraw method)
		mapView.invalidate();

	}

	private void showLastMapData() {
		// Show last Map data if it exists

		File SDCardRoot = Environment.getExternalStorageDirectory();
		File lastJson = new File(SDCardRoot + "/" + "Position" + "/" + "last.json");
		FileReader in;

		try {
			in = new FileReader(lastJson);
			BufferedReader stdin = new BufferedReader(in);

			String jsonString = "";
			String jsonString1 = null;
			while (((jsonString1 = stdin.readLine()) != null)) {
				jsonString = jsonString + jsonString1;
			}
			in.close();

			JSONObject jsonObj = new JSONObject(jsonString);
			String mapId = jsonObj.getString("mapID");

			showMapData(mapId);
			pointTitle.setText("最近的地圖!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pointTitle.setText("沒有最近的地圖!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pointTitle.setText("沒有最近的地圖!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			pointTitle.setText("沒有最近的地圖!");
		}

	}

	private void scanResultOk() {
		// Show map data when download finished
		showMapData(mapID);
		waitDownDialog.dismiss();

		File SD = Environment.getExternalStorageDirectory();
		File test = new File(SD + "/Position/" + mapID + "/" + mapID + ".json");
		try {

			FileReader in = new FileReader(test);
			BufferedReader stdin = new BufferedReader(in);

			String jsonString = "";
			String jsonString1 = null;
			while (((jsonString1 = stdin.readLine()) != null)) {
				jsonString = jsonString + jsonString1;
			}
			in.close();

			JSONObject jsonObj = new JSONObject(jsonString);
			JSONArray jsonObjArray = jsonObj.getJSONArray("points");

			// JSONArray jsonPhoto = jsonObjArray.getJSONObject(
			// Integer.valueOf(Global.PointId)).getJSONArray(
			// "photos");
			JSONObject jsonObjCoordObject;
			// ===================================================================

			// ===================================================================
			String title = "";
			String desc = "";
			for (int i = 0; i < jsonObjArray.length(); i++) {

				jsonObjCoordObject = jsonObjArray.getJSONObject(i);

				if (jsonObjCoordObject.getString("pointID").equals(Global.PointId)) {

					title = jsonObjCoordObject.getString("title");
					desc = jsonObjCoordObject.getString("description");
				}
			}

			LayoutInflater factory = LayoutInflater.from(PositionActivity.this);
			final View v1 = factory.inflate(R.layout.contentview, null);

			Builder gg = new AlertDialog.Builder(PositionActivity.this);
			gg.setTitle(title);
			// gg.setMessage(desc);
			gg.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});
			TextView titlet = (TextView) v1.findViewById(R.id.ttt);

			titlet.setTextSize(20);
			titlet.setTextColor(Color.YELLOW);
			titlet.setText(desc);

			gg.setView(v1);

			gg.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void scanResultfailed() {
		// Show failed dialog when download faileds
		waitDownDialog.dismiss();
		Builder ggg = new AlertDialog.Builder(PositionActivity.this);
		ggg.setMessage("請按確認繼續...");
		ggg.setTitle("下載失敗!").setPositiveButton("確認", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		ggg.show();
	}

	private void cleanFailFile() {

	}

	private Handler handler = new Handler() {
		Boolean isOldMapData;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case messageCode.DOWNLOAD_OK:
				scanResultOk();

				break;
			case messageCode.DOWNLOAD_FAILED:
				scanResultfailed();
				break;
			}

			// Show old map data if it exist otherwise show last map
			isOldMapData = checkOldMapData(mapID);
			if (isOldMapData) {
				showMapData(mapID);
			} else {
				showLastMapData();
			}

			super.handleMessage(msg);
		}
	};

}