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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class EditHospitalActivity extends Activity implements OnClickListener, OnItemClickListener, TextWatcher {
	private final int TASK_GETDATA = 1;
	private final int TASK_SETHOSPITAL = 2;
	private final int TASK_SETSICKNESS = 3;
	private final int TASK_GETCOORDINATES = 4;
	private final int TASK_GETHOSPITALINFO = 5;
	
	private HttpGetJSONConnection downloadData = null;			// szal a korhazak es betegsegek letoltesehez
	private HttpGetJSONConnection downloadHospitalInfo = null;	// szal a korhaz adatainak letoltesehez
	private HttpGetJSONConnection downloadGeoCode = null;		// szal a korhaz cimenek geokodolasahoz
	private HttpGetJSONConnection download1 = null;				// szal a webszerverhez csatlakozashoz
	private HttpGetJSONConnection download2 = null;
	
	private ArrayList<String> listHospital;		// korhazak listaja
	private ArrayList<String> listSickness;		// betegsegek listaja
	
	private String hospitalName;					// megadott korhaz neve
	private String hospitalAddress;					// 				   cime
	private String hospitalPhone;					//  			   telefonszama
	private String hospitalEmail;					//				   e-mail cime
	private ArrayList<String> listCuring = new ArrayList<String>(); // kezelt betegsegei
	private ListView curingList;
	
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
 		
 		curingList = (ListView) this.findViewById(R.edithospital.curinglist);
 		curingList.setOnItemClickListener(this);
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
			if (download1==null || (download1!=null && download1.isNotUsed())) saveHospitalRequest(true);
			break;
		case R.edithospital.deleteHospital:
			if (download1==null || (download1!=null && download1.isNotUsed())) saveHospitalRequest(false);
			break;
		case R.edithospital.addSickness:
			if (download2==null || (download2!=null && download2.isNotUsed())) addSicknessRequest(true);
			break;
		case R.edithospital.deleteSickness:
			if (download2==null || (download2!=null && download2.isNotUsed())) addSicknessRequest(false);
			break;
		case R.edithospital.back:
			finish();
			break;
		}	
	}
	
	// Listak esemenykezeloje
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		switch(parent.getId()) {
		case R.edithospital.curinglist:
			if (listCuring!=null) {
				String str = listCuring.get(position);
				sicknessText2.setText(str);
			}
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
				if (listHospital.contains(str) && !str.equals("")) {									// ha a listan szerepel, akkor adatok frissitese
	            	if (str.equals(hospitalName)) loadHospitalInfo();
	            	else {
	            		hospitalName = str;
	            		getHospitalInfo();
	            	}
				}
				else {																					// egyebkent torles
					deleteHospitalInfo();
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
			switch (msg.what) {
			// Korhazak betoltese
			case TASK_GETDATA:
				if(msg.arg1==1 && downloadData.isOK()) {				// Ha sikeres lekerdezes, akkor betolt...
					Log.v("EditHospitalActivity","Korhazak es betegsegek betoltve");
			         
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, downloadData.getStringArrayList("hospital"));
			        
			        hospitalText1.setAdapter(adapter);																		// Autocomplete lista
			        hospitalText2.setAdapter(adapter);
			        
			        listHospital = downloadData.getStringArrayList("hospital");												// Spinner lista
			        listHospital.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)
			        
			        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			        builder.setItems(downloadData.getStringArray("hospital", true), new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int position) {
			                String str = listHospital.get(position);
			                hospitalText1.setText(str);							// texview-ba  a kivalasztott
			                hospitalText2.setText(str);
				            
				            if (!str.equals("")) {
				            	if (str.equals(hospitalName)) loadHospitalInfo();
				            	else {
				            		hospitalName = str;
				            		getHospitalInfo();
				            	}
				            } else {
				            	deleteHospitalInfo();
				            }
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
				downloadData.setNotUsed();
				break;
			// megadott korhaz adatainak feldolgozasa
			case TASK_GETHOSPITALINFO:
				if (msg.arg1==1 && downloadHospitalInfo.isOK()) {		// Ha sikeres lekerdezes...
					Log.v("EditHospitalActivity","Sikeres adatlap lekerdezes");
					
					hospitalAddress = downloadHospitalInfo.getString("address");							// lekerdezett cim
					hospitalPhone = downloadHospitalInfo.getString("phone");								// lekerdezett telefonszam
					hospitalEmail = downloadHospitalInfo.getString("email");								// lekerdezett e-mail cim	
					listCuring = downloadHospitalInfo.getStringArrayList("sickness");						// lekerdezett betegsegek neve
					
					loadHospitalInfo();
				}
				downloadHospitalInfo.setNotUsed();
				break;
			// A megadott korhaz felvetele/modositasa sikeres
			case TASK_SETHOSPITAL:
				if (msg.arg1==0) {
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				}
				else if (download1.hasMessage()) {
					String message = download1.getMessage();							// Uzenet lekerdezese es megjelenitese
					Log.v("EditHospitalActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				if (download1.isOK()) getData();
				if (download1.isOK()) getHospitalInfo();
				download1.setNotUsed();
				break;
			// A megadott betegseg felvetele/modositasa sikeres
			case TASK_SETSICKNESS:
				if (msg.arg1==0) {
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				}
				else if (download2.hasMessage()) {
					String message = download2.getMessage();							// Uzenet lekerdezese es megjelenitese
					Log.v("EditHospitalActivity", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				if (download2.isOK()) getHospitalInfo();
				download2.setNotUsed();
				break;
			
			// A megadott cim koordinatainak feldolgozasa
			case TASK_GETCOORDINATES:
				if (msg.arg1==0) {
					Toast.makeText(activity, "Nem sikerült a megadott címet kódolni.", Toast.LENGTH_LONG).show();
					downloadGeoCode.setNotUsed();
					break;
				}
				
				JSONObject json = downloadGeoCode.getJSONObject();
				try {
					if (json.getJSONObject("Status").getInt("code") == 200) {	// Ha ervenyes a megadott cim
						JSONArray coordinates = json.getJSONArray("Placemark").getJSONObject(0).getJSONObject("Point").getJSONArray("coordinates");
						double longitude=coordinates.getDouble(0);	// y		// koordinatak lekerdezese
						double latitude=coordinates.getDouble(1);	// x

						setProgressBarIndeterminateVisibility(true);
	    	            String url = "SetHospital?name=" + URLEncoder.encode(hospitalName) + "&address=" + URLEncoder.encode(hospitalAddress) + 
    	            																 "&x=" + latitude +
    	            																 "&y=" + longitude  + 
    	            																 "&phone=" + URLEncoder.encode(hospitalPhone) +
    	            																 "&email=" + URLEncoder.encode(hospitalEmail) +
    	            																 "&ssid=" + UserInfo.getSSID();
	    	        	download1 = new HttpGetJSONConnection(url, mHandler, TASK_SETHOSPITAL);		// a megadott korhaz felvetele az adatbazisba
	    	        	download1.start();   
					} else {													// Ha a megadott cim ervenytelen
	            		Toast.makeText(activity, "A megadott cím hibás.", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
            		Toast.makeText(activity, "Geokódolási hiba.", Toast.LENGTH_SHORT).show();
				}
				downloadGeoCode.setNotUsed();
				break;
			}
			setProgressBarIndeterminateVisibility(false);
		}
	};

    
	// Hozzaadas keres
    private void saveHospitalRequest(boolean isSave){
    	
    	hospitalName = hospitalText1.getText().toString();		// a mezobe beirt korhaz neve
    	hospitalAddress = addressText1.getText().toString();	// a mezobe beirt korhaz cime
    	hospitalPhone = phoneText1.getText().toString();		// a mezobe beirt korhaz telefonszama
    	hospitalEmail = emailText1.getText().toString();		// a mezobe beirt korhaz telefonszama
    	
    	if (hospitalName.equals("")) {																		// ha nincs adat megadva: hibauzenet
    		Toast.makeText(activity, "Adja meg a kórház nevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if (isSave) {
	    	if (hospitalAddress.equals("")) {
	    		Toast.makeText(activity, "Adja meg a kórház címét!", Toast.LENGTH_SHORT).show();
	    		return;
	    	}
	    	
	    	setProgressBarIndeterminateVisibility(true);
	
	    	String url = "http://maps.google.com/maps/geo?key=" + R.string.apiKey + "&output=json&q="+URLEncoder.encode(hospitalAddress);
	    	downloadGeoCode = new HttpGetJSONConnection(mHandler, TASK_GETCOORDINATES);
	    	downloadGeoCode.setURL(url);
	    	downloadGeoCode.start();		// megadott cim kodolasa
    	}
    	else {
    		String url = "DeleteHospital?name=" + URLEncoder.encode(hospitalName) + "&ssid=" + UserInfo.getSSID();
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

    // Betegseg adatainak lekerese 
    private void getHospitalInfo(){
    	setProgressBarIndeterminateVisibility(true);
    	String url = "HospitalInfo?hospital=" + URLEncoder.encode(hospitalName) + "&ssid=" + UserInfo.getSSID();
	    downloadHospitalInfo = new HttpGetJSONConnection(url, mHandler, TASK_GETHOSPITALINFO);
	    downloadHospitalInfo.start();
    }

    // Betegseg adatainak betoltese 
    private void loadHospitalInfo(){
        addressText1.setText(hospitalAddress);
        phoneText1.setText(hospitalPhone);
        emailText1.setText(hospitalEmail);
        curingList.setAdapter(new ArrayAdapter<String>(activity, R.layout.listview_item, listCuring));
        setListViewHeightBasedOnChildren(curingList);
    }
    
    // Betegseg adatainak torlese
    private void deleteHospitalInfo() {
    	addressText1.setText("");
        phoneText1.setText("");
        emailText1.setText("");
        curingList.setAdapter (new ArrayAdapter<String>(activity, R.layout.listview_item, new ArrayList<String>()));
    	setListViewHeightBasedOnChildren(curingList);
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
