package proj.Position;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PositionActivity extends Activity {

	private MyWebView mapView;
	private ImageButton btnScan;

	private TextView pointTitleText;
	private TextView mapTitleText;

	private String contentJsonUrl;
	private String contentMapId;
	private String contentMapVer;
	private String contentPointId;
	private String contentPotintTitle;
	private String lastPointId;
	private SharedPreferences settings;

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
		pointTitleText.setTextColor(Color.RED);
		btnScan.setBackgroundResource(R.drawable.scanicon);
		settings = getPreferences(MODE_PRIVATE);

		if (checkSdCard()) {
			// Check SD card exist

			setListeners();
			makeRootDir();
			showEmptyMap();

			startScan();

		} else {

			Builder noSdCardDialog = new AlertDialog.Builder(this);
			noSdCardDialog.setTitle("請插入記憶卡!");
			noSdCardDialog.setMessage("程式即將關閉...");
			noSdCardDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					finish();

				}
			});
			noSdCardDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

					if ((keyCode == KeyEvent.KEYCODE_SEARCH) || (keyCode == KeyEvent.KEYCODE_BACK)) {

						finish();
						return true;

					} else {

						return false;

					}

				}
			});
			noSdCardDialog.show();

		}

	}

	private Boolean checkSdCard() {

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
			return false;
		} else {
			return true;
		}

	}

	private void findViews() {
		pointTitleText = (TextView) findViewById(R.id.pointTitleText);
		mapTitleText = (TextView) findViewById(R.id.mapTitleText);

		mapView = (MyWebView) findViewById(R.id.mainMapView);
		// Enable to zoom in/out
		mapView.getSettings().setBuiltInZoomControls(true);
		// Enable to zoom in/out by double tap
		mapView.getSettings().setUseWideViewPort(true);

		btnScan = (ImageButton) findViewById(R.id.scanBtn);

	}

	private void setListeners() {
		btnScan.setOnClickListener(new OnClickListener() {

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
		// Here are three requestCodes now (0,1,2), scan, point list, map list

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				Boolean isNeedUpdate;

				try {
					String[] contentArray = contents.split("\\?");
					String[] contentPart = contentArray[1].split("&");

					// Parse QR code content and assign value
					contentJsonUrl = contents + "&from=client";

					String[] tmp = contentPart[0].split("=");
					contentMapId = tmp[1];
					tmp = contentPart[1].split("=");
					contentMapVer = tmp[1];
					tmp = contentPart[2].split("=");
					contentPointId = tmp[1];
					tmp = contentPart[3].split("=");
					contentPotintTitle = tmp[1];

					// Assign map id to global variable
					Global.MapId = contentMapId;
					// Assign point id to global variable
					Global.PointId = contentPointId;
					// Assign title id to global variable
					Global.PointTitle = contentPotintTitle;

					// Assign point id preferences
					settings.edit().putString(Global.MapId, Global.PointId).commit();

					// Check whether it need to download JSON file
					isNeedUpdate = !checkMapVerUpdate(Global.MapId, contentMapVer);

					// ========================================================================
					// If want to separate Map img and JSON file, separate
					// mapVer into 2 parts
					// And separate download thread here (Need to Update...)
					// isNeedUpdate need to be updated
					// ========================================================================

					// Need to Update
					if (isNeedUpdate) {
						waitDownDialog = new ProgressDialog(PositionActivity.this);
						waitDownDialog.setTitle("下載中!");
						waitDownDialog.setMessage("請稍等...");
						waitDownDialog.setCancelable(false);
						waitDownDialog.show();

						// Start to download updated file
						thread = new Thread(new Runnable() {

							public void run() {

								try {

									downloadMapJson(Global.MapId, contentJsonUrl);

									JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/last.json"));
									JSONArray jsonObjArray;

									String map = jsonObj.getString("map");
									DownloadHelper downloader = new DownloadHelper();

									LookHelper look = new LookHelper();

									if (!look.look(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/", "map")) {
										// Check does it need to download map
										// image

										downloader.downFile(map, Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/", "map");
									}

									jsonObjArray = jsonObj.getJSONArray("points");

									//Do fast show img?=====================================================================================================
									try {
										// Download point's images
										for (int i = 0; i < jsonObjArray.length(); i++) {
											if (!jsonObjArray.getJSONObject(i).getString("photo").equals("")) {
												downloader.downFile(jsonObjArray.getJSONObject(i).getString("photo"), Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/", jsonObjArray.getJSONObject(i).getString("pointID"));
											}
										}
									} catch (Exception e) {
										// Still show map information even the point's images download failed
									}
									//Do fast show img?=====================================================================================================
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
						// Not need to Update

						JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));

						// Assign MapTitle
						Global.MapTitle = jsonObj.getString("title");

						showMapData(Global.MapId);
						showPointInfo(jsonObj, Global.PointId);

					}

				} catch (Exception e) {
					// Handle when scan QR code which is not our form

					// Assign point id to global variable
					Global.PointId = null;
					// Assign point title to global variable
					Global.PointTitle = null;

					showLastMapData();

					Builder QRerrorDialog = new AlertDialog.Builder(PositionActivity.this);
					QRerrorDialog.setTitle("條碼格式錯誤!").setMessage("請按確認繼續...").setPositiveButton("確認", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
					QRerrorDialog.show();

				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel

				// Assign point id to global variable
				Global.PointId = null;
				// Assign point title to global variable
				Global.PointTitle = null;

				// Show last map data
				showLastMapData();

			}
		}

		// After click point list's item
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {

				showMapData(Global.MapId);

				try {

					JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));
					showPointInfo(jsonObj, Global.PointId);

				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		// After click map list's item
		if (requestCode == 2) {
			if (resultCode == RESULT_OK) {

				showMapData(Global.MapId);

			}
		}

	}

	private void makeRootDir() {
		// Make root dir if not in SD card yet

		Boolean isExistent;
		LookHelper looker = new LookHelper();
		isExistent = looker.look(Global.SDPathRoot + "/", Global.MapDirName);

		if (!isExistent) {
			MakeDirHelper maker = new MakeDirHelper();
			maker.make(Global.SDPathRoot + "/", Global.MapDirName);
		}

	}

	private void downloadMapJson(String mapId, String downHttpUrl) throws IOException {
		// Download the JSON file

		DownloadHelper downloader = new DownloadHelper();

		MakeDirHelper maker = new MakeDirHelper();
		maker.make(Global.SDPathRoot + "/" + Global.MapDirName + "/", mapId);

		downloader.downFile(downHttpUrl, Global.SDPathRoot + "/" + Global.MapDirName + "/" + mapId + "/", mapId + ".json");

		// Copy last downloaded JSON file in to map dir root
		File srcFile = new File(Global.SDPathRoot + "/" + Global.MapDirName + "/" + mapId + "/" + mapId + ".json");
		File dstFile = new File(Global.SDPathRoot + "/" + Global.MapDirName + "/" + "last.json");

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(srcFile));
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

		try {

			JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));

			int verNow = jsonObj.getInt("mapVer");

			if (verNow >= Integer.valueOf(mapVer)) {
				result = true;
			}

		} catch (JSONException e) {

			e.printStackTrace();
			result = false;

		}

		return result;
	}

	private Boolean checkOldMapData(String MapId) {
		// Return true if there is old map data
		LookHelper look = new LookHelper();
		return look.look(Global.SDPathRoot + "/" + Global.MapDirName + "/" + MapId + "/", MapId + ".json");
	}

	private void showEmptyMap() {
		// In order to Focus when fist scan

		String data = "<body style=\"margin:0;\"><img src = \"file:///android_res/drawable/emptymap.png\"/></body>";

		final String mimeType = "text/html";
		final String encoding = "utf-8";
		mapView.loadDataWithBaseURL("about:blank", data, mimeType, encoding, "");
	}

	private void showChooseMap() {

		mapTitleText.setText("無選擇地圖");
		pointTitleText.setText(null);

		String data = "<body style=\"margin:0;\"><img src = \"file:///android_res/drawable/choosemap.png\"/></body>";

		final String mimeType = "text/html";
		final String encoding = "utf-8";
		mapView.loadDataWithBaseURL("about:blank", data, mimeType, encoding, "");
	}

	private void showMapData(String mapId) {

		// Show map title
		if (Global.MapTitle == null || Global.MapTitle.equals("")) {
			mapTitleText.setText("無地圖名稱");
		} else {
			mapTitleText.setText(Global.MapTitle);
		}

		// Show point title
		pointTitleText.setText(Global.PointTitle);

		// Copy last downloaded JSON file into map dir root
		File srcFile = new File(Global.SDPathRoot + "/" + Global.MapDirName + "/" + mapId + "/" + mapId + ".json");
		File dstFile = new File(Global.SDPathRoot + "/" + Global.MapDirName + "/" + "last.json");

		try {

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(srcFile));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dstFile));

			byte[] tmp = new byte[1024];

			while (in.read(tmp) != -1) {
				out.write(tmp);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// Enable to zoom in/out

		mapView.getSettings().setBuiltInZoomControls(true);
		// Enable to zoom in/out by double tap
		mapView.getSettings().setUseWideViewPort(true);

		// Bind the image path

		// String data = "<img src = \"file:///sdcard/somefile.jpg\" />";
		String data = "<body style=\"margin:0;\"><img src = \"file:///sdcard/" + Global.MapDirName + "/" + mapId + "/" + "map" + "\"/></body>";

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

		try {

			JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + "last.json"));

			// Assign MapTitle
			Global.MapTitle = jsonObj.getString("title");

			String mapId = jsonObj.getString("mapID");

			showMapData(mapId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Builder noMapDialog = new AlertDialog.Builder(PositionActivity.this);
			noMapDialog.setTitle("無任何地圖!").setMessage("請按確認繼續...").setPositiveButton("確認", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			noMapDialog.show();

		}

	}

	private void showPointInfo(JSONObject jsonObj, String pointId) {
		// JSONArray jsonPhoto = jsonObjArray.getJSONObject(
		// Integer.valueOf(Global.PointId)).getJSONArray(
		// "photos");
		// ===================================================================

		// ===================================================================

		JSONArray jsonObjArray;
		JSONObject jsonObjCoordObject;

		try {
			jsonObjArray = jsonObj.getJSONArray("points");
			JSONObject jsonObjCoordJsonObject;

			// Focus
			int x = 0, y = 0;
			for (int i = 0; i < jsonObjArray.length(); i++) {
				if (jsonObjArray.getJSONObject(i).getString("pointID").equals(pointId)) {
					jsonObjCoordJsonObject = jsonObjArray.getJSONObject(i).getJSONObject("coord");
					x = jsonObjCoordJsonObject.getInt("x");
					y = jsonObjCoordJsonObject.getInt("y");
					break;
				}
			}

			if (pointId != null) {
				mapView.focusPoint(x, y);
				mapView.invalidate();
			}

			// Show point's information
			String title = "";
			String desc = "";

			// For check deleted point(non found)
			Boolean tag = false;

			for (int i = 0; i < jsonObjArray.length(); i++) {

				jsonObjCoordObject = jsonObjArray.getJSONObject(i);

				if (jsonObjCoordObject.getString("pointID").equals(pointId)) {

					title = jsonObjCoordObject.getString("title");
					desc = jsonObjCoordObject.getString("description");
					if (desc.equals("null") || desc.equals("")) {
						desc = "此地點尚無描述!";
					}
					tag = true;
					break;
				}
			}

			LayoutInflater factory = LayoutInflater.from(PositionActivity.this);
			final View v = factory.inflate(R.layout.contentview, null);

			Builder showPointDesc = new AlertDialog.Builder(PositionActivity.this);

			showPointDesc.setPositiveButton("確認", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			TextView contentDesc = (TextView) v.findViewById(R.id.textDesc);
			ImageView contentImg = (ImageView) v.findViewById(R.id.pointImg);

			if (tag) {
				showPointDesc.setTitle(title);
				contentDesc.setText(desc);

			} else {
				showPointDesc.setTitle("抱歉");
				contentDesc.setText("此位置已被刪除!");
			}
			
			LookHelper look = new LookHelper();
			if (look.look(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/", pointId)) {
				Bitmap b= BitmapFactory.decodeFile(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/"+ pointId);
				contentImg.setImageBitmap(b);
			}else {
				//contentImg.setVisibility(visibility);
			}

			contentDesc.setTextSize(20);
			contentDesc.setTextColor(Color.YELLOW);

			showPointDesc.setView(v);
			showPointDesc.show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showLastPosition() {

		lastPointId = settings.getString(Global.MapId, "");

		if (!lastPointId.equals("")) {

			JSONObject jsonObj;
			JSONArray jsonObjArray;

			try {

				jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));

				jsonObjArray = jsonObj.getJSONArray("points");

				for (int i = 0; i < jsonObjArray.length(); i++) {
					if (jsonObjArray.getJSONObject(i).getString("pointID").equals(lastPointId)) {
						Global.PointTitle = jsonObjArray.getJSONObject(i).getString("title");
						break;
					}
				}

				Global.PointId = lastPointId;

				// Show point title
				pointTitleText.setText(Global.PointTitle);

				showPointInfo(jsonObj, lastPointId);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			Builder noLastPointDialog = new AlertDialog.Builder(PositionActivity.this);
			noLastPointDialog.setPositiveButton("確認", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			noLastPointDialog.setTitle("無上次掃描記錄");
			noLastPointDialog.setMessage("請按確認繼續...");
			noLastPointDialog.show();

		}

	}

	private void scanResultOk() {
		try {

			JSONObject jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));

			// Assign MapTitle
			Global.MapTitle = jsonObj.getString("title");

			// Show map data when download finished
			showMapData(Global.MapId);
			waitDownDialog.dismiss();

			showPointInfo(jsonObj, Global.PointId);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void scanResultfailed() {

		Boolean isOldMapData;

		// Show old map data if it exist otherwise show last map
		isOldMapData = checkOldMapData(Global.MapId);

		if (isOldMapData) {

			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(JsonParser.getJsonRespon(Global.SDPathRoot + "/" + Global.MapDirName + "/" + Global.MapId + "/" + Global.MapId + ".json"));

				// Assign MapTitle
				Global.MapTitle = jsonObj.getString("title");

				// Assign point title to global variable
				Global.PointTitle = null;

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			showMapData(Global.MapId);
		} else {
			showChooseMap();
		}

		// Show failed dialog when download failed
		waitDownDialog.dismiss();
		Builder showFailInfo = new AlertDialog.Builder(PositionActivity.this);
		showFailInfo.setMessage("請確認網路連線\n按確認繼續...");
		showFailInfo.setTitle("下載失敗!").setPositiveButton("確認", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		showFailInfo.show();

	}

	private void reDownloadData() {

	}

	private Handler handler = new Handler() {
		// Boolean isOldMapData;

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

			super.handleMessage(msg);
		}
	};

	protected static final int MENU_ShowPoints = Menu.FIRST;
	protected static final int MENU_ChooseMap = Menu.FIRST + 1;
	protected static final int MENU_RefreshData = Menu.FIRST + 2;
	protected static final int MENU_LastPosition = Menu.FIRST + 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ShowPoints, 0, "瀏覽定位點");
		menu.add(0, MENU_ChooseMap, 0, "選擇地圖");
		// menu.add(0, MENU_RefreshData, 0, "重整圖檔");
		menu.add(0, MENU_LastPosition, 0, "上次位置");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_ShowPoints:
			Intent intent1 = new Intent();
			intent1.setClass(PositionActivity.this, ShowPointList.class);
			startActivityForResult(intent1, 1);

			break;
		case MENU_ChooseMap:
			Intent intent2 = new Intent();
			intent2.setClass(PositionActivity.this, ShowMapList.class);
			startActivityForResult(intent2, 2);

			break;
		case MENU_RefreshData:

			break;
		case MENU_LastPosition:
			showLastPosition();

			break;
		}
		return true;
	}

}