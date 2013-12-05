package com.tumblr.railproboston.android.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.tumblr.railproboston.android.engine.types.Route;
import com.tumblr.railproboston.android.engine.types.StopTime;
import com.tumblr.railproboston.android.engine.types.Trip;

public class ScheduleEngine2 {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	public static final String ROUTES = "routes.txt";
	public static final String TRIPS = "trips.txt";
	public static final String STOP_TIMES = "stop_times.txt";
	public static final String CALENDAR = "calendar.txt";
	public static final String CALENDAR_EXCEPTIONS = "calendar_dates.txt";

	private Context ctx = null;

	public ScheduleEngine2(Context ctx) {
		this.ctx = ctx;
	}

	public Context ctx() {
		return ctx;
	}

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public Route getRoute(String routeId) {
		return ScheduleEngine.getRoute(ctx, routeId);
	}

	public List<Route> getRoutes() {
		return ScheduleEngine.getRoutes(ctx());
	}

	public Map<Route, List<Trip>> getTrips() {
		return ScheduleEngine.getTrips(ctx());
	}

	public List<Trip> getTrips(Route r) {
		return ScheduleEngine.getTrips(ctx, r);
	}

	public Trip getTrip(String tripId) {
		return ScheduleEngine.getTrip(ctx, tripId);
	}

	public Map<Trip, List<StopTime>> getStopTimes(Route r) {
		return ScheduleEngine.getStopTimes(ctx, r);
	}

	public List<StopTime> getStopTimes(Trip t) {
		return ScheduleEngine.getStopTimes(ctx, t);
	}

	public String getServiceIdToday() {
		return getServiceId(new Date());
	}

	public String getServiceId(Date d) {
		return ScheduleEngine.getServiceId(ctx, d);
	}

	public static SQLiteOpenHelper getDbHelper(Context ctx) {
		return new ScheduleReaderDbHelper(ctx);
	}
}

/*interface SqlContract2 {
	public String getSqlCreateTable();

	public String getSqlDeleteTable();
}

class ScheduleReaderDbHelper2 extends SQLiteOpenHelper {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Schedule.db";
	private List<SqlContract> contracts;

	public ScheduleReaderDbHelper2(Context ctx) {
		this(ctx, RoutesEngine2.getContract(), TripsEngine.getContract(), StopTimesEngine
				.getContract());
	}

	public ScheduleReaderDbHelper2(Context context, SqlContract... sqlContracts) {
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
}*/