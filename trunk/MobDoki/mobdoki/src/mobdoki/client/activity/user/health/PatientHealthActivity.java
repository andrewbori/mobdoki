package mobdoki.client.activity.user.health;


import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class PatientHealthActivity extends Activity {
	private String username;				// Felhasznalo azonositoja
	private HttpGetConnection download;		// szal a szerverhez csatlakozashoz
	private Activity activity= this;
	private Spinner spinner;
	
	private String[] currencies = 
    {
    	"1",
    	"2",
    	"3",
    	"4",
    	"5",
    	"6",
    	"7",
    	"8",
    	"9",
    	"10"
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.patienthealth);
		
		Bundle extras = getIntent().getExtras();
		username = extras.getString("username");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>
		(this,android.R.layout.simple_spinner_item,currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spinner = (Spinner)findViewById(R.patienthealth.spinner1);
        spinner.setAdapter(adapter);
        spinner.setSelection(4);
		
		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.patienthealth.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		// Feltoltes gomb esemenykezeloje
		((Button)findViewById(R.patienthealth.upload)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				uploadData();
			}
		});
	}
	
	// Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.patienthealth.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    }
	
	 // Az adatok feltolteset kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.patienthealth.progress)).setVisibility(ProgressBar.INVISIBLE);
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
		
    	((ProgressBar)findViewById(R.patienthealth.progress)).setVisibility(ProgressBar.VISIBLE);
    	
		String url = "PatientHealth?username=" + URLEncoder.encode(username) + 
					 "&bp1=" + URLEncoder.encode(bp1) + "&bp2=" + URLEncoder.encode(bp2) + "&pulse=" + URLEncoder.encode(pulse) + 
					 "&weight=" + URLEncoder.encode(weight) + "&mood=" + URLEncoder.encode((String)spinner.getSelectedItem()) + 
					 "&temperature=" + URLEncoder.encode(temperature);
		download = new HttpGetConnection(url,mHandler);
		download.start();
	}

}
