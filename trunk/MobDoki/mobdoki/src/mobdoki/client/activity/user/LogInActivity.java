package mobdoki.client.activity.user;

import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LogInActivity extends Activity {
	private HttpGetConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	private String username = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        // Bejelentkezes gomb esemenykezeloje
        Button loginButton = (Button) findViewById(R.login.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	if (download==null || (download!=null && !download.isAlive())) loginRequest();
            }
        });  
    
	    // Regisztracio gomb esemenykezeloje
	    Button registerButton = (Button) findViewById(R.login.register);
	    registerButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {
	            Intent myIntent = new Intent(view.getContext(), RegisterActivity.class);
	            startActivity(myIntent);
	        }
	    });
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.login.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    }
    
    // Bejelentkezest kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.login.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("LogInActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					
					if (download.isOK()) {										// Ha a feldolgozas sikeres
						String userType = download.getJSONString("userType");
						
						if (userType.equals("doctor")) {										// ha a felhazsnalo orvos
							((EditText)findViewById(R.login.username)).setText("");					// mezok torlese
							((EditText)findViewById(R.login.password)).setText("");
							
							Intent myIntent = new Intent(activity,HomeDoctorActivity.class);		// orvos fomenujenek betoltese
							myIntent.putExtra("username", username);
			                startActivity(myIntent);
						}
						else if (userType.equals("patient")) {									// ha a felhasznalo paciens
							((EditText)findViewById(R.login.username)).setText("");					// mezok torlese
							((EditText)findViewById(R.login.password)).setText("");
							
							Intent myIntent = new Intent(activity,HomePatientActivity.class);		// paciens fomenujenek betoltese
							myIntent.putExtra("username", username);
							startActivity(myIntent);
						} else {																// egyebkent: hibauzenet megjelenitese
							Log.v("LogInActivity",userType);
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
		}
	};
    
	// Belepes kezdemenyezes
    private void loginRequest(){
    	
    	username = ((EditText)findViewById(R.login.username)).getText().toString();			// a mezobe beirt felhasznalonev
    	String password = ((EditText)findViewById(R.login.password)).getText().toString();	// a mezobe beirt jelszo
			
    	if (username.equals("")) {															// ha nincs adat: hibauzenet	
    		Toast.makeText(activity, "Adja meg a felhasználónevét!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if (password.equals("")) {
    		Toast.makeText(activity, "Adja meg a jelszavát!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	int pass = password.hashCode();				// a mezobe beirt jelszo hashkodja
    	
    	((ProgressBar)findViewById(R.login.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "Login?username=" + URLEncoder.encode(username) + "&password=" + pass;
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
}