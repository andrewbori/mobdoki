package mobdoki.client.activity.user;

import java.net.URLEncoder;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener {
	private HttpGetJSONConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
	private String username;
	private String password1;
	private String usertype;
	
	private EditText usernameText;
	private EditText password1Text;
	private EditText password2Text;
	private RadioButton isDoctorRB;
	private ProgressDialog progress;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        setTitle("MobDoki: Regisztráció");
        
        usernameText = (EditText)findViewById(R.register.username);
    	password1Text = (EditText)findViewById(R.register.password1);
    	password2Text = (EditText)findViewById(R.register.password2);
    	isDoctorRB = (RadioButton)findViewById(R.register.doctorUser);

    	progress = new ProgressDialog(this);
        progress.setMessage("Regisztráció...");
        progress.setIndeterminate(true);
        progress.setCancelable(true);
        
        ((Button) findViewById(R.register.okay)).setOnClickListener(this);
        ((Button) findViewById(R.register.back)).setOnClickListener(this);
    }
    
    // Kattintas esemenykezelo
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.register.okay:				// Ok - Regisztracio
			if (download==null || download.isNotUsed()) registerRequest();
			break;
		case R.register.back:				// Vissza
			finish();
			break;
		}
	}
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		progress.cancel();
    	}
    }
    
    // A regisztraciot kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 0:
					Log.v("RegisterActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.hasMessage()) {
						String message = download.getMessage();							// Uzenet lekerdezese es megjelenitese
						Log.v("RegisterActivity", message);
						Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
						
						if(download.isOK()) {
							UserInfo.putString("username", username);
							UserInfo.putString("usetype", usertype);
							UserInfo.putString("password", password1);
							UserInfo.putBoolean("isLoggedIn", false);
							UserInfo.putBoolean("isSaved", true);
							finish();
						}
					}
					break;
			}
			progress.cancel();
			download.setNotUsed();
		}
	};
    
	// Regisztracio kezdemenyezese
    private void registerRequest(){
    	username = usernameText.getText().toString();		// a mezobe beirt felhasznalonev
    	password1 = password1Text.getText().toString();
    	String password2 = password2Text.getText().toString();
    	boolean isDoctor = isDoctorRB.isChecked();
		
    	if (username.equals("") || password1.equals("") || password2.equals("")) {					// Ha nincs adat: hibauzenet
    		Toast.makeText(activity, "Adja meg a regisztrációhoz szükséges adatokat!", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if (username.length()<6) {
    		Toast.makeText(activity, "A megadott felhasználónév túl rövid.", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	if (!password1.equals(password2)) {
    		Toast.makeText(activity, "A megadott jelszavak nem egyeznek.", Toast.LENGTH_SHORT).show();
    		password1Text.setText("");
    		password2Text.setText("");
    		return;
    	}
    	
    	if (password1.length()<6) {
    		Toast.makeText(activity, "A megadott jelszó túl rövid.", Toast.LENGTH_SHORT).show();
    		password1Text.setText("");
    		password2Text.setText("");
    		return;
    	}
    	
    	int pass = password1.hashCode();		// a mezobe beirt jelszo hashkodja
    											// a felhasznalo tipusa
    	if (isDoctor) usertype="doctor"; else usertype="patient";
    	
    	progress.show();
    	
	    String url = "Register?username=" + URLEncoder.encode(username) + "&password=" + pass + "&usertype=" + usertype;
	    download = new HttpGetJSONConnection(url, mHandler);
	    download.start();
    }
}
