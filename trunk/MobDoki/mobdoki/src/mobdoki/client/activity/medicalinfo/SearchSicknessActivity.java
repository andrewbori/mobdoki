package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchSicknessActivity extends Activity {
	HttpGetConnection download = null;				// szal a webszerverhez csatlakozashoz
	HttpGetConnection downloadSymptom = null;		// szál a tunetek letoltesehez
	private Activity activity=this;
	
	HashMap<String, Integer> lines = null;			// betegsegek
	private ArrayList<String> listElements;			// betegsegek (talalatok listaja)
	private ArrayList<String> listSymptom;			// tunetek listaja
	
	private ListView listview;						// talalati lista
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchsickness);
    
		// Kereses gomb esemenykezeloje
		Button searchButton = (Button) findViewById(R.searchsickness.search);
		searchButton.setOnClickListener(new View.OnClickListener() {
	      	public void onClick(View view) {
	      		if (download==null || (download!=null && !download.isAlive())) searchRequest();
	      	}
		});
   
		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.searchsickness.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
	
        // A tunetek spinnerjenek elemkivalaszto esemenykezeloje
        Spinner spinnerSymptom = (Spinner) findViewById(R.searchsickness.spinnerSymptom);
        spinnerSymptom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
                	MultiAutoCompleteTextView symptom = (MultiAutoCompleteTextView) findViewById(R.searchsickness.symptoms);
	                symptom.setText(symptom.getText() + listSymptom.get(position) + ", ");			// TextView-ba a kivalasztott hozzadasa
	                Spinner spinner = (Spinner) findViewById(R.searchsickness.spinnerSymptom);
	                spinner.setSelection(0);														// visszaugras a 0. elemre (ami ures)
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
	
		// Lista esemenykezeloje
		listview = (ListView) findViewById(R.searchsickness.sicknesslist);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
      		
				Log.v("SearchSicknessActivity",listElements.get(position));
				
				Intent myIntent = new Intent(activity, SicknessInfoActivity.class);
				myIntent.putExtra("sickness",listElements.get(position));
				startActivity(myIntent);
			}
		});
	
		MultiAutoCompleteTextView symptoms = (MultiAutoCompleteTextView) findViewById(R.searchsickness.symptoms);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
        																  new ArrayList<String>());
        symptoms.setAdapter(adapter);
        symptoms.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

	}
	
	// Indulaskor a tunetek lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	getSymptom();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.searchsickness.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if (downloadSymptom!=null && downloadSymptom.isAlive()) {
    		downloadSymptom.stop(); downloadSymptom=null;
    	}
    }
	
    // Tunetek betolteset kezelo Handler
	public Handler symptomHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch(msg.arg1){
				case 1:
					if(downloadSymptom.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("SearchSicknessActivity","Tunetek betoltve");
				        MultiAutoCompleteTextView symptoms = (MultiAutoCompleteTextView) findViewById(R.searchsickness.symptoms);	// Autocomplete lista
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
				        														downloadSymptom.getJSONStringArray("names"));
				        symptoms.setAdapter(adapter);
				        symptoms.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
				        
				        listSymptom = downloadSymptom.getJSONStringArray("names");												// Spinner lista
				        listSymptom.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        Spinner spinner = (Spinner) findViewById(R.searchsickness.spinnerSymptom);
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

	// Talalatok listazasat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.searchsickness.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("SearchSicknessActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {
						Log.v("SearchSicknessActivity","Sikeres keresés");
						int num = download.getJSONInt("size");									// Tuntek szama (amelyek alapjan kerestunk)
						
						HashMap<String, Integer> sicknesses = new HashMap<String, Integer>();	// betegsegek + talalati darabszamuk
						listElements = new ArrayList<String>();
						
						for (String sickness : download.getJSONStringArray("sicknesses")) {		// Talalatok beolvasasa, elofordulas szamolasa
							if (sicknesses.containsKey(sickness)) {										// Ha mar a listaban van a betegseg: darabszam novelese
								sicknesses.put(sickness, sicknesses.get(sickness)+1);
							} else {															// Ha nem, akkor felvetel, 1-es darabszammal (es a listahoz hozzaadas)
								sicknesses.put(sickness, 1);
								listElements.add(sickness);
							}
						}
						
						ArrayList<String> listElements2 = new ArrayList<String>();
						for (int i=0; i<listElements.size(); i++) {				// megjelenitendo listaelemek kiegeszitese szazalekos egyezessel
							String sickness = listElements.get(i);
							listElements2.add(sickness + " (" + (int)(((float)sicknesses.get(sickness))/num*100.0) + "%)");	
						}
						
						listview = (ListView) findViewById(R.searchsickness.sicknesslist);
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
																				R.layout.listview_item,  
																				listElements2);  
						listview.setAdapter(adapter);
					}
					else {
						Toast.makeText(activity, download.getMessage(), Toast.LENGTH_LONG).show();
					}
					
					break;
			}
		}
	};
	
	// Kereses kezdemenyezes
    private void searchRequest(){
    	
    	String symptoms = ((MultiAutoCompleteTextView)findViewById(R.searchsickness.symptoms)).getText().toString();		// a mezobe beirt tunetek beolvasasa
    	
    	if (symptoms.equals("")) {																					// ha nincs adat: hibauzenet
    		Toast.makeText(activity, "Adja meg a tüneteket!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	((ProgressBar)findViewById(R.searchsickness.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "SearchSickness?symptoms=" + URLEncoder.encode(symptoms);
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
    
    // Tunetek lekerdezese
    private void getSymptom(){
    	String url = "GetAll?table=Symptom";
	    downloadSymptom = new HttpGetConnection(url, symptomHandler);
	    downloadSymptom.start();
    }
}
