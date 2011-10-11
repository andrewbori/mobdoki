package mobdoki.client.activity.medicalinfo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.R.editsickness;
import mobdoki.client.activity.FileChooserActivity;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpPostConnection;
import mobdoki.client.connection.UserInfo;

import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class EditSicknessActivity extends Activity implements OnClickListener, OnItemClickListener, TextWatcher {
	private final int TASK_GETDATA = 1;
	private final int TASK_SETSICKNESS = 2;
	private final int TASK_DELETESICKNESS = 3;
	private final int TASK_SETSYMPTOM = 4;
	private final int TASK_UPLOAD = 5;
	private final int TASK_GETSICKNESSINFO = 6;
	
	private HttpGetJSONConnection downloadData = null;		// szal a betegsegek es tunetek letoltesehez
	private HttpGetJSONConnection downloadSicknessInfo = null;		// szal a betegsegek adatainak letoltesehez
	private HttpGetJSONConnection download1 = null;			// szal a webszerverhez csatlakozashoz
	private HttpPostConnection upload1 = null;				// szal a webszerverhez csatlakozashoz
	private HttpGetJSONConnection download2 = null;			// szal a webszerverhez csatlakozashoz
	private HttpPostConnection upload = null;				// szal a kep feltoltesehez
	
	private ArrayList<String> listSickness;			// betegsegek listaja
	private ArrayList<String> listSymptom;			// tunetek listaja
	
	private String sicknessName = "";				// betoltott betegseg neve
	private float sicknessSeriousness = 0;			//					  sulyossaga
	private String sicknessDetails = "";			// 					  leirasa
	private ArrayList<String> listDiagnosis = new ArrayList<String>(); // tunetei
	private ListView diagnosisList;
	
	private ImageView img;						// A tunet kepe
	private File file = null;					// A tunethez kivalaszott kepfajl
	private String filepath = null;				// A tunethez kivalasztott kepfajl utvonala
	
	private Activity activity = this;
	private TabHost tabs;
	private Button saveSicknessButton;
	private Button addSymptomButton;
	private Button addPictureButton;
	private Button deleteSicknessButton;
	private Button deleteSymptomButton;
	private Button backButton;
	
	private AlertDialog sicknessDialog;
	private AlertDialog symptomDialog;
	
	private ProgressDialog progress;
	
	private AutoCompleteTextView sicknessText1;
	private AutoCompleteTextView sicknessText2;
	private AutoCompleteTextView symptomText2;
	private AutoCompleteTextView symptomText3;
	
	private RatingBar rating;
	private EditText detailsText1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.editsickness);
        setTitle("MobDoki: Betegségek adminisztrációja");
        
        // Tabok beallitasa
 		tabs = (TabHost) findViewById(R.id.tabhost);
 		tabs.setup();
 		 
 		TabHost.TabSpec spec = tabs.newTabSpec("sicknessTab");   
 		spec.setContent(R.editsickness.sicknessTab);
 	   	spec.setIndicator("Betegség adatai");
   		tabs.addTab(spec);
 		
   		spec = tabs.newTabSpec("symptomTab");   
 		spec.setContent(R.editsickness.symptomTab);
 	   	spec.setIndicator("Diagnózis");
   		tabs.addTab(spec);
   		
   		spec = tabs.newTabSpec("pictureTab");   
 		spec.setContent(R.editsickness.pictureTab);
 	   	spec.setIndicator("Tünet");
   		tabs.addTab(spec);
 	    
 		tabs.setCurrentTab(0);
 		
 		for (int i = 0; i < tabs.getTabWidget().getTabCount(); i++) {
 		    tabs.getTabWidget().getChildAt(i).getLayoutParams().height = 45;
 		}
 		
 		tabs.setOnTabChangedListener(new OnTabChangeListener() {
 			@Override
 			public void onTabChanged(String arg0) {
 				switch (tabs.getCurrentTab()) {
	 				case 0:
	 					saveSicknessButton.setVisibility(Button.VISIBLE);
	 					deleteSicknessButton.setVisibility(Button.VISIBLE);
	 					addSymptomButton.setVisibility(Button.GONE);
	 					deleteSymptomButton.setVisibility(Button.GONE);
	 					addPictureButton.setVisibility(Button.GONE);
	 					break;
	 				case 1:
	 					saveSicknessButton.setVisibility(Button.GONE);
	 					deleteSicknessButton.setVisibility(Button.GONE);
	 					addSymptomButton.setVisibility(Button.VISIBLE);
	 					deleteSymptomButton.setVisibility(Button.VISIBLE);
	 					addPictureButton.setVisibility(Button.GONE);
	 					break;
	 				case 2:
	 					saveSicknessButton.setVisibility(Button.GONE);
	 					deleteSicknessButton.setVisibility(Button.GONE);
	 					addSymptomButton.setVisibility(Button.GONE);
	 					deleteSymptomButton.setVisibility(Button.GONE);
	 					addPictureButton.setVisibility(Button.VISIBLE);
	 					break;
 				}
 			}     
 		});  
 		
 		saveSicknessButton = (Button) findViewById(R.editsickness.saveSickness);
 		addSymptomButton = (Button) findViewById(R.editsickness.addSymptom);
 		addPictureButton = (Button) findViewById(R.editsickness.addPicture);
 		deleteSicknessButton = (Button) findViewById(R.editsickness.deleteSickness);
 		deleteSymptomButton = (Button) findViewById(R.editsickness.deleteSymptom);
 		backButton = (Button) findViewById(R.editsickness.back);
 		saveSicknessButton.setOnClickListener(this);
		deleteSicknessButton.setOnClickListener(this);
		addSymptomButton.setOnClickListener(this);
		deleteSymptomButton.setOnClickListener(this);
		addPictureButton.setOnClickListener(this);
		backButton.setOnClickListener(this);
		
		((ImageButton) findViewById(R.editsickness.buttonSickness1)).setOnClickListener(this);
		((ImageButton) findViewById(R.editsickness.buttonSickness2)).setOnClickListener(this);
		((ImageButton) findViewById(R.editsickness.buttonSymptom2)).setOnClickListener(this);
		((ImageButton) findViewById(R.editsickness.buttonSymptom3)).setOnClickListener(this);
		
		img = (ImageView)findViewById(R.editsickness.image);
		img.setOnClickListener(this);
		
		sicknessText1 = (AutoCompleteTextView) findViewById(R.editsickness.sickness1);
		sicknessText2 = (AutoCompleteTextView) findViewById(R.editsickness.sickness2);
		symptomText2 = (AutoCompleteTextView) findViewById(R.editsickness.symptom2);
		symptomText3 = (AutoCompleteTextView) findViewById(R.editsickness.symptom3);
		
		sicknessText1.addTextChangedListener(this);
		sicknessText2.addTextChangedListener(this);
		symptomText2.addTextChangedListener(this);
		symptomText3.addTextChangedListener(this);
		
		rating = (RatingBar) findViewById(R.editsickness.sicknessRating);
		detailsText1 = (EditText) findViewById(R.editsickness.sicknessDetails);
		diagnosisList = (ListView) findViewById(R.editsickness.diagnosislist);
		diagnosisList.setOnItemClickListener(this);
    }

    // Kattintas esemenykezeloje
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.editsickness.buttonSickness1:
		case R.editsickness.buttonSickness2:
			if (sicknessDialog!=null) sicknessDialog.show();
			break;
		case R.editsickness.buttonSymptom2:
		case R.editsickness.buttonSymptom3:
			if (symptomDialog!=null) symptomDialog.show();
			break;
		case R.editsickness.saveSickness:
			if (download1==null || (download1!=null && download1.isNotUsed())) saveSicknessRequest(true);
			break;
		case R.editsickness.deleteSickness:
			if (download1==null || (download1!=null && download1.isNotUsed())) saveSicknessRequest(false);
			break;
		case R.editsickness.addSymptom:
			if (download2==null || (download2!=null && download2.isNotUsed())) addSymptomRequest(true);
			break;
		case R.editsickness.deleteSymptom:
			if (download2==null || (download2!=null && download2.isNotUsed())) addSymptomRequest(false);
			break;
		case R.editsickness.addPicture:
			if (upload==null || (upload!=null && upload.isNotUsed())) uploadPictureRequest();	// Feltoltes
			break;
		case R.editsickness.image:
			Intent myIntent = new Intent(activity,FileChooserActivity.class);	// filechooser betoltese a kep kivalasztasahoz
			startActivityForResult(myIntent,0);
			break;
		case R.editsickness.back:
			finish();
			break;
		}	
	}
	
	// Listak esemenykezeloje
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		switch(parent.getId()) {
		case editsickness.diagnosislist:
			if (listDiagnosis!=null) {
				String str = listDiagnosis.get(position);
				symptomText2.setText(str);
				symptomText3.setText(str);
			}
			break;
		}
	}
	
	// A beviteli mezok valtozasanak esemenykezeloje
	@Override
	public void afterTextChanged(Editable editable) {
		String str="";
		
		switch (activity.getCurrentFocus().getId()) {
			case R.editsickness.sickness1: str = sicknessText1.getText().toString(); break;
			case R.editsickness.sickness2: str = sicknessText2.getText().toString(); break;
			case R.editsickness.symptom2:  str = symptomText2.getText().toString();  break;
			case R.editsickness.symptom3:  str = symptomText3.getText().toString();  break;
		}
		
		switch (activity.getCurrentFocus().getId()) {
		case R.editsickness.sickness1:								// Betegseg mezok
		case R.editsickness.sickness2:
			if (!sicknessText1.getText().toString().equals(str)) sicknessText1.setText(str);			// texview-ba  a valtozott ertek
			if (!sicknessText2.getText().toString().equals(str)) sicknessText2.setText(str);
			if (listSickness!=null) {
				if (listSickness.contains(str) && !str.equals("")) {									// ha a listan szerepel, akkor adatok frissitese
	            	if (str.equals(sicknessName)) loadSicknessInfo();
	            	else {
	            		sicknessName = str;
	            		getSicknessInfo();
	            	}
				}
				else {																					// egyebkent torles
					deleteSicknessInfo();
				}	
			}
			break;
		case R.editsickness.symptom2:								// Tunet mezok
			if (!symptomText3.getText().toString().equals(str)) symptomText3.setText(str);			// textview-ba a valtozott ertek
			break;
		case R.editsickness.symptom3:
			if (!symptomText2.getText().toString().equals(str)) symptomText2.setText(str);			// textview-ba a valtozott ertek
			break;
		}
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

    
    // Indulaskor a betegsegek lekerdezese  
    @Override
    public void onStart() {
    	super.onStart();
    	getData();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	setProgressBarIndeterminateVisibility(false);
    	if (downloadData!=null && downloadData.isUsed()) {
    		downloadData.setNotUsed();
    	}
    }
    
    // A szalak valaszat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {			
			switch (msg.what) {
			// Betegsegek es tunetek betoltese
			case TASK_GETDATA:
				if(msg.arg1==1 && downloadData.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
					Log.v("EditSicknessActivity","Betegsegek es tunetek betoltve");
			         
					// Betegsegek
					listSickness = downloadData.getStringArrayList("sickness");
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, listSickness);
			        
			        sicknessText1.setAdapter(adapter);																		// Autocomplete lista
			        sicknessText2.setAdapter(adapter);
			        
			        listSickness.add(0, "");		// az elso listaelem ures
			        
			        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			        builder.setItems(downloadData.getStringArray("sickness", true), new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int position) {
			            	String str = listSickness.get(position);
			            	sicknessText1.setText(str);							// texview-ba  a kivalasztott
				            sicknessText2.setText(str);
				            
				            if (!str.equals("")) {
				            	if (str.equals(sicknessName)) loadSicknessInfo();
				            	else {
				            		sicknessName = str;
				            		getSicknessInfo();
				            	}
				            } else {
				            	deleteSicknessInfo();
				            }
			            }
			        });
			        sicknessDialog = builder.create();
			        
			        // Tunetek
			        listSymptom = downloadData.getStringArrayList("symptom");
			        adapter = new ArrayAdapter<String>(activity, R.layout.list_item, listSymptom);
			        
			        symptomText2.setAdapter(adapter);																		// Autocomplete lista
			        symptomText3.setAdapter(adapter);
			        
			        listSymptom.add(0, "");		// az elso listaelem ures

			        builder = new AlertDialog.Builder(activity);
			        builder.setItems(downloadData.getStringArray("symptom", true), new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int position) {
			            	symptomText2.setText(listSymptom.get(position));							// TextView-ba a kivalasztott
			                symptomText3.setText(listSymptom.get(position));
			            }
			        });
			        symptomDialog = builder.create();
				}
				downloadData.setNotUsed();
				break;
			case TASK_GETSICKNESSINFO:
				if (msg.arg1==1 && downloadSicknessInfo.isOK()) {		// Ha sikeres lekerdezes...
					Log.v("EditSicknessActivity","Sikeres adatlap lekerdezes");
					
					sicknessSeriousness = (float)downloadSicknessInfo.getDouble("seriousness");			// lekerdezett adatok
					sicknessDetails = downloadSicknessInfo.getString("details");
					listDiagnosis = downloadSicknessInfo.getStringArrayList("symptom");

					loadSicknessInfo();
				}	
				downloadSicknessInfo.setNotUsed();
				break;
			// A megadott betegseg felvetele/modositasa sikeres
			case TASK_SETSICKNESS:
				if (msg.arg1==0) {
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				}
				else if (upload1.hasMessage()) {
					String message = upload1.getMessage();							// Uzenet lekerdezese es megjelenitese
					Log.v("EditSicknessActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				if (upload1.isOK()) getData();
				upload1.setNotUsed();
				break;
			case TASK_DELETESICKNESS:
				if (msg.arg1==1 && download1.hasMessage()) {
					String message = download1.getMessage();							// Uzenet lekerdezese es megjelenitese
					Log.v("EditSicknessActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				if (download1.isOK()) getData();
				download1.setNotUsed();
				break;
			// A megadott tunet felvetele/modositasa sikeres
			case TASK_SETSYMPTOM:
				if (msg.arg1==0) {
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				}
				else if (download2.hasMessage()) {
					String message = download2.getMessage();							// Uzenet lekerdezese es megjelenitese
					Log.v("EditSicknessActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				if (download2.isOK()) getSicknessInfo();
				download2.setNotUsed();
				break;
			// A megadott kep feltoltese sikeres
			case TASK_UPLOAD:
				if (msg.arg1==0) {
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				}
				else if (upload.hasMessage()) {
					String message = upload.getMessage();								// Uzenet lekerdezese es megjelenitese
					Log.v("EditSicknessActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				progress.cancel();
				upload.setNotUsed();
				break;
			}	
			setProgressBarIndeterminateVisibility(false);
		}
	};
	
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
                //((EditText)findViewById(R.addpictosymp.picturename)).setText(file.getName());
        	}
    }
    
	// Betegseg felvetel/modositas keres
    private void saveSicknessRequest(boolean isSave){
    	
    	String sickness = sicknessText1.getText().toString();	// a mezobe beirt betegseg
    	String details = detailsText1.getText().toString();		// a mezobe beirt betegseg leirasa
    	float seriousness = rating.getRating();
    	
    	if (sickness.equals("")) {																	// ha nincs megadva adat: hibauzenet
    		Toast.makeText(activity, "Adja meg a betegség nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url;
	    if (isSave) {
	    	StringEntity se = null;
			try {
				se = new StringEntity(details, "UTF-8");				
			} catch (UnsupportedEncodingException e) {}
	    	
	    	url = "SetSickness?sickness=" + URLEncoder.encode(sickness) + "&seriousness=" + seriousness + "&ssid=" + UserInfo.getSSID();
			upload1 = new HttpPostConnection(url, mHandler, se, TASK_SETSICKNESS);
			upload1.start();
	    }
	    else {
	    	url = "DeleteSickness?sickness=" + URLEncoder.encode(sickness) + "&ssid=" + UserInfo.getSSID();
		    download1 = new HttpGetJSONConnection(url, mHandler, TASK_DELETESICKNESS);
		    download1.start();
	    }
    }
    
	// Tunet hozzaadas kezdemenyezes
    private void addSymptomRequest(boolean isAdd){
    	String sickness = sicknessText2.getText().toString();		// a mezobe beirt betegseg
    	String symptom = symptomText2.getText().toString();		// a mezobe beirt tunet
    	
    	if (sickness.equals("")) {							// ha nincs megadva adat, akkor hibauzenet
    		Toast.makeText(activity, "Adja meg a betegség nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (symptom.equals("")) {
    		Toast.makeText(activity, "Adja meg a tünet nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url;
	    if (isAdd) url = "SetDiagnosis?sickness=" + URLEncoder.encode(sickness) + "&symptom=" + URLEncoder.encode(symptom) + "&ssid=" + UserInfo.getSSID();
	    else url = "DeleteDiagnosis?sickness=" + URLEncoder.encode(sickness) + "&symptom=" + URLEncoder.encode(symptom) + "&ssid=" + UserInfo.getSSID();
	    download2 = new HttpGetJSONConnection(url, mHandler, TASK_SETSYMPTOM);
	    download2.start();
    }
    
	// Hozzaadas kezdemenyezes (kep feltoltese)
    private void uploadPictureRequest(){	
    	String symptom = symptomText3.getText().toString();		// a mezobe beirt tunet
    	
    	if (symptom.equals("")) {																					// Tunet meg van adva?
    		Toast.makeText(activity, "Adja meg a tünet nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (file==null) {																							// Kep meg van adva?
    		Toast.makeText(activity, "Adja meg a feltöltendõ képet!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	setProgressBarIndeterminateVisibility(true);
    	progress = new ProgressDialog(this);
    	progress.setMessage("Kép feltöltése...");
        progress.setIndeterminate(true);
        progress.show();
        
    	FileEntity fe = new FileEntity(file, URLConnection.guessContentTypeFromName(file.getName()));
	    String url = "ImageUpload?table=Symptom&symptom=" + URLEncoder.encode(symptom) + "&ssid=" + UserInfo.getSSID();
	    upload = new HttpPostConnection(url, mHandler, fe, TASK_UPLOAD);
	    upload.start();
    }
    
    // Betegsegek es tunetek lekerdezese
    private void getData(){
    	setProgressBarIndeterminateVisibility(true);
    	String url = "GetAllSicknessSymptom?ssid=" + UserInfo.getSSID();
	    downloadData = new HttpGetJSONConnection(url, mHandler, TASK_GETDATA);
	    downloadData.start();
    }

    // Betegseg adatainak lekerese 
    private void getSicknessInfo(){
    	setProgressBarIndeterminateVisibility(true);
    	String url = "SicknessInfo?sickness=" + sicknessName + "&ssid=" + UserInfo.getSSID();
	    downloadSicknessInfo = new HttpGetJSONConnection(url, mHandler, TASK_GETSICKNESSINFO);
	    downloadSicknessInfo.start();
    }

    // Betegseg adatainak betoltese 
    private void loadSicknessInfo(){
        rating.setRating(sicknessSeriousness);
        detailsText1.setText(sicknessDetails);
        diagnosisList.setAdapter(new ArrayAdapter<String>(activity, R.layout.listview_item, listDiagnosis));
        setListViewHeightBasedOnChildren(diagnosisList);
    }
    
    // Betegseg adatainak torlese
    private void deleteSicknessInfo() {
        rating.setRating(0);
        detailsText1.setText("");
        diagnosisList.setAdapter (new ArrayAdapter<String>(activity, R.layout.listview_item, new ArrayList<String>()));
    	setListViewHeightBasedOnChildren(diagnosisList);
    }
    
    // ListView meretezese
    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
