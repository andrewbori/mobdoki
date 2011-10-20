package mobdoki.client.connection;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "MobDokiDB";
	private static final int DB_VERSION = 1;
	private SQLiteDatabase db;
	
	public LocalDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Sickness (ID INTEGER PRIMARY KEY, " + 
										   "Name TEXT, Seriousness REAL, Details TEXT);" );
		db.execSQL("CREATE TABLE Symptom (ID INTEGER PRIMARY KEY, " + 
										  "Name TEXT);" );
		db.execSQL("CREATE TABLE Hospital (ID INTEGER PRIMARY KEY, " + 
										   "Name TEXT, Address TEXT, Lat REAL, Lon REAL, " +
										   "Phone TEXT, Email TEXT);" );
		db.execSQL("CREATE TABLE Diagnosis (SicknessID INTEGER, SymptomID INTEGER);" );
		db.execSQL("CREATE TABLE Curing (HospitalID INTEGER, SicknessID INTEGER);" );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS Sickness;");
		db.execSQL("DROP TABLE IF EXISTS Symptom;");
		db.execSQL("DROP TABLE IF EXISTS Hospital;");
		db.execSQL("DROP TABLE IF EXISTS Diagnosis;");
		db.execSQL("DROP TABLE IF EXISTS Curing;");
		onCreate(db);
	}
	
	public SQLiteDatabase open() {
        this.db = this.getWritableDatabase();
        return this.db;
    }
	
	public SQLiteDatabase getOpenedDB() {
        return this.db;
    }
    
	void fillSickness(JSONArray sickness) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv;
		for (int i=0; i<sickness.length(); i++) {
			JSONObject s = sickness.getJSONObject(i);
			cv = new ContentValues();
			
			cv.put("ID", s.getInt("id"));
			cv.put("Name", s.getString("name"));
			cv.put("Seriousness", s.getDouble("seriousness"));
			cv.put("Details", s.getString("details"));
			
			db.insert("Sickness", null, cv);
		}
		db.close();	
	}
	
	void fillSymptom(JSONArray symtpom) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		for (int i=0; i<symtpom.length(); i++) {
			JSONObject s = symtpom.getJSONObject(i);
			cv = new ContentValues();
			
			cv.put("ID", s.getInt("id"));
			cv.put("Name", s.getString("name"));
			
			db.insert("Symptom", null, cv);
		}
		db.close();	
	}
	
	void fillHospital(JSONArray hospital) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		for (int i=0; i<hospital.length(); i++) {
			JSONObject h = hospital.getJSONObject(i);
			cv = new ContentValues();
			
			cv.put("ID", h.getInt("id"));
			cv.put("Name", h.getString("name"));
			cv.put("Lat", h.getDouble("lat"));
			cv.put("Lon", h.getDouble("lon"));
			cv.put("Address", h.getString("address"));
			cv.put("Phone", h.getString("phone"));
			cv.put("Email", h.getString("email"));
			
			db.insert("Hospital", null, cv);
		}	
		db.close();	
	}
	
	void fillDiagnosis(JSONArray diagnosis) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		for (int i=0; i<diagnosis.length(); i++) {
			JSONObject d = diagnosis.getJSONObject(i);
			cv = new ContentValues();
			
			cv.put("SicknessID", d.getInt("sicknessID"));
			cv.put("SymptomID", d.getInt("symptomID"));
			
			db.insert("Diagnosis", null, cv);
		}
		db.close();	
	}
    
    void fillCuring(JSONArray curing) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		for (int i=0; i<curing.length(); i++) {
			JSONObject c = curing.getJSONObject(i);
			cv = new ContentValues();
			
			cv.put("HospitalID", c.getInt("hopsitalID"));
			cv.put("SicknessID", c.getInt("sicknessID"));
			
			db.insert("Curing", null, cv);
		}
		db.close();	
	}
	
    // Az osszes bejegyzes nevenek lekerdezese a megadott tablabol a GetAll servlet mintajara 
	public ArrayList<String> getAll(String Table) {
		SQLiteDatabase db = this.getReadableDatabase();
		try {
	  		ArrayList<String> list = new ArrayList<String>();
	  		Cursor cursor = db.query(Table, new String[] { "Name" }, null, null, null, null, "Name asc");
			if (cursor.moveToFirst()) {
				do {
					list.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
			   cursor.close();
			}
			
			return list;
  		} catch (Exception e) {
  			return new ArrayList<String>();
  		} finally {
  			db.close();
  		}
	}
	
    // A megadott betegseg tuneteinek listaja
	public ArrayList<String> getAllSymptom(String Name) {
		SQLiteDatabase db = this.getReadableDatabase();
		try {
	  		ArrayList<String> list = new ArrayList<String>();
	  		Cursor cursor = db.rawQuery("SELECT sy.Name " +
	  									"FROM Sickness si, Symptom sy, Diagnosis d " +
	  									"WHERE si.Name=? AND " +
	  										  "si.ID=d.SicknessID AND " +
	  										  "d.SymptomID=sy.ID " +
	  										  "ORDER BY sy.Name ASC;", new String[]{Name});
			if (cursor.moveToFirst()) {
				do {
					list.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
			   cursor.close();
			}
			
			return list;
  		} catch (Exception e) {
  			return new ArrayList<String>();
  		} finally {
  			db.close();
  		}
	}
	
    // A megadott betegseget kezelo korhazak listaja
	public ArrayList<String> getAllHospital(String Name) {
		SQLiteDatabase db = this.getReadableDatabase();
		try {
	  		ArrayList<String> list = new ArrayList<String>();
	  		Cursor cursor = db.rawQuery("SELECT h.Name " +
	  									"FROM Sickness s, Hospital h, Curing c " +
	  									"WHERE s.Name=? AND " +
	  										  "s.ID=c.SicknessID AND " +
	  										  "c.HospitalID=h.ID " +
	  										  "ORDER BY h.Name ASC;", new String[]{Name});
			if (cursor.moveToFirst()) {
				do {
					list.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
			   cursor.close();
			}
			
			return list;
  		} catch (Exception e) {
  			return new ArrayList<String>();
  		} finally {
  			db.close();
  		}
	}
	
    // A megadott korhazban kezelt betegsegek listaja
	public ArrayList<String> getAllSickness(String Name) {
		SQLiteDatabase db = this.getReadableDatabase();
		try {
	  		ArrayList<String> list = new ArrayList<String>();
	  		Cursor cursor = db.rawQuery("SELECT s.Name " +
	  									"FROM Hospital h, Sickness s, Curing c " +
	  									"WHERE h.Name=? AND " +
	  										  "h.ID=c.HospitalID AND " +
	  										  "c.SicknessID=s.ID " +
	  										  "ORDER BY s.Name ASC;", new String[]{Name});
			if (cursor.moveToFirst()) {
				do {
					list.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
			   cursor.close();
			}
			
			return list;
  		} catch (Exception e) {
  			return new ArrayList<String>();
  		} finally {
  			db.close();
  		}
	}
	
	public Cursor SicknessInfo (String name) {
  		try {
			db = this.getReadableDatabase();
	
	  		Cursor cursor = db.query("Sickness", new String[] { "Seriousness", "Details" }, "Name=?", new String[] {name}, null, null, null);
	  		//db.close();
	  		
			return cursor;
  		} catch (Exception e) {
  			return null;
  		}
	}
	
	public Cursor HospitalInfo (String name) {
  		try {
			db = this.getReadableDatabase();
	
	  		Cursor cursor = db.query("Hospital", new String[] { "Lat", "Lon", "Address", "Phone", "Email" }, "Name=?", new String[] {name}, null, null, null);
	  		//db.close();
	  		
			return cursor;
  		} catch (Exception e) {
  			return null;
  		}
	}
	
	public TreeMap<String, Integer> SearchSickness(String symptoms) {
		SQLiteDatabase db = this.getReadableDatabase();
		try {
			ArrayList<String> symptomlist = new ArrayList<String>();        // tunetek listaja
	        StringTokenizer st = new StringTokenizer(symptoms, ", ");
	        while(st.hasMoreElements()) symptomlist.add(st.nextToken());    // tunetek parsolasa
	        
	        String sqlText = "SELECT si.Name " +
			                 "FROM Diagnosis d " +
			                       "INNER JOIN Symptom sy ON (d.SymptomID=sy.ID) " +
			                       "INNER JOIN Sickness si ON (d.SicknessID=si.ID) " +
			                 "WHERE sy.Name LIKE ?;";
			
			TreeMap<String, Integer> sicknessMap = new TreeMap<String, Integer>();	// betegsegek + talalati darabszamuk
			
			for (String symptom : symptomlist) {		            					// kereses a megadott tunetekre egyenkent
				Cursor cursor = db.rawQuery(sqlText, new String[]{"%" + symptom + "%"});
				
				if (cursor.moveToFirst()) {
					do {
						String sickness = cursor.getString(0);
						if (sicknessMap.containsKey(sickness)) {                        // Ha mar a listaban van a betegseg: darabszam novelese
						    sicknessMap.put(sickness, sicknessMap.get(sickness)+1);
						} else {                                                        // Ha nincs, akkor felvetel, 1-es darabszammal (es a listahoz hozzaadas)
							sicknessMap.put(sickness, 1);
						}
					} while (cursor.moveToNext());
				}
				if (cursor != null && !cursor.isClosed()) {
				   cursor.close();
				}
			}
			
			return sicknessMap;
  		} catch (Exception e) {
  			return new TreeMap<String, Integer>();
  		} finally {
  			db.close();
  		}
	}

}
