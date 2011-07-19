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
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	HttpGetConnection download = null;		// szal a webszerverhez csatlakozashoz
	private Activity activity = this;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        // Oke gomb esemenykezeloje
        Button okayButton = (Button) findViewById(R.register.okay);
        okayButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if (download==null || (download!=null && !download.isAlive())) registerRequest();
        	}
        });
        
        // Vissza gomb esemenykezeloje
        Button backButton = (Button) findViewById(R.register.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.register.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    }
    
    // A regisztraciot kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.register.progress)).setVisibility(ProgressBar.INVISIBLE);
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
					}
					break;
			}
		}
	};
    
	// Regisztracio kezdemenyezese
    private void registerRequest(){
    	String username = ((EditText)findViewById(R.register.username)).getText().toString();		// a mezobe beirt felhasznalonev
    	String password1 = ((EditText)findViewById(R.register.password1)).getText().toString();
    	String password2 = ((EditText)findViewById(R.register.password2)).getText().toString();
    	boolean isDoctor = ((RadioButton)findViewById(R.register.doctorUser)).isChecked();
		
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
    		((EditText)findViewById(R.register.password1)).setText("");
    		((EditText)findViewById(R.register.password2)).setText("");
    		return;
    	}
    	
    	if (password1.length()<6) {
    		Toast.makeText(activity, "A megadott jelszó túl rövid.", Toast.LENGTH_SHORT).show();
    		((EditText)findViewById(R.register.password1)).setText("");
    		((EditText)findViewById(R.register.password2)).setText("");
    		return;
    	}
    	
    	int pass = password1.hashCode();		// a mezobe beirt jelszo hashkodja
    	String usertype;						// a felhasznalo tipusa
    	if (isDoctor) usertype="doctor"; else usertype="patient";
    	
    	((ProgressBar)findViewById(R.register.progress)).setVisibility(ProgressBar.VISIBLE);
    	
	    String url = "Register?username=" + URLEncoder.encode(username) + "&password=" + pass + "&usertype=" + usertype;
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
}
