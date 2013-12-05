package com.tumblr.railproboston.android.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.tumblr.railproboston.android.engine.StopTimesReaderContract.StopTimeEntry;
import com.tumblr.railproboston.android.engine.types.StopTime;
import com.tumblr.railproboston.android.engine.types.Trip;

public class StopTimesEngine extends BaseScheduleEngine<Trip, StopTime> {
	static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	public StopTimesEngine(Context ctx) {
		super(ctx);
	}

	protected String getPluralName() {
		return "stop times";
	}

	protected String getFileName() {
		return ScheduleEngine.STOP_TIMES;
	}

	protected StopTime getValue(String line) {
		String[] rowData = line.split(",");
		if (rowData[0].substring(1, 3).equals("CR"))
			return new StopTime(rowData);
		else
			return null;
	}

	protected String unionQuerySortOrder() {
		return StopTimeEntry.COLUMN_NAME_TRIP_ID + " ASC";
	}

	protected String sortOrder() {
		return StopTimeEntry.COLUMN_NAME_STOP_SEQUENCE + " ASC";
	}

	protected String selectionColumn() {
		return StopTimeEntry.COLUMN_NAME_TRIP_ID;
	}

	protected String selection(Trip t) {
		return StopTimeEntry.COLUMN_NAME_TRIP_ID + "=" + "\"" + t.getTripId() + "\"";
	}

	protected String[] selectionArgs(Trip trip) {
		return new String[] {};
		//return new String[] { trip.tripId };
	}

	protected String[] columns() {
		return new String[] { StopTimeEntry.COLUMN_NAME_STOP_ID,
				StopTimeEntry.COLUMN_NAME_ARRIVAL_TIME,
				StopTimeEntry.COLUMN_NAME_STOP_SEQUENCE,
				StopTimeEntry.COLUMN_NAME_TRIP_ID };
	}

	protected StopTime extractValue(Cursor c) {
		String tripId = getStringByColumn(c, StopTimeEntry.COLUMN_NAME_TRIP_ID);
		String arrivalTime = getStringByColumn(c, StopTimeEntry.COLUMN_NAME_ARRIVAL_TIME);
		String stopId = getStringByColumn(c, StopTimeEntry.COLUMN_NAME_STOP_ID);
		String stopSequence = getStringByColumn(c, StopTimeEntry.COLUMN_NAME_STOP_SEQUENCE);
		return new StopTime(tripId, arrivalTime, stopId, stopSequence);
	}

	protected String getTableName() {
		return StopTimeEntry.TABLE_NAME;
	}

	protected ContentValues getContentValues(StopTime v) {
		ContentValues cv = new ContentValues();
		cv.put(StopTimeEntry.COLUMN_NAME_TRIP_ID, v.tripId);
		cv.put(StopTimeEntry.COLUMN_NAME_ARRIVAL_TIME, v.time);
		cv.put(StopTimeEntry.COLUMN_NAME_STOP_ID, v.stopId);
		cv.put(StopTimeEntry.COLUMN_NAME_STOP_SEQUENCE, v.stopSequence);

		return cv;
	}

	public static SqlContract getContract() {
		return new StopTimesReaderContract();
	}
}

final class StopTimesReaderContract implements SqlContract {
	// To prevent someone from accidentally instantiating the contract class, give it an empty
	// constructor.
	public StopTimesReaderContract() {}

	/* Inner class that defines the table contents */
	public static abstract class StopTimeEntry implements BaseColumns {
		public static final String TABLE_NAME = "stoptime";
		public static final String COLUMN_NAME_TRIP_ID = "tripid";
		public static final String COLUMN_NAME_ARRIVAL_TIME = "arrivaltime";
		public static final String COLUMN_NAME_STOP_ID = "stopid";
		public static final String COLUMN_NAME_STOP_SEQUENCE = "stopsequence";
	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + StopTimeEntry.TABLE_NAME + "  (" +
					StopTimeEntry._ID + " INTEGER PRIMARY KEY," +
					StopTimeEntry.COLUMN_NAME_TRIP_ID + TEXT_TYPE + COMMA_SEP +
					StopTimeEntry.COLUMN_NAME_ARRIVAL_TIME + TEXT_TYPE + COMMA_SEP +
					StopTimeEntry.COLUMN_NAME_STOP_ID + TEXT_TYPE + COMMA_SEP +
					StopTimeEntry.COLUMN_NAME_STOP_SEQUENCE + TEXT_TYPE + " )";

	static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + StopTimeEntry.TABLE_NAME;

	public String getSqlCreateTable() {
		return SQL_CREATE_ENTRIES;
	}

	public String getSqlDeleteTable() {
		return SQL_DELETE_ENTRIES;
	}
}

class StopTimesReaderDbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 4;
	public static final String DATABASE_NAME = "StopTimesReader.db";

	public StopTimesReaderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(StopTimesReaderContract.SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(StopTimesReaderContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
