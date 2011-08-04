package mobdoki.client.activity.user.symptom;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpPostUpConnection;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PictureUploadActivity extends Activity implements OnClickListener {
	
	HttpPostUpConnection picUpload = null;			// szal a kep feltoltesehez
	HttpPostUpConnection commentUpload = null;		// szal a komment feltoltesehez
	
	private String picturename = null;			// kep neve
	
	private ImageView mPictureView;
	
	private Activity activity = this;
	private String username = null;				// paciens felhasznaloneve
	
	private byte[] imageBytes = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pictureupload);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			imageBytes = extras.getByteArray("imageBytes");
			username = extras.getString("username");
		}
		
		mPictureView = (ImageView) findViewById(R.pictureupload.imageView);
		mPictureView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length));

		((Button) findViewById(R.pictureupload.uploadbutton)).setOnClickListener(this);
		((Button) findViewById(R.pictureupload.backbutton)).setOnClickListener(this);

	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		
			// Feltoltes gomb esemenykezeloje
			case R.pictureupload.uploadbutton:
				if (picUpload == null || (picUpload != null && !picUpload.isAlive())) {
					Upload();
				}
				break;
				
			// Vissza gomb esemenykezeloje
			case R.pictureupload.backbutton:
				setResult(RESULT_OK);
				finish();
				break;
		}	
	}

	// Megszakitaskor a futo szalak leallitasa
	@Override
	public void onPause() {
		super.onPause();
		if (picUpload != null && picUpload.isAlive()) {
			picUpload.stop();
			picUpload = null;
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(View.INVISIBLE);
		}
		if (commentUpload != null && commentUpload.isAlive()) {
			commentUpload.stop();
			commentUpload = null;
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(View.INVISIBLE);
		}
	}

	// A kep feltolteset kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(View.INVISIBLE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureUploadActivity", "Sikertelen feltöltés.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				try {
					if (picUpload.isOK()) {		// ha sikerult feltolteni a kepet
						((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(View.VISIBLE);
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
			((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(View.INVISIBLE);
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

	// Kep es komment feltoltes kezdemenyezese
	public void Upload() {
		String comment = ((EditText) findViewById(R.pictureupload.comment)).getText().toString();				// Komment
		picturename = ((AutoCompleteTextView) findViewById(R.pictureupload.picturename)).getText().toString();	// Kep neve
		
		if (picturename==null || picturename.equals("")) {
			Toast.makeText(activity, "Adja meg a kép nevét!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		StringEntity comEntity = null;
		try {
			comEntity = new StringEntity(comment);
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		
		((ProgressBar)findViewById(R.pictureupload.progress)).setVisibility(View.VISIBLE);
		
		ByteArrayEntity fe = new ByteArrayEntity(imageBytes);
		String url = "PictureUpload?picturename=" + URLEncoder.encode(picturename) + "&username=" + URLEncoder.encode(username);
		picUpload = new HttpPostUpConnection(url, mHandler, fe);
		picUpload.start();		// kep feltoltese

		String url2 = "CommentUpload?picturename=" + URLEncoder.encode(picturename);
		commentUpload = new HttpPostUpConnection(url2, mCommentHandler, comEntity);
		//commentUpload.start();	// komment feltoltese - azutan inditjuk, miutan a kep bekerult a DB-ba, mert hivatkozik ra FK-jel
	}
}
