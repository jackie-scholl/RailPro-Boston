package com.tumblr.railproboston.android.ui;

import java.util.ArrayList;
import java.util.List;

import jscholl.commuterrail.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tumblr.railproboston.android.engine.ScheduleEngine;
import com.tumblr.railproboston.android.engine.types.StopTime;
import com.tumblr.railproboston.android.engine.types.Trip;
import com.tumblr.railproboston.android.predict.PredictionEngine;

public class StopTimesFragment extends ListFragment implements OnPredictionFoundListener {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	public static final String EXTRA_TRIP = "jscholl.commuterrail.TRIP";

	private List<StopTime> stops;
	private List<String> listStrings;

	public interface OnStopTimeSelectedListener {
		void onStopTimeSelected(StopTime stopTime);
	}

	// This is the Adapter being used to display the list's data
	private ArrayAdapter<StopTime> mAdapter;

	private PredictionEngine predictionEngine;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		predictionEngine = new PredictionEngine(activity, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(CLASSNAME, "Fragment created");
		Log.i(CLASSNAME, "Classname is " + CLASSNAME);
		ScheduleEngine.setContext(getActivity());

		String tripId = null;
		Bundle args = getArguments();
		if (args != null)
			tripId = args.getString(EXTRA_TRIP);
		if (tripId == null)
			Log.e(CLASSNAME, "No saved trip id");

		Trip t = ScheduleEngine.getTrip(getActivity(), tripId);
		new GetStopTimesTask().execute(t);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.trips_view, container, false);
	}

	private class GetStopTimesTask extends AsyncTask<Trip, Void, List<StopTime>> {
		private String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

		protected List<StopTime> doInBackground(Trip... trips) {
			ScheduleEngine.waitOnStopTimes();
			return ScheduleEngine.getStopTimes(getActivity(), trips[0]);
		}

		protected void onPostExecute(List<StopTime> result) {
			stops = result;
			listStrings = new ArrayList<String>();
			for (StopTime st : stops)
				listStrings.add(String.format("%s - %s", st.getStopId(), st.getFormattedTime()));
			Activity a = getActivity();
			mAdapter = new ArrayAdapter<StopTime>(a, android.R.layout.simple_list_item_1, stops);
			setAdapter();
		}
	}

	private void setAdapter() {
		setListAdapter(mAdapter);
	}

	private static int clickedPosition = -1;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(CLASSNAME, "Clicked view is: " + v);
		clickedPosition = position;

		StopTime selected = stops.get(position);
		ScheduleEngine.setSelectedStopTime(selected);
		predictionEngine.onStopTimeSelected(selected);
	}

	@Override
	public void onPredictionFound(StopTime prediction) {
		//prediction.setPredictedLateness(prediction);
		setAdapter();
	}
}