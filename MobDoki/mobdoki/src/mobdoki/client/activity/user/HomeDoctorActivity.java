package mobdoki.client.activity.user;

import mobdoki.client.IconListArrayAdapter;
import mobdoki.client.R;
import mobdoki.client.activity.StatisticsActivity;
import mobdoki.client.activity.medicalinfo.AddHospitalActivity;
import mobdoki.client.activity.medicalinfo.AddPicToSymptomActivity;
import mobdoki.client.activity.medicalinfo.AddSymptomActivity;
import mobdoki.client.activity.medicalinfo.NearestHospitalsActivity;
import mobdoki.client.activity.medicalinfo.NewHospitalActivity;
import mobdoki.client.activity.medicalinfo.NewSicknessActivity;
import mobdoki.client.activity.medicalinfo.SearchSicknessActivity;
import mobdoki.client.activity.medicalinfo.SicknessListActivity;
import mobdoki.client.activity.user.health.DoctorGraphActivity;
import mobdoki.client.activity.user.symptom.PictureCheckActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeDoctorActivity extends Activity {
	  String[] menuElements =  
	  {  
	    "Betegség felvétele",
	    "Tünet megadása",
	    "Kép rendelése tünethez",
	    "Kórház felvétele",
	    "Kórház hozzárendelése betegséghez",
	    "Betegségek listája",
	    "Betegségek keresése", 
	    "Közeli kórházak",
	    "Tünet képek elbírálása",
	    "Páciensek egészségügyi grafikonjai",
	    "Statisztika",
	    "Felhasználói profil",
	    "Kilépés"
	  };
	  int[] icons =  
	  {  
	    android.R.drawable.ic_menu_upload,
	    android.R.drawable.ic_menu_set_as,
	    android.R.drawable.ic_menu_set_as,
	    android.R.drawable.ic_menu_add,
	    android.R.drawable.ic_menu_directions,
	    android.R.drawable.ic_menu_slideshow,
	    android.R.drawable.ic_menu_search,
	    android.R.drawable.ic_menu_compass,
	    android.R.drawable.ic_menu_view,
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
        
        Bundle extras = getIntent().getExtras();
		username = extras.getString("username");
        
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
                		myIntent.setClass(view.getContext(), NewSicknessActivity.class);
                        startActivity(myIntent);
                		break;
                	case 1: 
                		myIntent.setClass(view.getContext(), AddSymptomActivity.class);
                        startActivity(myIntent);
                		break;
                	case 2:
                		myIntent.setClass(view.getContext(), AddPicToSymptomActivity.class);
                		startActivity(myIntent);
                		break;
                	case 3: 
                		myIntent.setClass(view.getContext(), NewHospitalActivity.class);
                        startActivity(myIntent);
                		break;
                	case 4: 
                		myIntent.setClass(view.getContext(), AddHospitalActivity.class);
                        startActivity(myIntent);
                		break;
                	case 5:
                		myIntent.setClass(view.getContext(), SicknessListActivity.class);
                        startActivity(myIntent);
                		break;
                	case 6:
                		myIntent.setClass(view.getContext(), SearchSicknessActivity.class);
                        startActivity(myIntent);
                		break;
                	case 7: 
                		myIntent.setClass(view.getContext(), NearestHospitalsActivity.class);
                        startActivity(myIntent);
                		break;
                	case 8:
                		myIntent.setClass(view.getContext(), PictureCheckActivity.class);
                		startActivity(myIntent);
                		break;
                	case 9:
                		myIntent.setClass(view.getContext(),DoctorGraphActivity.class);
                		startActivity(myIntent);
                		break;
                 	case 10: 
                		myIntent.setClass(view.getContext(), StatisticsActivity.class);
                        startActivity(myIntent);
                		break;
                	case 11: 
                		myIntent.setClass(view.getContext(), UserProfileActivity.class);
                        startActivity(myIntent);
                		break;
                	case 12: 
                        finish();
                		break;
                }
                Log.v("MenuDoctorActivity",menuElements[position]);
        	}
        });
    }

}
