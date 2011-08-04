package mobdoki.client.activity;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class StatisticsActivity extends Activity implements OnClickListener {
	HttpGetConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
	private ProgressBar progressbar;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        
        // Gombokra kattintas esemenykezeloje maga az osztaly
        ((Button) findViewById(R.statistics.refresh)).setOnClickListener(this);
        ((Button) findViewById(R.statistics.back)).setOnClickListener(this);
        
        progressbar = (ProgressBar)findViewById(R.statistics.progress);
    }
    
    // Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
			// Frissites gomb esemenykezeloje
			case R.statistics.refresh:
				if (download==null || (download!=null && !download.isAlive())) statisticsRequest();
				break;
		    
		    // Vissza gomb esemenykezeloje
			case R.statistics.back:
				finish();
				break;
		}
	}
    
    // Indulaskor a statiszikai adatok lekerdezese
    @Override
    public void onStart () {
    	super.onStart();
    	statisticsRequest();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		progressbar.setVisibility(View.INVISIBLE);
    	}
    }
    
    // A lekerdezett statisztikat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressbar.setVisibility(View.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("StatisticsActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {																		// Ha sikeres lekerdezes
						((EditText)findViewById(R.statistics.sicknessCnt)).setText(download.getJSONString("sickness"));				// mutatok bellitasa
						((EditText)findViewById(R.statistics.symptomCnt)).setText(download.getJSONString("symptom"));
						((EditText)findViewById(R.statistics.symptomAvg)).setText(download.getJSONString("symptomAvarage"));
						((EditText)findViewById(R.statistics.hospitalCnt)).setText(download.getJSONString("hospital"));
						((EditText)findViewById(R.statistics.patientCnt)).setText(download.getJSONString("patient"));
					} else {
						Log.v("StatisticsActivity",download.getMessage());
						Toast.makeText(activity, download.getMessage(), Toast.LENGTH_LONG).show();
					}
					break;
			}
		}
	};
    
	// Statisztika lekerdezesenek kezdemenyezese
    private void statisticsRequest(){
    	
    	progressbar.setVisibility(View.VISIBLE);
    	
	    String url = "Statistics";
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
}
