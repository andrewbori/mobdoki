package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

public class SearchSicknessActivity extends Activity implements OnClickListener {
	private final int TASK_GETSYMPTOMS=1;
	private final int TASK_SEARCH=2;
	
	private HttpGetJSONConnection download = null;				// szal a webszerverhez csatlakozashoz
	private HttpGetJSONConnection downloadSymptom = null;		// szál a tunetek letoltesehez
	
	HashMap<String, Integer> lines = null;			// betegsegek
	private ArrayList<String> listElements;			// betegsegek (talalatok listaja)
	private ArrayList<String> listSymptom;			// tunetek listaja
	
	private Activity activity=this;
	private AlertDialog symptomDialog;
	private MultiAutoCompleteTextView symptomsText;
	private ListView listview;						// talalati lista
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.searchsickness);
		setTitle("MobDoki: Betegség keresése");
	
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
	
		symptomsText = (MultiAutoCompleteTextView) findViewById(R.searchsickness.symptoms);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item,
        																  new ArrayList<String>());
        symptomsText.setAdapter(adapter);
        symptomsText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        ((ImageButton) findViewById(R.searchsickness.buttonSymptom)).setOnClickListener(this);
		((Button) findViewById(R.searchsickness.search)).setOnClickListener(this);
	}
	
	// Kattintas esemenykezeloje
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.searchsickness.buttonSymptom:
			if (symptomDialog!=null) symptomDialog.show();
			break;
		case R.searchsickness.search:
			if (download==null || download.isNotUsed()) searchRequest();
			break;
		}	
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
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    	if (downloadSymptom!=null && downloadSymptom.isUsed()) {
    		downloadSymptom.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }
	
    // 
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			// Tunetek betoltese
			case TASK_GETSYMPTOMS:
				if (msg.arg1==1 && downloadSymptom.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
					Log.v("SearchSicknessActivity","Tunetek betoltve");
					
					listSymptom = downloadSymptom.getStringArrayList("names");
			        MultiAutoCompleteTextView symptoms = (MultiAutoCompleteTextView) findViewById(R.searchsickness.symptoms);	// Autocomplete lista
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, listSymptom);
			        symptoms.setAdapter(adapter);
			        symptoms.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
			        
			        listSymptom.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)

			        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			        builder.setItems(downloadSymptom.getStringArray("names", true), new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int position) {
			            	if (position!=0) {
			            		symptomsText.setText(symptomsText.getText() + listSymptom.get(position) + ", ");			// TextView-ba a kivalasztott hozzadasa
			                } else {
			                	symptomsText.setText("");			// TextView torlese
			                }
			            }
			        });
			        symptomDialog = builder.create();
				}
				downloadSymptom.setNotUsed();
				break;	
			
			// Talalatok listazasat kezelo Handler
			case TASK_SEARCH:
				switch(msg.arg1){
				case 0:
					Log.v("SearchSicknessActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {
						Log.v("SearchSicknessActivity","Sikeres keresés");
						
						listElements = download.getStringArrayList("sicknesses");
						ArrayList<String> listElements2 = download.getStringArrayList("list");
						
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
				download.setNotUsed();
				break;
			}
			if (!((download!=null && download.isUsed()) || (downloadSymptom!=null && downloadSymptom.isUsed()))) setProgressBarIndeterminateVisibility(false);
		}
	};
	
	// Kereses kezdemenyezes
    private void searchRequest(){
    	
    	String symptoms = ((MultiAutoCompleteTextView)findViewById(R.searchsickness.symptoms)).getText().toString();		// a mezobe beirt tunetek beolvasasa
    	
    	if (symptoms.equals("")) {																					// ha nincs adat: hibauzenet
    		Toast.makeText(activity, "Adja meg a tüneteket!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "SearchSickness?symptoms=" + URLEncoder.encode(symptoms) + "&ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler, TASK_SEARCH);
	    download.start();
    }
    
    // Tunetek lekerdezese
    private void getSymptom(){
    	setProgressBarIndeterminateVisibility(true);
    	
    	String url = "GetAll?table=Symptom"  + "&ssid=" + UserInfo.getSSID();
	    downloadSymptom = new HttpGetJSONConnection(url, mHandler, TASK_GETSYMPTOMS);
	    downloadSymptom.start();
    }
}
