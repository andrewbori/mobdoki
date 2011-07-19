package mobdoki.client.activity.medicalinfo;

import java.io.File;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.activity.FileChooserActivity;
import mobdoki.client.connection.HttpGetConnection;
import mobdoki.client.connection.HttpPostUpConnection;

import org.apache.http.entity.FileEntity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class AddPicToSymptomActivity extends Activity{
	
	HttpPostUpConnection upload = null;			// szal a kep feltoltesehez
	HttpGetConnection downloadSymptom = null;	// szal a tunetek letoltesehez
	private Activity activity=this;
	private ArrayList<String> listSymptom;		// tunetek listaja
	private ImageView img;						// A tunet kepe
	private File file = null;					// A tunethez kivalaszott kepfajl
	private String filepath = null;				// A tunethez kivalasztott kepfajl utvonala
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.addpictosymp);
		
		// A kepre kattintas esemenykezeloje
		img = (ImageView)findViewById(R.addpictosymp.image);
		img.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(activity,FileChooserActivity.class);	// filechooser betoltese a kep kivalasztasahoz
				startActivityForResult(myIntent,0);
				
			}
		});
		
		// Hozzaadas gomb esemenykezeloje
        Button addButton = (Button) findViewById(R.addpictosymp.addbutton);
        addButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (upload==null || (upload!=null && !upload.isAlive())) 
        			addRequest();	// Feltoltes
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.addpictosymp.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        
        // A tunetek spinnerjenek elemkivalaszto esemenykezeloje
        Spinner spinnerSymptom = (Spinner) findViewById(R.addpictosymp.spinnersymptom);
        spinnerSymptom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position!=0) {
	            	AutoCompleteTextView symptom = (AutoCompleteTextView) findViewById(R.addpictosymp.symptom2);
	                symptom.setText(listSymptom.get(position));								// TextView-ba a kivalasztott
	                Spinner spinner = (Spinner) findViewById(R.addpictosymp.spinnersymptom);
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
    	getSymptom();
    }
    
    // FileChooser eredmenye: a kivalasztott fajl feldolgozasa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        	if (resultCode==RESULT_OK) {					// Ha van kivalasztott fajl:
        		Bundle extras = intent.getExtras();
                if (extras != null) {
        			filepath = extras.getString("filepath");
        		}
                file = new File(filepath);
                img.setImageBitmap(BitmapFactory.decodeFile(filepath));
                ((EditText)findViewById(R.addpictosymp.picturename)).setText(file.getName());
        	}
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (upload!=null && upload.isAlive()) {
    		upload.stop(); upload=null;
    		((ProgressBar)findViewById(R.addpictosymp.progress)).setVisibility(ProgressBar.INVISIBLE);
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
						Log.v("AddPicToSymptomActivity","Tunetek betoltve");
				        AutoCompleteTextView symptom = (AutoCompleteTextView) findViewById(R.addpictosymp.symptom2);				// Autocomplete lista
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
				        													    downloadSymptom.getJSONStringArray("names"));
				        symptom.setAdapter(adapter);
				        
				        listSymptom = downloadSymptom.getJSONStringArray("names");													// Spinner lista
				        listSymptom.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        Spinner spinner = (Spinner) findViewById(R.addpictosymp.spinnersymptom);
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
			((ProgressBar)findViewById(R.addpictosymp.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("AddPicToSymptomActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (upload.hasMessage()) {
						String message = upload.getMessage();
						Log.v("AddPicToSymptomActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
	
	// Hozzaadas kezdemenyezes (kep feltoltese)
    private void addRequest(){	
    	String symptom = ((AutoCompleteTextView)findViewById(R.addpictosymp.symptom2)).getText().toString();		// a mezobe beirt tunet
    	
    	if (symptom.equals("")) {																					// Tunet meg van adva?
    		Toast.makeText(activity, "Adja meg a tünet nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (file==null) {																							// Kep meg van adva?
    		Toast.makeText(activity, "Adja meg a feltöltendõ képet!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	((ProgressBar)findViewById(R.addpictosymp.progress)).setVisibility(ProgressBar.VISIBLE);
    	FileEntity fe = new FileEntity(file, URLConnection.guessContentTypeFromName(file.getName()));
	    String url = "AddPictureToSymptom?symptom=" + URLEncoder.encode(symptom);
	    upload = new HttpPostUpConnection(url, mHandler, fe);
	    upload.start();
    }
    
    // Tunetek lekerdezese
    private void getSymptom(){
    	String url = "GetAll?table=Symptom";
	    downloadSymptom = new HttpGetConnection(url, symptomHandler);
	    downloadSymptom.start();
    }

}
