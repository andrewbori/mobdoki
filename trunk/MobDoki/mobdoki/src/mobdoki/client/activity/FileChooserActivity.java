package mobdoki.client.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import mobdoki.client.FileArrayAdapter;
import mobdoki.client.Option;
import mobdoki.client.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class FileChooserActivity extends ListActivity {
	private File currentDir;						// Pillanatnyilag megnyitott mappa
	private FileArrayAdapter adapter;
	Stack<File> dirStack = new Stack<File>();

	// Indulaskor a SD kartya gyokerkonyvtaranak betoltese
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		fill(currentDir);
	}
	
	// Megszakitas eseten sikertelenul kilep
	@Override
    public void onPause() {
    	super.onPause();
		Intent intent = new Intent();
		setResult(RESULT_CANCELED,intent);
    	finish();
    }

	// Megadott mappa tartalmanak megjelenitese rendezve
	private void fill(File f) {

		File[] dirs = f.listFiles();					// a megnyitott mappaban talalhato fajlok/mappak
		this.setTitle("Current Dir: " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {						// fajlok es mappak kulon listakba valogatasa
				if (ff.isDirectory())
					dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
				else {
					fls.add(new Option(ff.getName(), "File Size: " + ff.length(), ff.getAbsolutePath()));
				}
			}
		} catch (Exception e) {
			Log.v("FileChooser","Hiba a fajlok es mappak valogatasa kozben.");
		}
		Collections.sort(dir);							// Fajlok es mappak listainak rendezese
		Collections.sort(fls);
		dir.addAll(fls);								// a mappak listajahoz hozzafuzzuk a fajlok listajat
		
		if (!f.getName().equalsIgnoreCase("sdcard")) {							//Ha nem a gyokerkonyvtarban vagyunk, akkor:
			dir.add(0, new Option("..", "Parent Directory", f.getParent()));		// szulokonyvtar a lista elejere
		}
		
		adapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.file_view, dir);
		this.setListAdapter(adapter);
	}

	// Listaelemre (mappa/fajl) kattintas esemenykezeloje
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")) {							// Ha a kivalaszott elem egy mappa
			dirStack.push(currentDir);
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else if (o.getData().equalsIgnoreCase("parent directory")) {			// Ha a kivalasztott elem a szulo mappa
			currentDir = dirStack.pop();
			fill(currentDir);
		} else {																// Ha a kivalasztott elem egy fajl
			onFileClick(o);
		}
	}

	// Visszagomb megnyomasakor...
	@Override
	public void onBackPressed() {
		if (dirStack.size() == 0) {					// Ha a gyokerkonyvtarban vagyunk
			Intent intent = new Intent();				// akkor kilepes
			setResult(RESULT_CANCELED,intent);
			finish();
			return;
		}											// egyebkent a szulomappaba ugras
		currentDir = dirStack.pop();
		fill(currentDir);
	}

	// Fajlra kattintas: visszateres az eleresi utvonnallal
	private void onFileClick(Option o) {
		Intent intent = new Intent();
		intent.putExtra("filepath",o.getPath());
		setResult(RESULT_OK,intent);
		finish();
	}
}
