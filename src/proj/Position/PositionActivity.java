package proj.Position;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ntu.com.google.zxing.client.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PositionActivity extends Activity implements Runnable {

	private MyWebView mapView;
	private Button scan;

	private TextView pointTitle;
	private String jsonURL;
	private String mapID;
	private String mapVer;
	private String pointID;
	private String title;
	
	private Thread thread;
	    

	// public enum mapKey {
	// mapID, mapVer, title, map
	// }

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViews();
		setListeners();
		makeRootDir();
		startScan();
		//downloadMapJson(mapID);
		// Download.downImg();
		// showImage();// For test showing map without scan (just press return)

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

	private void showImage() {

		// Enable to zoom in/out

		mapView.getSettings().setBuiltInZoomControls(true);
		// Enable to zoom in/out by double tap
		mapView.getSettings().setUseWideViewPort(true);

		// Bind the image path
		// String data =
		// "<img src = \"file:///android_res/drawable/abc.png\" />";
		// String data =
		// "<img src = \"http:///indoorposition.appspot.com/image/map.jpg\" />";

		String data = "<img src = \"file:///sdcard/somefile.jpg\" />";

		final String mimeType = "text/html";
		final String encoding = "utf-8";
		mapView.loadDataWithBaseURL("about:blank", data, mimeType, encoding, "");

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
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

				try {
					String[] contentArray = contents.split("\\?");
					String[] contentPart = contentArray[1].split("&");

					// Assign value
					jsonURL = contents;
					
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
					

					//Check whether it need to download Json file
					checkMapVerUpdate(mapID,mapVer);
					//Start download Json file if need
					thread = new Thread(this);
					thread.start();
					//downloadMapJson(mapID);
					
				} catch (Exception e) {
					// TODO: handle exception
					// Handle when scan QR code which is not our form
					pointTitle.setText("QR code error!");
				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				
				//Show AlertDialog for temporarily handle (Need to show last map)
				Builder gg = new AlertDialog.Builder(PositionActivity.this);
				gg.setMessage("Scan failed");
				gg.setTitle("Scan failed").setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});
				gg.show();
			}
		}
	}

	private void makeRootDir() {
		// Make root dir if not in SD card yet

		Boolean isExistent;
		LookHelper looker = new LookHelper();
		isExistent = looker.look(Global.SDPathRoot, Global.MapDirName);

		if (!isExistent) {
			MakeFileHelper maker = new MakeFileHelper();
			maker.make(Global.SDPathRoot, Global.MapDirName);
		}

	}

	private Boolean searchMapDir(String mapID) {
		// Check whether the Map dir exists or not

		LookHelper looker = new LookHelper();

		return looker.look(Global.MapDirRoot, mapID);
	}

	private void downloadMapJson(String mapId) {
		// Download the JSON file

		DownloadHelper downloader = new DownloadHelper();
		LookHelper looker = new LookHelper();
		Boolean isExistent;
		
		File SDCardRoot = Environment.getExternalStorageDirectory();
		MakeFileHelper def = new MakeFileHelper();
		def.make(Global.MapDirRoot, mapId);

		try {
			downloader.downFile(
					"http://dl.dropbox.com/u/22034772/example.json",
					Global.MapDirRoot + "/" + mapId, mapId + ".json");

			
			File abc = new File(SDCardRoot + "/" + Global.MapDirRoot + "/"
					+ mapId + "/" + mapId + ".json");
			File dstFile = new File(SDCardRoot + "/" +Global.MapDirRoot + "/" + "last.json");

			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(abc));
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(dstFile));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// Handle when download failed
			e.printStackTrace();
		}
	}

	private Boolean checkMapVerUpdate(String mapId,String mapVer) {
		//Return False when it need to be updated
		return true;
	}
    public void run() 
    {
    	Log.e("test", "yo");
    	downloadMapJson(mapID);
//        Message m = new Message();
//	// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
//	m.what = MEG_INVALIDATE;
//	handler.sendMessage(m);
    }
}