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
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

public class HttpPostConnection extends HttpConnection {
	protected HttpEntity data;		// POST-olando entitas

	// Konstruktorok
	public HttpPostConnection (String url0, Handler handler0, HttpEntity data0, int what0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
		data = data0;
		what = what0;
	}
	public HttpPostConnection (String url0, Handler handler0, HttpEntity data0) {
		url = new String(Connection.myhost + url0);
		handler = handler0;
		data = data0;
	}
	public HttpPostConnection (Handler handler0) {
		handler = handler0;
	}
	
	@Override
	public void connectionTask() {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
        HttpPost request = new HttpPost(url);
		
        try{ 
        	Log.v("HttpPostConnection", "Valasz lekerdezese...");
        	
        	request.setEntity(data);
        	HttpResponse response = client.execute(request);
        	
            InputStream input = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input),8000);
            StringBuilder sb = new StringBuilder();
            
            responseJSON = new JSONObject();

            String line = null;
            while ((line = reader.readLine()) != null) {
            	sb.append(line + "\n");					// sorok beolvasasa
            }
            input.close();
            
            Log.v("HttpPostConnection", "Válasz JSON objektummá alakitása...");
            String responseStr = sb.toString();
            responseJSON = new JSONObject(responseStr);
            
            msg = handler.obtainMessage();
            msg.what = what;
            msg.arg1 = 1;								// 1 = sikeres csatlakozas
                
        } catch(Exception ex){
        	Log.v("HttpPostConnection", "Csatlakozasi hiba!" + ex.getMessage());
        	 
        	msg = handler.obtainMessage();
        	msg.what = what;
            msg.arg1 = 0;								// 0 = sikertelen csatlakozas
        }
	}
}

