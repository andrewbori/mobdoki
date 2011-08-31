package mobdoki.client.activity.user.health;


import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PatientHealthActivity extends Activity implements OnClickListener {
	private HttpGetJSONConnection download;		// szal a szerverhez csatlakozashoz
	
	private Activity activity=this;
	private SeekBar seekbarMood;
	private TextView textMood;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.setContentView(R.layout.patienthealth);
		
		setTitle("MobDoki: Egészségügyi állapot");
        
		textMood = (TextView)findViewById(R.patienthealth.moodText);
        seekbarMood = (SeekBar)findViewById(R.patienthealth.moodBar);
        seekbarMood.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				textMood.setText(progress+"");
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
		});
		
		((Button) findViewById(R.patienthealth.back)).setOnClickListener(this);
		((Button) findViewById(R.patienthealth.upload)).setOnClickListener(this);
	}
	
    // Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
			// Feltoltes gomb esemenykezeloje
			case R.patienthealth.upload:
				if (download==null || (download!=null && download.isNotUsed())) uploadData();
				break;
		    
		    // Vissza gomb esemenykezeloje
			case R.patienthealth.back:
				finish();
				break;
		}
	}
	
	// Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }
	
	 // Az adatok feltolteset kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 0:
					Log.v("PatientHealthActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.hasMessage()) {
						// Uzenet lekerdezese es megjelenitese
						Toast.makeText(activity, download.getMessage(), Toast.LENGTH_SHORT).show();
					}
					break;
			}
			setProgressBarIndeterminateVisibility(false);
			download.setNotUsed();
		}
	};
	
	public void uploadData(){
		String bp1 = ((EditText)findViewById(R.patienthealth.bloodpressure1)).getText().toString();			// Vernyomas: systoles erek
		String bp2 = ((EditText)findViewById(R.patienthealth.bloodpressure2)).getText().toString();			// Vernyomas: diastoles ertek
		String pulse = ((EditText)findViewById(R.patienthealth.pulse)).getText().toString();				// Pulzus
		String temperature = ((EditText)findViewById(R.patienthealth.temperature)).getText().toString();	// Testhomerseklet
		String weight = ((EditText)findViewById(R.patienthealty.weight)).getText().toString();				// Testtomeg
		
    	if (bp1.equals("") || bp2.equals("") || pulse.equals("") || temperature.equals("") || weight.equals("")) {				// Mindegyik adat kell
    		Toast.makeText(activity, "Nincs minden adat megadva!", Toast.LENGTH_SHORT).show();
    		return;
    	}
		
    	setProgressBarIndeterminateVisibility(true);
    	
		String url = "PatientHealth?ssid=" + UserInfo.getSSID() + 
					 "&bp1=" + URLEncoder.encode(bp1) + "&bp2=" + URLEncoder.encode(bp2) + "&pulse=" + URLEncoder.encode(pulse) + 
					 "&weight=" + URLEncoder.encode(weight) + "&mood=" + seekbarMood.getProgress() + 
					 "&temperature=" + URLEncoder.encode(temperature);
		download = new HttpGetJSONConnection(url, mHandler);
		download.start();
	}

}
