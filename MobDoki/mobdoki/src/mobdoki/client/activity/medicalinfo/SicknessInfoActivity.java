package mobdoki.client.activity.medicalinfo;

import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpGetByteConnection;
import mobdoki.client.connection.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class SicknessInfoActivity extends Activity implements OnClickListener, AdapterView.OnItemClickListener {
	private final int TASK_GETDATA  = 1;
	private final int TASK_GETIMAGE = 2;
	
	private String sickness;								// adott betegseg neve
	private float sicknessSeriousness;						//				  sulyossaga
	private String sicknessURL;								//				  informacios weblapja
	private String symptom = "";
	private ArrayList<String> listSymptoms = null;			// betegseg tuneteinek listaja
	private ArrayList<String> listHospitals = null;			// betegseget kezelo korhazak nevenek listaja
	private ArrayList<GeoPoint> listCoordinates = null;		// betegseget kezelo korhazak koordinatainak listaja
	
	private HttpGetJSONConnection download = null;			// szal a tunetek es korhazak letoltesehez
	private HttpGetByteConnection downloadImage = null;		// szal a tunetkep letoltesehez
	
	private Activity activity=this;
	private Dialog dialog;
	private ImageView image;
	private ProgressBar progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.sicknessinfo);
		setTitle("MobDoki: Betegség adatlapja");
      
		Bundle extras = getIntent().getExtras();
		sickness = extras.getString("sickness");

		((TextView)findViewById(R.sicknessinfo.sickness)).setText(sickness);
		((EditText) findViewById(R.sicknessinfo.sicknessname)).setText(sickness);
		
		// Tabok beallitasa
		TabHost tabs = (TabHost) findViewById(R.id.tabhost);
		tabs.setup();
		 
		TabHost.TabSpec spec = tabs.newTabSpec("sicknessTab");   
		spec.setContent(R.sicknessinfo.sicknessTab);
	   	spec.setIndicator("Betegség");
  		tabs.addTab(spec);
		
  		spec = tabs.newTabSpec("symptomTab");   
		spec.setContent(R.sicknessinfo.symptomTab);
	   	spec.setIndicator("Tünetek");
  		tabs.addTab(spec);
  		
		spec=tabs.newTabSpec("hospitalTab");
		spec.setContent(R.sicknessinfo.hospitalTab);
		spec.setIndicator("Kórházak");
		tabs.addTab(spec);
	    
		tabs.setCurrentTab(0);
		
		for (int i = 0; i < tabs.getTabWidget().getTabCount(); i++) {
		    tabs.getTabWidget().getChildAt(i).getLayoutParams().height = 30;
		}  
	
		((Button) findViewById(R.sicknessinfo.back)).setOnClickListener(this);
		((Button) findViewById(R.sicknessinfo.show)).setOnClickListener(this);
		((Button) findViewById(R.sicknessinfo.google)).setOnClickListener(this);
		((Button) findViewById(R.sicknessinfo.wiki)).setOnClickListener(this);
		
		// A korhazak listajanak esemenykezeloje
		ListView listview = ((ListView) findViewById(R.sicknessinfo.hospitallist));
		listview.setOnItemClickListener(this);
		
		// A tunetek listajanak esemenykezeloje
		ListView symptomview = (ListView)findViewById(R.sicknessinfo.symptomlist);
		symptomview.setOnItemClickListener(this);
	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.sicknessinfo.back:				// Vissza gomb esemenykezeloje
			finish();
			break;		
		case R.sicknessinfo.show:
			if (!sicknessURL.equals("")) {
				Uri uri = Uri.parse(sicknessURL);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			break;
		case R.sicknessinfo.google:
			Uri uri = Uri.parse("http://www.google.hu/search?q=" + sickness);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		case R.sicknessinfo.wiki:
			/* WebView webview = new WebView(this);
			 setContentView(webview);
			 webview.loadUrl(Uri.parse("http://hu.wikipedia.org/?search=" + sickness).toString());
			 */
			uri = Uri.parse("http://hu.wikipedia.org/?search=" + sickness);
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		}
	}
	
	// Listak esemenykezeloje
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		switch (parent.getId()) {
		case R.sicknessinfo.hospitallist:			// Korhaz listaelemre kattintas
			Intent myIntent = new Intent(activity, ShowHospitalActivity.class);
			myIntent.putExtra("x", listCoordinates.get(position).getLatitudeE6());
			myIntent.putExtra("y", listCoordinates.get(position).getLongitudeE6());
			myIntent.putExtra("hospital", listHospitals.get(position));	
			Log.v("SicknessInfoActivity",listHospitals.get(position));
			startActivity(myIntent);
			break;
		case R.sicknessinfo.symptomlist:			// Tunet listaelemre kattintas
			symptom = listSymptoms.get(position);
			getImage();
			break;
		}
	}
	
	// Indulaskor a lista lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	getData();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }

    // Lekerdezett listat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Betegseg adatainak lekerdezese
			case TASK_GETDATA:
				switch(msg.arg1){
				case 0:
					Log.v("SicknessInfoActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {		// Ha sikeres lekerdezes...
						Log.v("SicknessInfoActivity","Sikeres lekerdezes");
						
						sicknessSeriousness = (float)download.getDouble("seriousness");
						sicknessURL = download.getString("url");
						
						listSymptoms = download.getStringArrayList("symptom");				// lekerdezett tunetek
						listHospitals = download.getStringArrayList("hospital");			// lekerdezett korhazak neve
						listCoordinates = new ArrayList<GeoPoint>();						// 						koordinatai
						
						JSONArray coordinates = download.getJSONArray("coordinates");
						try {
							for (int i=0; i<coordinates.length(); i++) {
								JSONObject xy = coordinates.getJSONObject(i);
								int x = (int)(xy.getDouble("x")*1E6);
								int y = (int)(xy.getDouble("y")*1E6);
								listCoordinates.add(new GeoPoint(x,y));
							}
						} catch (Exception e) {
							Log.v("SicknessInfoActivity","Sikertelen korhazlistazas.");
							listHospitals = null;
						}
						
						((RatingBar) findViewById(R.sicknessinfo.sicknessRating)).setRating(sicknessSeriousness);
						((EditText) findViewById(R.sicknessinfo.sicknessURL)).setText(sicknessURL);
						
						((ListView) findViewById(R.sicknessinfo.symptomlist)).setAdapter (new ArrayAdapter<String>(activity,	// Listak feltoltese
																						  R.layout.listview_item,  
																						  listSymptoms));
						((ListView) findViewById(R.sicknessinfo.hospitallist)).setAdapter (new ArrayAdapter<String>(activity,
																						   R.layout.listview_item,  
																						   listHospitals));
					} else Toast.makeText(activity, download.getMessage(), Toast.LENGTH_SHORT).show();
					break;
				}
				setProgressBarIndeterminateVisibility(false);
				download.setNotUsed();
				break;
			// Kivalasztott tunet kepenek megjelenitese
			case TASK_GETIMAGE:				
				if (msg.arg1==1) {
					byte[] map = downloadImage.getResponse();											// letoltott kep lekerdezese
					
					if (map.length>0) {			// Ha van kep
						image.setImageBitmap(BitmapFactory.decodeByteArray(map, 0, map.length));		// kep megjelenitese
					}
					else {						// Ha nincs kep
						image.setImageResource(R.drawable.nopicture);
					}
				} else {
					image.setImageResource(R.drawable.nopicture);
				}
				progress.setVisibility(ProgressBar.GONE);
				break;
			}
		}
	};
	
	// Lista lekeresenek kezdemenyezese
    private void getData(){
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "SicknessInfo?sickness=" + URLEncoder.encode(sickness) + "&ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler, TASK_GETDATA);
	    download.start();
    }

	// Kep letoltesenek kezdemenyezese
	public void getImage(){
		if (dialog==null) {
			dialog = new Dialog(activity);
			dialog.setContentView(R.layout.dialog_image);
			image = (ImageView) dialog.findViewById(R.dialog_image.image);
			progress = (ProgressBar) dialog.findViewById(R.dialog_image.progress);
		}
		
		dialog.setTitle(symptom);
		image.setImageResource(ImageView.NO_ID);
		progress.setVisibility(ProgressBar.VISIBLE);
		dialog.show();
		
		String url = "ImageDownload?large=true&symptom=" + URLEncoder.encode(symptom) + "&ssid=" + UserInfo.getSSID();
	    downloadImage = new HttpGetByteConnection(url, mHandler, TASK_GETIMAGE);
	    downloadImage.start();
	}
    
}
