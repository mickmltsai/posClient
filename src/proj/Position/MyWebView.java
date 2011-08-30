package proj.Position;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class MyWebView extends WebView {
	float pointX = 200;
	float pointY = 100;
	float pointR = 50;
	Context c;


	AlertDialog.Builder gg;

	public MyWebView(Context context,AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		
		super(context, attrs);
		c = context;
		this.setClickable(true);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_UP) {

			float tx = (getScrollX() + event.getX()) / getScale();
			float ty = (getScrollY() + event.getY()) / getScale();

			if ((pointX - tx) * (pointX - tx) + (pointY - ty) * (pointY - ty) <= pointR
					* pointR) {
				Toast.makeText(c, "test", Toast.LENGTH_SHORT).show();

				gg = new AlertDialog.Builder(c);
				gg.setMessage("test");
				gg.setTitle("test").setPositiveButton("OK",
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
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float x = pointX * getScale();
		float y = pointY * getScale();

		float r = pointR * getScale();
		// float r = pointR * 0.5f;

		Paint p = new Paint();

		canvas.drawCircle(x, y, r, p);

	}

}
