package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpPostDownConnection;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ShowSymptomPictureActivity extends Activity {
	String symptom = null;						// A tunet neve
	HttpPostDownConnection download = null;		// szal a kep letoltesehez
	private ImageView mPictureView;				// A tunetet abrazolo kep
	private Activity activity=this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.symptompicture);
		
		Bundle extras = getIntent().getExtras();							// Megjelenitendo tunet nevenek lekerdezese
		symptom = extras.getString("symptom");
		((TextView)findViewById(R.symptompicture.symptomName)).setText(symptom);
		
		mPictureView = (ImageView)findViewById(R.symptompicture.imageView1);
		
		// Vissza gomb esemenykezeloje
		((Button)findViewById(R.symptompicture.backbutton)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
					setResult(RESULT_OK);
					finish();	
			}
		});
	}
	
	// Indulaskor a kep letoltese
	@Override
    public void onStart() {
    	super.onStart();
    	getData();
    }
	
	// Megszakitaskor a futo szalak leallitasa
	@Override
	public void onPause() {
		super.onPause();
		if (download != null && download.isAlive()) {
			download.stop();
			download = null;
			((ProgressBar)findViewById(R.symptompicture.progress)).setVisibility(ProgressBar.INVISIBLE);
		}
	}
	
	// Letoltott kepet kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.symptompicture.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("SymptomPictureActivity","Sikertelen letoltes.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					Log.v("SymptomPictureActivity", "Sikeres letoltes");
					 
					byte[] map = download.getResponse();												// letoltott kep lekerdezese
					
					if (map.length>0) {			// Ha van kep
						mPictureView.setImageBitmap(BitmapFactory.decodeByteArray(map, 0, map.length));		// kep megjelenitese
					}
					else {						// Ha nincs kep
						mPictureView.setImageResource(R.drawable.nopicture);
						Toast.makeText(activity, "A tünethez nincs kép megadva.", Toast.LENGTH_LONG).show();
					}
					
					break;
			}
		}
	};
	
	// Kep letoltesenek kezdemenyezese
	public void getData(){
		((ProgressBar)findViewById(R.symptompicture.progress)).setVisibility(ProgressBar.VISIBLE);
		
		String url = "GetPictureOfSymptom?symptom=" + URLEncoder.encode(symptom);
	    download = new HttpPostDownConnection(url, mHandler);
	    download.start();
	}

}
