package mobdoki.client.activity.user;

import java.io.File;
import java.net.URLConnection;
import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.activity.CameraActivity;
import mobdoki.client.activity.FileChooserActivity;
import mobdoki.client.connection.HttpGetByteConnection;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpPostConnection;
import mobdoki.client.connection.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

public class UserProfileActivity extends Activity implements OnClickListener {
	private final int TASK_GETDATA = 1;
	private final int TASK_DOWNLOADIMAGE = 2;
	private final int TASK_SAVEDATA = 3;
	private final int TASK_UPLOADIMAGE = 4;
	private final int TASK_CHANGEPASSWORD = 5;
	
	private HttpGetJSONConnection download = null;			// szal a webszerverhez csatlakozashoz
	private HttpGetJSONConnection downloadUserData = null;	// szal a felhasznalo adatainak lekeresere
	private HttpGetByteConnection downloadImage = null;
	private HttpPostConnection uploadImage = null;
	
	private int userID;
	private String username;				// bejelentkezett felhasznalo felhasznaloneve
	private String password1;
	
	private Activity activity = this;
	private TabHost tabs;
	private EditText nameText;
	private EditText addressText;
	private EditText emailText;
	private EditText oldpasswordText;
	private EditText password1Text;
	private EditText password2Text;
	private ImageView img;
	private AlertDialog imageDialog = null;
	private ProgressBar progress;
	
