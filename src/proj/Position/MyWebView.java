package proj.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.xml.xpath.XPath;

import org.json.JSONArray;
import org.json.JSONObject;

import android.R.integer;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class MyWebView extends WebView {

	float pointR = 30;
	Context c;

	AlertDialog.Builder test;

	public MyWebView(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub

		super(context, attrs);
		c = context;
		this.setClickable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		try {

			int[] x;
			int[] y;

			if (Global.MapId != null) {
				// Not check touchevent when MapId = null

				// Parse x y points into x,y arrays
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

				JSONArray jsonObjArray = jsonObj.getJSONArray("points");

				x = new int[jsonObjArray.length()];
				y = new int[jsonObjArray.length()];

				JSONObject jsonObjCoordJsonObject;
				for (int i = 0; i < jsonObjArray.length(); i++) {

					jsonObjCoordJsonObject = jsonObjArray.getJSONObject(i)
							.getJSONObject("coord");

					x[i] = jsonObjCoordJsonObject.getInt("x");
					y[i] = jsonObjCoordJsonObject.getInt("y");

				}

				if (event.getAction() == MotionEvent.ACTION_UP) {

					float tx1 = (getScrollX() + event.getX()) / getScale();
					float ty1 = (getScrollY() + event.getY()) / getScale();

					for (int i = 0; i < jsonObjArray.length(); i++) {

						if ((tx1 - x[i]) * (tx1 - x[i]) + (ty1 - y[i])
								* (ty1 - y[i]) <= pointR * pointR) {
							Toast.makeText(c, "test", Toast.LENGTH_SHORT)
									.show();

							Builder testt;
							testt = new AlertDialog.Builder(c);
							testt.setMessage(jsonObjArray.getJSONObject(i).getString("description"));
							testt.setTitle(jsonObjArray.getJSONObject(i).getString("title")).setPositiveButton("OK",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub

										}
									});
							testt.show();

						}

					}

				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int[] x;
		int[] y;
		try {

			// Parse x y points into x,y arrays
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

			JSONArray jsonObjArray = jsonObj.getJSONArray("points");

			x = new int[jsonObjArray.length()];
			y = new int[jsonObjArray.length()];

			JSONObject jsonObjCoordJsonObject;
			for (int i = 0; i < jsonObjArray.length(); i++) {

				jsonObjCoordJsonObject = jsonObjArray.getJSONObject(i)
						.getJSONObject("coord");

				x[i] = jsonObjCoordJsonObject.getInt("x");
				y[i] = jsonObjCoordJsonObject.getInt("y");

			}

			// float x = pointX * getScale();
			// float y = pointY * getScale();

			float r = pointR; // * getScale();
			//
			Paint p = new Paint();

			for (int i = 0; i < jsonObjArray.length(); i++) {
				float xx = x[i] * getScale();
				float yy = y[i] * getScale();
				canvas.drawCircle(xx, yy, r, p);

			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
