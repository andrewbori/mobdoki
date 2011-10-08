package mobdoki.client.activity.medicalinfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import mobdoki.client.CommentListArrayAdapter;
import mobdoki.client.R;
import mobdoki.client.connection.HttpGetByteConnection;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpPostConnection;
import mobdoki.client.connection.UserInfo;

import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class SicknessInfoActivity extends Activity implements OnClickListener, AdapterView.OnItemClickListener {
	private final int TASK_GETDATA  = 1;
	private final int TASK_GETCOMMENTS = 2;
	private final int TASK_GETIMAGE = 3;
	private final int TASK_UPLOADCOMMENT = 4;
	private final int TASK_GETUSERIMAGE = 5;
	
	private String sickness;								// adott betegseg neve
	private float sicknessSeriousness;						//				  sulyossaga
	private String sicknessDetails;								//				  informacios weblapja
	private String symptom = "";
	private ArrayList<String> listSymptoms = null;			// betegseg tuneteinek listaja
	private ArrayList<String> listHospitals = null;			// betegseget kezelo korhazak nevenek listaja
	
	private ArrayList<String> commentList = null;			// komment lista
	private ArrayList<String> senderList = null;			// kommentet kuldo felhasznalok listaja
	private ArrayList<String> dateList;						// komment datumanak listaja
	private String username;								// a felhasznalo neve akinek a kepet eppen letoltjuk
	private HashMap<String,Bitmap> userImage = new HashMap<String,Bitmap>();	// felhasznalok neve és a hozzajuk tartozo profilkep
	private ListView listviewComments;						// kommenteket megjelenito lista
	private ArrayAdapter<String> adapterComments;
	
	private HttpGetJSONConnection download = null;			// szal a tunetek es korhazak letoltesehez
	private HttpGetJSONConnection downloadComments = null;	// szal a kommentek letoltesehez
	private HttpGetByteConnection downloadImage = null;		// szal a tunetkep letoltesehez
	private HttpGetByteConnection downloadUserImage = null;	// szal a kommentelo profilkepenek letoltesehez
	private HttpPostConnection uploadComment = null;		// szal a komment feltoltesehez
	
	private Activity activity=this;
	private AlertDialog.Builder newCommentDialog;
	private EditText newComment;
	private Dialog symptomImageDialog;
	private ImageView symptomImage;
	private ProgressBar progress;
	private ProgressDialog progressdialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.sicknessinfo);
      
		Bundle extras = getIntent().getExtras();
		sickness = extras.getString("sickness");
		setTitle("MobDoki: " + sickness);
		
		((TextView) findViewById(R.sicknessinfo.sicknessname)).setText(sickness);
		
		// ---------------------------- Tabok beallitasa ------------------------
		TabHost tabs = (TabHost) findViewById(R.id.tabhost);
		tabs.setup();
		
		TabHost.TabSpec spec = tabs.newTabSpec("sicknessTab");   
		spec.setContent(R.sicknessinfo.sicknessTab);
	   	spec.setIndicator("Adatlap");
  		tabs.addTab(spec);
		
  		spec = tabs.newTabSpec("symptomTab");   
		spec.setContent(R.sicknessinfo.symptomTab);
	   	spec.setIndicator("Tünetek");
  		tabs.addTab(spec);
  		
		spec=tabs.newTabSpec("hospitalTab");
		spec.setContent(R.sicknessinfo.hospitalTab);
		spec.setIndicator("Kórházak");
		tabs.addTab(spec);
	    
		spec = tabs.newTabSpec("commentTab");   
		spec.setContent(R.sicknessinfo.commentTab);
	   	spec.setIndicator("Kommentek");
  		tabs.addTab(spec);
		
		tabs.setCurrentTab(0);
		
		for (int i = 0; i < tabs.getTabWidget().getTabCount(); i++) {
		    tabs.getTabWidget().getChildAt(i).getLayoutParams().height = 45;
		}
		// ----------------------------------------------------------------------
	
		((Button) findViewById(R.sicknessinfo.google)).setOnClickListener(this);
		((Button) findViewById(R.sicknessinfo.wiki)).setOnClickListener(this);
		((Button) findViewById(R.sicknessinfo.newcomment)).setOnClickListener(this);
		
		// Kommentek listaja
		listviewComments = (ListView) findViewById(R.sicknessinfo.commentlist);
		// A korhazak listajanak esemenykezeloje
		ListView listview = ((ListView) findViewById(R.sicknessinfo.hospitallist));
		listview.setOnItemClickListener(this);
		// A tunetek listajanak esemenykezeloje
		listview = (ListView)findViewById(R.sicknessinfo.symptomlist);
		listview.setOnItemClickListener(this);
	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {	
		case R.sicknessinfo.newcomment:
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
			Intent myIntent = new Intent(activity, HospitalInfoActivity.class);
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
			// Betegseg adatainak lekerdezese
			case TASK_GETDATA:
				switch(msg.arg1){
				case 0:
					Log.v("SicknessInfoActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isOK()) {		// Ha sikeres lekerdezes...
						Log.v("SicknessInfoActivity","Sikeres adatlap lekerdezes");
						
						sicknessSeriousness = (float)download.getDouble("seriousness");
						sicknessDetails = download.getString("details");
						
						listSymptoms = download.getStringArrayList("symptom");				// lekerdezett tunetek
						listHospitals = download.getStringArrayList("hospital");			// lekerdezett korhazak neve
						
						((RatingBar) findViewById(R.sicknessinfo.sicknessRating)).setRating(sicknessSeriousness);
						((TextView) findViewById(R.sicknessinfo.sicknessDetails)).setText(sicknessDetails);
						
						((ListView) findViewById(R.sicknessinfo.symptomlist)).setAdapter (new ArrayAdapter<String>(activity,	// Listak feltoltese
																						  R.layout.listview_item,  
																						  listSymptoms));
						((ListView) findViewById(R.sicknessinfo.hospitallist)).setAdapter (new ArrayAdapter<String>(activity,
																						   R.layout.listview_item,  
																						   listHospitals));
					} else Toast.makeText(activity, download.getMessage(), Toast.LENGTH_SHORT).show();
					break;
				}
				download.setNotUsed();
				break;
			// Lekerdezett kommentek kezelese
			case TASK_GETCOMMENTS:
				if (downloadComments.isOK()) {		// Ha sikeres lekerdezes...
					Log.v("SicknessInfoActivity","Sikeres komment lekerdezes");
					
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
					Log.v("SicknessInfoActivity","Sikeres komment kuldes");
					progressdialog.cancel();
					setProgressBarIndeterminateVisibility(false);
					if (downloadComments==null || downloadComments.isNotUsed()) getComments();
				}
				else if (uploadComment.hasMessage()) Toast.makeText(activity, uploadComment.getMessage(), Toast.LENGTH_SHORT).show();
				progressdialog.cancel();
				uploadComment.setNotUsed();
				break;
			// Kivalasztott tunet kepenek megjelenitese
			case TASK_GETIMAGE:				
				if (msg.arg1==1) {
					byte[] map = downloadImage.getResponse();											// letoltott kep lekerdezese
					
					if (map.length>0) {			// Ha van kep
						symptomImage.setImageBitmap(BitmapFactory.decodeByteArray(map, 0, map.length));		// kep megjelenitese
					}
					else {						// Ha nincs kep
						symptomImage.setImageResource(R.drawable.nopicture);
					}
				} else {
					symptomImage.setImageResource(R.drawable.nopicture);
				}
				progress.setVisibility(ProgressBar.GONE);
				downloadImage.setNotUsed();
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
				!(downloadImage!=null && downloadImage.isUsed()) && !(downloadUserImage!=null && downloadUserImage.isUsed()) &&
				!(uploadComment!=null && uploadComment.isUsed())) setProgressBarIndeterminateVisibility(false);
		}
	};
	
	// Betegseg adatainak lekeresenek kezdemenyezese
    private void getData(){
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "SicknessInfo?sickness=" + URLEncoder.encode(sickness) + "&ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler, TASK_GETDATA);
	    download.start();
    }
    
    // Commentek lekeresenek kezdemenyezese
    private void getComments(){
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "GetComments?table=Sickness&name=" + URLEncoder.encode(sickness) + "&ssid=" + UserInfo.getSSID();
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
		
		String url = "NewComment?table=Sickness&name=" + URLEncoder.encode(sickness) + "&ssid=" + UserInfo.getSSID();
		
		uploadComment = new HttpPostConnection(url, mHandler, se, TASK_UPLOADCOMMENT);
		uploadComment.start();
	}

	// Kep letoltesenek kezdemenyezese
	public void getImage(){
		if (symptomImageDialog==null) {
			symptomImageDialog = new Dialog(activity);
			symptomImageDialog.setContentView(R.layout.dialog_image);
			symptomImage = (ImageView) symptomImageDialog.findViewById(R.dialog_image.image);
			progress = (ProgressBar) symptomImageDialog.findViewById(R.dialog_image.progress);
		}
		
		symptomImageDialog.setTitle(symptom);
		symptomImage.setImageResource(ImageView.NO_ID);
		setProgressBarIndeterminateVisibility(true);
		progress.setVisibility(ProgressBar.VISIBLE);
		symptomImageDialog.show();
		
		String url = "ImageDownload?large=true&symptom=" + URLEncoder.encode(symptom) + "&ssid=" + UserInfo.getSSID();
	    downloadImage = new HttpGetByteConnection(url, mHandler, TASK_GETIMAGE);
	    downloadImage.start();
	}
	
	// Kommentelo felhasznalo profilkepenek letoltesenek kezdemenyezese
    private void getUserImage(){
    	
    	username=null;
    	for(int i=0; i<senderList.size(); i++) {	// kovetkezo felhasznalo kepe
    		String str=senderList.get(i);
    		if (!userImage.containsKey(str)) {
    			username=str;
    			break;
    		}
    	}
    	
    	if (username==null) return;
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "ImageDownload?large=false&username=" + username +
	    						  "&ssid=" + UserInfo.getSSID();
	    downloadUserImage = new HttpGetByteConnection(url, mHandler, TASK_GETUSERIMAGE);
	    downloadUserImage.start();
    }    
}
