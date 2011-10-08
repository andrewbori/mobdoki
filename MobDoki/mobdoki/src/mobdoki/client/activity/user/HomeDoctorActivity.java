package mobdoki.client.activity.user;

import mobdoki.client.IconListArrayAdapter;
import mobdoki.client.R;
import mobdoki.client.activity.StatisticsActivity;
import mobdoki.client.activity.medicalinfo.EditHospitalActivity;
import mobdoki.client.activity.medicalinfo.EditSicknessActivity;
import mobdoki.client.activity.medicalinfo.NearestHospitalsActivity;
import mobdoki.client.activity.medicalinfo.SearchSicknessActivity;
import mobdoki.client.activity.medicalinfo.MedicalItemListActivity;
import mobdoki.client.activity.user.health.DoctorGraphActivity;
import mobdoki.client.activity.user.message.MessagesActivity;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeDoctorActivity extends Activity {
	  String[] menuElements =  
	  {  
	    "Betegségek adminisztrációja",
	    "Kórházak adminisztrációja",
	    "Betegségek listája",
	    "Betegségek keresése", 
	    "Kórházak listája",
	    "Közeli kórházak",
	    "Üzenetek",
	    "Páciensek egészségügyi grafikonjai",
	    "Statisztika",
	    "Felhasználói profil",
	    "Kijelentkezés"
	  };
	  int[] icons =  
	  {  
	    android.R.drawable.ic_menu_upload,
	    android.R.drawable.ic_menu_add,
	    android.R.drawable.ic_menu_slideshow,
	    android.R.drawable.ic_menu_search,
	    android.R.drawable.ic_menu_slideshow,
	    android.R.drawable.ic_menu_compass,
	    android.R.drawable.ic_menu_send,
	    android.R.drawable.ic_menu_gallery,
	    android.R.drawable.ic_menu_info_details,
	    android.R.drawable.ic_menu_myplaces,
	    android.R.drawable.ic_lock_power_off
	  };
	  private String username;
	  private Intent myIntent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setTitle("MobDoki: " + UserInfo.getString("username"));
        
		username = UserInfo.getString("username");
		
		UserInfo.putBoolean("isLoggedIn", true);
        
        ListView listview=(ListView) findViewById(R.home.menu);
        ArrayAdapter<String> adapter = new IconListArrayAdapter<String>(this,  
                R.layout.listview_item2,  
                R.listview_item2.icon,  
                R.listview_item2.label,  
                this.icons,  
                this.menuElements); 

        listview.setAdapter(adapter);
        
        
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        		myIntent = new Intent();
        		myIntent.putExtra("username", username);	// felhasznalonev atadasa
        		
                switch(position) {
                	case 0:
                		myIntent.setClass(view.getContext(), EditSicknessActivity.class);
                        startActivity(myIntent);
                		break;
                	case 1: 
                		myIntent.setClass(view.getContext(), EditHospitalActivity.class);
                        startActivity(myIntent);
                		break;
                	case 2:
                		myIntent.setClass(view.getContext(), MedicalItemListActivity.class);
                		myIntent.putExtra("type", "Sickness");
                        startActivity(myIntent);
                		break;
                	case 3:
                		myIntent.setClass(view.getContext(), SearchSicknessActivity.class);
                        startActivity(myIntent);
                		break;
                	case 4:
                		myIntent.setClass(view.getContext(), MedicalItemListActivity.class);
                		myIntent.putExtra("type", "Hospital");
                        startActivity(myIntent);
                		break;
                	case 5: 
                		myIntent.setClass(view.getContext(), NearestHospitalsActivity.class);
                        startActivity(myIntent);
                		break;
                	case 6:
                		myIntent.setClass(view.getContext(), MessagesActivity.class);
                		myIntent.putExtra("inbox", true);
                		startActivity(myIntent);
                		break;
                	case 7:
                		myIntent.setClass(view.getContext(),DoctorGraphActivity.class);
                		startActivity(myIntent);
                		break;
                 	case 8: 
                		myIntent.setClass(view.getContext(), StatisticsActivity.class);
                        startActivity(myIntent);
                		break;
                	case 9: 
                		myIntent.setClass(view.getContext(), UserProfileActivity.class);
                        startActivity(myIntent);
                		break;
                	case 10:
                		HttpGetJSONConnection logout = new HttpGetJSONConnection("Logout?ssid=" + UserInfo.getSSID(), new Handler());
                		logout.start();
                		
                		UserInfo.putBoolean("isLoggedIn", false);
                		if (!UserInfo.getBoolean("isSaved")) {
                			UserInfo.remove("username");
                			UserInfo.remove("password");
                			UserInfo.remove("usertype");
                		}
                		UserInfo.remove("ssid");

                		Intent intent = new Intent();				// exit
                		setResult(RESULT_OK,intent);
                		finish();
                		break;
                }
                Log.v("MenuDoctorActivity",menuElements[position]);
        	}
        });
    }
	
	@Override
	public void onBackPressed(){
		Intent intent = new Intent();				// exit
		setResult(RESULT_CANCELED,intent);
		finish();
	}

}
