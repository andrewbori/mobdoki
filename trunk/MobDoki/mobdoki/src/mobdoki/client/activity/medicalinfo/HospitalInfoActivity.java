package mobdoki.client.activity.medicalinfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobdoki.client.CommentListArrayAdapter;
import mobdoki.client.CustomItemizedOverlay;
import mobdoki.client.R;
import mobdoki.client.connection.HttpGetByteConnection;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpPostConnection;
import mobdoki.client.connection.UserInfo;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class HospitalInfoActivity extends MapActivity implements OnClickListener, AdapterView.OnItemClickListener {
	private final int TASK_GETDATA  = 1;
	private final int TASK_GETCOMMENTS = 2;
	private final int TASK_UPLOADCOMMENT = 3;
	private final int TASK_GETUSERIMAGE = 4;
	
	private String hospital;								// adott korhaz neve
	private String address;									// 		címe
	private String phone = null;							// 		telefonszama
	private String email = null;							// 		e-mail cime
	private ArrayList<String> listSicknesses = null;		// korhazban kezelt betegsegek listaja
	
	private ArrayList<String> commentList = null;			// komment lista
	private ArrayList<String> senderList = null;			// kommentet kuldo felhasznalok listaja
	private ArrayList<String> dateList;						// komment datumanak listaja
	private String username;								// a felhasznalo neve akinek a kepet eppen letoltjuk
	private HashMap<String,Bitmap> userImage = new HashMap<String,Bitmap>();	// felhasznalok neve és a hozzajuk tartozo profilkep
	private ListView listviewComments;						// kommenteket megjelenito lista
	private ArrayAdapter<String> adapterComments;
	
	private HttpGetJSONConnection download = null;			// szal a korhaz adatainak letoltesehez
	private HttpGetJSONConnection downloadComments = null;	// szal a kommentek letoltesehez
	private HttpGetByteConnection downloadUserImage = null;	// szal a kommentelo profilkepenek letoltesehez
	private HttpPostConnection uploadComment = null;		// szal a komment feltoltesehez
	
	private Activity activity=this;
	private AlertDialog.Builder newCommentDialog;
	private EditText newComment;
	private ProgressDialog progressdialog;
	
	private ViewSwitcher switcher;
	private boolean isMapDisplayed = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.hospitalinfo);
      
		Bundle extras = getIntent().getExtras();
		hospital = extras.getString("hospital");
		
		setTitle("MobDoki: " + hospital);
		((TextView) findViewById(R.hospitalinfo.hospitalname)).setText(hospital);
		
		// ---------------------------- Tabok beallitasa ------------------------
		TabHost tabs = (TabHost) findViewById(R.id.tabhost);
		tabs.setup();
		
		TabHost.TabSpec spec = tabs.newTabSpec("hospitalTab");   
		spec.setContent(R.hospitalinfo.hospitalTab);
	   	spec.setIndicator("Adatlap");
  		tabs.addTab(spec);
  		
		spec=tabs.newTabSpec("sicknessTab");
		spec.setContent(R.hospitalinfo.sicknessTab);
		spec.setIndicator("Betegségek");
		tabs.addTab(spec);
	    
		spec = tabs.newTabSpec("commentTab");   
		spec.setContent(R.hospitalinfo.commentTab);
	   	spec.setIndicator("Kommentek");
  		tabs.addTab(spec);
		
		tabs.setCurrentTab(0);
		
		for (int i = 0; i < tabs.getTabWidget().getTabCount(); i++) {
		    tabs.getTabWidget().getChildAt(i).getLayoutParams().height = 45;
		}  
		// ----------------------------------------------------------------------
		
		switcher = (ViewSwitcher) findViewById(R.hospitalinfo.viewSwitcher);
		
		((Button) findViewById(R.hospitalinfo.showmap)).setOnClickListener(this);
		((Button) findViewById(R.hospitalinfo.dialBtn)).setOnClickListener(this);
		((Button) findViewById(R.hospitalinfo.emailBtn)).setOnClickListener(this);
		((Button) findViewById(R.hospitalinfo.google)).setOnClickListener(this);
		((Button) findViewById(R.hospitalinfo.newcomment)).setOnClickListener(this);
		
		// Kommentek listaja
		listviewComments = (ListView) findViewById(R.hospitalinfo.commentlist);
		// A korhazak listajanak esemenykezeloje
		ListView listview = ((ListView) findViewById(R.hospitalinfo.sicknesslist));
		listview.setOnItemClickListener(this);
	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.hospitalinfo.newcomment:
			newCommentDialog = new AlertDialog.Builder(activity);
			newCommentDialog.setTitle("Új hozzászólás");
			newComment = new EditText(activity);
			newComment.setMinLines(7);
			newComment.setGravity(Gravity.TOP);
			newCommentDialog.setView(newComment);
			newCommentDialog.setNegativeButton("Mégsem",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.cancel();
						}
					});
			newCommentDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String comment = newComment.getText().toString();
					sendComment(comment);
				}
			});
			newCommentDialog.show();
			break;
		// Térkép gomb esemenykezeloje
		case R.hospitalinfo.showmap:
    		isMapDisplayed = true;
    		switcher.showNext();
			break;
		// Tarcsazas gomb esemenykezeloje
		case R.hospitalinfo.dialBtn:
			if (phone!=null) {
				try {
					startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
				} catch (Exception e) {
					Toast.makeText(activity, "Sikertelen tárcsázás.", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		// E-mail kuldes gombe esemenykezeloje
		case R.hospitalinfo.emailBtn:
			if (email==null || email.equals("") || email.equals("-")) break;
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
//			i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
//			i.putExtra(Intent.EXTRA_TEXT   , "body of email");
			try {
			    startActivity(Intent.createChooser(i, "Üzenet küldése..."));
			} catch (android.content.ActivityNotFoundException ex) {
			    Toast.makeText(activity, "Nem található e-mail kliens a készüléken.", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.hospitalinfo.google:
			Uri uri = Uri.parse("http://www.google.hu/search?q=" + hospital);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
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
	
	// Listak esemenykezeloje
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		switch (parent.getId()) {
		case R.hospitalinfo.sicknesslist:			// Korhaz listaelemre kattintas
			Intent myIntent = new Intent(activity, SicknessInfoActivity.class);
			myIntent.putExtra("sickness", listSicknesses.get(position));	
			Log.v("HospitalInfoActivity", listSicknesses.get(position));
			startActivity(myIntent);
			break;
		}
	}
	
	// Indulaskor a lista lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	getData();
    	getComments();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    	if (downloadComments!=null && downloadComments.isUsed()) {
    		downloadComments.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    	if (downloadUserImage!=null && downloadUserImage.isUsed()) {
    		downloadUserImage.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }

    // Lekerdezett listat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Korhaz adatainak lekerdezese
			case TASK_GETDATA:
				switch(msg.arg1){
				case 0:
					Log.v("HospitalInfoActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {		// Ha sikeres lekerdezes...
						Log.v("HospitalInfoActivity","Sikeres adatlap lekerdezes");
						
						address = download.getString("address");							// lekerdezett cim
						phone = download.getString("phone");								// lekerdezett telefonszam
						email = download.getString("email");								// lekerdezett e-mail cim
						((TextView) findViewById(R.hospitalinfo.address)).setText(address);
						((TextView) findViewById(R.hospitalinfo.phone)).setText(phone);
						((TextView) findViewById(R.hospitalinfo.email)).setText(email);
						
						JSONObject xy = download.getJSONObject("coordinates");				// lekerdezett koordinatak
						try {
							int x = (int)(xy.getDouble("x")*1E6);
							int y = (int)(xy.getDouble("y")*1E6);
							GeoPoint coordinates = new GeoPoint(x,y);
							setMap(coordinates);						// terkep beallitasa
						} catch (Exception e) {}
						
						listSicknesses = download.getStringArrayList("sickness");			// lekerdezett betegsegek neve
						((ListView) findViewById(R.hospitalinfo.sicknesslist)).setAdapter (new ArrayAdapter<String>(activity,
																						   R.layout.listview_item,  
																						   listSicknesses));
					} else Toast.makeText(activity, download.getMessage(), Toast.LENGTH_SHORT).show();
					break;
				}
				download.setNotUsed();
				break;
			// Lekerdezett kommentek kezelese
			case TASK_GETCOMMENTS:
				if (downloadComments.isOK()) {		// Ha sikeres lekerdezes...
					Log.v("HospitalInfoActivity","Sikeres komment lekerdezes");
					
					senderList = downloadComments.getStringArrayList("sender");
					dateList = downloadComments.getStringArrayList("date");
					commentList = downloadComments.getStringArrayList("comment");

					adapterComments = new CommentListArrayAdapter<String>(activity,  
												   R.layout.listview_comment,  
									               R.listview_comment.sender, R.listview_comment.date, R.listview_comment.comment, 
									               R.listview_comment.image,
									               senderList, dateList, commentList, userImage);
					listviewComments.setAdapter(adapterComments);
					getUserImage();
				}
				downloadComments.setNotUsed();
				break;
			// Komment feltoltesenek eredmenye
			case TASK_UPLOADCOMMENT:
				if (uploadComment.isOK()) {		// Ha sikeres lekerdezes...
					Log.v("HospitalInfoActivity","Sikeres komment kuldes");
					progressdialog.cancel();
					setProgressBarIndeterminateVisibility(false);
					if (downloadComments==null || downloadComments.isNotUsed()) getComments();
				}
				if (uploadComment.hasMessage()) Toast.makeText(activity, uploadComment.getMessage(), Toast.LENGTH_SHORT).show();
				progressdialog.cancel();
				uploadComment.setNotUsed();
				break;
			// Kivalasztott tunet kepenek megjelenitese
			case TASK_GETUSERIMAGE:				
				Bitmap bm=null;
				if (msg.arg1==1) {
					byte[] map = downloadUserImage.getResponse();										// letoltott kep lekerdezese
					
					if (map.length>0) {			// Ha van kep
						bm=BitmapFactory.decodeByteArray(map, 0, map.length);						// kep konvertalasa
					}
				}
				userImage.put(username, bm);
				
				((CommentListArrayAdapter<String>) adapterComments).setImages(userImage);
				listviewComments.setAdapter(adapterComments);
				
				downloadUserImage.setNotUsed();
				getUserImage();
				break;
			}
			if (!(download!=null && download.isUsed()) && !(downloadComments!=null && downloadComments.isUsed()) &&
				!(downloadUserImage!=null && downloadUserImage.isUsed()) &&
				!(uploadComment!=null && uploadComment.isUsed())) setProgressBarIndeterminateVisibility(false);
		}
	};
	
	// Korhaz adatainak lekeresenek kezdemenyezese
    private void getData(){
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "HospitalInfo?hospital=" + URLEncoder.encode(hospital) + "&ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler, TASK_GETDATA);
	    download.start();
    }
    
    // Commentek lekeresenek kezdemenyezese
    private void getComments(){
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "GetComments?table=Hospital&name=" + URLEncoder.encode(hospital) + "&ssid=" + UserInfo.getSSID();
	    downloadComments = new HttpGetJSONConnection(url, mHandler, TASK_GETCOMMENTS);
	    downloadComments.start();
    }
    
	// Comment feltoltes kezdemenyezese
	public void sendComment(String comment) {
		
		if (comment==null || comment.equals("")) {
			Toast.makeText(activity, "Üres hozzászólás!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		StringEntity se = null;
		try {
			se = new StringEntity(comment, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		
		setProgressBarIndeterminateVisibility(true);
		if (progressdialog==null) {
			progressdialog = new ProgressDialog(this);
	        progressdialog.setMessage("Hozzászólás küldése...");
		}
		progressdialog.setIndeterminate(true);
        progressdialog.show();
		
		String url = "NewComment?table=Hospital&name=" + URLEncoder.encode(hospital) + "&ssid=" + UserInfo.getSSID();
		
		uploadComment = new HttpPostConnection(url, mHandler, se, TASK_UPLOADCOMMENT);
		uploadComment.start();
	}
	
	// Kommentelo felhasznalo profilkepenek letoltesenek kezdemenyezese
    private void getUserImage(){
    	//setProgressBarIndeterminateVisibility(true);
    	
    	username=null;
    	for(int i=0; i<senderList.size(); i++) {	// kovetkezo felhasznalo kepe
    		String str=senderList.get(i);
    		if (!userImage.containsKey(str)) {
    			username=str;
    			break;
    		}
    	}
    	
    	if (username==null) return;
    	
	    String url = "ImageDownload?large=false&username=" + username +
	    						  "&ssid=" + UserInfo.getSSID();
	    downloadUserImage = new HttpGetByteConnection(url, mHandler, TASK_GETUSERIMAGE);
	    downloadUserImage.start();
    }
    
	// Terkep beallitasa
    public void setMap (GeoPoint coordinates) {
  		MapView mapView = (MapView) findViewById(R.googlemaps.mapview);
		mapView.setBuiltInZoomControls(true);								// a terkepen legyen nagyito
		
		List<Overlay> mapOverlays = mapView.getOverlays();									// terkep megjelolt helyeinek listaja
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker_hospital);	// alapertelmezett marker a megjelolt helyre
		CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable,this);	// sajat lista letrehozasa

		OverlayItem overlayitem = new OverlayItem(coordinates, "", hospital);				// uj hely letrehozasa
        itemizedOverlay.addOverlay(overlayitem);											// uj hely hozzaadasa a sajat listahoz
        mapOverlays.add(itemizedOverlay);													// sajat lista hozzaadasa a terkep listajahoz
        
		MapController mapController = mapView.getController();
		mapController.setCenter(coordinates);			// kozeppont beallitasa
		mapController.setZoom(16);				// nagyitas nagysaganak beallitasa
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
