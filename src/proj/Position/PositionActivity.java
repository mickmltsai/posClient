package proj.Position;

import ntu.com.google.zxing.client.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PositionActivity extends Activity {

	private TextView decodeText;
	private MyWebView mapView;
	// private WebView webView;
	private Button scan;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViews();
		setListeners();
		startScan();
		//Download.downImg();
		showImage();// For test showing map without scan (just press return)

	}

	private void findViews() {
		decodeText = (TextView) findViewById(R.id.plainText);
		// webView = (WebView) findViewById(R.id.webView1);
		mapView = (MyWebView) findViewById(R.id.myWebView1);
		scan = (Button) findViewById(R.id.scan);
	}

	private void setListeners() {
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
		// Intent intent=new Intent("ntu.com.google.zxing.client.android.SCAN");
		// intent.setClassName("ntu.com.google.zxing.client.android",
		// "ntu.com.google.zxing.client.android.CaptureActivity");
		// startActivityForResult(intent, 0);

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

				decodeText.setText(contents);
				showImage();

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	private void updateDate(String dataName) {
	
	}
	
}