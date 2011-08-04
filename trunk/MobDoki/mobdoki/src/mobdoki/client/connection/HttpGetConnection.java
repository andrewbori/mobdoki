package mobdoki.client.connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpGetConnection extends Thread {
	private String url;							// a cimzett
	private Handler handler;					// a hivo osztaly Handler-je
	private int what = 1;						// a hivo azonositoja - ezzel osztalyonkent eleg egy Handler
	private JSONObject responseJSON;			// válasz
	
	public HttpGetConnection (String url0, Handler handler0, int what0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
		what = what0;
	} 
	
	public HttpGetConnection (String url0, Handler handler0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
	} 

	public HttpGetConnection (Handler handler0) {
		handler = handler0;
	}
	
	@Override
    public void run() {
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpGet request = new HttpGet(url);
		
        try{
        	Log.v("HttpGetConnection", "Valasz lekerdezese...");
        	HttpResponse response = client.execute(request);
        	
            InputStream input = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input),8000);
            StringBuilder sb = new StringBuilder();
            
            responseJSON = new JSONObject();
            String line = null;
            while ((line = reader.readLine()) != null) {
            	sb.append(line + "\n");				// sorok beolvasasa
            }
            input.close();
             
            Log.v("HttpGetConnection", "Válasz JSON objektummá alakitása...");
            String responseStr = sb.toString();
            responseJSON = new JSONObject(responseStr);

            Message msg = handler.obtainMessage();
            msg.what = what;
            msg.arg1 = 1;							// 1 = sikeres csatlakozas
            handler.sendMessage(msg);				// uzenet elkuldese a hivo Handler-jenek
                
        } catch(Exception ex){
        	Log.v("HttpGetConnection", "Csatlakozasi hiba!");
            
        	Message msg = handler.obtainMessage();
        	msg.what = what;
            msg.arg1 = 0;								// 0 = sikertelen csatlakozas
            handler.sendMessage(msg);					// uzenet elkuldese a hivo Handler-jenek
        }
    }
	
	// Kapott JSON objektum
	public JSONObject getJSON () {
		return responseJSON;
	}
	
	// JSON valasz name kulcsanak String erteke
	public String getJSONString (String name) {
		try {
			return responseJSON.getString(name);
		} catch (JSONException e) {
			return null;
		}
	}

	// JSON valasz name kulcsanak Integer erteke
	public int getJSONInt (String name) {
		try {
			return responseJSON.getInt(name);
		} catch (JSONException e) {
			return 0;
		}
	}

	// JSON valasz name kulcsahoz tartozo JSONArray
	public JSONArray getJSONArray (String name) {
		try {
			return responseJSON.getJSONArray(name);
		} catch (JSONException e) {
			return null;
		}
	}
	
	// JSON valasz name kulcsahoz tartozo String tomb
	public ArrayList<String> getJSONStringArray (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<String> list = new ArrayList<String>();
			
			for(int i=0; i<array.length(); i++) {
				list.add(array.getString(i));
			}
			
			return list;
		} catch (JSONException e) {
			return new ArrayList<String>();
		}
	}
	
	// JSON valasz name kulcsahoz tartozo Boolean tomb
	public ArrayList<Boolean> getJSONBooleanArray (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<Boolean> list = new ArrayList<Boolean>();
			
			for(int i=0; i<array.length(); i++) {
				list.add(array.getBoolean(i));
			}
			
			return list;
		} catch (JSONException e) {
			return new ArrayList<Boolean>();
		}
	}
	
	// A csatlakozas eredmenyenek allapotkodja
	private int getStatus() {
		try {
			return responseJSON.getInt("status");
		} catch (JSONException e) {
			return 0;
		}
	}
	
	// A kapcsolat eredmenyes volt?
	public boolean isOK() {
		return (getStatus()==Status.OK);
	}
	
	// A kapcsolat kozben hiba tortent? (adatbazis hiba)
	public boolean isERROR() {
		return (getStatus()==Status.ERROR);
	}
	
	// A kapcsolat kozben fatalis hiba tortent? (adatbazis nem erheto el)
	public boolean isFERROR() {
		return (getStatus()==Status.FATAL_ERROR);
	}
	
	// Kuldott uzenetet a szerver? (Ervenyes allapotkoddal tert vissza?)
	public boolean hasMessage() {
		int s = getStatus();
		return (s==Status.OK || s==Status.ERROR || s==Status.FATAL_ERROR);
	}
	
	// A szerver uzenete (JSON objektum message kulcsahoz tartozo szoveg)
	public String getMessage() {
		try {
			return responseJSON.getString("message");
		} catch (JSONException e) {
			return null;
		}
	}
	
	// Szerver url-jenek beallitasa
	public void setURL(String url) {
		this.url=new String(url);
	}
}
