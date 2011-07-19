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

public class AddSymptomActivity extends Activity {
	HttpGetConnection downloadSickness = null;		// szal a betegsegek letoltesehez
	HttpGetConnection downloadSymptom = null;		// szal a tunetek letoltesehez
	HttpGetConnection download = null;				// szal a webszerverhez csatlakozashoz
	private Activity activity=this;
	
	private ArrayList<String> listSickness;		// betegsegek listaja
	private ArrayList<String> listSymptom;		// tunetek listaja
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addsymptom);
        
        // Hozzaadas gomb esemenykezeloje
        Button addButton = (Button) findViewById(R.addsymptom.add);
        addButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) addRequest();
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.addsymptom.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        
        // A betegsegek spinnerjenek elemkivalaszto esemenykezeloje
        Spinner spinnerSickness = (Spinner) findViewById(R.addsymptom.spinnerSickness);
        spinnerSickness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
	            	AutoCompleteTextView sickness = (AutoCompleteTextView) findViewById(R.addsymptom.sickness);
	                sickness.setText(listSickness.get(position));							// texview-ba  a kivalasztott
	                Spinner spinner = (Spinner) findViewById(R.addsymptom.spinnerSickness);
	                spinner.setSelection(0);												// visszaugras a 0. elemre (ami ures)
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        // A tunetek spinnerjenek elemkivalaszto esemenykezeloje
        Spinner spinnerSymptom = (Spinner) findViewById(R.addsymptom.spinnerSymptom);
        spinnerSymptom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
	            	AutoCompleteTextView symptom = (AutoCompleteTextView) findViewById(R.addsymptom.symptom);
	                symptom.setText(listSymptom.get(position));								// TextView-ba a kivalasztott
	                Spinner spinner = (Spinner) findViewById(R.addsymptom.spinnerSymptom);
	                spinner.setSelection(0);												// visszaugras a 0. elemre (ami ures)
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    // Indulaskor a betegsegek es tunetek lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	getSickness();
    	getSymptom();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.addsymptom.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if (downloadSickness!=null && downloadSickness.isAlive()) {
    		downloadSickness.stop(); downloadSickness=null;
    	}
    	if (downloadSymptom!=null && downloadSymptom.isAlive()) {
    		downloadSymptom.stop(); downloadSymptom=null;
    	}
    }
    
    // A betegsegek betolteset kezelo Handler
    public Handler sicknessHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					if(downloadSickness.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("AddSymptomActivity","Betegsegek betoltve");
				        AutoCompleteTextView sickness = (AutoCompleteTextView) findViewById(R.addsymptom.sickness);				// Autocomplete lista
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
				        														downloadSickness.getJSONStringArray("names"));
				        sickness.setAdapter(adapter);
				        
				        listSickness = downloadSickness.getJSONStringArray("names");											// Spinner lista
				        listSickness.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        Spinner spinner = (Spinner) findViewById(R.addsymptom.spinnerSickness);
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
	
	// Tunetek betolteset kezelo Handler
    public Handler symptomHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					if(downloadSymptom.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("AddSymptomActivity","Tunetek betoltve");
				        AutoCompleteTextView symptom = (AutoCompleteTextView) findViewById(R.addsymptom.symptom);				// Autocomplete lista
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
				        													    downloadSymptom.getJSONStringArray("names"));
				        symptom.setAdapter(adapter);
				        
				        listSymptom = downloadSymptom.getJSONStringArray("names");												// Spinner lista
				        listSymptom.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        Spinner spinner = (Spinner) findViewById(R.addsymptom.spinnerSymptom);
				        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, 
				        								   listSymptom);
				        
				        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				        spinner.setAdapter(adapter);
				        spinner.setSelection(0);
					}
					break;
			}
		}
	};

	// Megadott elemek felvetelet kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.addsymptom.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("AddSymptomActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.hasMessage()) {
						String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("AddSymptomActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
    
	// Hozzaadas kezdemenyezes
    private void addRequest(){	
    	String sickness = ((AutoCompleteTextView)findViewById(R.addsymptom.sickness)).getText().toString();		// a mezobe beirt betegseg
    	String symptom = ((AutoCompleteTextView)findViewById(R.addsymptom.symptom)).getText().toString();		// a mezobe beirt tunet
    	
    	if (sickness.equals("")) {							// ha nincs megadva adat, akkor hibauzenet
    		Toast.makeText(activity, "Adja meg a betegség nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (symptom.equals("")) {
    		Toast.makeText(activity, "Adja meg a tünet nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	((ProgressBar)findViewById(R.addsymptom.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "AddSymptom?sickness=" + URLEncoder.encode(sickness) + "&symptom=" + URLEncoder.encode(symptom);
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }

    // Betegsegek lekerdezese
    private void getSickness(){
    	String url = "GetAll?table=Sickness";
	    downloadSickness = new HttpGetConnection(url, sicknessHandler);
	    downloadSickness.start();
    }
    
    // Tunetek lekerdezese
    private void getSymptom(){
    	String url = "GetAll?table=Symptom";
	    downloadSymptom = new HttpGetConnection(url, symptomHandler);
	    downloadSymptom.start();
    }
}
