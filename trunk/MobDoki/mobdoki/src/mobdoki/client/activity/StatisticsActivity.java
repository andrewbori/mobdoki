package mobdoki.client.activity;

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
import android.widget.Toast;

public class StatisticsActivity extends Activity implements OnClickListener {
	private HttpGetJSONConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.statistics);
        
        setTitle("MobDoki: Statisztika");
        
        ((Button) findViewById(R.statistics.refresh)).setOnClickListener(this);
        ((Button) findViewById(R.statistics.back)).setOnClickListener(this);
    }
    
    // Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
			// Frissites gomb esemenykezeloje
			case R.statistics.refresh:
				if (download==null || download.isNotUsed()) statisticsRequest();
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
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }
    
    // A lekerdezett statisztikat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			switch(msg.arg1){
				case 0:
					Log.v("StatisticsActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {																		// Ha sikeres lekerdezes
						((EditText)findViewById(R.statistics.sicknessCnt)).setText(download.getString("sickness"));				// mutatok bellitasa
						((EditText)findViewById(R.statistics.symptomCnt)).setText(download.getString("symptom"));
						((EditText)findViewById(R.statistics.symptomAvg)).setText(download.getString("symptomAvarage"));
						((EditText)findViewById(R.statistics.hospitalCnt)).setText(download.getString("hospital"));
						((EditText)findViewById(R.statistics.patientCnt)).setText(download.getString("patient"));
					} else {
						String message = download.getMessage();
						Log.v("StatisticsActivity",message);
						Toast.makeText(activity, download.getMessage(), Toast.LENGTH_LONG).show();
					}
					break;
			}
			download.setNotUsed();
		}
	};
    
	// Statisztika lekerdezesenek kezdemenyezese
    private void statisticsRequest(){
    	
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "Statistics?ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler);
	    download.start();
    }
}
