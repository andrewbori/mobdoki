package mobdoki.client.connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

public abstract class HttpConnection extends Thread {
	protected String url;									// a meghivando url
	protected Handler handler;								// a hivo osztaly Handler-je
	protected int what = 1;									// a hivo azonositoja
	protected JSONObject responseJSON = new JSONObject();	// JSON valasz
	protected Message msg;									// handlernek atadando uzenet
	
	protected volatile boolean isUsed = true;				// A szal fut vagy hasznalatban van?
	
	// Szerver url-jenek beallitasa
	public void setURL(String url0) {
		url = new String(url0);
	}
	/************************************************************************/
	// Hasznalatban van meg a szal?
	public boolean isUsed() {
		return isUsed;
	}
	// Nincs hasznalatban?
	public boolean isNotUsed() {
		return !isUsed;
	}
	// Mar nem kell a szal
	public void setNotUsed() {
		isUsed = false;
	}
	/************************************************************************/
	@Override
    public void run() {
		connectionTask();
		if (getStatus()==401) {									// Ha lejart a session
			if (refreshSession()) connectionTask();					// session frissitese
			if (getStatus()==401) {									// Ha a frissitessel se sikerult jogot kapni
				try {
					responseJSON.put("status", 400);					// 400>>401, hibauzenet miatt
				} catch (Exception e) {}
			}
		}
		if (isUsed) handler.sendMessage(msg);					// uzenet elkuldese a hivo Handler-jenek (ha nem lett leallitva...)
    }
	
	abstract public void connectionTask();
	
	// Megnyitott Session meghosszabbitasa
	public boolean refreshSession () {
		String ssid = UserInfo.getSSID();
		String username = UserInfo.getString("username");
		int password = UserInfo.getString("password").hashCode();
		
		try {
			URL url = new URL(Connection.myhost + "RefreshSession?ssid=" + URLEncoder.encode(ssid) +
											  				"&username=" + URLEncoder.encode(username) +
											  				"&password=" + password);
			URLConnection urlConn = url.openConnection();
			InputStream input = urlConn.getInputStream();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            while (reader.readLine() != null) {}
            input.close();
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/************************************************************************
	 *						 JSON objektum lekerdezese						*
	 ************************************************************************/
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
	/************************************************************************/
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
	/************************************************************************/
	// JSON valasz name kulcsanak String erteke
	public String getString (String name) {
		try {
			return responseJSON.getString(name);
		} catch (Exception e) {
			return null;
		}
	}
	// JSON valasz name kulcsanak Integer erteke
	public int getInt (String name) {
		try {
			return responseJSON.getInt(name);
		} catch (Exception e) {
			return 0;
		}
	}
	// JSON valasz name kulcsanak Double erteke
	public double getDouble (String name) {
		try {
			return responseJSON.getDouble(name);
		} catch (Exception e) {
			return 0.0;
		}
	}
	/************************************************************************/
}
