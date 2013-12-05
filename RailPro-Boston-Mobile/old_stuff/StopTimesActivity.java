package jscholl.commuterrail;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView.LayoutParams;
import android.widget.*;

public class StopTimesActivity extends ListActivity {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
	public static final String EXTRA_TRIP = "jscholl.commuterrail.TRIP";
	
	private Trip trip;
	private List<StopTime> stops;
	private List<String> listStrings;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(CLASSNAME, "Stop Times activity created");

		Intent intent = getIntent();
		String tripId = intent.getStringExtra(EXTRA_TRIP);
		trip = ScheduleEngine.getTrip(this, tripId);

		Log.d(CLASSNAME, "Trip found: " + trip.toString());

		// Create a progress bar to display while the list loads
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.CENTER));
		progressBar.setIndeterminate(true);
		getListView().setEmptyView(progressBar);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);
		
		new GetStopTimesTask().execute(trip);
		
		Log.d(CLASSNAME, "Trips set up");
	}
	
	private class GetStopTimesTask extends AsyncTask<Trip, Void, List<StopTime>> {
		private String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
		protected List<StopTime> doInBackground(Trip... trips) {
			ScheduleEngine.waitOnStopTimes();
			return StopTimesEngine.getStopTimes(StopTimesActivity.this, trips[0]);
		}

		protected void onPostExecute(List<StopTime> result) {
			stops = result;
			listStrings = new ArrayList<String>();
			for (StopTime st : stops)
				listStrings.add(String.format("%s - %s", st.stopId, st.getFormattedTime()));
			setListAdapter(new ArrayAdapter<String>(StopTimesActivity.this, android.R.layout.simple_list_item_1, listStrings));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trip, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
