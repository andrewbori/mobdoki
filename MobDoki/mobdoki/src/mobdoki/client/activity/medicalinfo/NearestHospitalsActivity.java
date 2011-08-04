package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import mobdoki.client.CustomItemizedOverlay;
import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NearestHospitalsActivity extends MapActivity implements OnClickListener {

	private HttpGetConnection downloadHospital = null;		// szal a korhazak letoltesehez
	private HttpGetConnection downloadGeoCode = null;		// szal a megadott cim koordinatainak lekerdezesehez
	private ArrayList<Hospital> listHospitals = null;		// lekerdezett korhazak tombje
	
	private LocationManager locationManager;
	private LocationListener locationListenerGPS;			// koordinatak valtozasat figyelo objektumok
	private LocationListener locationListenerNetwork;
	private MapView mapView;
	private MapController mapController;
	private Location myLocation = null;
	private boolean isCurrentPosition=true;					// Igaz ha a sajat pozicio szamit
	private String address0="";								// Legutobbi cim
	private double alatitude=0, alongitude=0;				// Legutobbi cim koordinatai
	private double latitude0=0, longitude0=0;				// Legutobbi pozicio koordinatai
	private double distance0 = 0;							// legutobbi tavolsag
	
	private boolean isMapDisplayed = false;					// Igaz ha a terkep meg van jelenitve
	private Activity activity = this;
	private ProgressBar progressbar;
	private RadioButton currentRadio;
	private RadioButton customRadio;
	private ViewSwitcher switcher;
	private EditText addressBox;
	private EditText distanceBox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.nearesthospitals);
	    
	    progressbar = (ProgressBar)findViewById(R.nearesthospitals.progress);
	    switcher = (ViewSwitcher) findViewById(R.nearesthospitals.viewSwitcher);
	    addressBox = (EditText) findViewById(R.nearesthospitals.address);
	    distanceBox = (EditText)findViewById(R.nearesthospitals.distance);
	    currentRadio = (RadioButton) findViewById(R.nearesthospitals.currentPos);
        customRadio = (RadioButton) findViewById(R.nearesthospitals.customPos);
	    
        ((Button) findViewById(R.nearesthospitals.show)).setOnClickListener(this);
        ((Button) findViewById(R.nearesthospitals.back)).setOnClickListener(this);
        addressBox.setOnClickListener(this);
	    
        // Terkep beallitasa
	    mapView = (MapView) findViewById(R.googlemaps.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    mapController = mapView.getController();
	    mapController.setZoom(14); 
	    
	    // Helyzetmeghatarozas beregisztralasa
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListenerGPS = new MyLocationListener();
		locationListenerNetwork = new MyLocationListener();		
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 1, locationListenerGPS);
		} catch (Exception e) { Log.v("NearestHospitalActivity","GPS PROVIDER nincs engedélyezve."); }
		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1, locationListenerNetwork);
		} catch (Exception e) { Log.v("NearestHospitalActivity","NETWORK PROVIDER nincs engedélyezve."); }
	}
	
	// Gombnyomás eseménykezelõ
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
			// Mutasd! gomb esemenykezeloje
			case R.nearesthospitals.show:
				isCurrentPosition = currentRadio.isChecked();
				if (isCurrentPosition) {
	        		setLocations(myLocation);
	        		//if (download==null || (download!=null && !download.isAlive())) addRequest();
	        		isMapDisplayed = true;
	        		switcher.showNext();
				}
				else {
					addressCheck();
				}
				break;
				
			// Vissza gomb esemenykezeloje
			case R.nearesthospitals.back:
				finish();
				break;
			
			// A cimmezore kattintas esemenykezeloje
			case R.nearesthospitals.address:
				customRadio.setChecked(true);
				break;
		}	
	}

	// Keszulek vissza gombjanak megnyomasakor...
	@Override
	public void onBackPressed() {
		if (isMapDisplayed) {
			isMapDisplayed=false;
			switcher.showPrevious();
		} else super.onBackPressed();
	}
	
	// Helyzetvaltozast figyelo osztaly
	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			myLocation = new Location(location);
			if (isCurrentPosition) {
				setLocations(location);
			}
		}
		@Override
		public void onProviderDisabled(String provider) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}
	
	// Markerek beallitasa a helyzet es a tavolsag fuggvenyeben
	private void setLocations (Location location) {
		if (location != null) {
			setLocations (location.getLatitude(), location.getLongitude());
		}
	}
	private void setLocations (double latitude, double longitude) {
		String distanceStr = distanceBox.getText().toString();	// tavolsag beolvasasa
		int distance = 0;
		if (!distanceStr.equals("")) distance = (int)Double.parseDouble(distanceStr);
		
		if (distance>0) {
			if (latitude==latitude0 && longitude==longitude0 && distance==distance0) return;	// Nincs valtozas, nem kell ujra szamolni
			else {
				latitude0=latitude;
				longitude0=longitude;
				distance0=distance;
			}
			Log.v("NearestHospitalsActivity","Tavolsagok ujraszamolasa.");
			
			GeoPoint point = new GeoPoint(  (int) (latitude * 1E6), 		// helyzet beolvasasa
											(int) (longitude * 1E6));
			
			mapController.animateTo(point);			// menj az uj helyre
			// mapController.setZoom(14);				// zoomolj ra

			List<Overlay> mapOverlays = mapView.getOverlays();											// terkep megjelolt helyeinek listaja
			mapOverlays.clear();																		// lista torlese

			CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(							// sajat lista letrehozasa
										activity.getResources().getDrawable(R.drawable.marker_android),	// 		sajat pozicio markerrel
										activity
			);

			OverlayItem overlayitem = new OverlayItem(point, "", "Saját pozíció");				// uj hely letrehozasa
			itemizedOverlay.addOverlay(overlayitem);											// uj hely hozzaadasa a sajat listahoz
			mapOverlays.add(itemizedOverlay);													// sajat lista hozzaadasa a terkep listajahoz
		      
			if(listHospitals == null) return;
			
			itemizedOverlay = new CustomItemizedOverlay(								// sajat lista letrehozasa
					activity.getResources().getDrawable(R.drawable.marker_hospital),	// 		korhazak markerrel
					activity
			);
			
		    for (int index=0; index<listHospitals.size(); index++) {	// korhazak beolvasasa, uj tavolsag szamitasa
		    	double x = listHospitals.get(index).latitude;
		    	double y = listHospitals.get(index).longitude;
		        
		    	try {
		    		listHospitals.get(index).distance = gps2m(latitude,longitude,x,y);
				    
				    if (listHospitals.get(index).distance<distance) {
				    	
				    	overlayitem = new OverlayItem(new GeoPoint((int)(x*1E6),(int)(y*1E6)), "", listHospitals.get(index).name);	// uj hely letrehozasa
						itemizedOverlay.addOverlay(overlayitem);																	// uj hely hozzaadasa a sajat listahoz	        
			        }
		    	} catch (Exception e) {}
			}
		    
		    if (itemizedOverlay.size()>0) mapOverlays.add(itemizedOverlay);		// sajat lista hozzaadasa a terkep listajahoz				
			mapView.invalidate();
		}
	}
	
	// GPS koordinatak kozotti tavolsag meterben
	private double gps2m (double lat_a, double lng_a, double lat_b, double lng_b) throws Exception {      
    	double pk = 180.0/Math.PI;
    	double a1 = lat_a / pk;    
    	double a2 = lng_a / pk;     
    	double b1 = lat_b / pk;     
    	double b2 = lng_b / pk;

    	double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
    	double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
    	double t3 = Math.sin(a1)*Math.sin(b1);
     
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000*tt;
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
    	if (downloadHospital!=null && downloadHospital.isAlive()) {
    		downloadHospital.stop(); downloadHospital=null;
    	}
    	if (downloadGeoCode!=null && downloadGeoCode.isAlive()) {
    		downloadGeoCode.stop(); downloadGeoCode=null;
    		progressbar.setVisibility(View.INVISIBLE);
    	}
    }
	  
      // Korhazak lekerdezeset / cim koordinakka forditasat kezelo handler
	  public Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				switch(msg.what) {
				
				// Korhazak lekerdezese (msg.what=1)
				case 1:
					if(msg.arg1==1){
						if (downloadHospital.isOK()) {
							Log.v("NearestHospitalsActivity","Korhazak lekerdezve");
					        
							listHospitals = new ArrayList<Hospital>();
							
							try {
								JSONArray hospitals = downloadHospital.getJSONArray("hospitals");
								for (int i=0; i<hospitals.length(); i++) {						// korhazak beolvasasa
									JSONObject hospital = (JSONObject)hospitals.get(i);
									
									String name = hospital.getString("name");							// korhaz neve
									JSONObject coordinates = hospital.getJSONObject("coordinates");		// korhaz koordinatai
									double x = coordinates.getDouble("x");
									double y = coordinates.getDouble("y");
									
									listHospitals.add( new Hospital(name, x, y) );						// korhaz hozzaadasa a listahoz
								}
							} catch (Exception e) {
								Log.v("NearestHospitalsActivity","JSON beolvasasi hiba!");
							}
						}
					}
				break;
					
				// Megadott cim koordinatakka forditasa (msg.what=2)
				case 2:
					progressbar.setVisibility(View.INVISIBLE);
					switch(msg.arg1){
						case 0:
							Log.v("NewHospitalActivity","Sikertelen GeoCode lekeres.");
							Toast.makeText(activity, "Nem sikerült a megadott címet kódolni.", Toast.LENGTH_LONG).show();
						break;
						case 1:
							JSONObject json = downloadGeoCode.getJSON();
							try {
								if (json.getJSONObject("Status").getInt("code") == 200) {	// Ha ervenyes a megadott cim
									JSONArray coordinates = json.getJSONArray("Placemark").getJSONObject(0).getJSONObject("Point").getJSONArray("coordinates");
									alongitude=coordinates.getDouble(0);	// y		// koordinatak lekerdezese
									alatitude=coordinates.getDouble(1);		// x
									setLocations(alatitude, alongitude);
									isMapDisplayed = true;
					        		switcher.showNext();
								} else {
									Toast.makeText(activity, "A megadott cím hibás.", Toast.LENGTH_SHORT).show();
									address0="";
								}
							} catch (Exception e) {
			            		Toast.makeText(activity, "Geokódolási hiba.", Toast.LENGTH_SHORT).show();
			            		address0="";
							}
						break;
					}
				break;
				}
			}
		};
		
	    // Korhazak lekerdezese
	    private void getHospital(){
	    	String url = "NearestHospitals";
		    downloadHospital = new HttpGetConnection(url, mHandler);
		    downloadHospital.start();
	    }
	    
		// Megadott cim koordinatainak lekerdezese
	    private void addressCheck(){   	
	    	String address = addressBox.getText().toString();		// a mezobe beirt cim
	    	
	    	if (address.equals("")) {
	    		Toast.makeText(activity, "Adjon meg egy tetszõleges címet!", Toast.LENGTH_SHORT).show();
	    		return;
	    	}
	    	
	    	if (!address.equals(address0)) {	// Ha a cim meg nem lett kodolva
	    		address0 = address;
		    	progressbar.setVisibility(View.VISIBLE);
	
		    	String url = "http://maps.google.com/maps/geo?key=yourkeyhere&output=json&q="+URLEncoder.encode(address);
		    	downloadGeoCode = new HttpGetConnection("", mHandler, 2);
		    	downloadGeoCode.setURL(url);
		    	downloadGeoCode.start();		// megadott cim kodolasa
	    	}
	    	else {
	    		setLocations(alatitude, alongitude);
				isMapDisplayed = true;
        		switcher.showNext();
	    	}
	    }
	    
	    // Korhaz adatait tartalmazo osztaly
	    private class Hospital {
	    	String name = "";			// korhaz neve
	    	double latitude = 0.0;		// korhaz koordinatai: szelessegi (x)
	    	double longitude = 0.0;		//					   hosszusagi (y)
	    	double distance = 0.0;		// tavolsag egy masik ponttol (meterben)
	    	
	    	Hospital (String name, double latitude, double longitude) {
	    		this.name = name;
	    		this.latitude = latitude;
	    		this.longitude = longitude;
	    	}
	    }
	    
		@Override
		protected boolean isRouteDisplayed() {
			return false;
		}
}
