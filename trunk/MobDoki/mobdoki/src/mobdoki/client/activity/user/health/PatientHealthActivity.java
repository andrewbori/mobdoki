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
	private EditText bp1Text;
	private EditText bp2Text;
	private EditText pulseText;
	private EditText tempText;
	private EditText weightText;
	
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
        
        bp1Text = (EditText)findViewById(R.patienthealth.bloodpressure1);		// Vernyomas: systoles erek
		bp2Text = (EditText)findViewById(R.patienthealth.bloodpressure2);		// Vernyomas: diastoles ertek
		pulseText = (EditText)findViewById(R.patienthealth.pulse);				// Pulzus
		tempText = (EditText)findViewById(R.patienthealth.temperature);			// Testhomerseklet
		weightText = (EditText)findViewById(R.patienthealth.weight);			// Testtomeg
		
		((Button) findViewById(R.patienthealth.clean)).setOnClickListener(this);
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
			case R.patienthealth.clean:
				bp1Text.setText("");
				bp2Text.setText("");
				pulseText.setText("");
				tempText.setText("");
				weightText.setText("");
				seekbarMood.setProgress(0);
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
		String bp1 = bp1Text.getText().toString();				// Vernyomas: systoles erek
		String bp2 = bp2Text.getText().toString();				// Vernyomas: diastoles ertek
		String pulse = pulseText.getText().toString();			// Pulzus
		String temperature = tempText.getText().toString();		// Testhomerseklet
		String weight = weightText.getText().toString();		// Testtomeg
		
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
