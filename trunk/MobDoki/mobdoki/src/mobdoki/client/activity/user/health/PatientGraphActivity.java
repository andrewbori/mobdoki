package mobdoki.client.activity.user.health;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import mobdoki.client.PatientHealthChart;
import mobdoki.client.R;
import mobdoki.client.connection.HttpGetConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PatientGraphActivity extends Activity {
	private Activity activity = this;
	HttpGetConnection download = null;				// szal a serverhez csatlakozashoz
	private String[] list = null;					// grafikonok listaja
	private ListView listview;
	private String username = null;					// paciens felhasznaloneve
	private PatientHealthChart diagram = null;		// Megjelenitendo diagram
	private ArrayList<Date> dateList;					// idobelyeg lista
	private ArrayList<Double> weightList;				// testomeg (kg) lista
	private ArrayList<Integer> bp1List;					// vernyomas: systoles ertek lista (felso)
	private ArrayList<Integer> bp2List;					// vernyomas: diastoles ertek lista (also)
	private ArrayList<Integer> pulseList;				// pultus lista
	private ArrayList<Double> temperatureList;			// testhomerseklet (�C) lista
	private ArrayList<Integer> moodList;				// kozerzet (1-10) lista

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.patientgraph);

		Bundle extras = getIntent().getExtras();
		username = extras.getString("username");

		list = new String[] { "V�rnyom�s grafikon", "Pulzus grafikon", "Testt�meg grafikon", 
				"Testh�m�rs�klet grafikon", "K�z�rzet grafikon", "�sszes egyben" };

		// Vissza gomb esemenykezeloje
		Button backButton = (Button) findViewById(R.patientgraph.backbutton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		// A grafikonok listajanak esemenykezeloje
		listview = (ListView) findViewById(R.patientgraph.list1);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				
				Log.v("PatientGraphActivity", list[position]);
				// Ha mindegyik listanak van legalabb egy eleme
				if (bp1List!=null && bp2List!=null && pulseList!=null && weightList!=null &&
					temperatureList!=null && moodList!=null && dateList!=null &&
					bp1List.size()>0 && bp2List.size()>0 && pulseList.size()>0 && weightList.size()>0 &&
					temperatureList.size()>0 && moodList.size()>0 && dateList.size()>0) {
						
					diagram = new PatientHealthChart();
					Intent intent;
					switch (position) {
						case 0:
							intent = diagram.executeBP(activity, bp1List, bp2List, dateList);
							startActivity(intent);
							break;
						case 1:
							intent = diagram.executePulse(activity, pulseList, dateList);
							startActivity(intent);
							break;
						case 2:
							intent = diagram.executeWeight(activity, weightList, dateList);							
							startActivity(intent);
							break;
						case 3:
							intent = diagram.executeTemperature(activity, temperatureList, dateList);
							startActivity(intent);
							break;
						case 4:
							intent = diagram.executeMood(activity, moodList, dateList);			
							startActivity(intent);
							break;
						case 5:
							intent = diagram.executeAll(activity, bp1List, bp2List, pulseList, weightList, temperatureList, moodList, dateList);
							startActivity(intent);
							break;
					}
				} else {
					Toast.makeText(activity, "Nincs megjelen�thet� adat.", Toast.LENGTH_LONG).show();
				}
			}
		});

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
																R.layout.listview_item, list);
		listview.setAdapter(adapter);
	}

	// Indulaskor a listak lekerdezese
	@Override
	public void onStart() {
		super.onStart();
		refreshRequest();
	}

	// Megszakitaskor a futo szalak leallitasa
	@Override
	public void onPause() {
		super.onPause();
		if (download != null && download.isAlive()) {
			((ProgressBar)findViewById(R.patientgraph.progress)).setVisibility(ProgressBar.INVISIBLE);
			download.stop();
			download = null;
		}
	}

	// Lekerdezett listat kezelo Handler
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((ProgressBar)findViewById(R.patientgraph.progress)).setVisibility(ProgressBar.INVISIBLE);
			switch (msg.arg1) {
				case 0:
					Log.v("PatientGraphActivity", "Sikertelen lekeres.");
					Toast.makeText(activity, "A szerver nem �rhet� el.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					if (download.isERROR() || download.isFERROR()) {
						Toast.makeText(activity, download.getMessage(), Toast.LENGTH_LONG).show();
						return;
					}
					
					Log.v("PatientGraphActivity", "Sikeres lekeres");
	
					weightList = new ArrayList<Double>();
					bp1List = new ArrayList<Integer>();
					bp2List = new ArrayList<Integer>();
					pulseList = new ArrayList<Integer>();
					moodList = new ArrayList<Integer>();
					dateList = new ArrayList<Date>();
					temperatureList = new ArrayList<Double>();
					
					JSONArray elements = download.getJSONArray("elements");				// allapotok listaja
					
					for (int i = 0; i < elements.length(); i++) {						// Egeszsegugyi allapotok feldolgozasa
						try {
							JSONObject element = elements.getJSONObject(i);
		
							bp1List.add(element.getInt("bp1"));							// vernyomas
							bp2List.add(element.getInt("bp2"));
							
							pulseList.add(element.getInt("pulse"));						// pulzus
							
							weightList.add(element.getDouble("weight"));				// testtomeg
							
							temperatureList.add(element.getDouble("temperature"));		// testhomerseklet
							
							moodList.add(element.getInt("mood"));						// kozerzet
							
							String t = element.getString("date");
							SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
							Date dateObj = null;
							dateObj = (Date)curFormater.parse(t);
							dateList.add(dateObj);
						} catch (Exception e) {
							Log.v("PatientGraphActivity", "Listaelem beolvasasi hiba");
						}
					}
					break;
			}
		}
	};

	// Listak lekeresenek kezdemenyezese
	private void refreshRequest() {
		((ProgressBar)findViewById(R.patientgraph.progress)).setVisibility(ProgressBar.VISIBLE);
		
		String url = "PatientGraph?username=" + URLEncoder.encode(username);
		download = new HttpGetConnection(url, mHandler);
		download.start();
	}

}
