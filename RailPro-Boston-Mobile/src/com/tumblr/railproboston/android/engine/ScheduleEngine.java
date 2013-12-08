package com.tumblr.railproboston.android.engine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.tumblr.railproboston.android.engine.types.Route;
import com.tumblr.railproboston.android.engine.types.StopTime;
import com.tumblr.railproboston.android.engine.types.Trip;
import com.tumblr.railproboston.android.ui.DownloadZipDialog;

//import jscholl.commuterrail.engine.TripsReaderContract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class ScheduleEngine {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	public static final String ROUTES = "routes.txt";
	public static final String TRIPS = "trips.txt";
	public static final String STOP_TIMES = "stop_times.txt";
	public static final String CALENDAR = "calendar.txt";
	public static final String CALENDAR_EXCEPTIONS = "calendar_dates.txt";

	private static FutureTask<Void> tripsFuture;
	private static FutureTask<Void> stopTimesFuture;
	private static ZipFile gtfs = null;
	private static Context context = null;

	private static ScheduleEngine2 scheduleEngine2;
	private static RoutesEngine routesEngine;
	private static StopTimesEngine stopTimesEngine;

	public static void setContext(Context ctx) {
		if (ScheduleEngine.context != ctx) {
			context = ctx;
			scheduleEngine2 = new ScheduleEngine2(context);
			routesEngine = new RoutesEngine(context);
			stopTimesEngine = new StopTimesEngine(context);
		}
	}

	public ScheduleEngine2 engine() {
		return scheduleEngine2;
	}

	private static Context ctx() {
		return context;
	}

	private static File getGtfs() {
		File x = new File(ctx().getCacheDir(), "MBTA_GTFS");
		x.mkdirs();
		if (!x.isDirectory())
			Log.w(CLASSNAME, "Unable to create gtfs directory: " + x);
		return x;
	}

	private static File gtfsFile() {
		return new File(getGtfs(), "MBTA_GTFS.zip");
	}

	private static ZipFile getGtfsZip() {
		Log.i(CLASSNAME, "Is external storage writeable? " + isExternalStorageWritable());
		if (gtfs == null) {
			if (!gtfsFile().exists()) {
				boolean b = downloadFile(
						"https://sites.google.com/site/rs0site/MBTA_GTFS-CR.zip?attredirects=0&d=1",
						gtfsFile());
				if (!b) {
					Log.w(CLASSNAME, "Failed to download MBTA GTFS file");
					return null;
				}
			} else {
				Log.i(CLASSNAME, "GTFS file already exists");
			}

			try {
				gtfs = new ZipFile(gtfsFile());
			} catch (IOException e) {
				Log.w(CLASSNAME, "Failed to initiate zip file: ", e);
			}
		}

		Log.d(CLASSNAME, "gtfs = " + gtfs);
		return gtfs;
	}

	private static boolean downloadFile(String sUrl, File downloadLoc) {
		Log.i(CLASSNAME, "Trying to download MBTA GTFS file");
		Log.d(CLASSNAME, "Is external storage writeable? " + isExternalStorageWritable());

		//new DownloadZipDialogFragment().show(MainActivity.getInstance().getSupportFragmentManager(), "DownloadZipDialog");
		boolean b = new DownloadZipDialog().call();
		if (!b) {
			Log.w(CLASSNAME, "User denied file download");
			return false;
		}

		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(sUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.w(CLASSNAME, "Bad response code " + connection.getResponseCode());
				return false;
			}

			// download the file
			input = connection.getInputStream();
			downloadLoc.createNewFile();
			output = new FileOutputStream(downloadLoc);

			byte data[] = new byte[4096];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
		} catch (IOException e) {
			Log.e(CLASSNAME, "Failed to download MBTA GTFS file because of exception ", e);
			b = false;
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {}

			if (connection != null)
				connection.disconnect();
		}

		return b;
	}

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private static InputStream getZipStream(String name) {
		try {
			Log.d(CLASSNAME, "Attempting to open zip stream for name " + name);
			ZipFile gtfsZip = getGtfsZip();
			ZipEntry entry = gtfsZip.getEntry(name);
			return gtfsZip.getInputStream(entry);
		} catch (IOException e) {
			Log.e(CLASSNAME, "Can't open zip stream " + name, e);
		}
		return null;
	}

	public static BufferedReader getReader(String name) {
		return new BufferedReader(new InputStreamReader(getZipStream("MBTA_GTFS-CR/" + name)));
	}

	/*public static void prepareDatabasesAsync(final Context ctx) {
		tripsFuture = new FutureTask<Void>(new Runnable() {
			public void run() {
				TripsEngine.prepareDatabase(ctx);
			}
		}, null);
		stopTimesFuture = new FutureTask<Void>(new Runnable() {
			public void run() {
				StopTimesEngine.prepareDatabase(ctx);
			}
		}, null);
		tripsFuture.run();
		stopTimesFuture.run();
	}*/

	public static void prepareDatabases(Context ctx) {
		setContext(ctx);
		routesEngine.prepareDatabase();
		TripsEngine.prepareDatabase(ctx);
		stopTimesEngine.prepareDatabase();
		getServiceIdToday(ctx);
	}

	public static void waitOnTrips() {
		try {
			if (tripsFuture != null)
				tripsFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static void waitOnStopTimes() {
		try {
			if (stopTimesFuture != null)
				stopTimesFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static void clearCache() {
		serviceIds = new HashMap<Date, String>();
	}

	public static Route getRoute(Context ctx, String routeId) {
		for (Route r : getRoutes(ctx)) {
			if (routeId.equals(r.routeId)) {
				return r;
			}
		}
		return null;
	}

	public static List<Route> getRoutes(Context ctx) {
		return routesEngine.get(null);
	}

	public static Map<Route, List<Trip>> getTrips(Context ctx) {
		Map<Route, List<Trip>> tripMap = new HashMap<Route, List<Trip>>();
		for (Route r : getRoutes(ctx)) {
			tripMap.put(r, getTrips(ctx, r));
		}
		return tripMap;
	}

	public static List<Trip> getTrips(Context ctx, Route r) {
		return TripsEngine.getTrips(ctx, r);
	}

	public static Trip getTrip(Context ctx, String tripId) {
		for (List<Trip> tripList : getTrips(ctx).values())
			for (Trip t : tripList)
				if (tripId.equals(t.tripId))
					return t;
		return null;
	}

	public static Map<Trip, List<StopTime>> getStopTimes(Context ctx, Route r) {
		return getStopTimes(ctx, getTrips(ctx, r));
	}

	public static Map<Trip, List<StopTime>> getStopTimes(Context ctx, List<Trip> trips) {
		setContext(ctx);
		List<StopTime> stopTimes = stopTimesEngine.getAll(trips);
		return associate(trips, stopTimes);
	}

	private static Map<Trip, List<StopTime>> associate(List<Trip> trips, List<StopTime> stopTimes) {
		Map<String, Trip> tripMap = new HashMap<String, Trip>();
		for (Trip t : trips)
			tripMap.put(t.tripId, t);

		Map<Trip, List<StopTime>> stopTimeMap = new HashMap<Trip, List<StopTime>>();
		for (Trip t : trips)
			stopTimeMap.put(t, new ArrayList<StopTime>());
		for (StopTime st : stopTimes)
			stopTimeMap.get(tripMap.get(st.tripId)).add(st);

		return stopTimeMap;
	}

	public static List<StopTime> getStopTimes(Context ctx, Trip t) {
		return stopTimesEngine.get(t);
	}

	private static Map<Date, String> serviceIds = new HashMap<Date, String>();

	public static String getServiceIdToday(Context ctx) {
		return getServiceId(ctx, new Date());
	}

	public static String getServiceId(Context ctx, Date d) {
		if (!serviceIds.containsKey(d)) {
			List<String> sids = CalendarEngine.getServiceIDs(ctx, d);
			if (sids.size() != 1)
				Log.e(CLASSNAME, "Wrong number of service ids. All service ids: " + sids);
			Log.d(CLASSNAME, "Today's service ID is: " + sids.get(0));
			serviceIds.put(d, sids.get(0));
		}
		return serviceIds.get(d);
	}

	public static String clean(String s) {
		return s.replace("\"", "");
	}

	private static Route selectedRoute;

	public static Route getSelectedRoute() {
		return selectedRoute;
	}

	public static void setSelectedRoute(Route route) {
		selectedRoute = route;
	}

	private static Trip selectedTrip;

	public static Trip getSelectedTrip() {
		return selectedTrip;
	}

	public static void setSelectedTrip(Trip trip) {
		selectedTrip = trip;
	}

	private static StopTime selectedStopTime;

	public static StopTime getSelectedStopTime() {
		return selectedStopTime;
	}

	public static void setSelectedStopTime(StopTime stopTime) {
		selectedStopTime = stopTime;
	}

	public static SQLiteOpenHelper getDbHelper(Context ctx) {
		return new ScheduleReaderDbHelper(ctx);
	}
}

interface SqlContract {
	public String getSqlCreateTable();

	public String getSqlDeleteTable();
}

class ScheduleReaderDbHelper extends SQLiteOpenHelper {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Schedule.db";
	private List<SqlContract> contracts;

	public ScheduleReaderDbHelper(Context ctx) {
		this(ctx, RoutesEngine.getContract(), TripsEngine.getContract(), StopTimesEngine
				.getContract());
	}

	public ScheduleReaderDbHelper(Context context, SqlContract... sqlContracts) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.contracts = new ArrayList<SqlContract>();
		for (SqlContract c : sqlContracts) {
			this.contracts.add(c);
		}
	}

	public void onCreate(SQLiteDatabase db) {
		Log.d(CLASSNAME, "About to create database");
		for (SqlContract c : contracts)
			db.execSQL(c.getSqlCreateTable());
		Log.d(CLASSNAME, "Finished creating database");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		for (SqlContract c : contracts) {
			db.execSQL(c.getSqlDeleteTable());
		}
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}