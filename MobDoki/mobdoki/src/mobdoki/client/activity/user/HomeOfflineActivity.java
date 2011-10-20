package mobdoki.client.activity.user;

import mobdoki.client.IconListArrayAdapter;
import mobdoki.client.R;
import mobdoki.client.activity.medicalinfo.MedicalItemListActivity;
import mobdoki.client.activity.medicalinfo.SearchSicknessActivity;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeOfflineActivity extends Activity {
	  String[] menuElements =  
	  {  
		"Betegségek listája",
	    "Betegségek keresése",
	    "Kórházak listája",
	    "Kilépés"
	  };
	  int[] icons =  
	  {   
		android.R.drawable.ic_menu_slideshow,
	    android.R.drawable.ic_menu_search,
	    android.R.drawable.ic_menu_slideshow,
	    android.R.drawable.ic_lock_power_off,  
	  };
	  private Intent myIntent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setTitle("MobDoki: Offline");
        
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
        		
                switch(position) {
                	case 0:
                		myIntent.setClass(view.getContext(), MedicalItemListActivity.class);
                		myIntent.putExtra("type", "Sickness");
                        startActivity(myIntent);
                		break;
                	case 1:
                		myIntent.setClass(view.getContext(), SearchSicknessActivity.class);
                        startActivity(myIntent);
                		break;
                	case 2:
                		myIntent.setClass(view.getContext(), MedicalItemListActivity.class);
                		myIntent.putExtra("type", "Hospital");
                        startActivity(myIntent);
                		break;
                	case 3:
                		Intent intent = new Intent();				// exit
                		setResult(RESULT_OK,intent);
                		UserInfo.putBoolean("offline", false);
                		finish();
                		break;
                }
                Log.v("MenuPatientActivity", menuElements[position]);
        	}
        });
    }
	
	@Override
	public void onBackPressed(){
		Intent intent = new Intent();				// exit
		setResult(RESULT_CANCELED,intent);
		UserInfo.putBoolean("offline", false);
		finish();
	}
}
