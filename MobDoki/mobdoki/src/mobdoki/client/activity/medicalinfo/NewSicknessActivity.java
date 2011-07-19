package mobdoki.client.activity.medicalinfo;

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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class NewSicknessActivity extends Activity {
	HttpGetConnection downloadSickness = null;	// szal a betegsegek letoltesehez
	HttpGetConnection download = null;			// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsickness);
        
        // Hozzaadas gomb esemenykezeloje
        Button addButton = (Button) findViewById(R.newsickness.add);
        addButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) addRequest();
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.newsickness.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
    
    // Indulaskor a betegsegek lekerdezese  
    @Override
    public void onStart() {
    	super.onStart();
    	getSickness();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.newsickness.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if (downloadSickness!=null && downloadSickness.isAlive()) {
    		downloadSickness.stop(); downloadSickness=null;
    	}
    }
    
    // A betegsegek betolteset kezelo Handler
    public Handler sicknessHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					if(downloadSickness.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("NewSicknessActivity","Betegsegek betoltve");
				        AutoCompleteTextView sickness = (AutoCompleteTextView) findViewById(R.newsickness.sickness);
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, downloadSickness.getJSONStringArray("names"));
				        sickness.setAdapter(adapter);
					}
					break;
			}
		}
	};
    
	// A megadott betegseg felvetelet kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.newsickness.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("NewSicknessActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.hasMessage()) {
						String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("NewSicknessActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
    
	// Hozzaadas keres
    private void addRequest(){
    	
    	String sickness = ((EditText)findViewById(R.newsickness.sickness)).getText().toString();	// a mezobe beirt betegseg
    	
    	if (sickness.equals("")) {																	// ha nincs megadva adat: hibauzenet
    		Toast.makeText(activity, "Adja meg a betegség nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	((ProgressBar)findViewById(R.newsickness.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "NewSickness?sickness=" + URLEncoder.encode(sickness);
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
    
    // Betegsegek lekerdezese
    private void getSickness(){
    	String url = "GetAll?table=Sickness";
	    downloadSickness = new HttpGetConnection(url, sicknessHandler);
	    downloadSickness.start();
    }
}
