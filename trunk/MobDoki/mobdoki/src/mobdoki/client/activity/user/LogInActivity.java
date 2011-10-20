package mobdoki.client.activity.user;

import java.net.URLEncoder;

import mobdoki.client.MessageService;
import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.LocalDatabase;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends Activity implements OnClickListener {
	private HttpGetJSONConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	private String username = null;
	private String password = null;
	private String usertype = null;
	
	private EditText usernameText;
	private EditText passwordText;
	private CheckBox checkbox;
	
	ProgressDialog progress;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.login);
        setTitle("MobDoki: Bejelentkezés");
        
        UserInfo.init(this.getApplicationContext());
        LocalDatabase.init(this.getApplicationContext());
        
        usernameText = (EditText)findViewById(R.login.username);
        passwordText = (EditText)findViewById(R.login.password);

	    checkbox = (CheckBox) findViewById(R.login.checkBox1);
	    checkbox.setOnClickListener(this);
	    
        ((Button) findViewById(R.login.login)).setOnClickListener(this);
	    ((Button) findViewById(R.login.register)).setOnClickListener(this);
	    ((Button) findViewById(R.login.offline)).setOnClickListener(this);
    }
    
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.login.login:				// Bejelentkezes
			if (download==null || download.isNotUsed()) {
				UserInfo.putBoolean("offline", false);
				loginRequest();
			}
			break;
		case R.login.register:			// Regisztracio
			Intent myIntent = new Intent(activity, RegisterActivity.class);
            startActivity(myIntent);
			break;
		case R.login.offline:			// Regisztracio
			if (download==null || download.isNotUsed()) {
				UserInfo.putBoolean("offline", true);
				myIntent = new Intent(activity, HomeOfflineActivity.class);		// orvos fomenujenek betoltese
				startActivityForResult(myIntent, 0);
			}
			break;
		case R.login.checkBox1:
			UserInfo.putBoolean("isSaved", checkbox.isChecked());
			break;
		}
	}
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    		progress.cancel();
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if (UserInfo.getBoolean("isLoggedIn")) {
    		usertype = UserInfo.getString("usertype");
    		username = UserInfo.getString("username");
    		if (usertype.equals("doctor")) {										// ha a felhazsnalo orvos
				Intent myIntent = new Intent(activity,HomeDoctorActivity.class);		// orvos fomenujenek betoltese
				startActivityForResult(myIntent,0);
			}
			else if (usertype.equals("patient")) {									// ha a felhasznalo paciens
				Intent myIntent = new Intent(activity,HomePatientActivity.class);		// paciens fomenujenek betoltese
				startActivityForResult(myIntent,0);
			}
    	}
    	
    	if (UserInfo.getBoolean("isSaved")) {
    		checkbox.setChecked(true);
	    	usernameText.setText(UserInfo.getString("username"));
	    	passwordText.setText(UserInfo.getString("password"));
    	}
    	else {
    		checkbox.setChecked(false);
    		usernameText.setText("");
	    	passwordText.setText("");
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        	if (resultCode==RESULT_CANCELED) {					// ha nem jelentkezett ki, akkor a keszulek fomenujebe lep
        		finish();
        	}
        	else {
        		stopService(new Intent(LogInActivity.this, MessageService.class));
        		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        		notificationManager.cancel(R.string.app_message_notification_id);
        	}
    }
    
    // Bejelentkezest kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 0:
					Log.v("LogInActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					
					if (download.isOK()) {										// Ha a feldolgozas sikeres
						String SSID = download.getString("ssid");
						int userid = download.getInt("userid");
						usertype = download.getString("usertype");
						int usertypeid = download.getInt("usertypeid");
						String lastmailcheck = download.getString("lastmailcheck");
						
						UserInfo.putString("ssid", SSID);
						UserInfo.putInt("userid", userid);
						UserInfo.putString("username", username);
						UserInfo.putInt("usertype", usertypeid);
						UserInfo.putString("usertype", usertype);
						UserInfo.putString("password", password);
						UserInfo.putString("lastmailcheck", lastmailcheck);
						
						LocalDatabase.fillDB();
						
						if (usertype.equals("doctor")) {										// ha a felhazsnalo orvos
							startService(new Intent(activity, MessageService.class));
							Intent myIntent = new Intent(activity,HomeDoctorActivity.class);		// orvos fomenujenek betoltese
							startActivityForResult(myIntent,0);
						}
						else if (usertype.equals("patient")) {									// ha a felhasznalo paciens
							startService(new Intent(activity, MessageService.class));
							Intent myIntent = new Intent(activity,HomePatientActivity.class);		// paciens fomenujenek betoltese
							startActivityForResult(myIntent,0);
						} else {																// egyebkent: hibauzenet megjelenitese
							Log.v("LogInActivity",usertype);
							Toast.makeText(activity, "Ismeretlen felhasználói típus.", Toast.LENGTH_SHORT).show();
						}
					} else if (download.isERROR()) {						// ha egyszeru hiba tortent
						String message = download.getMessage();
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					} else if (download.isFERROR()) {					// ha sulyos hiba tortent
						String message = download.getMessage();
						new AlertDialog.Builder(activity)
						.setMessage(message)
						.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {}
						}).show();
					}
					break;
			}
			setProgressBarIndeterminateVisibility(false);
			progress.cancel();
			
			download.setNotUsed();
		}
	};
    
	// Belepes kezdemenyezes
    private void loginRequest(){
    	
    	username = usernameText.getText().toString();			// a mezobe beirt felhasznalonev
    	password = passwordText.getText().toString();			// a mezobe beirt jelszo
			
    	if (username.equals("")) {															// ha nincs adat: hibauzenet	
    		Toast.makeText(activity, "Adja meg a felhasználónevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (password.equals("")) {
    		Toast.makeText(activity, "Adja meg a jelszavát!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	int pass = password.hashCode();				// a mezobe beirt jelszo hashkodja
    	
    	setProgressBarIndeterminateVisibility(true);
    	progress = new ProgressDialog(this);
        progress.setMessage("Bejelentkezés...");
        progress.setIndeterminate(true);
        progress.setCancelable(true);
    	progress.show();
    	
	    String url = "Login?username=" + URLEncoder.encode(username) + "&password=" + pass;
	    download = new HttpGetJSONConnection(url, mHandler);
	    download.start();
    }
}