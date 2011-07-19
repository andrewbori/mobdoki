package mobdoki.client.activity.medicalinfo;

import java.util.List;

import mobdoki.client.CustomItemizedOverlay;
import mobdoki.client.R;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class ShowHospitalActivity extends MapActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showhospital);
		
		Bundle extras = getIntent().getExtras();
		String hospital = extras.getString("hospital");						// megkapott korhaz nevenek lekerdezese
		
		int x = extras.getInt("x");											// megkapott x koordinata
		int y = extras.getInt("y");											// megkapott y koordinata
        GeoPoint point = new GeoPoint(x, y);
        
		MapView mapView = (MapView) findViewById(R.googlemaps.mapview);
		mapView.setBuiltInZoomControls(true);								// a terkepen legyen nagyito
		
		List<Overlay> mapOverlays = mapView.getOverlays();									// terkep megjelolt helyeinek listaja
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker_hospital);	// alapertelmezett marker a megjelolt helyre
		CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable,this);	// sajat lista letrehozasa

		OverlayItem overlayitem = new OverlayItem(point, "", hospital);						// uj hely letrehozasa
        itemizedOverlay.addOverlay(overlayitem);											// uj hely hozzaadasa a sajat listahoz
        mapOverlays.add(itemizedOverlay);													// sajat lista hozzaadasa a terkep listajahoz
        
		MapController mapController = mapView.getController();
		mapController.setCenter(point);			// kozeppont beallitasa
		mapController.setZoom(16);				// nagyitas nagysaganak beallitasa
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}