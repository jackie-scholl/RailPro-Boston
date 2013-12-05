package com.tumblr.railproboston.android.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tumblr.railproboston.android.engine.TripsReaderContract.TripEntry;
import com.tumblr.railproboston.android.engine.types.Route;
import com.tumblr.railproboston.android.engine.types.Trip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class TripsEngine {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

	private static boolean setUpTrips(Context ctx) {
		Log.i(CLASSNAME, "Setting up trips");
		List<Trip> trips = new ArrayList<Trip>();

		try {
			BufferedReader br = ScheduleEngine.getReader(ScheduleEngine.TRIPS);
			br.readLine(); // Clears the line with the headers
			String line = br.readLine();
			int i = 0;
			while (line != null) {
				String[] rowData = line.split(",");

				if (i % 200 == 0) {
					Log.d(CLASSNAME, rowData[3]);
				}

				if (rowData[0].substring(1, 3).equals("CR")) {
					new Trip(rowData);
					trips.add(new Trip(rowData));
				}
				i++;
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			return false;
		}

		Log.i(CLASSNAME, "Writing trips to database");

		//TripsReaderDbHelper mDbHelper = new TripsReaderDbHelper(ctx);
		SQLiteOpenHelper mDbHelper = ScheduleEngine.getDbHelper(ctx);
		
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		for (Trip x : trips) {
			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			values.put(TripEntry.COLUMN_NAME_ROUTE_ID, x.routeId);
			values.put(TripEntry.COLUMN_NAME_SERVICE_ID, x.serviceId);
			values.put(TripEntry.COLUMN_NAME_TRIP_ID, x.tripId);
			values.put(TripEntry.COLUMN_NAME_TRIP_HEADSIGN, x.headsign);
			values.put(TripEntry.COLUMN_NAME_DIRECTION_ID, x.direction);
			values.put(TripEntry.COLUMN_NAME_BLOCK_ID, x.blockId);
			values.put(TripEntry.COLUMN_NAME_SHAPE_ID, x.shapeId);

			//Log.d(CLASSNAME, "Adding trip " + values);

			// Insert the new row
			db.insert(TripEntry.TABLE_NAME, null, values);
		}

		db.close();

		Log.i(CLASSNAME, "Done writing trips to database");

		Log.d(CLASSNAME, "Done setting up trips");
		return true;
	}

	public static List<Trip> getTrips(Context ctx, Route route) {
		return getTrips(ctx, route, ScheduleEngine.getServiceIdToday(ctx));
	}

	public static List<Trip> getTrips(Context ctx, Route route, String serviceIdDate) {
		Log.d(CLASSNAME, "About to get trips");

		SQLiteDatabase db = getPopulatedReadableDatabase(ctx);

		// How you want the results sorted in the resulting Cursor
		String sortOrder = TripEntry.COLUMN_NAME_TRIP_ID + " DESC";
		String selection = TripEntry.COLUMN_NAME_ROUTE_ID + "=? AND " + TripEntry.COLUMN_NAME_SERVICE_ID + "=?"; // SQL where clause
		String[] selectionArgs = { route.routeId, serviceIdDate };

		Log.d(CLASSNAME, "Query is: " + selection);
		Log.i(CLASSNAME, "About to make query");

		Cursor c = db.query(TripEntry.TABLE_NAME, // The table to query
				null, // The columns to return (null means all)
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);

		Log.i(CLASSNAME, "About to process query results");
		c.moveToFirst();
		Log.d(CLASSNAME, "There were this many results: " + c.getCount());

		List<Trip> trips = new ArrayList<Trip>();
		while (!c.isAfterLast()) {
			String routeId = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_ROUTE_ID));
			String serviceId = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_SERVICE_ID));
			String tripId = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_TRIP_ID));
			String tripHeadsign = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_TRIP_HEADSIGN));
			String directionId = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_DIRECTION_ID));
			String blockId = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_BLOCK_ID));
			String shapeId = c.getString(c.getColumnIndexOrThrow(TripEntry.COLUMN_NAME_SHAPE_ID));
			Trip t = new Trip(routeId, serviceId, tripId, tripHeadsign, directionId, blockId, shapeId);
			trips.add(t);
			c.moveToNext();
		}

		db.close();

		Log.d(CLASSNAME, "Done processing query results");

		return trips;
	}

	public static void prepareDatabase(Context ctx) {
		getPopulatedReadableDatabase(ctx).close();
	}

	private static SQLiteDatabase getPopulatedReadableDatabase(Context ctx) {
		//TripsReaderDbHelper mDbHelper = new TripsReaderDbHelper(ctx);
		SQLiteOpenHelper mDbHelper = ScheduleEngine.getDbHelper(ctx);
		
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		int count = db.query(TripEntry.TABLE_NAME, null, null, null, null, null, null).getCount(); // Get number of rows in table
		Log.d(CLASSNAME, "First count is " + count);
		if (count > 0)
			return db;
		db.close();
		setUpTrips(ctx);
		db = mDbHelper.getReadableDatabase();
		int newCount = db.query(TripEntry.TABLE_NAME, null, null, null, null, null, null).getCount(); // Get number of rows in table
		Log.d(CLASSNAME, "New count is " + newCount);
		return db;
	}
	
	public static SqlContract getContract() {
		return new TripsReaderContract();
	}

}

final class TripsReaderContract implements SqlContract {
	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public TripsReaderContract() {}

	/* Inner class that defines the table contents */
	public static abstract class TripEntry implements BaseColumns {
		public static final String TABLE_NAME = "trip";
		public static final String COLUMN_NAME_ROUTE_ID = "routeid";
		public static final String COLUMN_NAME_SERVICE_ID = "serviceid";
		public static final String COLUMN_NAME_TRIP_ID = "tripid";
		public static final String COLUMN_NAME_TRIP_HEADSIGN = "tripheadsign";
		public static final String COLUMN_NAME_DIRECTION_ID = "directionid";
		public static final String COLUMN_NAME_BLOCK_ID = "blockid";
		public static final String COLUMN_NAME_SHAPE_ID = "shapeid";
	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + TripEntry.TABLE_NAME + "  (" +
					TripEntry._ID + " INTEGER PRIMARY KEY," +
					TripEntry.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP +
					TripEntry.COLUMN_NAME_SERVICE_ID + TEXT_TYPE + COMMA_SEP +
					TripEntry.COLUMN_NAME_TRIP_ID + TEXT_TYPE + COMMA_SEP +
					TripEntry.COLUMN_NAME_TRIP_HEADSIGN + TEXT_TYPE + COMMA_SEP +
					TripEntry.COLUMN_NAME_DIRECTION_ID + TEXT_TYPE + COMMA_SEP +
					TripEntry.COLUMN_NAME_BLOCK_ID + TEXT_TYPE + COMMA_SEP +
					TripEntry.COLUMN_NAME_SHAPE_ID + TEXT_TYPE + " )";

	static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME;
	
	public String getSqlCreateTable() {
		return SQL_CREATE_ENTRIES;
	}

	public String getSqlDeleteTable() {
		return SQL_DELETE_ENTRIES;
	}
}

class TripsReaderDbHelper extends SQLiteOpenHelper {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();
	
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "TripsReader.db";

	public TripsReaderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TripsReaderContract.SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(TripsReaderContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}