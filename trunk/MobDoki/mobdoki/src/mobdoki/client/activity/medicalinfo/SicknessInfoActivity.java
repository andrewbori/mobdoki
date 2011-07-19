package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class SicknessInfoActivity extends Activity {
	private String sickness;								// adott betegseg neve
	HttpGetConnection download = null;						// szal a tunetek es korhazak letoltesehez
	private Activity activity=this;
	private ArrayList<String> listSymptoms = null;			// betegseg tuneteinek listaja
	private ArrayList<String> listHospitals = null;			// betegseget kezelo korhazak nevenek listaja
	private ArrayList<GeoPoint> listCoordinates = null;		// betegseget kezelo korhazak koordinatainak listaja
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sicknessinfo);
      
		Bundle extras = getIntent().getExtras();
		sickness = extras.getString("sickness");

		((TextView)findViewById(R.sicknessinfo.sickness)).setText(sickness);
		
		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.sicknessinfo.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		// A korhazak listajanak esemenykezeloje
		ListView listview = ((ListView) findViewById(R.sicknessinfo.hospitallist));
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				Intent myIntent = new Intent(activity,ShowHospitalActivity.class);
				myIntent.putExtra("x", listCoordinates.get(position).getLatitudeE6());
				myIntent.putExtra("y", listCoordinates.get(position).getLongitudeE6());
				myIntent.putExtra("hospital", listHospitals.get(position));	
				Log.v("SicknessInfoActivity",listHospitals.get(position));
				//Intent myIntent = new Intent();
				// myIntent.setClassName("onlab.aprohirdetes","onlab.aprohirdetes.MyGoogleMaps");
				startActivity(myIntent);
			}
		});
		
		// A tunetek listajanak esemenykezeloje
		ListView symptomview = (ListView)findViewById(R.sicknessinfo.symptomlist);
		symptomview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				Intent myIntent = new Intent(activity,ShowSymptomPictureActivity.class);
				myIntent.putExtra("symptom", listSymptoms.get(position));
				startActivity(myIntent);
			}
		});
		
	}
	
	// Indulaskor a lista lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	getData();
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
			((ProgressBar)findViewById(R.sicknessinfo.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("SicknessInfoActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {		// Ha sikeres lekerdezes...
						Log.v("SicknessInfoActivity","Sikeres lekerdezes");

						
						listSymptoms = download.getJSONStringArray("symptom");				// lekerdezett tunetek
						listHospitals = download.getJSONStringArray("hospital");			// lekerdezett korhazak neve
						listCoordinates = new ArrayList<GeoPoint>();						// 						koordinatai
						
						JSONArray coordinates = download.getJSONArray("coordinates");
						try {
							for (int i=0; i<coordinates.length(); i++) {
								JSONObject xy = coordinates.getJSONObject(i);
								int x = (int)(xy.getDouble("x")*1E6);
								int y = (int)(xy.getDouble("y")*1E6);
								listCoordinates.add(new GeoPoint(x,y));
							}
						} catch (Exception e) {
							Log.v("SicknessInfoActivity","Sikertelen korhazlistazas.");
							listHospitals = null;
						}
						
						((ListView) findViewById(R.sicknessinfo.symptomlist)).setAdapter (new ArrayAdapter<String>(activity,	// Listak feltoltese
								  R.layout.listview_item,  
								  listSymptoms));
						((ListView) findViewById(R.sicknessinfo.hospitallist)).setAdapter (new ArrayAdapter<String>(activity,
														  R.layout.listview_item,  
														  listHospitals));
					} else Toast.makeText(activity, download.getMessage(), Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	
	// Lista lekeresenek kezdemenyezese
    private void getData(){
    	((ProgressBar)findViewById(R.sicknessinfo.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "SicknessInfo?sickness=" + URLEncoder.encode(sickness);
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
}
