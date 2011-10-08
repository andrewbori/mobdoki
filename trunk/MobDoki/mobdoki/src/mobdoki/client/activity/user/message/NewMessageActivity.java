package mobdoki.client.activity.user.message;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.R;
import mobdoki.client.activity.CameraActivity;
import mobdoki.client.activity.FileChooserActivity;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.HttpPostConnection;
import mobdoki.client.connection.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NewMessageActivity extends Activity implements OnClickListener {
	private final int TASK_UPLOADMESSAGE = 1;
	private final int TASK_UPLOADIMAGE = 2;
	private final int TASK_GETPATIENTS = 3;
	
	private HttpPostConnection uploadMessage = null;			// szal az uzenet feltoltesehez
	private HttpPostConnection uploadImage = null;				// szal a kep feltoltesehez
	private HttpGetJSONConnection downloadPatient = null;		// szal a paciensek listajanak letoltesejez
	
	private ArrayList<String> patientList;						// paciensek listaja
	private ArrayList<Integer> patientIDList;
	private AlertDialog patientDialog;
	private AutoCompleteTextView recipientText;
	
	private String filepath=null;
	private File file;
	private boolean hasImage = false;
	
	private String subject;					// uzenet targya
	private String text;					// uzenet szovege
	private String recipient;				// cimzett felhasznaloneve
	private int recipientID;
	private int answeredID;
	private int messageID;
	
	private Activity activity = this;
	private AlertDialog alert = null;
	
	private ProgressDialog progress;
	
	private EditText subjectText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.newmessage);
		setTitle("MobDoki: Új üzenet");

		recipientText = (AutoCompleteTextView) findViewById(R.newmessage.username);
		subjectText = (EditText) findViewById(R.newmessage.subject);
		
		recipientID=-1;
		if ((UserInfo.getString("usertype")).equals("patient")) {
			recipientText.setText("[Minden orvos]");
			recipientText.setFocusable(false);
			((ImageButton) findViewById(R.newmessage.buttonUsers)).setVisibility(ImageButton.GONE);
			recipientID=0;
		}
		
		Bundle extras = getIntent().getExtras();				// Valasz eseten, a megvalaszolando uzenet adatai
		if (extras != null) {
			recipient = extras.getString("recipient");
			if (recipient!=null) {
				recipientID = extras.getInt("recipientID");
				recipientText.setText(recipient);
				recipientText.setFocusable(false);
				((ImageButton) findViewById(R.newmessage.buttonUsers)).setVisibility(ImageButton.GONE);
			}
			subject = extras.getString("subject");
			if (subject!= null) {
				subject = "Re: " + subject;
				subjectText.setText(subject);
				subjectText.setFocusable(false);
			}
			answeredID = extras.getInt("id");
		}
		
		((ImageButton) findViewById(R.newmessage.buttonUsers)).setOnClickListener(this);
		((Button) findViewById(R.newmessage.sendbutton)).setOnClickListener(this);
		((Button) findViewById(R.newmessage.backbutton)).setOnClickListener(this);
		((Button) findViewById(R.newmessage.imagebutton)).setOnClickListener(this);
	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		
			// Feltoltes gomb esemenykezeloje
			case R.newmessage.sendbutton:
				if ((uploadMessage == null || uploadMessage.isNotUsed()) && (uploadImage == null || uploadImage.isNotUsed())) {
					uploadMessage();
				}
				break;
				
			// Kep csatolas gomb esemenykezeloje
			case R.newmessage.imagebutton:
				if (alert==null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Kép forrása")
					       .setCancelable(false)
					       .setPositiveButton("Kamera", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   Intent myIntent = new Intent(activity,CameraActivity.class);			// camera betoltese a kep kivalasztasahoz
								   startActivityForResult(myIntent,0);
					           }
					       })
					       .setNeutralButton("SD kártya", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   Intent myIntent = new Intent(activity,FileChooserActivity.class);	// filechooser betoltese a kep kivalasztasahoz
								   startActivityForResult(myIntent,0);
					           }
					       })
					       .setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   dialog.cancel();
					           }
					       });
					alert = builder.create();
				}
				alert.show();
				break;
				
			// Vissza gomb esemenykezeloje
			case R.newmessage.backbutton:
				finish();
				break;
			// Paciens kivalasztasa
			case R.newmessage.buttonUsers:
				if (patientDialog!=null) patientDialog.show();
				break;
		}	
	}
	
    // FileChooser eredmenye: a kivalasztott fajl feldolgozasa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    	if (resultCode==RESULT_OK) {					// Ha van kivalasztott fajl:
    		Bundle extras = intent.getExtras();
            if (extras != null) {
            	filepath = extras.getString("filepath");	                	
    			file = new File(filepath);
                ((ImageView)findViewById(R.newmessage.imageView)).setImageBitmap(BitmapFactory.decodeFile(filepath));
                ((LinearLayout)findViewById(R.newmessage.imagelayout)).setVisibility(LinearLayout.VISIBLE);
                hasImage=true;
    		}
    	}
    }

    @Override
    public void onStart() {
    	super.onStart();
    	if (recipientID==-1) getPatientList();
    }

	// Az osztaly handler-je
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			switch (msg.arg1) {
			case 0:
				Log.v("NewMessage", "Sikertelen küldés.");
				Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
				if (msg.what==TASK_GETPATIENTS) downloadPatient.setNotUsed();
				else if (msg.what==TASK_UPLOADMESSAGE)  { uploadMessage.setNotUsed(); progress.cancel(); }
				else if (msg.what==TASK_UPLOADIMAGE) { uploadImage.setNotUsed(); progress.cancel(); }
				break;
			case 1:
				switch (msg.what) {
				case TASK_GETPATIENTS:
					if (msg.arg1==1 && downloadPatient.isOK()) {					// Ha sikeres lekerdezes, akkor betolt...
						Log.v("DoctorGraphActivity","Paciensek betoltve");
						
						patientList = downloadPatient.getStringArrayList("usernames");
						patientIDList = downloadPatient.getIntegerArrayList("ids");
				        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, patientList);
				        recipientText.setAdapter(adapter);																		// Autocomplete lista
				        
				        patientList.add(0, "");		// az elso listaelem ures (mindig ez van kivalasztva)*/
				        patientIDList.add(0, -1);
				        
				        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				        builder.setItems(downloadPatient.getStringArray("usernames", true), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int item) {
				            	recipientText.setText(patientList.get(item));
				            }
				        });
				        patientDialog = builder.create();
					}
					downloadPatient.setNotUsed();
					break;
				case TASK_UPLOADMESSAGE:
					if (uploadMessage.isOK()) {
						if (hasImage && filepath!=null) {
							messageID = uploadMessage.getInt("id");
							FileEntity fe = new FileEntity(file, URLConnection.guessContentTypeFromName(file.getName()));
							uploadImage(fe);
						} else finish();
					}
					else if (uploadMessage.hasMessage()) {
						Toast.makeText(activity, uploadMessage.getMessage(), Toast.LENGTH_SHORT).show();
						progress.cancel();
					}
					uploadMessage.setNotUsed();
					break;
				case TASK_UPLOADIMAGE:
					if (!uploadImage.isOK() && uploadImage.hasMessage()) {
						Toast.makeText(activity, uploadImage.getMessage(), Toast.LENGTH_SHORT).show();
					}
					progress.cancel();
					if(uploadImage.isOK()) finish();
					uploadImage.setNotUsed();
					break;
				}
				break;
			}
		}
	};

	// Uzenet feltoltes kezdemenyezese
	public void uploadMessage() {
		if (patientList!=null) {
			recipient = recipientText.getText().toString();
			int i = patientList.indexOf(recipient);
			if (i!=-1 && i!=0) {
				recipientID = patientIDList.get(i);
			}
			else {
				Toast.makeText(activity, "A címzett nem található!", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
		subject = subjectText.getText().toString();											// Uzenet targya
		text = ((EditText) findViewById(R.newmessage.text)).getText().toString();			// Uzenet szovege
		
		if (subject==null || subject.equals("")) {
			Toast.makeText(activity, "Adja meg az üzenet tárgyát!", Toast.LENGTH_SHORT).show();
			return;
		}
		if (text==null || text.equals("")) {
			Toast.makeText(activity, "Adja meg az üzenet szövegét!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		StringEntity se = null;
		try {
			se = new StringEntity(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		
		setProgressBarIndeterminateVisibility(true);
		progress = new ProgressDialog(this);
        progress.setMessage("Üzenet küldése...");
        progress.setIndeterminate(true);
        progress.show();
		
		String url = "MessageUpload?to=" + recipientID +
								   "&subject=" + URLEncoder.encode(subject) +
								   "&ssid=" + UserInfo.getSSID();
		
		if (answeredID!=0) {
			url = url + "&id=" + answeredID;
		}
		
		uploadMessage = new HttpPostConnection(url, mHandler, se, TASK_UPLOADMESSAGE);
		uploadMessage.start();
	}
	
	// Kep feltoltesenek kezdemenyezese
    private void uploadImage(HttpEntity he){	    	
    	setProgressBarIndeterminateVisibility(true);
    	progress.setMessage("Kép feltöltése...");

    	String url = "ImageUpload?table=Message&id=" + messageID + "&ssid=" + UserInfo.getSSID();
	    uploadImage = new HttpPostConnection(url, mHandler, he, TASK_UPLOADIMAGE);
	    uploadImage.start();
    }
    
	// Paciensek felhasznalonevenek lekerdezese
	private void getPatientList(){
		setProgressBarIndeterminateVisibility(true);
		
    	String url = "GetUsers?usertype=patient" + "&ssid=" + UserInfo.getSSID();
	    downloadPatient = new HttpGetJSONConnection(url, mHandler, TASK_GETPATIENTS);
	    downloadPatient.start();
    }
}
