package mobdoki.client.connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpPostUpConnection extends Thread {
	private String url;								// a cimzett
	private Handler handler;						// a hivo osztaly Handler-je
	private JSONObject responseJSON;				// válasz
	private HttpEntity data;
	
	public HttpPostUpConnection (String url0, Handler handler0, HttpEntity data0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
		data = data0;
	}

	public HttpPostUpConnection (Handler handler0) {
		handler = handler0;
	}
	
	@Override
    public void run() {
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpPost request = new HttpPost(url);
		
        try{ 
        	Log.v("HttpPostUpConnection", "Valasz lekerdezese...");
        	
        	request.setEntity(data);
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
            
            Log.v("HttpPostUpConnection", "Válasz JSON objektummá alakitása...");
            String responseStr = sb.toString();
            responseJSON = new JSONObject(responseStr);
            
            Message msg = handler.obtainMessage();
            msg.arg1 = 1;							// 1 = sikeres csatlakozas
            handler.sendMessage(msg);				// uzenet elkuldese a hivo Handler-jenek
                
        } catch(Exception ex){
        	Log.v("HttpPostUpConnection", "Csatlakozasi hiba!" + ex.getMessage());
        	 
        	Message msg = handler.obtainMessage();
            msg.arg1 = 0;								// 0 = sikertelen csatlakozas
            handler.sendMessage(msg);					// uzenet elkuldese a hivo Handler-jenek
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

