package com.tumblr.railproboston.android.ui;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.tumblr.railproboston.android.engine.ScheduleEngine;
import com.tumblr.railproboston.android.engine.types.Route;
import com.tumblr.railproboston.android.engine.types.Trip;

import jscholl.commuterrail.R;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TripsFragment extends ListFragment {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

	public static final String EXTRA_ROUTE = "jscholl.commuterrail.ROUTE";

	private List<Trip> trips;

	public interface OnTripSelectedListener {
		void onTripSelected(Trip trip);
	}

	// This is the Adapter being used to display the list's data
	private ArrayAdapter<Trip> mAdapter;

	private OnTripSelectedListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			listener = (OnTripSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTripSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(CLASSNAME, "Fragment created");
		Log.i(CLASSNAME, "Classname is " + CLASSNAME);
		ScheduleEngine.setContext(getActivity());

		String routeId = null;
		Bundle args = getArguments();
		if (args != null)
			routeId = args.getString(EXTRA_ROUTE);
		if (routeId == null)
			Log.e(CLASSNAME, "No saved route id");

		Route r = ScheduleEngine.getRoute(getActivity(), routeId);
		new GetTripsTask().execute(r);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.trips_view, container, false);

	}

	private class GetTripsTask extends AsyncTask<Route, Void, List<Trip>> {
		private String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

		protected List<Trip> doInBackground(Route... route) {
			ScheduleEngine.waitOnTrips();
			ScheduleEngine.getStopTimes(getActivity(), route[0]);
			List<Trip> trips = ScheduleEngine.getTrips(getActivity(), route[0]);
			for (Trip t : trips)
				t.getStopTimes(getActivity());
			return trips;
		}

		protected void onPostExecute(List<Trip> result) {
			Context ctx = getActivity();
			trips = result;
			if (trips.size() == 0) {
				// Handle no trips today?
				Toast.makeText(getActivity(), "No trips on this route", Toast.LENGTH_LONG).show();
				Log.i(CLASSNAME, "Toast shown: No trips on this route");
				return;
			}
			for (Trip t : trips)
				t.getStopTimes(ctx);
			Collections.sort(trips);
			mAdapter = new ArrayAdapter<Trip>(ctx, android.R.layout.simple_list_item_1, trips);
			setListAdapter(mAdapter);
			Trip best = trips.get(0);
			Date bestStartTime = best.getStartTime(ctx);
			for (Trip t : trips) {
				Date startTime = t.getStartTime(ctx);
				if (startTime.compareTo(new Date()) < 0 && startTime.compareTo(bestStartTime) > 0) {
					best = t;
					bestStartTime = startTime;
				}
			}
			Log.i(CLASSNAME, "Best matching trip is: " + best);
			setSelection(mAdapter.getPosition(best));
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.onTripSelected(trips.get(position));
	}

	/* Get private permanent storage directory for route data */
	public File getStorageDir() {
		String directoryName = "Commuter Rail";
		File file = new File(getActivity().getExternalFilesDir(null), directoryName);
		if (!file.mkdirs()) {
			Log.e(CLASSNAME, "Directory not created");
		}
		return file;
	}

	public static String clean(String s) {
		return s.replace("\"", "");
	}
}