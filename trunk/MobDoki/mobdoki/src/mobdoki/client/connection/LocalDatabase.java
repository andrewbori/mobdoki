package mobdoki.client.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LocalDatabase {
	private static LocalDatabaseHelper ldbh;
	private static HttpGetJSONConnection download = null;			// szal a tunetek es korhazak letoltesehez
	
    public static void init(Context context)
    {
    	ldbh = new LocalDatabaseHelper(context);
    }

    public static LocalDatabaseHelper getDB() {
    	return ldbh;
    }
    
    // Lekerdezett adatokat kezelo Handler
    public static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if (msg.arg1==1 && download.isOK()) {		// Ha sikeres lekerdezes...
					ldbh.onUpgrade(ldbh.open(), 1, 1);
					Log.v("LocalDatabase", "Sikeres adatlekerdezes");
										
					try {
						ldbh.fillSickness(download.getJSONArray("sickness"));
						ldbh.fillSymptom(download.getJSONArray("symptom"));
						ldbh.fillHospital(download.getJSONArray("hospital"));
						ldbh.fillDiagnosis(download.getJSONArray("diagnosis"));
						ldbh.fillCuring(download.getJSONArray("curing"));
					} catch (Exception e) {
						Log.v("LocalDatabase", "JSON hiba!");
					}
			}
			download.setNotUsed();
			download=null;
		}
    };
    
    public static void fillDB() {
    	String url = "GetEverything?ssid=" + UserInfo.getSSID();
		download = new HttpGetJSONConnection(url, mHandler);
		download.start();
    }

}
