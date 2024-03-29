package mobdoki.client.activity;

import java.io.FileOutputStream;

import mobdoki.client.CameraPreview;
import mobdoki.client.R;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {
	private CameraPreview preview;					// Kamera
	private boolean mIsPreviewed = true;				// Van kamerakep?
	private FrameLayout framelayout = null;
	
	private boolean hasImage=false;
	private final String filepath = "/sdcard/temp_mobdoki.jpg";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera);

		preview = new CameraPreview(this);
		framelayout = ((FrameLayout) findViewById(R.id.preview));
		framelayout.addView(preview);
	}

	// Gombnyomas esemenykezeloje
	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {						// ha kozepso gomb lenyomva

			if (this.mIsPreviewed) {											// ha van kamerakep
				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);	// kep keszitese
				preview.camera.stopPreview();											// kamerakep megallitasa
				mIsPreviewed = false;
			} else {
				preview.camera.startPreview();											// kamerakep mutatasa
				mIsPreviewed = true;
			}
		}
		return super.onKeyDown(keycode, event);
	}

	// Menu megjelenitese
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cameramenu, menu);
		return true;
	}

	// Menuelem kivalasztasa
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.newpicture:
				if (this.mIsPreviewed) {											// ha van kamerakep
					preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);	// kep keszitese
					preview.camera.stopPreview();											// kamerakep megallitasa
					mIsPreviewed = false;
				} else {
					preview.camera.startPreview();											// kamerakep mutatasa
					mIsPreviewed = true;
				}
				break;
			case R.id.upload:
				if (hasImage) {
					Intent intent = new Intent();
					intent.putExtra("source", "camera");
					intent.putExtra("filepath", filepath);
					setResult(RESULT_OK,intent);
					finish();
				}
				break;
		}
		return true;
	}
	
	// Visszagomb megnyomasakor...
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		if (hasImage) {									// Ha keszult kep
			intent.putExtra("source", "camera");
			intent.putExtra("filepath", filepath);
			setResult(RESULT_OK, intent);
		} else {
			setResult(RESULT_CANCELED, intent);
		}
		finish();
	}

	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream fos = null;
			try {
				// Writes to SD Card
				fos = new FileOutputStream(filepath);
				fos.write(data);
				fos.close();
				hasImage=true;
			} catch (Exception e) {
				hasImage=false;
			}
		}
	};
	
	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
		}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};
}

