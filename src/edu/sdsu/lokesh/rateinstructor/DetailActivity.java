package edu.sdsu.lokesh.rateinstructor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loki.app.rateinstructor.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends BaseActivity {

	public static final String EXTRA_IMAGE = "DetailActivity:image";
	private String mID = "";
	private String url = "http://bismarck.sdsu.edu/rateme/instructor/";
	private String urlComments = "http://bismarck.sdsu.edu/rateme/comments/";
	private String postRateUrl = "http://bismarck.sdsu.edu/rateme/rating/";
	private String postCommentUrl = "http://bismarck.sdsu.edu/rateme/comment/";
	private ProgressDialog pDialog;
	private TextView mTextView;
	private TextView mName;
	private String res = "";
	private InstructorDetails mDetails;
	private ArrayList<InstructorComment> mInstructorComment = new ArrayList<InstructorComment>();
	private GridViewAdapter adaptor;
	private GridView gridView;
	private Handler mHandler;
	private String rating = "";
	private String commentByUser = "";

	public enum PostTaskType {
		RATE, COMMENT
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTextView = (TextView) findViewById(R.id.text);
		mName = (TextView) findViewById(R.id.detailName);
		mHandler = new Handler();
		Intent intent = getIntent();
		if (intent != null)
			mID = intent.getStringExtra("INSTRUCTORID");

		url = url + mID;

		urlComments = urlComments + mID;

		postCommentUrl = postCommentUrl + mID;

		postRateUrl = postRateUrl + mID + "/";

		gridView = (GridView) findViewById(R.id.gridViewComment);
		adaptor = new GridViewAdapter(mInstructorComment);
		gridView.setAdapter(adaptor);

		// Query for json
		new GetInstructor().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// Load All the comments
		new GetInstructorComment()
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	@Override
	protected int getLayoutResource() {
		return R.layout.activity_detail;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detailmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_comment) {
			commentDialog();
		} else if (id == R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean commentDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				DetailActivity.this);

		alertDialogBuilder.setTitle("Comment & Rate the Instructor");

		LayoutInflater li = LayoutInflater.from(DetailActivity.this);
		final View dialogView = li.inflate(R.layout.option, null);

		final EditText input = (EditText) dialogView
				.findViewById(R.id.txtConnectedBy);

		Spinner spinner = (Spinner) dialogView.findViewById(R.id.viewSpin);
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int val,
					long arg3) {
				rating = String.valueOf(val + 1);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		alertDialogBuilder.setView(dialogView);

		alertDialogBuilder.setPositiveButton("Comment",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						commentByUser = input.getText().toString();
						postRateUrl = postRateUrl + rating;

						if (Utility.isOnline(getApplicationContext())) {
							new PostTask(PostTaskType.RATE)
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							new PostTask(PostTaskType.COMMENT)
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						} else {
							Toast.makeText(
									getApplicationContext(),
									"Cannot comment or rate!! Connect to internet",
									Toast.LENGTH_SHORT).show();
						}
					}

				});

		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						// cancel the alert box and put a Toast to the user

						dialog.cancel();

					}

				});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
		return true;

	}

	public static void launch(BaseActivity activity, View transitionView,
			String id) {
		ActivityOptionsCompat options = ActivityOptionsCompat
				.makeSceneTransitionAnimation(activity, transitionView,
						EXTRA_IMAGE);
		Intent intent = new Intent(activity, DetailActivity.class);
		intent.putExtra("INSTRUCTORID", id);
		ActivityCompat.startActivity(activity, intent, options.toBundle());
	}

	/**
	 * Async task class to get json by making HTTP call
	 * */
	private class GetInstructor extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			if (pDialog == null)
				pDialog = new ProgressDialog(DetailActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			String jsonStr = "";

			if (Utility.isOnline(getApplicationContext())) {
				// Creating service handler class instance
				ServiceHandler sh = new ServiceHandler();
				// Making a request to url and getting response
				jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
			} else {
				jsonStr = Utility.getPreference(getApplicationContext(), mID,
						CacheManager.InstructorDetails);
			}
			if (jsonStr != null) {

				try {
					org.json.simple.JSONObject json = (org.json.simple.JSONObject) new JSONParser()
							.parse(jsonStr);
					String id = String.valueOf((Long) json.get("id"));
					String firstname = (String) json.get("firstName");
					String lastname = (String) json.get("lastName");
					String office = (String) json.get("office");
					String phone = (String) json.get("phone");
					String email = (String) json.get("email");
					org.json.simple.JSONObject jsonAverage = (org.json.simple.JSONObject) json
							.get("rating");
					String average = String.valueOf(jsonAverage.get("average"));
					String totalratings = String.valueOf(jsonAverage
							.get("totalRatings"));

					mDetails = new InstructorDetails(new Instructor(id,
							firstname, lastname), office, phone, email,
							average, totalratings);

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
			try {
				if (pDialog.isShowing()) {
					pDialog.dismiss();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pDialog.dismiss();
			}
			mName.setText(mDetails.InstructorObj.FirstName + " "
					+ mDetails.InstructorObj.LastName);
			mTextView.setText("Office Location : " + mDetails.Office + "\n"
					+ "Phone : " + mDetails.Phone + "\n" + "Email : "
					+ mDetails.Email + "\n" + "Average Rating : "
					+ mDetails.Average + "\n" + "Total Ratings : "
					+ mDetails.TotalRatings);
		}

	}

	/**
	 * Async task class to get json by making HTTP call
	 * */
	private class PostTask extends AsyncTask<Void, Void, Void> {
		PostTaskType mType;

		public PostTask(PostTaskType type) {
			mType = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			String response;
			if (Utility.isOnline(getApplicationContext())) {

				// Creating service handler class instance
				ServiceHandler sh = new ServiceHandler();
				if (mType == PostTaskType.RATE) {

					StringEntity rate = null;
					try {
						rate = new StringEntity(rating, HTTP.UTF_8);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Making a request to url and getting response
					response = sh.makeServiceCall(postRateUrl,
							ServiceHandler.POST, rate);
				} else if (mType == PostTaskType.COMMENT) {

					StringEntity comment = null;
					try {
						comment = new StringEntity(commentByUser, HTTP.UTF_8);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// Making a request to url and getting response
					response = sh.makeServiceCall(postCommentUrl,
							ServiceHandler.POST, comment);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Query for json
			new GetInstructor()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			// Load All the comments
			new GetInstructorComment()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

	}

	/**
	 * Async task class to get json by making HTTP call
	 * */
	private class GetInstructorComment extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mInstructorComment.clear();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			String jsonStr = "";

			if (Utility.isOnline(getApplicationContext())) {
				// Creating service handler class instance
				ServiceHandler sh = new ServiceHandler();
				// Making a request to url and getting response
				jsonStr = sh.makeServiceCall(urlComments, ServiceHandler.GET);
			} else {
				jsonStr = Utility.getPreference(getApplicationContext(), mID,
						CacheManager.InstructorComments);
			}
			if (jsonStr != null) {
				try {
					org.json.simple.JSONArray json = (org.json.simple.JSONArray) new JSONParser()
							.parse(jsonStr);
					for (int i = 0; i < json.size(); i++) {
						String text = "";
						String date = "";

						String res = json.get(i).toString();

						res = res.replace("{", "").replace("}", "")
								.replaceAll("\"", "");
						String[] entries = res.split(",");
						for (String entry : entries) {
							try {
								String[] keyValue = entry.split(":");
								if ("text".equals(keyValue[0])) {
									text = keyValue[1];
								} else if ("date".equals(keyValue[0])) {
									date = keyValue[1];
									date = date.replaceAll("/", "");
								}
							} catch (ArrayIndexOutOfBoundsException e) {
								// TODO: handle exception
							}

						}
						mInstructorComment
								.add(new InstructorComment(text, date));

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										adaptor.notifyDataSetChanged();
									}
								});
							}
						});
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

		}

	}

	private static class GridViewAdapter extends BaseAdapter {
		ArrayList<InstructorComment> list;

		public GridViewAdapter(
				ArrayList<InstructorComment> instructorCommentList) {
			list = instructorCommentList;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public InstructorComment getItem(int i) {
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
			text.setText(getItem(i).Text + "\n" + getItem(i).Date);

			return view;
		}
	}

}
