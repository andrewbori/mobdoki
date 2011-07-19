package mobdoki.client.activity.user.symptom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import mobdoki.client.Preview;
import mobdoki.client.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

public class CameraActivity extends Activity {
	Preview preview;							// Kamera
	Long mMillisec;								// keszitett kep szama
	boolean mIsPreviewed = true;				// Van kamerakep?
	private FrameLayout framelayout = null;
	private String username = null; 			// Paciens felhasznaloneve

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cameraac);
		
		Bundle extras = getIntent().getExtras();
		username = extras.getString("username");

		preview = new Preview(this);
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
		if (!mIsPreviewed)
			switch (item.getItemId()) {
			
			case R.id.deleteall:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Törölni akarja a képet?")
						.setCancelable(false)
						.setPositiveButton("Igen",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										File file = new File(String.format(
												"/sdcard/DCIM/%d.jpg",
												mMillisec));
										if (file.delete())
											Toast.makeText(
													getApplicationContext(),
													"Kép törölve.",
													Toast.LENGTH_SHORT).show();
										mIsPreviewed = true;
										preview.camera.startPreview();
									}
								})
						.setNegativeButton("Nem",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				break;
			case R.id.newpicture:
				this.mIsPreviewed = true;
				preview.camera.startPreview();
				break;
			case R.id.upload:
				Intent intent = new Intent(this,PictureUploadActivity.class);
				intent.putExtra("Picture_num", mMillisec);
				intent.putExtra("username", username);
				startActivityForResult(intent, 0);
				break;
			}
		return true;
	}

	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// Writes to SD Card
				mMillisec = System.currentTimeMillis();
				outStream = new FileOutputStream(String.format(
						"/sdcard/DCIM/%d.jpg", mMillisec));
				outStream.write(data);
				outStream.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		}
	};
}

