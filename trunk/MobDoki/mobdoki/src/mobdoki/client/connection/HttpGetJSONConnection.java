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
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

public class HttpGetJSONConnection extends HttpConnection {
	
	// Konstruktorok
	public HttpGetJSONConnection (String url0, Handler handler0, int what0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
		what = what0;
	} 
	public HttpGetJSONConnection (String url0, Handler handler0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
	} 
	public HttpGetJSONConnection (Handler handler0, int what0) {
		handler = handler0;
		what = what0;
	}
	public HttpGetJSONConnection (Handler handler0) {
		handler = handler0;
	}
	
	@Override
	public void connectionTask() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpGet request = new HttpGet(url);
		
        try{
        	Log.v("HttpGetJSONConnection", "Valasz lekerdezese...");
        	HttpResponse response = client.execute(request);
        	
            InputStream input = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input),8000);
            StringBuilder sb = new StringBuilder();
            
            String line = null;
            while ((line=reader.readLine())!=null && isUsed) {
            	sb.append(line + "\n");				// sorok beolvasasa
            }
            input.close();
            
            /*Megszakitas*/	if (!isUsed) return; /*Megszakitas*/
            
            Log.v("HttpGetJSONConnection", "Válasz JSON objektummá alakitása...");
            String responseStr = sb.toString();
            responseJSON = new JSONObject(responseStr);

            msg = handler.obtainMessage();
            msg.what = what;
            msg.arg1 = 1;							// 1 = sikeres csatlakozas
                
        } catch(Exception ex){
        	Log.v("HttpGetJSONConnection", "Csatlakozasi hiba!");
            
        	msg = handler.obtainMessage();
        	msg.what = what;
            msg.arg1 = 0;								// 0 = sikertelen csatlakozas
        }
	}
	
	// Kapott JSON objektum
	public JSONObject getJSONObject () {
		return responseJSON;
	}
		
	// JSON valasz name kulcsahoz tartozo JSONArray
	public JSONArray getJSONArray (String name) {
		try {
			return responseJSON.getJSONArray(name);
		} catch (Exception e) {
			return new JSONArray();
		}
	}
	
/*	// JSON valasz name kulcsahoz tartozo String tomb
	public <T> ArrayList<T> getJSONArrayList (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<T> list = new ArrayList<T>();
			
			for(int i=0; i<array.length(); i++) {
				list.add((T)array.get(i));
			}
			
			return list;
		} catch (Exception e) {
			return new ArrayList<T>();
		}
	}*/
	
	// JSON valasz name kulcsahoz tartozo String tomb
	public String[] getStringArray (String name, boolean firstVoid) {
		int p=0;
		if (firstVoid) p=1;
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			String[] list = new String[array.length()+p];
			list[0]="";
			
			for (int i=0; i<array.length(); i++) {
				list[i+p]=array.getString(i);
			}
			
			return list;
		} catch (Exception e) {
			String[] list = {""};
			return list;
		}
	}
	
	// JSON valasz name kulcsahoz tartozo String tomb
	public ArrayList<String> getStringArrayList (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<String> list = new ArrayList<String>();
			
			for(int i=0; i<array.length(); i++) {
				list.add((String)array.get(i));
			}
			
			return list;
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}

	// JSON valasz name kulcsahoz tartozo integer tomb
	public ArrayList<Integer> getIntegerArrayList (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			for(int i=0; i<array.length(); i++) {
				list.add((int)array.getInt(i));
			}
			
			return list;
		} catch (Exception e) {
			return new ArrayList<Integer>();
		}
	}
	
	// JSON valasz name kulcsahoz tartozo Double tomb
	public ArrayList<Float> getFloatArrayList (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<Float> list = new ArrayList<Float>();
			
			for(int i=0; i<array.length(); i++) {
				list.add((float)array.getDouble(i));
			}
			
			return list;
		} catch (Exception e) {
			return new ArrayList<Float>();
		}
	}
	
	// JSON valasz name kulcsahoz tartozo Boolean tomb
	public ArrayList<Boolean> getBooleanArrayList (String name) {
		try {
			JSONArray array = responseJSON.getJSONArray(name);
			ArrayList<Boolean> list = new ArrayList<Boolean>();
			
			for(int i=0; i<array.length(); i++) {
				list.add((Boolean)array.get(i));
			}
			
			return list;
		} catch (Exception e) {
			return new ArrayList<Boolean>();
		}
	}
	
}
