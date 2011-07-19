package mobdoki.client.activity.user.symptom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpPostUpConnection;

import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PictureUploadActivity extends Activity {
	
	HttpPostUpConnection picUpload = null;			// szal a kep feltoltesehez
	HttpPostUpConnection commentUpload = null;		// szal a komment feltoltesehez
	
	private Long mPictureNum = null;
	private String picturename = null;			// kep neve
	
	private Button mUploadButton;
	private ImageView mPictureView;
	
	private Activity activity = this;
	private File file = null;					// feltoltendo kepfajl
	private String username = null;				// paciens felhasznaloneve

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pictureupload);

		mUploadButton = (Button) findViewById(R.pictureupload.uploadbutton);
		mPictureView = (ImageView) findViewById(R.pictureupload.imageView);

		//file = new File("/data/Koala.jpg");
		//file = new File("/sdcard/laz.jpg");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mPictureNum = extras.getLong("Picture_num");
			username = extras.getString("username");
		}
		
		file = new File(String.format("/sdcard/DCIM/%d.jpg", mPictureNum));

		mPictureView.setImageBitmap(BmpFromString());

		// Feltoltes gomb esemenykezeloje
		mUploadButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if (picUpload == null || (picUpload != null && !picUpload.isAlive()))
					Upload();
			}

		});

		// Vissza gomb esemenykezeloje
		((Button) findViewById(R.pictureupload.backbutton))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						setResult(RESULT_OK);
						finish();
					}
		});
	}

	// Megszakitaskor a futo szalak leallitasa
	@Override
	public void onPause() {
		super.onPause();
		if (picUpload != null && picUpload.isAlive()) {
			picUpload.stop();
			picUpload = null;
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(ProgressBar.INVISIBLE);
		}
		if (commentUpload != null && commentUpload.isAlive()) {
			commentUpload.stop();
			commentUpload = null;
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(ProgressBar.INVISIBLE);
		}
	}

	// A kep feltolteset kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureUploadActivity", "Sikertelen feltöltés.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				try {
					if (picUpload.isOK()) {		// ha sikerult feltolteni a kepet
						((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(ProgressBar.VISIBLE);
						commentUpload.start();	// komment feltoltese
					}
					if (picUpload.hasMessage()) {
						String message = picUpload.getMessage();
						Log.v("PictureUploadActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.v("PictureUploadActivity", "az onPause nullazta a szalat: NullPointerException");
				}
				break;
			}
		}
	};

	// A komment feltolteset kezelo Handler
	public Handler mCommentHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureUploadActivity", "Sikertelen feltöltés.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				try {
					if (commentUpload.hasMessage()) {
						String message = commentUpload.getMessage();
						Log.v("PictureUploadActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.v("PictureUploadActivity", "az onPause nullazta a szalat: NullPointerException");
				}
				break;
			}
		}
	};

	public Bitmap BmpFromString() {

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return BitmapFactory.decodeFile(String.format("/sdcard/DCIM/%d.jpg", mPictureName));

		return BitmapFactory.decodeStream(input);
	}

	// Kep es komment feltoltes kezdemenyezese
	public void Upload() {
		String comment = ((EditText) findViewById(R.pictureupload.comment)).getText().toString();				// Komment
		picturename = ((AutoCompleteTextView) findViewById(R.pictureupload.picturename)).getText().toString();	// Kep neve
		
		if (picturename==null || picturename.equals("")) {
			Toast.makeText(activity, "Adja meg a kép nevét!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// file = new File(String.format(
		// "/sdcard/DCIM/%d.jpg",S
		// mPictureName));
		
		StringEntity comEntity = null;
		try {
			comEntity = new StringEntity(comment);
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		
		((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(ProgressBar.VISIBLE);
		
		FileEntity fe = new FileEntity(file, URLConnection.guessContentTypeFromName(file.getName()));
		String url = "PictureUpload?picturename=" + URLEncoder.encode(picturename) + "&username=" + URLEncoder.encode(username);
		picUpload = new HttpPostUpConnection(url, mHandler, fe);
		picUpload.start();		// kep feltoltese

		String url2 = "CommentUpload?picturename=" + URLEncoder.encode(picturename);
		commentUpload = new HttpPostUpConnection(url2, mCommentHandler, comEntity);
		//commentUpload.start();	// komment feltoltese - azutan inditjuk, miutan a kep bekerult a DB-ba, mert hivatkozik ra FK-jel

	}

}