	private int imageID=0;
	private String filepath;
	private File file;
	private boolean hasImage=false;
	private byte[] imageBytes;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.userprofile);
        setTitle("MobDoki: Felhasználói profil");
        
        // Tabok beallitasa
  		tabs = (TabHost) findViewById(R.id.tabhost);
  		tabs.setup();
  		 
  		TabHost.TabSpec spec = tabs.newTabSpec("userTab");   
  		spec.setContent(R.userprofile.userTab);
  	   	spec.setIndicator("Adatlap");
    	tabs.addTab(spec);
  		
    	spec = tabs.newTabSpec("imageTab");   
  		spec.setContent(R.userprofile.imageTab);
  	   	spec.setIndicator("Profilkép");
    	tabs.addTab(spec);
    	
    	spec = tabs.newTabSpec("sicknessTab");   
  		spec.setContent(R.userprofile.passwordTab);
  	   	spec.setIndicator("Jelszó");
    	tabs.addTab(spec);
    		
  	    
  		tabs.setCurrentTab(0);
  		
  		for (int i = 0; i < tabs.getTabWidget().getTabCount(); i++) {
  		    tabs.getTabWidget().getChildAt(i).getLayoutParams().height = 45;
  		}
        
  		userID = UserInfo.getInt("userid");
		username = UserInfo.getString("username");
		((EditText)findViewById(R.userprofile.username)).setText(username);
		
		nameText = (EditText)findViewById(R.userprofile.name);
		addressText = (EditText)findViewById(R.userprofile.address);
		emailText = (EditText)findViewById(R.userprofile.email);
		oldpasswordText = (EditText)findViewById(R.userprofile.oldpassword);
		password1Text = (EditText)findViewById(R.userprofile.password1);
		password2Text = (EditText)findViewById(R.userprofile.password2);
        
		img = (ImageView)findViewById(R.userprofile.image);
		img.setOnClickListener(this);
		
		progress = (ProgressBar) findViewById(R.userprofile.progress);
		
        ((Button) findViewById(R.userprofile.save)).setOnClickListener(this);
        ((Button) findViewById(R.userprofile.back)).setOnClickListener(this);
        
        getUserData();
    }
    
    // Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
			// Mentes gomb esemenykezeloje
			case R.userprofile.save:
				switch (tabs.getCurrentTab()) {
	 				case 0:
	 					if (download==null || download.isNotUsed()) saveDataRequest();
	 					break;
	 				case 1:
	 					if (hasImage && filepath!=null) {
							FileEntity fe = new FileEntity(file, URLConnection.guessContentTypeFromName(file.getName()));
							uploadImage(fe);
						}
	 					break;
	 				case 2:
	 					if (download==null || download.isNotUsed()) changePasswordRequest();
	 					break;
				}
				break;
		    
		    // Vissza gomb esemenykezeloje
			case R.userprofile.back:
				finish();
				break;
			case R.userprofile.image:
				if (imageDialog==null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Kép forrása")
					       .setCancelable(false)
					       .setPositiveButton("Kamera", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   Intent myIntent = new Intent(activity,CameraActivity.class);	// camera betoltese a kep kivalasztasahoz
								   startActivityForResult(myIntent,0);
					           }
					       })
					       .setNeutralButton("SD kártya", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   Intent myIntent = new Intent(activity,FileChooserActivity.class);	// filechooser betoltese a kep kivalasztasahoz
								   startActivityForResult(myIntent,0);
					           }
					       })
					       .setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   dialog.cancel();
					           }
					       });
					imageDialog = builder.create();
				}
				imageDialog.show();
				break;
		}
	}

	// FileChooser eredmenye: a kivalasztott fajl feldolgozasa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    	if (resultCode==RESULT_OK) {					// Ha van kivalasztott fajl:
    		Bundle extras = intent.getExtras();
            if (extras != null) {
            	filepath = extras.getString("filepath");	                	
    			file = new File(filepath);
                img.setImageBitmap(BitmapFactory.decodeFile(filepath));
                hasImage=true;
    		}
    	}
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		// download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    	if (downloadUserData!=null && downloadUserData.isUsed()) {
    		downloadUserData.setNotUsed();
    	}
    }
    
    // Lekerdezett adatok feldolgozasa
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// A felhasznalo lekerdezett adatainak feldolgozasa
			case TASK_GETDATA:
				if (msg.arg1==1 && downloadUserData.isOK()) {		// Ha sikeres volt a lekerdezes
					Log.v("UserProfileActivity","Adatok betoltve");
				        if(downloadUserData.getString("name")!=null)		// adatok beolvasasa, ha megvannak adva
				        	nameText.setText(downloadUserData.getString("name"));
				        if(downloadUserData.getString("address")!=null)
				        	addressText.setText(downloadUserData.getString("address"));
				        if(downloadUserData.getString("email")!=null)
				        	emailText.setText(downloadUserData.getString("email"));
				        imageID = downloadUserData.getInt("imageID");
				        if (imageID!=0) downloadImage();
				}
				downloadUserData.setNotUsed();
				break;
			// Felhasznalo adatainak mentese sikeres volt?
			case TASK_SAVEDATA:
			case TASK_CHANGEPASSWORD:
				switch (msg.arg1){
					case 0:
						Log.v("UserProfileActivity","Sikertelen lekeres.");
						Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
						break;
					case 1:
						if (download.isOK() && msg.what==TASK_CHANGEPASSWORD) {
							UserInfo.putString("password", password1);
						}
							
						if (download.hasMessage()) {
							String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
							Log.v("UserProfileActivity", message);
							Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
						}
						break;
				}
				download.setNotUsed();
				break;
			case TASK_DOWNLOADIMAGE:
				if(downloadImage != null){
					imageBytes = downloadImage.getResponse();
					img.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
				}
				progress.setVisibility(ProgressBar.GONE);
				downloadImage.setNotUsed();
				break;
			case TASK_UPLOADIMAGE:
				if (uploadImage.hasMessage()) {
					String message = uploadImage.getMessage();
					Log.v("NewMessage", message);
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
				}
				uploadImage.setNotUsed();
				break;
			}
			if (!(download!=null && download.isUsed()) && !(downloadImage!=null && downloadImage.isUsed()) && !(uploadImage!=null && uploadImage.isUsed())) setProgressBarIndeterminateVisibility(false);
		}
	};
    
	// Adatlap mentes kezdemenyezese
    private void saveDataRequest(){
    	String name = nameText.getText().toString();		// a mezobe beirt felhasznalonev
    	String address = addressText.getText().toString();
    	String email = emailText.getText().toString();

    	String url = "UserProfile?name=" + URLEncoder.encode(name) +
    							"&address=" + URLEncoder.encode(address) +
    							"&email=" + URLEncoder.encode(email) + "&ssid=" + UserInfo.getSSID();
    	
    	setProgressBarIndeterminateVisibility(true);

	    download = new HttpGetJSONConnection(url, mHandler, TASK_SAVEDATA);
	    download.start();
    }

	// Kep letoltesenek kezdemenyezese
    private void downloadImage() {	    	
    	setProgressBarIndeterminateVisibility(true);
    	img.setImageResource(ImageView.NO_ID);
    	progress.setVisibility(ProgressBar.VISIBLE);
    	
    	String url = "ImageDownload?large=true&id=" + imageID + "&ssid=" + UserInfo.getSSID();
	    downloadImage = new HttpGetByteConnection(url, mHandler, TASK_DOWNLOADIMAGE);
	    downloadImage.start();
    }
    
	// Kep feltoltesenek kezdemenyezese
    private void uploadImage(HttpEntity he){	    	
    	setProgressBarIndeterminateVisibility(true);

    	String url = "ImageUpload?table=User&id=" + userID + "&ssid=" + UserInfo.getSSID();
	    uploadImage = new HttpPostConnection(url, mHandler, he, TASK_UPLOADIMAGE);
	    uploadImage.start();
    }
    
	// Jelszo megvaltoztatasanak kezdemenyezese
    private void changePasswordRequest(){
    	String oldpassword = oldpasswordText.getText().toString();
    	password1 = password1Text.getText().toString();
    	String password2 = password2Text.getText().toString();
    	
    	if (oldpassword.equals("")) {		// Ha nincs megadva a regi jelszo, akkor az uj jelszo figyelmen kivul hagyasa
    		oldpassword=null;
    		password1=null; password2=null;
    	} else {							// Ha meg van adava a regi jelszo
	    	if ( (!password1.equals("") && !password2.equals("")) ) {	// es ha az uj jelszavak is meg vannak adva
	    		if (!password1.equals(password2) ) {						// ha nem egyeznek: hiba
	    			Toast.makeText(activity, "A megadott jelszavak nem egyeznek.", Toast.LENGTH_SHORT).show();
		    		return;
	    		}
	    	} else {													// ha nincsenek megadva az uj jelszavak, akkor figyelmen kivul hagyas
	    		oldpassword = null;
	    		password1 = null;
	    		password2 = null;
	    	}
    	}
    	
    	if (password1!=null && password1.length()<6) {			// ha a jelszavak meg vannak adva, es azok rovidek: hibauzenet
    		Toast.makeText(activity, "A megadott jelszó túl rövid.", Toast.LENGTH_SHORT).show();
    		return;
    	}

    	String url = "ChangePassword?oldpassword=" + oldpassword.hashCode() +
    								"&newpassword=" + password1.hashCode() + "&ssid=" + UserInfo.getSSID();
    	
    	setProgressBarIndeterminateVisibility(true);

	    download = new HttpGetJSONConnection(url, mHandler, TASK_CHANGEPASSWORD);
	    download.start();
    }
    
    // Felhasznalo adatainak lekerdezese
    private void getUserData(){
    	String url = "GetUserData?ssid=" + UserInfo.getSSID();
	    downloadUserData = new HttpGetJSONConnection(url, mHandler, TASK_GETDATA);
	    downloadUserData.start();
    }
}
