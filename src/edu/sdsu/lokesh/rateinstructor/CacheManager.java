package edu.sdsu.lokesh.rateinstructor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class CacheManager extends AsyncTask<Object, Object, Object> {
	public static final String InstructorList = "InstructorList";
	public static final String InstructorDetails = "InstructorDetails";
	public static final String InstructorComments = "InstructorComments";
	private String urlDetails = "http://bismarck.sdsu.edu/rateme/instructor/";
	private String urlComments = "http://bismarck.sdsu.edu/rateme/comments/";
	private CacheEntity mCacheEntity;
	private Context mContext;
	private String mJson;
	private String mID;

	public enum CacheEntity {
		INSTRUCTORLIST, INSTRUCTORDETAILS, INSTRUCTORCOMMENTS
	}

	public CacheManager(Context context, CacheEntity cacheentity, String json,
			String id) {
		mContext = context;
		mCacheEntity = cacheentity;
		mJson = json;
		mID = id;
	}

	@Override
	protected Object doInBackground(Object... arg0) {
		if (CacheEntity.INSTRUCTORLIST == mCacheEntity) {
			setPreference(mContext, mJson, mID, InstructorList);
			return null;
		} else if (CacheEntity.INSTRUCTORDETAILS == mCacheEntity) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();
			urlDetails = urlDetails + mID;
			// Making a request to url and getting response
			String jsonStr = sh.makeServiceCall(urlDetails, ServiceHandler.GET);

			setPreference(mContext, jsonStr, mID, InstructorDetails);
			return null;
		} else if (CacheEntity.INSTRUCTORCOMMENTS == mCacheEntity) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();
			urlComments = urlComments + mID;
			// Making a request to url and getting response
			String jsonStr = sh
					.makeServiceCall(urlComments, ServiceHandler.GET);

			setPreference(mContext, jsonStr, mID, InstructorComments);
			return null;
		}
		return null;
	}

	private boolean setPreference(Context c, String value, String id,
			String PREFS_NAME) {
		SharedPreferences settings;
		settings = c.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(id, value);
		return editor.commit();
	}

}
