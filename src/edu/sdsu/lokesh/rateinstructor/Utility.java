package edu.sdsu.lokesh.rateinstructor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {
	public static String getPreference(Context c, String id, String PREFS_NAME) {
		SharedPreferences settings;
		settings = c.getSharedPreferences(PREFS_NAME, 0);
		String value = settings.getString(id, "");
		return value;
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfoMob = cm.getNetworkInfo(cm.TYPE_MOBILE);
		NetworkInfo netInfoWifi = cm.getNetworkInfo(cm.TYPE_WIFI);
		if ((netInfoMob != null && netInfoMob.isConnectedOrConnecting())
				|| netInfoWifi != null && netInfoWifi.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
}
