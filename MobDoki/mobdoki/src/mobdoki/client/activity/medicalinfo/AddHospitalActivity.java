package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class AddHospitalActivity extends Activity {
	HttpGetConnection downloadSickness = null;		// szal a betegsegek letoltesehez
	HttpGetConnection downloadHospital = null;		// szl a korhazak letoltesehez
	HttpGetConnection download = null;				// szal a webszerverhez csatlakozashoz
	private Activity activity=this;
	
	private ArrayList<String> listSickness;		// betegsegek listaja
	private ArrayList<String> listHospital;		// korhazak listaja
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addhospital);
        
        // Hozzaadas gomb esemenykezeloje
        Button addButton = (Button) findViewById(R.addhospital.add);
        addButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) addRequest();
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.addhospital.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        
     // A betegsegek spinnerjenek elemkivalaszto esemenykezeloje
        Spinner spinnerSickness = (Spinner) findViewById(R.addhospital.spinnerSickness);
        spinnerSickness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
	            	AutoCompleteTextView sickness = (AutoCompleteTextView) findViewById(R.addhospital.sickness);
	                sickness.setText(listSickness.get(position));							// texview-ba  a kivalasztott
	                Spinner spinner = (Spinner) findViewById(R.addhospital.spinnerSickness);
	                spinner.setSelection(0);												// visszaugras a 0. elemre (ami ures)
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        // A tunetek spinnerjenek elemkivalaszto esemenykezeloje
        Spinner spinnerHospital = (Spinner) findViewById(R.addhospital.spinnerHospital);
        spinnerHospital.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
	            	AutoCompleteTextView symptom = (AutoCompleteTextView) findViewById(R.addhospital.hospital);
	                symptom.setText(listHospital.get(position));								// TextView-ba a kivalasztott
	                Spinner spinner = (Spinner) findViewById(R.addhospital.spinnerHospital);
	                spinner.setSelection(0);													// visszaugras a 0. elemre (ami ures)
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    // Indulaskor betegsegek es korhazak lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	getSickness();
    	getHospital();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.addhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if (downloadSickness!=null && downloadSickness.isAlive()) {
    		downloadSickness.stop(); downloadSickness=null;
    	}
    	if (downloadHospital!=null && downloadHospital.isAlive()) {
    		downloadHospital.stop(); downloadHospital=null;
    	}
    }
    
    // A betegsegek betolteset kezelo Handler    
    public Handler sicknessHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					if(downloadSickness.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("AddHospitalActivity","Betegsegek betoltve");
				        AutoCompleteTextView sickness = (AutoCompleteTextView) findViewById(R.addhospital.sickness);		// Autocomplete lista
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
				        														downloadSickness.getJSONStringArray("names"));
				        sickness.setAdapter(adapter);
				        
				        listSickness = downloadSickness.getJSONStringArray("names");										// Spinner lista
				        listSickness.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        Spinner spinner = (Spinner) findViewById(R.addhospital.spinnerSickness);
				        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, 
				        								   listSickness);
				        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        spinner.setAdapter(adapter);
				        spinner.setSelection(0);
					}
					break;
			}
		}
	};
	
    // A korhazak betolteset kezelo Handler	
    public Handler hospitalHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					if(downloadHospital.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("AddHospitalActivity","Korházak betoltve");
				        AutoCompleteTextView hospital = (AutoCompleteTextView) findViewById(R.addhospital.hospital);		// Autocomplete lista
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
				        														downloadHospital.getJSONStringArray("names"));
				        hospital.setAdapter(adapter);
						
				        listHospital = downloadHospital.getJSONStringArray("names");										// Spinner lista
				        listHospital.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        Spinner spinner = (Spinner) findViewById(R.addhospital.spinnerHospital);
				        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, 
				        								   listHospital);
				        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        spinner.setAdapter(adapter);
				        spinner.setSelection(0);
					}
					break;
			}
		}
	};

	// A megadott betegseg-korhaz felvetelet kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.addhospital.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("AddHospitalActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.hasMessage()) {
						String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("AddHospitalActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
    
	// Hozzaadas keres
    private void addRequest(){
    	
    	String sickness = ((AutoCompleteTextView)findViewById(R.addhospital.sickness)).getText().toString();	// a mezobe beirt betegseg
    	String hospital = ((AutoCompleteTextView)findViewById(R.addhospital.hospital)).getText().toString();	// a mezobe beirt korhaz
    	
    	if (sickness.equals("")) {																				// Ha nincs adat megadva: hibauzenet
    		Toast.makeText(activity, "Adja meg a betegség nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (hospital.equals("")) {
    		Toast.makeText(activity, "Adja meg a kórház nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	((ProgressBar)findViewById(R.addhospital.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "AddHospital?sickness=" + URLEncoder.encode(sickness) + "&hospital=" + URLEncoder.encode(hospital);
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
    
    // Betegsegek lekerdezese
    private void getSickness(){
    	String url = "GetAll?table=Sickness";
	    downloadSickness = new HttpGetConnection(url, sicknessHandler);
	    downloadSickness.start();
    }
    
    // Korhazak lekerdezese
    private void getHospital(){
    	String url = "GetAll?table=Hospital";
	    downloadHospital = new HttpGetConnection(url, hospitalHandler);
	    downloadHospital.start();
    }
}
