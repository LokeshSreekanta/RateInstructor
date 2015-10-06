package edu.sdsu.lokesh.rateinstructor;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loki.app.rateinstructor.R;

import edu.sdsu.lokesh.rateinstructor.CacheManager.CacheEntity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class HomeActivity extends BaseActivity {

	private DrawerLayout drawer;

	private ProgressDialog pDialog;

	private GridViewAdapter adaptor;

	private GridView gridView;

	// URL to get contacts JSON
	private String url = "http://bismarck.sdsu.edu/rateme/list";

	private ArrayList<Instructor> mInstructorList = new ArrayList<Instructor>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarIcon(R.drawable.ic_ab_drawer);

		gridView = (GridView) findViewById(R.id.gridView);
		adaptor = new GridViewAdapter(mInstructorList);
		gridView.setAdapter(adaptor);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l) {
				DetailActivity.launch(HomeActivity.this,
						view.findViewById(R.id.text), mInstructorList.get(i).ID);
			}
		});

		drawer = (DrawerLayout) findViewById(R.id.drawer);
		drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		// Query for json
		new GetInstructorList()
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	protected int getLayoutResource() {
		return R.layout.activity_home;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			drawer.openDrawer(Gravity.START);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static class GridViewAdapter extends BaseAdapter {
		ArrayList<Instructor> list;

		public GridViewAdapter(ArrayList<Instructor> mInstructorList) {
			list = mInstructorList;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Instructor getItem(int i) {
			return list.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {

			if (view == null) {
				view = LayoutInflater.from(viewGroup.getContext()).inflate(
						R.layout.grid_item, viewGroup, false);
			}

			TextView text = (TextView) view.findViewById(R.id.text);
			text.setText(getItem(i).FirstName + " " + getItem(i).LastName);

			return view;
		}
	}

	/**
	 * Async task class to get json by making HTTP call
	 * */
	private class GetInstructorList extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(HomeActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@SuppressWarnings("unused")
		@Override
		protected Void doInBackground(Void... arg0) {
			String jsonStr = "";

			if (Utility.isOnline(getApplicationContext())) {
				// Creating service handler class instance
				ServiceHandler sh = new ServiceHandler();
				// Making a request to url and getting response
				jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
			} else {
				jsonStr = Utility.getPreference(getApplicationContext(),
						"list", CacheManager.InstructorList);
			}

			// Caching in a different thread.
			if (Utility.isOnline(getApplicationContext())) {
				new CacheManager(getApplicationContext(),
						CacheEntity.INSTRUCTORLIST, jsonStr, "list")
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			if (jsonStr != null) {

				try {
					org.json.simple.JSONArray json = (org.json.simple.JSONArray) new JSONParser()
							.parse(jsonStr);
					for (int i = 0; i < json.size(); i++) {
						String id = "";
						String firstname = "";
						String lastname = "";

						String res = json.get(i).toString();

						res = res.replace("{", "").replace("}", "")
								.replaceAll("\"", "");
						String[] entries = res.split(",");
						for (String entry : entries) {
							String[] keyValue = entry.split(":");
							if ("id".equals(keyValue[0])) {
								id = keyValue[1];
							} else if ("firstName".equals(keyValue[0])) {
								firstname = keyValue[1];
							} else {
								lastname = keyValue[1];
							}
						}
						mInstructorList.add(new Instructor(id, firstname,
								lastname));
						if (Utility.isOnline(getApplicationContext())) {
							new CacheManager(getApplicationContext(),
									CacheEntity.INSTRUCTORDETAILS, "", id)
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							new CacheManager(getApplicationContext(),
									CacheEntity.INSTRUCTORCOMMENTS, "", id)
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();

			adaptor.notifyDataSetChanged();
		}

	}
}
