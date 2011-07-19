package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class NewHospitalActivity extends Activity {
	HttpGetConnection downloadHospital = null;	// szal a korhazak letoltesehez
	HttpGetConnection downloadGeoCode = null;		// szal a korhaz cimenek geokodolasahoz
	HttpGetConnection download = null;			// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
	private String name;		// megadott korhaz neve
	private String address;		// megadott korhaz cime
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newhospital);
        
        // Hozzaadas gomb esemenykezeloje
        Button addButton = (Button) findViewById(R.newhospital.add);	// Hozzaadas az adatbazishoz
        addButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) addRequest();
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.newhospital.back);	// kilepes az activitibol
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
  
    // Indulaskor a korhazak lekerdezese
    @Override
    public void onStart() {		// kezdesnel a korhazak letoltese az adatbazisbol
    	super.onStart();
    	getHospital();
    }

    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {		// megszakitaskor a futo szalak leallitasa
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.newhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if (downloadHospital!=null && downloadHospital.isAlive()) {
    		downloadHospital.stop(); downloadHospital=null;
    	}
    }

    // A korhazak betolteset kezelo Handler
    public Handler hospitalHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					if(downloadHospital.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("NewHospitalActivity","Korhazak betoltve");
				        AutoCompleteTextView hospital = (AutoCompleteTextView) findViewById(R.newhospital.hospital);
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, downloadHospital.getJSONStringArray("names"));
				        hospital.setAdapter(adapter);
					}
					break;
			}
		}
	};

    // A geokodolas eredmenyet feldolgozo Handler
    public Handler geoCodeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 0:
					((ProgressBar)findViewById(R.newhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
					Log.v("NewHospitalActivity","Sikertelen GeoCode lekeres.");
					Toast.makeText(activity, "Nem sikerült a megadott címet kódolni.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					JSONObject json = downloadGeoCode.getJSON();
					try {
						if (json.getJSONObject("Status").getInt("code") == 200) {	// Ha ervenyes a megadott cim
							JSONArray coordinates = json.getJSONArray("Placemark").getJSONObject(0).getJSONObject("Point").getJSONArray("coordinates");
							double longitude=coordinates.getDouble(0);	// y		// koordinatak lekerdezese
							double latitude=coordinates.getDouble(1);	// x
	
		    	            String url = "NewHospital?name=" + URLEncoder.encode(name) + "&address=" + URLEncoder.encode(address) + 
		    	            																 "&x=" + latitude +
		    	            																 "&y=" + longitude;
		    	        	download = new HttpGetConnection(url, mHandler);		// a megadott korhaz felvetele az adatbazisba
		    	        	download.start();      
						} else {													// Ha a megadott cim ervenytelen
							((ProgressBar)findViewById(R.newhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
		            		Toast.makeText(activity, "A megadott cím hibás.", Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						((ProgressBar)findViewById(R.newhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
	            		Toast.makeText(activity, "Geokódolási hiba.", Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
	
	// A megadott korhaz felvetelet kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.newhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("NewHospitalActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el", Toast.LENGTH_LONG).show();
					break;
				case 1:
					
					if (download.hasMessage()) {
						String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("NewHospitalActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
    
	// Hozzaadas keres
    private void addRequest(){
    	
    	name = ((AutoCompleteTextView)findViewById(R.newhospital.hospital)).getText().toString();	// a mezobe beirt korhaz neve
    	address = ((EditText)findViewById(R.newhospital.address)).getText().toString();				// a mezobe beirt korhaz cime
    	
    	if (name.equals("")) {																		// ha nincs adat megadva: hibauzenet
    		Toast.makeText(activity, "Adja meg a kórház nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (address.equals("")) {
    		Toast.makeText(activity, "Adja meg a kórház címét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	((ProgressBar)findViewById(R.newhospital.progress)).setVisibility(ProgressBar.VISIBLE);

    	String url = "http://maps.google.com/maps/geo?key=yourkeyhere&output=json&q="+URLEncoder.encode(address);
    	downloadGeoCode = new HttpGetConnection(geoCodeHandler);
    	downloadGeoCode.setURL(url);
    	downloadGeoCode.start();		// megadott cim kodolasa
    }
 
    // Korhazak lekerdezese
    private void getHospital(){
    	String url = "GetAll?table=Hospital";
	    downloadHospital = new HttpGetConnection(url, hospitalHandler);
	    downloadHospital.start();
    }
}
