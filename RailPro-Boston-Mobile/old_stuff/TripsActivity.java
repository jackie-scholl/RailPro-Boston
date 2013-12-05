package jscholl.commuterrail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class TripsActivity extends ListActivity {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();

	public static Route route;
	public static List<Trip> trips;
	private List<String> tripNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(CLASSNAME, "Route activity created");

		Intent intent = getIntent();
		String routeId = intent.getStringExtra(MainActivity.EXTRA_ROUTE);
		route = null;
		for (Route r : ScheduleEngine.getRoutes(this)) {
			if (r.id.equals(routeId)) {
				route = r;
			}
		}

		Log.d(CLASSNAME, "Route found: " + route.toString());

		// Create a progress bar to display while the list loads
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.CENTER));
		progressBar.setIndeterminate(true);
		getListView().setEmptyView(progressBar);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		new GetTripsTask().execute(route);

		Log.d(CLASSNAME, "Trips set up");
	}

	private class GetTripsTask extends AsyncTask<Route, Void, List<Trip>> {
		private String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
		protected List<Trip> doInBackground(Route... routes) {
			ScheduleEngine.waitOnTrips();
			return TripsEngine.getTrips(TripsActivity.this, routes[0]);
		}

		protected void onPostExecute(List<Trip> result) {
			Context ctx = TripsActivity.this;
			trips = result;
			for (Trip t : trips)
				t.getStopTimes(ctx);
			Collections.sort(trips);
			tripNames = new ArrayList<String>();
			for (Trip t : trips) {
				tripNames.add(String.format("%s %tk:%<tM - %tk:%<tM", t.direction.equals("1") ? "Inbound" : "Outbound",
						t.getStartTime(ctx), t.getEndTime(ctx)));
			}
			setListAdapter(new ArrayAdapter<String>(TripsActivity.this, android.R.layout.simple_list_item_1, tripNames));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route, menu);
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(CLASSNAME, "Setting up intent");
		Intent intent = new Intent(this, StopTimesActivity.class);
		Trip t = trips.get(position);
		intent.putExtra(StopTimesActivity.EXTRA_TRIP, t.tripId);
		startActivity(intent);
		Log.d(CLASSNAME, "Intent sent");
	}
}
