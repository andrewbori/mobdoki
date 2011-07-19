package mobdoki.client.activity.user.symptom;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import mobdoki.client.connection.HttpPostDownConnection;
import mobdoki.client.connection.HttpPostUpConnection;

import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PictureCheckActivity extends Activity {
	private ImageView imgView;
	private TextView text;				// kep sorszamanak megjelenitoje
	private TextView name;				// kep nevenek megjelenitoje
	// private Gallery gallery;
	
	private int actualpos = 1;					// Megjelenitett kep sorszama
	private String picturename = null;			// Megjelenitett kep neve
	private ArrayList<String> picNames = null;	// kepnevek listaja

	private HttpGetConnection downloadPicnames = null;		// szal a kepnevek listajanak letoltesehez
	private HttpGetConnection downloadComment = null;		// szal a komment letoltesehez
	private HttpPostDownConnection downloadPicture = null;	// szal a kep letoltesehez
	private HttpPostUpConnection uploadAnswer = null;		// szal a valasz feltoltesehez

	private Activity activity = this;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.picturecheck);
		// Bundle extras = getIntent().getExtras();
		// username = extras.getString("username");
		// gallery = (Gallery) findViewById(R.id.gallery1);
		imgView = (ImageView) findViewById(R.picturecheck.image);
		text = (TextView) findViewById(R.picturecheck.which);
		name = (TextView) findViewById(R.picturecheck.kepnev);

		picNames = new ArrayList<String>();

		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.picturecheck.backbutton);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		// Balra (elozo kep) gomb esemenykezeloje
		ImageButton leftButton = (ImageButton) findViewById(R.picturecheck.imageButton1);
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getLeftPicture();
			}
		});
		
		// Jobbra (kovetkezo kep) gomb esemenykezeloje
		ImageButton rightButton = (ImageButton) findViewById(R.picturecheck.imageButton2);
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getRightPicture();
			}
		});

		// Feltoltes gomb esemenykezeloje
		Button upButton = (Button) findViewById(R.picturecheck.uploadbutton);
		upButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				answerUpload();
			}
		});

		imgView.setImageResource(R.drawable.loading);

		// gallery.setAdapter(new AddImgAdp(this));
		/*
		 * gallery.setOnItemClickListener(new OnItemClickListener() { public
		 * void onItemClick(AdapterView parent, View v, int position, long id) {
		 * imgView.setImageBitmap(ImgArray.get(position)); } });
		 */

		// E-mail gomb esemenykezeloje
		Button emailButton = (Button) findViewById(R.picturecheck.emailbutton);
		emailButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	// Indulaskor a kepnevek listajanak lekerdezese
	@Override
	public void onStart() {
		super.onStart();
		getPictureNames();
	}

	// Megszakitaskor a futo szalak leallitasa
	@Override
	public void onPause() {
		super.onPause();
		if (downloadComment != null && downloadComment.isAlive()) {
			downloadComment.stop();
			downloadComment = null;
		}
		if (uploadAnswer != null && uploadAnswer.isAlive()) {
			uploadAnswer.stop();
			uploadAnswer = null;
		}
		if (downloadPicnames != null && downloadPicnames.isAlive()) {
			downloadPicnames.stop();
			downloadPicnames = null;
		}
		if (downloadPicture != null && downloadPicture.isAlive()) {
			downloadPicture.stop();
			downloadPicture = null;
			((ProgressBar)findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
		}
	}

	// Komment letolteset kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// ((ProgressBar)findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureCheckActivity", "Sikertelen komment lekeres.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				try {
					if (downloadComment.isOK()) {
						Log.v("PictureCheckActivity", "Sikeres komment letoltes.");
						
						String comment = downloadComment.getJSONString("comment");
						((EditText) findViewById(R.picturecheck.comment)).setText(comment);
					} else {
						Log.v("PictureCheckActivity", "Sikertelen komment letoltes.");
						Toast.makeText(activity, downloadComment.getMessage(), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.v("PictureCheckActivity", "Tul gyors kepvaltas... (NullPointerException)");
				}
				break;
			}
		}
	};

	// Valasz feltolteset kezelo Handler
	public Handler mCommentHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// ((ProgressBar)findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureCheckActivity", "Sikertelen valasz feltoltes.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				if (uploadAnswer.hasMessage()) {
					String message = uploadAnswer.getMessage();
					Log.v("PictureCheckActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				
				if (uploadAnswer.isOK()) {	// siker eseten a keplista frissitese
					getPictureNames();
				}
				break;
			}
		}
	};

	// Letoltott kepnevek listajat kezelo Handler
	public Handler mPicnamesHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// ((ProgressBar)findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureCheckActivity", "Sikertelen keplista lekeres.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				try {
					if (downloadPicnames.isOK()) {
						Log.v("PictureCheckActivity", "Sikeres keplista lekeres.");
						picNames = downloadPicnames.getJSONStringArray("picNames");		// kepnev tomb
						if (picNames.size()<actualpos) actualpos = picNames.size();		// ha a lista vegen alltunk, legyunk ott!
						text.setText(actualpos + "/" + picNames.size());				// kep sorszam beallitasa
						if (picNames.size()!=0) {	// Ha van kep, akkor megjelenit
							getPictures();	
						}
						else {						// Ha nincs, mezoket nullaz
							((ProgressBar) findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
							imgView.setImageResource(R.drawable.nopicture);
							((EditText) findViewById(R.picturecheck.comment)).setText("");		// komment/valasz/kep/kepnev torlese
							((EditText) findViewById(R.picturecheck.answerText)).setText("");
							name.setText("-");
						}
					} else {
						((ProgressBar) findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
						Toast.makeText(activity, downloadPicnames.getMessage(),Toast.LENGTH_SHORT).show();
						((EditText) findViewById(R.picturecheck.comment)).setText("");		// komment/valasz/kep/kepnev torlese
						((EditText) findViewById(R.picturecheck.answerText)).setText("");
						name.setText("-");
						Toast.makeText(activity, downloadPicnames.getMessage(),Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.v("PictureCheckActivity", "az onPause nullazta a szalat: NullPointerException");
				}
				break;
			}
		}
	};

	// Letoltott kepet kezelo Handler
	public Handler mPictureDownloadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.arg1) {
			case 0:
				Log.v("PictureCheckActivity", "Sikertelen kepletoltes.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				Log.v("PictureCheckActivity", "Sikeres kepletoltes");
				
				if(downloadPicture != null){
					try {
						byte[] map = downloadPicture.getResponse();
						// ImgArray.add(BitmapFactory.decodeByteArray(map, 0, map.length));
						imgView.setImageBitmap(BitmapFactory.decodeByteArray(map, 0, map.length));
					} catch (Exception e) {
						Log.v("PictureCheckActivity", "Tul gyors kepvaltas... (NullPointerException)");
					}
					((ProgressBar) findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.GONE);
				}

				break;
			}
		}
	};

	/*
	 * public class AddImgAdp extends BaseAdapter { int GalItemBg; private
	 * Context cont;
	 * 
	 * public AddImgAdp(Context c) { cont = c; TypedArray typArray =
	 * obtainStyledAttributes(R.styleable.PictureCheckActivity); GalItemBg =
	 * typArray .getResourceId(
	 * R.styleable.PictureCheckActivity_android_galleryItemBackground, 0);
	 * typArray.recycle(); }
	 * 
	 * public int getCount() { return ImgArray.size(); }
	 * 
	 * public Object getItem(int position) { return position; }
	 * 
	 * public long getItemId(int position) { return position; }
	 * 
	 * public View getView(int position, View convertView, ViewGroup parent) {
	 * ImageView imgView = new ImageView(cont);
	 * 
	 * imgView.setImageBitmap((ImgArray.get(position)));
	 * imgView.setLayoutParams(new Gallery.LayoutParams(120, 100));
	 * imgView.setScaleType(ImageView.ScaleType.FIT_XY);
	 * imgView.setBackgroundResource(GalItemBg);
	 * 
	 * return imgView; } }
	 */

	// Komment letoltese
	public void commentDownload() {
		if (downloadComment != null && downloadComment.isAlive()) {
			downloadComment.stop();
			downloadComment = null;
		}
		String url = "CommentDownload?picturename=" + URLEncoder.encode(picturename);
		downloadComment = new HttpGetConnection(url, mHandler);
		downloadComment.start();
	}

	// Valasz feltoltese
	public void answerUpload() {
		
		if (actualpos==0) {
			Toast.makeText(activity, "Nincs mire válaszolni.", Toast.LENGTH_LONG).show();
			return;
		}
		
		String answer = ((EditText) findViewById(R.picturecheck.answerText)).getText().toString();	// valasz
		((EditText) findViewById(R.picturecheck.answerText)).setText("");
		
		if (answer==null || answer.equals("")) {
			Toast.makeText(activity, "Adja meg a válaszát!", Toast.LENGTH_LONG).show();
			return;
		}
		
		StringEntity comEntity = null;
		try {
			comEntity = new StringEntity(answer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String url2 = "AnswerUpload?picturename=" + URLEncoder.encode(picturename);
		uploadAnswer = new HttpPostUpConnection(url2, mCommentHandler, comEntity);
		uploadAnswer.start();	// valasz feltoltese
	}

	// Kep letoltese
	public void getPictures() {
		if (downloadPicture != null && downloadPicture.isAlive()) {			// Ha tolt, allitsa le
			downloadPicture.stop();
			downloadPicture = null;
		}
		
		picturename = picNames.get(actualpos - 1);	// kepnev beallitasa
		commentDownload();							// kephez tartozo komment letoltese
		name.setText(picturename);

		String url = "PictureDownload?picturename=" + URLEncoder.encode(picturename);
		downloadPicture = new HttpPostDownConnection(url, mPictureDownloadHandler);
		downloadPicture.start();
	}

	// Elozo kep letoltesenek kerese
	public void getLeftPicture() {
		if (actualpos > 1) {
			actualpos--;
			text.setText(actualpos + "/" + picNames.size());
			((ProgressBar) findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.VISIBLE);
			getPictures();
		}
	}

	// Kovetkezo kep letoltesenek kerese
	public void getRightPicture() {
		if (actualpos < picNames.size()) {
			actualpos++;
			text.setText(actualpos + "/" + picNames.size());
			((ProgressBar) findViewById(R.picturecheck.progress)).setVisibility(ProgressBar.VISIBLE);
			getPictures();
		}
	}

	// Kepnevek listajanak letoltese
	public void getPictureNames() {
		String url = "GetPictureNames";
		downloadPicnames = new HttpGetConnection(url, mPicnamesHandler);
		downloadPicnames.start();
	}

}
