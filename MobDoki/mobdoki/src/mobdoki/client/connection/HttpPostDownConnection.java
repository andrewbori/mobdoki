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

public class HttpPostDownConnection extends Thread {
	private String url;						// a cimzett
	private Handler handler;				// a hivo osztaly Handler-je
	private byte[] map;						// címzett válasza byte[]-ben
	
	public HttpPostDownConnection (String url0, Handler handler0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
	}

	public HttpPostDownConnection (Handler handler0) {
		handler = handler0;
	}
	
	@Override
    public void run() {
		
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpGet request = new HttpGet(url);
		
        try{
        	Log.v("HttpPostDownConnection", "Valasz lekerdezese...");
        	HttpResponse response = client.execute(request);
        	
        	InputStream input = response.getEntity().getContent();
             
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int ch;
            while ((ch = input.read()) != -1) {			// szervertol kapott adatok beolvasasa
                bos.write(ch);
            }
            map = bos.toByteArray();					// byte tombbe konvertalas
            
            input.close();
               
            Message msg = handler.obtainMessage();
            msg.arg1 = 1;								// 1 = sikeres csatlakozas
            handler.sendMessage(msg);					// uzenet elkuldese a hivo Handler-jenek
                
        } catch(Exception ex){
        	Log.v("HttpPostDownConnection", "Csatlakozasi hiba!");
            
        	Message msg = handler.obtainMessage();
            msg.arg1 = 0;								// 0 = sikertelen csatlakozas
            handler.sendMessage(msg);					// uzenet elkuldese a hivo Handler-jenek
        }
    }
	
	// Szervertol kapott adat lekerdezese
	public byte[] getResponse(){
		return map;
	}
	
	// Cimzett cimenek beallitasa
	public void setURL(String url) {
		this.url=new String(url);
	}
}

