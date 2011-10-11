package mobdoki.client.activity.user.message;

import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpGetByteConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ShowMessageActivity extends Activity implements OnClickListener {
	private final int TASK_GETMESSAGE    = 1;
	private final int TASK_GETIMAGE      = 2;
	private final int TASK_GETUSERIMAGE  = 3;
	private final int TASK_DELETEMESSAGE = 4;
	
	private int id;
	private int senderID;
	private String username;
	private String subject;
	private String date;
	private boolean inbox;
	private int imageID;
	
	private HttpGetJSONConnection download = null;				// szal az uzenet letoltesehez
	private HttpGetJSONConnection download2 = null;
	private HttpGetByteConnection downloadImage = null;
	private HttpGetByteConnection downloadUserImage = null;
	
	private Activity activity=this;
	private AlertDialog alert;
	
	private ProgressBar progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.showmessage);
		setTitle("MobDoki: Üzenet");
      
		Bundle extras = getIntent().getExtras();
		id = extras.getInt("id");				// Uzenet ID-ja
		subject = extras.getString("subject");		// targya
		username = extras.getString("sender");		// feladoja/cimzettje
		date = extras.getString("date");			// datuma
		imageID = extras.getInt("image");			// kep id-je
		inbox = extras.getBoolean("inbox");		// Ez egy bejovo level?

		((TextView)findViewById(R.showmessage.subject)).setText(subject);
		((TextView)findViewById(R.showmessage.username)).setText(username);
		((TextView) findViewById(R.showmessage.date)).setText(date);
		
		if (!inbox) ((Button) findViewById(R.showmessage.answer)).setVisibility(Button.GONE);
		
		progress = (ProgressBar) findViewById(R.showmessage.progress);
		
		((Button) findViewById(R.showmessage.back)).setOnClickListener(this);
		((Button) findViewById(R.showmessage.delete)).setOnClickListener(this);
		((Button) findViewById(R.showmessage.answer)).setOnClickListener(this);
	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.showmessage.back:				// Vissza gomb esemenykezeloje
			finish();
			break;		
		case R.showmessage.delete:
			if (alert==null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Biztos törli az üzenetet?")
				       .setCancelable(false)
				       .setPositiveButton("Igen", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   deleteMessage();
				           }
				       })
				       .setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   dialog.cancel();
				           }
				       });
				alert = builder.create();
			}
			if (download==null || download.isNotUsed()) alert.show();
			break;
		case R.showmessage.answer:
			Intent myIntent = new Intent(activity, NewMessageActivity.class);
			myIntent.putExtra("id", id);
			myIntent.putExtra("recipientID", senderID);
			myIntent.putExtra("recipient", username);
			myIntent.putExtra("subject", subject);
    		startActivity(myIntent);
			break;
		}
	}
	
	// Indulaskor a lista lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	progress.setVisibility(ProgressBar.VISIBLE);
    	getMessage();
    	getImage();
    	getUserImage();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    	if (downloadImage!=null && downloadImage.isUsed()) {
    		downloadImage.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    	if (downloadUserImage!=null && downloadUserImage.isUsed()) {
    		downloadUserImage.setNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }

    // Lekerdezett listat kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case 0:
					Log.v("ShowMessageActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					if (msg.what==TASK_GETMESSAGE) download.setNotUsed();
					else if (msg.what==TASK_GETIMAGE)  downloadImage.setNotUsed();
					else if (msg.what==TASK_GETUSERIMAGE) downloadUserImage.setNotUsed();
					else if (msg.what==TASK_DELETEMESSAGE) download2.setNotUsed();
					break;
				case 1:
					switch(msg.what) {
					case TASK_GETMESSAGE:
						if (download.isOK()) {		// Ha sikeres lekerdezes...
							Log.v("ShowMessageActivity","Sikeres lekerdezes");
							
							senderID = download.getInt("senderID");
							
							((TextView)findViewById(R.showmessage.text)).setText(download.getString("text"));
	
						} else Toast.makeText(activity, download.getMessage(), Toast.LENGTH_SHORT).show();
						 download.setNotUsed();
						break;
					case TASK_GETIMAGE:
						byte[] map = downloadImage.getResponse();
						((ImageView)findViewById(R.showmessage.imageView)).setImageBitmap(BitmapFactory.decodeByteArray(map, 0, map.length));
						downloadImage.setNotUsed();
						break;
					case TASK_GETUSERIMAGE:
						byte[] map2 = downloadUserImage.getResponse();
						((ImageView)findViewById(R.showmessage.userImage)).setImageBitmap(BitmapFactory.decodeByteArray(map2, 0, map2.length));
						downloadUserImage.setNotUsed();
						break;
					case TASK_DELETEMESSAGE:
						if (download2.hasMessage()) {
							Toast.makeText(activity, download2.getMessage(), Toast.LENGTH_SHORT).show();
						}
						if (download2.isOK()) finish();
						download2.setNotUsed();
						break;
					}
					break;
			}
			if (!(download!=null && download.isUsed()) && !(downloadImage!=null && downloadImage.isUsed())) {
				progress.setVisibility(ProgressBar.GONE);
				if (!(download2!=null && download2.isUsed()) && !(downloadUserImage!=null && downloadUserImage.isUsed())) setProgressBarIndeterminateVisibility(false);
			}
		}
	};
	
	// Uzenet lekeresenek kezdemenyezese
    private void getMessage(){
    	setProgressBarIndeterminateVisibility(true);
    	
    	String str;
    	if (inbox) str="true"; else str="false";
	    String url = "MessageDownload?id=" + id + "&inbox=" + str +
	    							 "&ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler, TASK_GETMESSAGE);
	    download.start();
    }

	// Kep lekeresenek kezdemenyezese
    private void getImage(){
    	if (imageID==0) return;
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "ImageDownload?size=medium&id=" + imageID +
	    						  "&ssid=" + UserInfo.getSSID();
	    downloadImage = new HttpGetByteConnection(url, mHandler, TASK_GETIMAGE);
	    downloadImage.start();
    }
    
	// Kep lekeresenek kezdemenyezese
    private void getUserImage(){
    	if (username.equals("[Minden orvos]")) return;
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "ImageDownload?size=small&username=" + username +
	    						  "&ssid=" + UserInfo.getSSID();
	    downloadUserImage = new HttpGetByteConnection(url, mHandler, TASK_GETUSERIMAGE);
	    downloadUserImage.start();
    }
    
	// Uzenet torlesenek kezdemenyezese
    private void deleteMessage() {
    	setProgressBarIndeterminateVisibility(true);
    	
	    String url = "MessageDelete?id=" + id + "&imageID=" + imageID + "&ssid=" + UserInfo.getSSID();
	    download2 = new HttpGetJSONConnection(url, mHandler, TASK_DELETEMESSAGE);
	    download2.start();
    }
}
