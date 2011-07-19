package mobdoki.client.activity.user.symptom;

import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PictureInfoActivity extends Activity {
	HttpGetConnection download = null;					// szal a panaszkeplista letoltesehez
	HttpGetConnection downloadAnswer = null;			// szal az orvos valaszanak letoltesehez
	
	private Activity activity=this;
	private ArrayList<String> listElements = null;		// panaszkepnev lista
	private ArrayList<Boolean> answeredList = null;		// megvalaszolt? lista
	private ListView listview;
	
	private String username = null;						// A paciens felhasznaloneve
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.pictureinfo);
		
		Bundle extras = getIntent().getExtras();
		username = extras.getString("username");
		
		// Frissites gomb esemenykezeloje
		Button refreshButton = (Button) findViewById(R.pictureinfo.refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {
	      	public void onClick(View view) {
	      		if (download==null || (download!=null && !download.isAlive())) 
	      			refreshRequest();
	      	}
		});
		
		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.pictureinfo.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		// A megvalaszolt? (igen/nem) lista esemenykezeloje
		listview = (ListView) findViewById(R.pictureinfo.piclist);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				((EditText)findViewById(R.pictureinfo.answer)).setText("");
				Log.v("PictureInfoActivity", listElements.get(position));
				((CheckBox)findViewById(R.pictureinfo.checkBox1)).setChecked(answeredList.get(position));
				if (answeredList.get(position))
					downloadAnswer(listElements.get(position));
			}
		});
	}
	
	// Indulaskor a panaszkep lista lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	refreshRequest();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isAlive()) {
    		download.stop(); download=null;
    		((ProgressBar)findViewById(R.pictureinfo.progress)).setVisibility(ProgressBar.INVISIBLE);
    	}
    	if(downloadAnswer!=null && downloadAnswer.isAlive()){
    		downloadAnswer.stop(); downloadAnswer=null;
    	}
    }
    
    // Lekerdezett panaszkep listat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.pictureinfo.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch(msg.arg1){
				case 0:
					Log.v("PictureInfoActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					Log.v("PictureInfoActivity","Sikeres keresés");
					
					listElements = download.getJSONStringArray("imagename");
					answeredList = download.getJSONBooleanArray("answered");

					
					// listview = (ListView) findViewById(R.pictureinfo.piclist);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
																			R.layout.listview_item,  
																			listElements);  
					listview.setAdapter(adapter);
					
					break;
			}
		}
	};
	
	// Az orvos valaszanak letolteset kezelo Handler
	public Handler mdownHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.pictureinfo.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch (msg.arg1) {
			case 0:
				Log.v("PictureInfoActivity", "Sikertelen valasz lekeres.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				break;
			case 1:
				try {
					if (downloadAnswer.isOK()) {
						String answer = downloadAnswer.getJSONString("answer");
						Log.v("PictureInfoActivity", answer);
						((EditText)findViewById(R.pictureinfo.answer)).setText(answer);
					} else Toast.makeText(activity, downloadAnswer.getMessage(), Toast.LENGTH_SHORT).show();
				} catch (Exception e) {}
				break;
			}
		}
	};
    
	// Panaszkep lista lekeresenek kezdemenyezese
    private void refreshRequest(){    	
    	((ProgressBar)findViewById(R.pictureinfo.progress)).setVisibility(ProgressBar.VISIBLE);
	    String url = "PictureInfo?username=" + URLEncoder.encode(username);
	    download = new HttpGetConnection(url, mHandler);
	    download.start();
    }
    
    // Orvos valaszanak letoltesenek kezdemenyezese
    public void downloadAnswer(String picturename){
    	if(downloadAnswer!=null && downloadAnswer.isAlive()){
    		downloadAnswer.stop(); downloadAnswer=null;
    	}
    	((ProgressBar)findViewById(R.pictureinfo.progress)).setVisibility(ProgressBar.VISIBLE);
    	String url = "AnswerDownload?picturename=" + URLEncoder.encode(picturename);
    	downloadAnswer = new HttpGetConnection(url, mdownHandler);
    	downloadAnswer.start();
    }
}
