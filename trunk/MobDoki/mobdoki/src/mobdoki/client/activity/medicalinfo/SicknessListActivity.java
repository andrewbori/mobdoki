package mobdoki.client.activity.medicalinfo;

import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SicknessListActivity extends Activity {
	HttpGetConnection download = null;				// szal a webszerverhez csatlakozashoz
	private Activity activity=this;
	private ArrayList<String> listElements = null;	// betegsegek listaja
	
	private ListView listview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exploresickness);
      
		// Frissites gomb esemenykezeloje
		Button refreshButton = (Button) findViewById(R.exploresickness.refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {
	      	public void onClick(View view) {
	      		if (download==null || (download!=null && !download.isAlive())) refreshRequest();
	      	}
		});
      
		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.exploresickness.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		// A betegsegek listajanak esemenykezeloje
		listview = (ListView) findViewById(R.exploresickness.sicknesslist);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
      		
				Log.v("SicknessListActivity",listElements.get(position));
				
				Intent myIntent = new Intent(activity, SicknessInfoActivity.class);
				myIntent.putExtra("sickness",listElements.get(position));
				startActivity(myIntent);
			}
		});
	}
	
	// Indulaskor a lista lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	refreshRequest();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.addsymptom.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    }

    // Lekerdezett listat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.exploresickness.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("SicknessListActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem �rhet� el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if(download.isOK()) {					// ha sikeres lekerdezes: lista feltotlese
						Log.v("SicknessListActivity","Sikeres keres�s");
						listElements = download.getJSONStringArray("names");
						listview = (ListView) findViewById(R.exploresickness.sicknesslist);
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
																				R.layout.listview_item,  
																				listElements);  
						listview.setAdapter(adapter);
					}
					else {
						Toast.makeText(activity, download.getMessage(), Toast.LENGTH_LONG).show();
					}
					break;
			}
		}
	};
	
	// Lista lekeresenek/frissitesenek kezdemenyezese
    private void refreshRequest(){    	
    	((ProgressBar)findViewById(R.exploresickness.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "GetAll?table=Sickness";
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
}
