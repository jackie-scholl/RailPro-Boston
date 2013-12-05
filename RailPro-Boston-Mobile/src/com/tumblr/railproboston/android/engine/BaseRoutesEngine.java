package com.tumblr.railproboston.android.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tumblr.railproboston.android.engine.RoutesReaderContract.RouteEntry;
import com.tumblr.railproboston.android.engine.types.Route;

public abstract class BaseRoutesEngine {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	protected Context ctx;
	protected List<Route> cache;
	protected Date cacheLastUpdated;

	public BaseRoutesEngine(Context ctx) {
		this.ctx = ctx;
		cache = null;
		cacheLastUpdated = new Date(0);
	}

	abstract String getTableName();

	abstract List<Route> getRoutesFromFile();

	abstract String sortBy();

	abstract ContentValues getContentValues(Route x);

	abstract Route getItem(Cursor c);

	private boolean isCacheExpired() {
		long cacheUpdated = cacheLastUpdated.getTime();
		long now = cacheLastUpdated.getTime();
		long diff = now - cacheUpdated;
		long expiryTime = 1000 * 60 * 30; // 30 minutes
		return diff > expiryTime;
	}

	public List<Route> getRoutes() {
		if (isCacheExpired()) {
			cache = getRoutes2();
		}
		return cache;
	}

	private List<Route> getRoutes2() {
		SQLiteDatabase db = getPopulatedReadableDatabase();

		// How you want the results sorted in the resulting Cursor
		String sortOrder = sortBy() + " DESC";

		Log.i(CLASSNAME, "About to make query");

		Cursor c = db.query(getTableName(), // The table to query
				null, // The columns to return (null means all)
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);

		Log.i(CLASSNAME, "About to process query results");
		c.moveToFirst();
		Log.d(CLASSNAME, "There were this many results: " + c.getCount());

		List<Route> routes = new ArrayList<Route>();
		while (!c.isAfterLast()) {
			Route r = getItem(c);
			routes.add(r);
			c.moveToNext();
		}
		db.close();
		Log.d(CLASSNAME, "Done processing query results");
		return routes;
	}

	private SQLiteDatabase getPopulatedReadableDatabase() {
		SQLiteOpenHelper mDbHelper = ScheduleEngine.getDbHelper(ctx);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = db.query(getTableName(), null, null, null, null, null, null); // Get all rows
		int count = c.getCount(); // Get number of rows
		if (count > 0)
			return db;
		db.close();
		setUpRoutes();
		return mDbHelper.getReadableDatabase();
	}

	public void prepareDatabase() {
		getPopulatedReadableDatabase().close();
	}

	private void setUpRoutes() {
		Log.i(CLASSNAME, "Setting up routes");
		List<Route> routes = getRoutesFromFile();

		SQLiteOpenHelper mDbHelper = ScheduleEngine.getDbHelper(ctx);

		Log.d(CLASSNAME, "About to get writable database");
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Log.d(CLASSNAME, "Just got writable database");

		for (Route x : routes) {
			// Create a new map of values, where column names are the keys
			ContentValues values = getContentValues(x);
			Log.d(CLASSNAME, "Adding route " + values);
			// Insert the new row
			db.insert(RouteEntry.TABLE_NAME, null, values);
		}
		db.close();
		Log.i(CLASSNAME, "Done writing routes to database");
	}

}