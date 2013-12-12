package com.tumblr.railproboston.android.engine;

import java.util.*;
import java.util.zip.ZipFile;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tumblr.railproboston.android.engine.types.*;

public class ScheduleEngine {
	static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	public static final String ROUTES = "routes.txt";
	public static final String STOP_TIMES = "stop_times.txt";
	public static final String CALENDAR = "calendar.txt";
	public static final String CALENDAR_EXCEPTIONS = "calendar_dates.txt";

	static ZipFile gtfs = null;
	private static Context context = null;

	private static RoutesEngine routesEngine;
	private static TripsEngine tripsEngine;
	private static StopTimesEngine stopTimesEngine;
	private static CalendarEngine calendarEngine;

	public static void setContext(Context ctx) {
		if (ScheduleEngine.context != ctx) {
			context = ctx;
			routesEngine = new RoutesEngine(context);
			tripsEngine = new TripsEngine(context);
			stopTimesEngine = new StopTimesEngine(context);
			calendarEngine = new CalendarEngine(context);
		}
	}

	static Context ctx() {
		return context;
	}

	public static void prepareDatabases(Context ctx) {
		setContext(ctx);
		routesEngine.prepareDatabase();
		tripsEngine.prepareDatabase();
		stopTimesEngine.prepareDatabase();
		calendarEngine.prepareDatabase();
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
		setContext(ctx);
		return tripsEngine.get(r);
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
		List<StopTime> stopTimes = stopTimesEngine.getList(trips);
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

	public static String getServiceIdToday(Context ctx) {
		return getServiceId(ctx, new Date());
	}

	public static String getServiceId(Context ctx, Date d) {
		setContext(ctx);
		return calendarEngine.getServiceIDs(d);
	}

	public static String clean(String s) {
		return s.replace("\"", "");
	}

	//TODO: Move this to MainActivity
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