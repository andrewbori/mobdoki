package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class EditHospitalActivity extends Activity implements OnClickListener, TextWatcher {
	private final int TASK_GETDATA = 1;
	private final int TASK_SETHOSPITAL = 2;
	private final int TASK_SETSICKNESS = 3;
	private final int TASK_GETCOORDINATES = 4;
	
	private HttpGetJSONConnection downloadData = null;			// szal a korhazak es betegsegek letoltesehez
	private HttpGetJSONConnection downloadGeoCode = null;		// szal a korhaz cimenek geokodolasahoz
	private HttpGetJSONConnection download1 = null;				// szal a webszerverhez csatlakozashoz
	private HttpGetJSONConnection download2 = null;
	
	private ArrayList<String> listHospital;		// korhazak listaja
	private ArrayList<String> listAddress;		// korhazak cimenek listaja
	private ArrayList<String> listPhone;		// korhazak telefonszamanak listaja
	private ArrayList<String> listEmail;		// korhazak e-mail cimenek listaja
	private ArrayList<String> listSickness;		// betegsegek listaja
	
	private String name;		// megadott korhaz neve
	private String address;		// megadott korhaz cime
	private String phone;		// megadott korhaz telefonszama
	private String email;		// megadott korhaz e-mail cime
	
	private Activity activity = this;
	
	private TabHost tabs;
	private Button saveHospitalButton;
	private Button addSicknessButton;
	private Button deleteHospitalButton;
	private Button deleteSicknessButton;
	private Button backButton;
	
	private AlertDialog hospitalDialog;
	private AlertDialog sicknessDialog;
	
	private AutoCompleteTextView hospitalText1;
	private EditText addressText1;
	private EditText phoneText1;
	private EditText emailText1;
	private AutoCompleteTextView hospitalText2;
	private AutoCompleteTextView sicknessText2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.edithospital);
        setTitle("MobDoki: Kórházak adminisztrációja");
        
        // Tabok beallitasa
  		tabs = (TabHost) findViewById(R.id.tabhost);
  		tabs.setup();
  		 
  		TabHost.TabSpec spec = tabs.newTabSpec("hospitalTab");   
  		spec.setContent(R.edithospital.hospitalTab);
  	   	spec.setIndicator("Kórház adatai");
    	tabs.addTab(spec);
  		
    	spec = tabs.newTabSpec("sicknessTab");   
  		spec.setContent(R.edithospital.sicknessTab);
  	   	spec.setIndicator("Kezelések");
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
 	 					saveHospitalButton.setVisibility(Button.VISIBLE);
 	 					deleteHospitalButton.setVisibility(Button.VISIBLE);
 	 					addSicknessButton.setVisibility(Button.GONE);
 	 					deleteSicknessButton.setVisibility(Button.GONE);
 	 					break;
 	 				case 1:
 	 					saveHospitalButton.setVisibility(Button.GONE);
 	 					deleteHospitalButton.setVisibility(Button.GONE);
 	 					addSicknessButton.setVisibility(Button.VISIBLE);
 	 					deleteSicknessButton.setVisibility(Button.VISIBLE);
 	 					break;
  				}
  			}     
  		});  
  		
  		saveHospitalButton = (Button) findViewById(R.edithospital.saveHospital);
  		addSicknessButton = (Button) findViewById(R.edithospital.addSickness);
  		deleteHospitalButton = (Button) findViewById(R.edithospital.deleteHospital);
  		deleteSicknessButton = (Button) findViewById(R.edithospital.deleteSickness);
  		backButton = (Button) findViewById(R.edithospital.back);
  		saveHospitalButton.setOnClickListener(this);
  		addSicknessButton.setOnClickListener(this);
  		deleteHospitalButton.setOnClickListener(this);
  		deleteSicknessButton.setOnClickListener(this);
 		backButton.setOnClickListener(this);
 		
 		((ImageButton) findViewById(R.edithospital.buttonHospital1)).setOnClickListener(this);
 		((ImageButton) findViewById(R.edithospital.buttonHospital2)).setOnClickListener(this);
 		((ImageButton) findViewById(R.edithospital.buttonSickness2)).setOnClickListener(this);
 		
 		hospitalText1 = (AutoCompleteTextView) findViewById(R.edithospital.hospital1);
 		hospitalText2 = (AutoCompleteTextView) findViewById(R.edithospital.hospital2);
 		sicknessText2 = (AutoCompleteTextView) findViewById(R.edithospital.sickness2);
 		addressText1 = (EditText) findViewById(R.edithospital.address);
 		phoneText1 = (EditText) findViewById(R.edithospital.phone);
 		emailText1 = (EditText) findViewById(R.edithospital.email);
 		
 		hospitalText1.addTextChangedListener(this);
 		hospitalText2.addTextChangedListener(this);     
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.edithospital.buttonHospital1:
		case R.edithospital.buttonHospital2:
			if (hospitalDialog!=null) hospitalDialog.show();
			break;
		case R.edithospital.buttonSickness2:
			if (sicknessDialog!=null) sicknessDialog.show();
			break;
		case R.edithospital.saveHospital:
			if (download1==null || (download1!=null && !download1.isAlive())) saveHospitalRequest(true);
			break;
		case R.edithospital.deleteHospital:
			if (download1==null || (download1!=null && !download1.isAlive())) saveHospitalRequest(false);
			break;
		case R.edithospital.addSickness:
			if (download2==null || (download2!=null && !download2.isAlive())) addSicknessRequest(true);
			break;
		case R.edithospital.deleteSickness:
			if (download2==null || (download2!=null && !download2.isAlive())) addSicknessRequest(false);
			break;
		case R.edithospital.back:
			finish();
			break;
		}	
	}
	
	// A beviteli mezok valtozasanak esemenykezeloje
	@Override
	public void afterTextChanged(Editable editable) {
		String str="";
		
		switch (activity.getCurrentFocus().getId()) {
			case R.edithospital.hospital1: str = hospitalText1.getText().toString(); break;
			case R.edithospital.hospital2: str = hospitalText2.getText().toString(); break;
		}
		
		switch (activity.getCurrentFocus().getId()) {
		case R.edithospital.hospital1:								// Korhaz mezok
		case R.edithospital.hospital2:
			if (!hospitalText1.getText().toString().equals(str)) hospitalText1.setText(str);			// texview-ba  a valtozott ertek
			if (!hospitalText2.getText().toString().equals(str)) hospitalText2.setText(str);
			if (listHospital!=null) {
				if (listHospital.contains(str)) {									// ha a listan szerepel, akkor adatok frissitese
					int position = listHospital.indexOf(str);
	                addressText1.setText(listAddress.get(position));
	                phoneText1.setText(listPhone.get(position));
	                emailText1.setText(listEmail.get(position));
				}
				else {																// egyebkent torles
					addressText1.setText("");
					phoneText1.setText("");
					emailText1.setText("");
				}	
			}
			break;
		}
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
  
		
    // Indulaskor a korhazak lekerdezese
    @Override
    public void onStart() {		// kezdesnel a korhazak letoltese az adatbazisbol
    	super.onStart();
    	getData();
    }

    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {		// megszakitaskor a futo szalak leallitasa
    	super.onPause();
    	setProgressBarIndeterminateVisibility(false);
    	if (downloadData!=null && downloadData.isUsed()) {
    		downloadData.setNotUsed();
    	}
    	if (downloadGeoCode!=null && downloadGeoCode.isUsed()) {
    		downloadGeoCode.setNotUsed();
    	}
    }

    // A szalak valaszat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			if (msg.arg1==0) {			// Sikertelen
				switch (msg.what) {
				case TASK_SETHOSPITAL:
				case TASK_SETSICKNESS:
					Log.v("EditHospitalActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case TASK_GETCOORDINATES:
					Log.v("EditHospitalActivity","Sikertelen GeoCode lekeres.");
					Toast.makeText(activity, "Nem sikerült a megadott címet kódolni.", Toast.LENGTH_LONG).show();
					break;
				}
			} else
			if (msg.arg1==1) {			// Sikeres
				switch (msg.what) {
				// Korhazak betoltese
				case TASK_GETDATA:
					if(downloadData.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
						Log.v("EditHospitalActivity","Korhazak es betegsegek betoltve");
				         
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, downloadData.getStringArrayList("hospital"));
				        
				        hospitalText1.setAdapter(adapter);																		// Autocomplete lista
				        
				        listHospital = downloadData.getStringArrayList("hospital");												// Spinner lista
				        listHospital.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
				        listAddress = downloadData.getStringArrayList("address");
				        listAddress.add(0, "");
				        listPhone = downloadData.getStringArrayList("phone");
				        listPhone.add(0, "");
				        listEmail = downloadData.getStringArrayList("email");
				        listEmail.add(0, "");
				        
				        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				        builder.setItems(downloadData.getStringArray("hospital", true), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int position) {
				            	hospitalText1.setText(listHospital.get(position));							// texview-ba  a kivalasztott
				                hospitalText2.setText(listHospital.get(position));
				                addressText1.setText(listAddress.get(position));
				                phoneText1.setText(listPhone.get(position));
				                emailText1.setText(listEmail.get(position));
				            }
				        });
				        hospitalDialog = builder.create();

				        // Betegsegek
				        adapter = new ArrayAdapter<String>(activity, R.layout.list_item, downloadData.getStringArrayList("sickness"));	
				        sicknessText2.setAdapter(adapter);																		// Autocomplete lista
				        
				        listSickness = downloadData.getStringArrayList("sickness");												// Spinner lista
				        listSickness.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
	
				        builder = new AlertDialog.Builder(activity);
				        builder.setItems(downloadData.getStringArray("sickness", true), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int position) {
								sicknessText2.setText(listSickness.get(position));
				            }
				        });
				        sicknessDialog = builder.create();
					}
					break;
				// A megadott korhaz felvetele/modositasa sikeres
				case TASK_SETHOSPITAL:
					if (download1.hasMessage()) {
						String message = download1.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("EditHospitalActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
				// A megadott betegseg felvetele/modositasa sikeres
				case TASK_SETSICKNESS:
					if (download2.hasMessage()) {
						String message = download2.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("EditHospitalActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
				
				// A megadott cim koordinatainak feldolgozasa
				case TASK_GETCOORDINATES:
					JSONObject json = downloadGeoCode.getJSONObject();
					try {
						if (json.getJSONObject("Status").getInt("code") == 200) {	// Ha ervenyes a megadott cim
							JSONArray coordinates = json.getJSONArray("Placemark").getJSONObject(0).getJSONObject("Point").getJSONArray("coordinates");
							double longitude=coordinates.getDouble(0);	// y		// koordinatak lekerdezese
							double latitude=coordinates.getDouble(1);	// x
	
							setProgressBarIndeterminateVisibility(true);
		    	            String url = "SetHospital?name=" + URLEncoder.encode(name) + "&address=" + URLEncoder.encode(address) + 
	    	            																 "&x=" + latitude +
	    	            																 "&y=" + longitude  + 
	    	            																 "&phone=" + URLEncoder.encode(phone) +
	    	            																 "&email=" + URLEncoder.encode(email) +
	    	            																 "&ssid=" + UserInfo.getSSID();
		    	        	download1 = new HttpGetJSONConnection(url, mHandler, TASK_SETHOSPITAL);		// a megadott korhaz felvetele az adatbazisba
		    	        	download1.start();   
						} else {													// Ha a megadott cim ervenytelen
		            		Toast.makeText(activity, "A megadott cím hibás.", Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
	            		Toast.makeText(activity, "Geokódolási hiba.", Toast.LENGTH_SHORT).show();
					}
					break;
				}	
			}
		}
	};

    
	// Hozzaadas keres
    private void saveHospitalRequest(boolean isSave){
    	
    	name = hospitalText1.getText().toString();		// a mezobe beirt korhaz neve
    	address = addressText1.getText().toString();	// a mezobe beirt korhaz cime
    	phone = phoneText1.getText().toString();		// a mezobe beirt korhaz telefonszama
    	email = emailText1.getText().toString();		// a mezobe beirt korhaz telefonszama
    	
    	if (name.equals("")) {																		// ha nincs adat megadva: hibauzenet
    		Toast.makeText(activity, "Adja meg a kórház nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if (isSave) {
	    	if (address.equals("")) {
	    		Toast.makeText(activity, "Adja meg a kórház címét!", Toast.LENGTH_SHORT).show();
	    		return;
	    	}
	    	
	    	setProgressBarIndeterminateVisibility(true);
	
	    	String url = "http://maps.google.com/maps/geo?key=yourkeyhere&output=json&q="+URLEncoder.encode(address);
	    	downloadGeoCode = new HttpGetJSONConnection(mHandler, TASK_GETCOORDINATES);
	    	downloadGeoCode.setURL(url);
	    	downloadGeoCode.start();		// megadott cim kodolasa
    	}
    	else {
    		String url = "DeleteHospital?name=" + URLEncoder.encode(name) + "&ssid=" + UserInfo.getSSID();
			download1 = new HttpGetJSONConnection(url, mHandler, TASK_SETHOSPITAL);		// a megadott korhaz torlese az adatbazisbol
			download1.start();   
    	}
    }
    
	// Betegseg hozzaadas keres
    private void addSicknessRequest(boolean isAdd){
    	
    	String sickness = sicknessText2.getText().toString();	// a mezobe beirt betegseg
    	String hospital = hospitalText2.getText().toString();	// a mezobe beirt korhaz
    	
    	if (sickness.equals("")) {																				// Ha nincs adat megadva: hibauzenet
    		Toast.makeText(activity, "Adja meg a betegség nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (hospital.equals("")) {
    		Toast.makeText(activity, "Adja meg a kórház nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url;
	    if (isAdd) url = "SetCuring?sickness=" + URLEncoder.encode(sickness) + "&hospital=" + URLEncoder.encode(hospital) + "&ssid=" + UserInfo.getSSID();
	    else url = "DeleteCuring?sickness=" + URLEncoder.encode(sickness) + "&hospital=" + URLEncoder.encode(hospital) + "&ssid=" + UserInfo.getSSID();
	    download2 = new HttpGetJSONConnection(url, mHandler, TASK_SETSICKNESS);
	    download2.start();
    }
    
    // Korhazak es betegsegek lekerdezese
    private void getData(){
    	String url = "GetAllSicknessHospital?ssid=" + UserInfo.getSSID();
	    downloadData = new HttpGetJSONConnection(url, mHandler, TASK_GETDATA);
	    downloadData.start();
    }
}
