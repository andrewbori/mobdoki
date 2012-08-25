package mobdoki.client.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserInfo {
	private static UserInfo userinfo;
	
	private Context context;
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	
	protected UserInfo(Context context0) {
		context = context0;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = sharedPreferences.edit();
	}
	public static void init(Context context0) {
		userinfo = new UserInfo(context0);
	}
	
	public static String getSSID() {
		return getString("ssid");
	}
	
	public static boolean isOffline() {
		return getBoolean("offline");
	}
	
	public static String getServerAdress() {
		return getString("server_address");
	}
	
	public static void putString(String key, String value){
		userinfo.editor.putString(key, value);
		userinfo.editor.commit();
	}
	
	public static String getString(String key) {
		return userinfo.sharedPreferences.getString(key, null);
	}
	
	public static void putInt(String key, int value){
		userinfo.editor.putInt(key, value);
		userinfo.editor.commit();
	}
	
	public static int getInt(String key) {
		return userinfo.sharedPreferences.getInt(key, 0);
	}
	
	public static void putBoolean(String key, boolean value){
		userinfo.editor.putBoolean(key, value);
		userinfo.editor.commit();
	}
	
	public static boolean getBoolean(String key) {
		return userinfo.sharedPreferences.getBoolean(key, false);
	}
	
	public static void remove(String key) {
		userinfo.editor.remove(key);
		userinfo.editor.commit();
	}
}
