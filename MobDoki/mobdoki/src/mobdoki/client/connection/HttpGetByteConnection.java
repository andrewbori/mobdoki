package mobdoki.client.connection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpGetByteConnection extends Thread {
	private String url;						// a cimzett
	private Handler handler;				// a hivo osztaly Handler-je
	private int what = 1;					// a hivo azonositoja - ezzel osztalyonkent eleg egy Handler
	private byte[] map;						// címzett válasza byte[]-ben
	Message msg;
	
	private volatile boolean isUsed = true;			// A szal fut vagy hasznalatban van
	
	public HttpGetByteConnection (String url0, Handler handler0, int what0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
		what=what0;
	}
	public HttpGetByteConnection (String url0, Handler handler0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
	}
	public HttpGetByteConnection (Handler handler0) {
		handler = handler0;
	}
	
	// Cimzett cimenek beallitasa
	public void setURL(String url0) {
		url = new String(url0);
	}
	
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
	
	@Override
    public void run() {
		connectionTask();
		handler.sendMessage(msg);					// uzenet elkuldese a hivo Handler-jenek
    }
	
	public void connectionTask() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpGet request = new HttpGet(url);
		
        try{
        	Log.v("HttpGetByteConnection", "Valasz lekerdezese...");
        	HttpResponse response = client.execute(request);
        	
        	InputStream input = response.getEntity().getContent();
             
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int ch;
            while ((ch = input.read()) != -1) {			// szervertol kapott adatok beolvasasa
                bos.write(ch);
            }
            map = bos.toByteArray();					// byte tombbe konvertalas
            
            input.close();
               
            msg = handler.obtainMessage();
            msg.what = what;
            msg.arg1 = 1;								// 1 = sikeres csatlakozas
                
        } catch(Exception ex){
        	Log.v("HttpGetByteConnection", "Csatlakozasi hiba!");
            
        	msg = handler.obtainMessage();
        	msg.what = what;
            msg.arg1 = 0;								// 0 = sikertelen csatlakozas
        }
	}
	
	// Szervertol kapott adat lekerdezese
	public byte[] getResponse(){
		return map;
	}
}

