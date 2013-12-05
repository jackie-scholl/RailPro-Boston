package com.tumblr.railproboston.android.predict;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.tumblr.railproboston.android.engine.ScheduleEngine;
import com.tumblr.railproboston.android.engine.types.StopTime;
import com.tumblr.railproboston.android.ui.OnPredictionFoundListener;
import com.tumblr.railproboston.android.ui.StopTimesFragment;

public class PredictionEngine implements StopTimesFragment.OnStopTimeSelectedListener {
	private final static String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	private OnPredictionFoundListener predictionListener;
	private Context ctx;

	public PredictionEngine(Context ctx, OnPredictionFoundListener listener) {
		this.ctx = ctx;
		predictionListener = listener;
	}

	@Override
	public void onStopTimeSelected(StopTime stopTime) {
		Log.i(CLASSNAME, "StopTime selected: " + stopTime);
		//Log.i(CLASSNAME, "About to execute task");
		new PredictTask().execute(stopTime);
	}

	private boolean isNetworkConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	private static URL getPredictUrl(StopTime stopTime) throws MalformedURLException {
		Map<String, String> params = getPredictionInterfaceQuery(stopTime);
		StringBuffer qb = new StringBuffer();
		for (String key : params.keySet())
			qb.append(key + "=" + params.get(key) + "&");
		String query = qb.substring(0, qb.length() - 1);

		try {
			return new URL("http://heroic-district-391.appspot.com/api/v1/predict?" + query);
		} catch (MalformedURLException e) {
			Log.e(CLASSNAME, "Trying to form prediction url", e);
			throw e;
		}
	}

	private static Map<String, String> getPredictionInterfaceQuery(StopTime stopTime) {
		String routeId = stopTime.getRouteId();
		String tripId = stopTime.getTripId();
		String station = stopTime.getStopId();

		Log.i(CLASSNAME, "Trip " + tripId + " has route " + routeId);
		//DateFormat myTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		//String scheduled = myTimeFormat.format(ScheduleEngine.getSelectedStopTime().getTime());

		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("routeid", PredictionEngine.encode(routeId));
		params.put("tripid", PredictionEngine.encode(tripId));
		params.put("station", PredictionEngine.encode(station));
		//params.put("scheduled", PredictionEngine.encode(scheduled));
		return params;
	}

	private static String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(CLASSNAME, "Failed to url-encode string: " + str, e);
			return null;
		}
	}

	private static String fetch(URL url) {
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(false);
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			return readStream(in);
		} catch (IOException e) {
			try {
				Log.e(CLASSNAME,
						"Failed reading prediction; response code: "
								+ urlConnection.getResponseCode(), e);
			} catch (IOException e1) {
				Log.e(CLASSNAME, "Failed getting response code", e1);
			}
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}

		return null;
	}

	private static String readStream(InputStream in) throws IOException {
		InputStream is = new BufferedInputStream(in);
		StringBuffer sb = new StringBuffer();

		int c;
		while ((c = is.read()) != -1)
			sb.append((char) c);

		return sb.toString();
	}

	private class PredictTask extends AsyncTask<StopTime, Void, StopTime> {
		final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

		@Override
		protected StopTime doInBackground(StopTime... args) {
			Log.i(CLASSNAME, "Task started");
			StopTime stopTime = args[0];

			URL url;
			try {
				url = getPredictUrl(stopTime);
			} catch (MalformedURLException e2) {
				return null;
			}
			Log.i(CLASSNAME, "Predict URL constructed: " + url);

			if (!isNetworkConnected()) {
				Log.w(CLASSNAME, "Could not connect to network");
				return null;
			}

			String response = fetch(url);
			if (response == null) {
				Log.w(CLASSNAME, "Background finished unsuccessfully");
				return null;
			}

			stopTime.setPredictedLateness(response);

			Log.i(CLASSNAME, "Background finished successfully");
			return stopTime;
		}

		protected void onPostExecute(StopTime result) {
			Log.i(CLASSNAME, "About to call prediction listener");
			predictionListener.onPredictionFound(result);
			//Log.i(CLASSNAME, "Done calling prediction listener");
		}
	}

}
