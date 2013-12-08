package com.tumblr.railproboston.android.engine;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tumblr.railproboston.android.engine.CalendarExceptionReaderContract.CalendarExceptionEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class CalendarExceptionEngine {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
	public static void setUpCalendarExceptions(Context ctx) {
		Log.i(CLASSNAME, "Setting up calendar exceptions");
		List<CalendarException> exceptions = new ArrayList<CalendarException>();

		try {
			BufferedReader br = ScheduleEngine.getReader(ScheduleEngine.CALENDAR_EXCEPTIONS);
			String line = br.readLine();
			while (line != null) {
				String[] rowData = line.split(",");
				if (rowData[0].substring(1, 3).equals("CR"))
					exceptions.add(new CalendarException(rowData[0], rowData[1], rowData[2]));
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			Log.w(CLASSNAME, "Quit setting up calendar due to I/O error");
		}
		
		Log.d(CLASSNAME, "Done reading calendar exceptions from file");
		
		CalendarExceptionReaderDbHelper mDbHelper = new CalendarExceptionReaderDbHelper(ctx);

		Log.d(CLASSNAME, "About to get writable database");
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Log.d(CLASSNAME, "Just got writable database");

		for (CalendarException x : exceptions) {
			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			values.put(CalendarExceptionEntry.COLUMN_NAME_SERVICE_ID, x.id);
			values.put(CalendarExceptionEntry.COLUMN_NAME_DATE, x.date);
			values.put(CalendarExceptionEntry.COLUMN_NAME_TYPE, x.type);
			
			Log.d(CLASSNAME, "Adding service date " + values);
			// Insert the new row
			db.insert(CalendarExceptionEntry.TABLE_NAME, null, values);
		}
		db.close();
		Log.d(CLASSNAME, "Done writing calendars to database");
		
		Log.i(CLASSNAME, "Done setting up calendar");
	}
	
	public static List<CalendarException> getExceptions(Context ctx) {
		Log.i(CLASSNAME, "Getting exceptions");
		
		SQLiteDatabase db = getPopulatedReadableDatabase(ctx);
		
		// How you want the results sorted in the resulting Cursor
		String sortOrder = CalendarExceptionEntry.COLUMN_NAME_DATE + " DESC";
	
		Log.i(CLASSNAME, "About to make query");
	
		Cursor c = db.query(CalendarExceptionEntry.TABLE_NAME, // The table to query
				null, // The columns to return (null means all)
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);
	
		Log.i(CLASSNAME, "About to process query results");
		Log.d(CLASSNAME, "There were this many results: " + c.getCount());
		
		List<CalendarException> exceptions = getExceptions(c);
		
		db.close();
		Log.i(CLASSNAME, "Done processing query results");
		return exceptions;
	}
	
	public static List<CalendarException> getExceptions(Context ctx, String serviceId) {
		SQLiteDatabase db = getPopulatedReadableDatabase(ctx);
		
		String selection = CalendarExceptionEntry.COLUMN_NAME_SERVICE_ID + "=?"; // SQL where clause
		String[] selectionArgs = { serviceId };		
	
		Log.i(CLASSNAME, "About to make query");
	
		Cursor c = db.query(CalendarExceptionEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
	
		Log.d(CLASSNAME, "There were this many results: " + c.getCount());
		List<CalendarException> exceptions = getExceptions(c);
		db.close();
		Log.d(CLASSNAME, "Done processing query results");
		return exceptions;
	}
	/*
	public static List<String> getServiceIDs(Context ctx, Calendar calendar) {
		Log.d(CLASSNAME, "Getting service ids for " + calendar);
		List<ServiceDate> serviceDates = getServiceDates(ctx);
		List<String> serviceIDs = new ArrayList<String>();
		Log.d(CLASSNAME, "Checking service dates");
		for (ServiceDate sd : serviceDates) {
			if (sd.isWithinService(calendar))
				serviceIDs.add(sd.id);
		}
		return serviceIDs;
	}*/
	
	private static List<CalendarException> getExceptions(Cursor c) {
		c.moveToFirst();
		List<CalendarException> exceptions = new ArrayList<CalendarException>();
		while (!c.isAfterLast()) {
			String serviceId = c.getString(c.getColumnIndexOrThrow(CalendarExceptionEntry.COLUMN_NAME_SERVICE_ID));
			String date = c.getString(c.getColumnIndexOrThrow(CalendarExceptionEntry.COLUMN_NAME_DATE));
			String type = c.getString(c.getColumnIndexOrThrow(CalendarExceptionEntry.COLUMN_NAME_TYPE));
			exceptions.add(new CalendarException(serviceId, date, type));
			c.moveToNext();
		}
		c.close();
		return exceptions;
	}
	
	private static SQLiteDatabase getPopulatedReadableDatabase(Context ctx) {
		Log.i(CLASSNAME, "Getting database");
		
		CalendarExceptionReaderDbHelper mDbHelper = new CalendarExceptionReaderDbHelper(ctx);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Log.i(CLASSNAME, "Getting initial readable database");
		
		Cursor c = db.query(CalendarExceptionEntry.TABLE_NAME, null, null, null, null, null, null); // Get all rows
		int count = c.getCount(); // Get number of rows
		Log.i(CLASSNAME, "Count is " + count);
		
		if (count > 0)
			return db;
		db.close();
		setUpCalendarExceptions(ctx);
		return mDbHelper.getReadableDatabase();
	}
}

final class CalendarExceptionReaderContract {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public CalendarExceptionReaderContract() {}

	/* Inner class that defines the table contents */
	public static abstract class CalendarExceptionEntry implements BaseColumns {
		public static final String TABLE_NAME = "calendarexceptions";
		public static final String COLUMN_NAME_SERVICE_ID = "serviceid";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_TYPE = "exceptiontype";
	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + CalendarExceptionEntry.TABLE_NAME + "  (" +
			CalendarExceptionEntry._ID + " INTEGER PRIMARY KEY," +
			CalendarExceptionEntry.COLUMN_NAME_SERVICE_ID + TEXT_TYPE + COMMA_SEP +
			CalendarExceptionEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
			CalendarExceptionEntry.COLUMN_NAME_TYPE + TEXT_TYPE +" )";

	static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + CalendarExceptionEntry.TABLE_NAME;
}

class CalendarExceptionReaderDbHelper extends SQLiteOpenHelper {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 5;
	public static final String DATABASE_NAME = "CalendarExceptionReader.db";
	
	public CalendarExceptionReaderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		Log.d(CLASSNAME, "About to create database");
		db.execSQL(CalendarExceptionReaderContract.SQL_CREATE_ENTRIES);
		Log.d(CLASSNAME, "Finished creating database");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(CalendarExceptionReaderContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}

class CalendarException {
	String id;
	String date;
	String type;
	
	public CalendarException(String service_id, String date, String exception_type) {
		this.id = ScheduleEngine.clean(service_id);
		this.date = ScheduleEngine.clean(date);
		this.type = ScheduleEngine.clean(exception_type);
	}

	public CalendarException(String[] arr) {
		this(arr[0], arr[1], arr[2]);
	}
}