package mobdoki.client.activity.medicalinfo;

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
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NearestHospitalsActivity extends MapActivity {

	HttpGetConnection downloadHospital = null;				// szal a korhazak letoltesehez
	private ArrayList<Hospital> listHospitals = null;		// lekerdezett korhazak tombje
	
	private LocationManager locationManager;
	private LocationListener locationListenerGPS;			// koordinatak valtozasat figyelok
	private LocationListener locationListenerNetwork;
	private MapView mapView;
	private MapController mapController;
	private Location myLocation = null;
	
	private Activity activity = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.nearesthospitals);
	    
        // Mutasd! gomb esemenykezeloje
        Button showButton = (Button) findViewById(R.nearesthospitals.show);
        showButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		setLocations(myLocation);
        		//if (download==null || (download!=null && !download.isAlive())) addRequest();
        	}
        });
	    
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.nearesthospitals.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
	    
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
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListenerGPS);
		} catch (Exception e) { Log.v("NearestHospitalActivity","GPS PROVIDER nincs engedélyezve."); }
		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListenerNetwork);
		} catch (Exception e) { Log.v("NearestHospitalActivity","NETWORK PROVIDER nincs engedélyezve."); }
	}

	// Helyzetvaltozast figyelo osztaly
	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			myLocation = new Location(location);
			setLocations(location);
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
		String distanceStr = ((EditText)findViewById(R.nearesthospitals.distance)).getText().toString();	// tavolsag beolvasasa
		int distance = 0;
		if (!distanceStr.equals("")) distance = (int)Double.parseDouble(distanceStr);
		
		if (location != null && distance>0) {																// helyzet beolvasasa
			GeoPoint point = new GeoPoint(  (int) (location.getLatitude() * 1E6), 
											(int) (location.getLongitude() * 1E6));
			
			mapController.animateTo(point);			// menj az uj helyre
			// mapController.setZoom(14);				// zoomolj ra

			List<Overlay> mapOverlays = mapView.getOverlays();											// terkep megjelolt helyeinek listaja
			mapOverlays.clear();																		// lista torlese

			CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(							// sajat lista letrehozasa
										activity.getResources().getDrawable(R.drawable.marker_android),	// 		sajat pozicio markerrel
										activity
			);

			OverlayItem overlayitem = new OverlayItem(point, "", "Saját pozició");				// uj hely letrehozasa
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
		    		listHospitals.get(index).distance = gps2m(location.getLatitude(),location.getLongitude(),x,y);
				    
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
	
	private double gps2m (double lat_a, double lng_a, double lat_b, double lng_b) throws Exception {	// GPS koordinatak kozotti tavolsag meterben       
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
	    }
	  
	  public Handler hospitalHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.arg1){
					case 1:
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
						break;
				}
			}
		};
		
	    // Korhazak lekerdezese
	    private void getHospital(){
	    	String url = "NearestHospitals";
		    downloadHospital = new HttpGetConnection(url, hospitalHandler);
		    downloadHospital.start();
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
