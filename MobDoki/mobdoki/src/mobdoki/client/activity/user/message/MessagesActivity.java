package mobdoki.client.activity.user.message;

import java.util.ArrayList;

import mobdoki.client.MessageListArrayAdapter;
import mobdoki.client.R;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MessagesActivity extends Activity implements OnClickListener, OnItemClickListener {
	private HttpGetJSONConnection download = null;		// szal az uzenetek letoltesehez
	
	private ArrayList<Integer> idList = null;			// uzenet id lista
	private ArrayList<String> subjectList = null;		// targy lista
	private ArrayList<String> senderList = null;		// kuldo lista
	private ArrayList<String> dateList;					// datum lista
	private ArrayList<Boolean> answeredList = null;		// megvalaszolt? lista
	private ArrayList<Boolean> viewedList = null;		// megtekinett? lista
	private ArrayList<Integer> imageList = null;		// kep id lista
	
	private boolean inbox;
	
	private Activity activity=this;
	private ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.setContentView(R.layout.message);
		
		Bundle extras = getIntent().getExtras();
		inbox = extras.getBoolean("inbox");
		
		listview = (ListView) findViewById(R.message.list);
		listview.setClickable(true);
		listview.setOnItemClickListener(this);
		
		Button backButton = (Button) findViewById(R.message.back);
		Button sentButton = (Button) findViewById(R.message.sent);
		Button inboxButton = (Button) findViewById(R.message.inbox);
		Button newmessageButton = (Button) findViewById(R.message.newmessage);
		
		if (inbox) {
			setTitle("MobDoki: Beérkezõ üzenetek");
			
			backButton.setOnClickListener(this);
			sentButton.setOnClickListener(this);
		} else {
			setTitle("MobDoki: Elküldött üzenetek");
			
			backButton.setVisibility(Button.GONE);
			sentButton.setVisibility(Button.GONE);
			inboxButton.setVisibility(Button.VISIBLE);
			newmessageButton.setVisibility(Button.VISIBLE);
			
			inboxButton.setOnClickListener(this);
			newmessageButton.setOnClickListener(this);
		}
	}
	
	// Kattintas esemenykezelo
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.message.back:				// Vissza gomb esemenykezeloje
			finish();
			break;
		case R.message.sent:
			Intent myIntent = new Intent();
			myIntent.setClass(activity, MessagesActivity.class);
    		myIntent.putExtra("inbox", false);
    		startActivity(myIntent);
			break;
		case R.message.inbox:
			finish();
			break;
		case R.message.newmessage:
			myIntent = new Intent();
			myIntent.setClass(activity, NewMessageActivity.class);
    		startActivity(myIntent);
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Log.v("MessagesActivity", "Uzenet megtekintese: " + position);
		Intent myIntent = new Intent(activity, ShowMessageActivity.class);
		myIntent.putExtra("id",idList.get(position));
		myIntent.putExtra("sender",senderList.get(position));
		myIntent.putExtra("subject",subjectList.get(position));
		myIntent.putExtra("date",dateList.get(position));
		myIntent.putExtra("inbox", inbox);
		myIntent.putExtra("image",imageList.get(position));
		startActivity(myIntent);
	}
	
	// Indulaskor az uzenetek lekerdezese
    @Override
    public void onStart() {
    	super.onStart();
    	refreshRequest();
    }
    
    // Megszakitaskor a futo szalak leallitasa
    @Override
    public void onPause() {
    	super.onPause();
    	if (download!=null && download.isUsed()) {
    		download.isNotUsed();
    		setProgressBarIndeterminateVisibility(false);
    	}
    }
    
    // Lekerdezett uzeneteket kezelo Handler
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			switch(msg.arg1){
				case 0:
					Log.v("MessagesActivity","Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem érhetõ el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					Log.v("MessagesActivity","Sikeres keresés");
					
					idList = download.getIntegerArrayList("id");
					subjectList = download.getStringArrayList("subject");
					senderList = download.getStringArrayList("sender");
					dateList = download.getStringArrayList("date");
					viewedList = download.getBooleanArrayList("viewed");
					answeredList = download.getBooleanArrayList("answered");
					imageList = download.getIntegerArrayList("image");

					ArrayAdapter<String> adapter = new MessageListArrayAdapter<String>(activity,  
												   R.layout.listview_message,  
									               R.listview_message.subject, R.listview_message.sender, R.listview_message.date,
									               R.listview_message.mailicon, R.listview_message.imageicon,
									               subjectList, senderList, dateList, viewedList, answeredList, imageList);
					listview.setAdapter(adapter);

					break;
			}
			download.setNotUsed();
		}
	};
    
	// Uzenetek lekeresenek kezdemenyezese
    private void refreshRequest(){    	
    	setProgressBarIndeterminateVisibility(true);
    	String str;
    	if (inbox) str = "true"; else str = "false";
	    String url = "GetMessages?inbox=" + str + "&ssid=" + UserInfo.getSSID();
	    download = new HttpGetJSONConnection(url, mHandler);
	    download.start();
    }
}
