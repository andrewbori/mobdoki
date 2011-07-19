package mobdoki.client.activity.user;

import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UserProfileActivity extends Activity {
	HttpGetConnection download = null;			// szal a webszerverhez csatlakozashoz
	HttpGetConnection downloadUserData = null;	// szal a felhasznalo adatainak lekeresere
	private Activity activity = this;
	private String username;				// bejelentkezett felhasznalo felhasznaloneve
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userprofile);
        
        Bundle extras = getIntent().getExtras();		// a hivo Activity altal kuldott felhasznalonev
		username = extras.getString("username"); 
        ((EditText)findViewById(R.userprofile.username)).setText(username);
        
        // Mentes gomb esemenykezeloje
        Button saveButton = (Button) findViewById(R.userprofile.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) saveRequest();
        	}
        });
       
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.userprofile.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
    
    // Indulaskor a felhasznalo adatainak lekerdezese
    @Override
    public void onStart () {
    	super.onStart();
    	getUserData();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.statistics.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if (downloadUserData!=null && downloadUserData.isAlive()) {
    		downloadUserData.stop(); downloadUserData=null;
    	}
    }
    
    // A felhasznalo lekerdezett adatait kezelo Handler
    public Handler userDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 1:
					Log.v("UserProfileActivity","Adatok betoltve");
					if (downloadUserData.isOK()) {		// Ha sikeres volt a lekerdezes
				        if(downloadUserData.getJSONString("name")!=null)		// adatok beolvasasa, ha megvannak adva
				        	((EditText)findViewById(R.userprofile.name)).setText(downloadUserData.getJSONString("name"));
				        if(downloadUserData.getJSONString("address")!=null)
				        	((EditText)findViewById(R.userprofile.address)).setText(downloadUserData.getJSONString("address"));
				        if(downloadUserData.getJSONString("email")!=null)
				        	((EditText)findViewById(R.userprofile.email)).setText(downloadUserData.getJSONString("email"));
					}
			        break;
			}
		}
	};
    
	// Mentes eredmenyet kezelo handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.userprofile.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("UserProfileActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.hasMessage()) {
						String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("UserProfileActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
    
	// Mentes kezdemenyezese
    private void saveRequest(){
    	String name = ((EditText)findViewById(R.userprofile.name)).getText().toString();		// a mezobe beirt felhasznalonev
    	String address = ((EditText)findViewById(R.userprofile.address)).getText().toString();
    	String email = ((EditText)findViewById(R.userprofile.email)).getText().toString();
    	String oldpassword = ((EditText)findViewById(R.userprofile.oldpassword)).getText().toString();
    	String password1 = ((EditText)findViewById(R.userprofile.password1)).getText().toString();
    	String password2 = ((EditText)findViewById(R.userprofile.password2)).getText().toString();
    	
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

    	String url = "UserProfile?username=" + URLEncoder.encode(username) + 
    							"&name=" + URLEncoder.encode(name) +
    							"&address=" + URLEncoder.encode(address) +
    							"&email=" + URLEncoder.encode(email);
    	if (oldpassword!=null && password1!=null) url = url + "&oldpassword=" + oldpassword.hashCode() + "&newpassword=" + password1.hashCode();
    	
    	((ProgressBar)findViewById(R.userprofile.progress)).setVisibility(ProgressBar.VISIBLE);

	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
    
    // Felhasznalo adatainak lekerdezese
    private void getUserData(){
    	String url = "GetUserData?username=" + URLEncoder.encode(username);
	    downloadUserData = new HttpGetConnection(url, userDataHandler);
	    downloadUserData.start();
    }
}
