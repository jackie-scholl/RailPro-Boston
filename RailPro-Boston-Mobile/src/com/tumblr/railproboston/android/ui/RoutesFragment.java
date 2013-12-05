package com.tumblr.railproboston.android.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.tumblr.railproboston.android.engine.ScheduleEngine;
import com.tumblr.railproboston.android.engine.types.Route;

import jscholl.commuterrail.R;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RoutesFragment extends ListFragment {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	public static final String EXTRA_ROUTE = "jscholl.commuterrail.ROUTE";

	public interface OnRouteSelectedListener {
		void onRouteSelected(Route route);
	}

	private List<Route> routes;
	private List<String> routeNames;

	// This is the Adapter being used to display the list's data
	private ArrayAdapter<String> mAdapter;

	private OnRouteSelectedListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			listener = (OnRouteSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnRouteSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(CLASSNAME, "Fragment created");
		Log.i(CLASSNAME, "Classname is " + CLASSNAME);
		ScheduleEngine.setContext(getActivity());
		new GetRoutesTask().execute();

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.routes_view, container, false);

	}

	private class GetRoutesTask extends AsyncTask<Void, Void, List<Route>> {
		private String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

		protected List<Route> doInBackground(Void... routes) {
			Log.i(CLASSNAME, "Starting to get routes");
			ScheduleEngine.prepareDatabases(getActivity());

			return ScheduleEngine.getRoutes(getActivity());
		}

		protected void onPostExecute(List<Route> result) {
			Log.i(CLASSNAME, "Done getting routes");
			Context ctx = getActivity();
			routes = result;
			routeNames = new ArrayList<String>();
			for (Route r : routes) {
				routeNames.add(r.longName);
			}
			mAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1,
					routeNames);
			setListAdapter(mAdapter);
			Log.i(CLASSNAME, "Line Picker list adapter set");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.onRouteSelected(routes.get(position));
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	/* Get private permanent storage directory for route data */
	public File getStorageDir() {
		String directoryName = "Commuter Rail";
		File file = new File(getActivity().getExternalFilesDir(null), directoryName);
		if (!file.mkdirs()) {
			Log.e("jscholl.commuterrail.MainActivity:getStorageDir", "Directory not created");
		}
		return file;
	}

	public static String clean(String s) {
		return s.replace("\"", "");
	}
}

/*@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Create a progress bar to display while the list loads
	ProgressBar progressBar = new ProgressBar(getActivity());
	progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
			Gravity.CENTER));
	progressBar.setIndeterminate(true);
	getListView().setEmptyView(progressBar);

	// Must add the progress bar to the root of the layout
	ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
	root.addView(progressBar);
}*/