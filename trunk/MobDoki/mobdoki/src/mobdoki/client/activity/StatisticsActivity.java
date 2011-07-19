package mobdoki.client.activity;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class StatisticsActivity extends Activity {
	HttpGetConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        
        // Frissites gomb esemenykezeloje
        Button refreshButton = (Button) findViewById(R.statistics.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) statisticsRequest();
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.statistics.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
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
    		((ProgressBar)findViewById(R.statistics.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    }
    
    // A lekerdezett statisztikat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.statistics.progress)).setVisibility(ProgressBar.INVISIBLE);
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
    	
    	((ProgressBar)findViewById(R.statistics.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "Statistics";
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
}
