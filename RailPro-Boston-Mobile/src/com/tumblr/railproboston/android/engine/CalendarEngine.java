package com.tumblr.railproboston.android.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import com.tumblr.railproboston.android.engine.CalendarReaderContract.CalendarEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class CalendarEngine {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	public static void setUpServiceDates(Context ctx) {
		Log.i(CLASSNAME, "Setting up calendar");
		Map<String, ServiceDate> calendar = new HashMap<String, ServiceDate>();

		try {
			BufferedReader br = ScheduleEngine.getReader(ScheduleEngine.CALENDAR);
			String line = br.readLine();
			while (line != null) {
				String[] rowData = line.split(",");
				if (rowData[0].substring(1, 3).equals("CR"))
					calendar.put(rowData[0], new ServiceDate(rowData));
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			Log.w(CLASSNAME, "Quit setting up calendar due to I/O error");
		}

		Log.d(CLASSNAME, "Done reading calendar from file");

		CalendarReaderDbHelper mDbHelper = new CalendarReaderDbHelper(ctx);

		Log.d(CLASSNAME, "About to get writable database");
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		Log.d(CLASSNAME, "Just got writable database");

		for (ServiceDate x : calendar.values()) {
			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			values.put(CalendarEntry.COLUMN_NAME_SERVICE_ID, x.id);
			values.put(CalendarEntry.COLUMN_NAME_WEEKDAYS, x.weekdays);
			values.put(CalendarEntry.COLUMN_NAME_SATURDAYS, x.saturdays);
			values.put(CalendarEntry.COLUMN_NAME_SUNDAYS, x.sundays);
			values.put(CalendarEntry.COLUMN_NAME_START_DATE, x.startDate);
			values.put(CalendarEntry.COLUMN_NAME_END_DATE, x.endDate);

			Log.d(CLASSNAME, "Adding service date " + values);
			// Insert the new row
			db.insert(CalendarEntry.TABLE_NAME, null, values);
		}
		db.close();
		Log.d(CLASSNAME, "Done writing calendars to database");

		Log.i(CLASSNAME, "Done setting up calendar");
	}

	public static List<ServiceDate> getServiceDates(Context ctx) {
		SQLiteDatabase db = getPopulatedReadableDatabase(ctx);

		// How you want the results sorted in the resulting Cursor
		String sortOrder = CalendarEntry.COLUMN_NAME_SERVICE_ID + " DESC";

		Log.i(CLASSNAME, "About to make query");

		Cursor c = db.query(CalendarEntry.TABLE_NAME, // The table to query
				null, // The columns to return (null means all)
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);

		Log.i(CLASSNAME, "About to process query results");
		Log.d(CLASSNAME, "There were this many results: " + c.getCount());

		List<ServiceDate> calendar = getServiceDates(c);
		List<CalendarException> exceptions = CalendarExceptionEngine.getExceptions(ctx);
		for (ServiceDate sd : calendar) {
			sd.addExceptions(exceptions);
		}

		db.close();
		Log.i(CLASSNAME, "Done processing query results");
		return calendar;
	}

	public static ServiceDate getServiceDate(Context ctx, String serviceId) {
		SQLiteDatabase db = getPopulatedReadableDatabase(ctx);

		String selection = CalendarEntry.COLUMN_NAME_SERVICE_ID + "=?"; // SQL where clause
		String[] selectionArgs = { serviceId };

		Log.i(CLASSNAME, "About to make query");

		Cursor c = db.query(CalendarEntry.TABLE_NAME, null, selection, selectionArgs, null, null,
				null);

		Log.d(CLASSNAME, "There were this many results (expecting 1): " + c.getCount());
		List<ServiceDate> calendar = getServiceDates(c);
		ServiceDate sd = calendar.get(0);

		List<CalendarException> exceptions = CalendarExceptionEngine.getExceptions(ctx, serviceId);
		sd.addExceptions(exceptions);

		db.close();
		Log.d(CLASSNAME, "Done processing query results");
		return sd;
	}

	/**
	 * @deprecated Use {@link #getServiceIDs(ScheduleEngine2,Date)} instead
	 */
	public static List<String> getServiceIDs(Context ctx, Date d) {
		return getServiceIDs(ctx, d);
	}

	public static List<String> getServiceIDs(ScheduleEngine2 scheduleEngine, Date d) {
		Log.d(CLASSNAME, "Getting service ids for " + d);
		List<ServiceDate> serviceDates = getServiceDates(scheduleEngine.ctx());
		List<String> serviceIDs = new ArrayList<String>();
		//Log.d(CLASSNAME, "Checking service dates");
		for (ServiceDate sd : serviceDates) {
			if (sd.isWithinService(d))
				serviceIDs.add(sd.id);
		}
		return serviceIDs;
	}

	/**
	 * @deprecated Use {@link #getServiceIdToday(ScheduleEngine2)} instead
	 */
	public static String getServiceIdToday(Context ctx) {
		return getServiceIdToday(ctx);
	}

	public static String getServiceIdToday(ScheduleEngine2 scheduleEngine) {
		List<String> serviceIds = getServiceIDs(scheduleEngine, new Date());
		Log.d(CLASSNAME,
				"There were this many service IDs today (expecting 1): " + serviceIds.size());
		Log.d(CLASSNAME, "Today's service ID is: " + serviceIds.get(0));
		return serviceIds.get(0);
	}

	private static List<ServiceDate> getServiceDates(Cursor c) {
		c.moveToFirst();
		List<ServiceDate> calendar = new ArrayList<ServiceDate>();
		while (!c.isAfterLast()) {
			String serviceId = c.getString(c
					.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_SERVICE_ID));
			String weekdays = c.getString(c
					.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_WEEKDAYS));
			String saturdays = c.getString(c
					.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_SATURDAYS));
			String sundays = c
					.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_SUNDAYS));
			String startDate = c.getString(c
					.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_START_DATE));
			String endDate = c.getString(c
					.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_END_DATE));
			ServiceDate sid = new ServiceDate(serviceId, weekdays, saturdays, sundays, startDate,
					endDate);
			calendar.add(sid);
			c.moveToNext();
		}
		c.close();
		return calendar;
	}

	private static SQLiteDatabase getPopulatedReadableDatabase(Context ctx) {
		CalendarReaderDbHelper mDbHelper = new CalendarReaderDbHelper(ctx);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = db.query(CalendarEntry.TABLE_NAME, null, null, null, null, null, null); // Get all rows
		int count = c.getCount(); // Get number of rows
		if (count > 0)
			return db;
		db.close();
		setUpServiceDates(ctx);
		return mDbHelper.getReadableDatabase();
	}
}

final class CalendarReaderContract {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public CalendarReaderContract() {}

	/* Inner class that defines the table contents */
	public static abstract class CalendarEntry implements BaseColumns {
		public static final String TABLE_NAME = "calendar";
		public static final String COLUMN_NAME_SERVICE_ID = "serviceid";
		public static final String COLUMN_NAME_WEEKDAYS = "weekdays";
		public static final String COLUMN_NAME_SATURDAYS = "saturdays";
		public static final String COLUMN_NAME_SUNDAYS = "sundays";
		public static final String COLUMN_NAME_START_DATE = "startdate";
		public static final String COLUMN_NAME_END_DATE = "enddate";
	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + CalendarEntry.TABLE_NAME + "  (" +
			CalendarEntry._ID + " INTEGER PRIMARY KEY," + CalendarEntry.COLUMN_NAME_SERVICE_ID
			+ TEXT_TYPE + COMMA_SEP +
			CalendarEntry.COLUMN_NAME_WEEKDAYS + TEXT_TYPE + COMMA_SEP +
			CalendarEntry.COLUMN_NAME_SATURDAYS + TEXT_TYPE + COMMA_SEP +
			CalendarEntry.COLUMN_NAME_SUNDAYS + TEXT_TYPE + COMMA_SEP +
			CalendarEntry.COLUMN_NAME_START_DATE + TEXT_TYPE + COMMA_SEP +
			CalendarEntry.COLUMN_NAME_END_DATE + TEXT_TYPE + " )";

	static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + CalendarEntry.TABLE_NAME;
}

class CalendarReaderDbHelper extends SQLiteOpenHelper {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 5;
	public static final String DATABASE_NAME = "CalendarReader.db";

	public CalendarReaderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		Log.d(CLASSNAME, "About to create database");
		db.execSQL(CalendarReaderContract.SQL_CREATE_ENTRIES);
		Log.d(CLASSNAME, "Finished creating database");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(CalendarReaderContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}

class ServiceDate {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	String id;
	String weekdays;
	String saturdays;
	String sundays;
	String startDate;
	String endDate;
	List<String> serviceAddedExceptions;
	List<String> serviceRemovedExceptions;

	public ServiceDate(String service_id, String monday, String saturday, String sunday,
			String start_date, String end_date) {
		this.id = ScheduleEngine.clean(service_id);
		this.weekdays = ScheduleEngine.clean(monday);
		this.saturdays = ScheduleEngine.clean(saturday);
		this.sundays = ScheduleEngine.clean(sunday);
		this.startDate = ScheduleEngine.clean(start_date);
		this.endDate = ScheduleEngine.clean(end_date);
		this.serviceAddedExceptions = new ArrayList<String>();
		this.serviceRemovedExceptions = new ArrayList<String>();
	}

	public ServiceDate(String[] arr) {
		this(arr[0], arr[1], arr[6], arr[7], arr[8], arr[9]);
	}

	public void addService(String date) {
		serviceAddedExceptions.add(date);
	}

	public void removeService(String date) {
		serviceRemovedExceptions.add(date);
	}

	public void addException(String date, String type) {
		if (type.equals("1"))
			addService(date);
		else if (type.equals("2"))
			removeService(date);
		else
			Log.w(CLASSNAME, "Unexpected service exception type " + type);
	}

	public void addException(CalendarException exception) {
		if (exception.id.equals(id)) {
			addException(exception.date, exception.type);
		}
	}

	public void addExceptions(List<CalendarException> exceptions) {
		for (CalendarException ce : exceptions) {
			addException(ce);
		}
	}

	public boolean isWithinService(Date d) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		return isWithinService(cal);
	}

	public boolean isWithinService(Calendar c) {
		String formattedDate = String.format("%04d%02d%02d", c.get(Calendar.YEAR),
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		//Log.d(CLASSNAME, "Formatted date is "+formattedDate);
		for (String date : serviceAddedExceptions) {
			if (date.equals(formattedDate))
				return true;
		}

		for (String date : serviceRemovedExceptions) {
			if (date.equals(formattedDate))
				return false;
		}

		if (formattedDate.compareTo(startDate) < 0 || formattedDate.compareTo(endDate) > 0) {
			Log.d(CLASSNAME, "Out of service");
			//Log.d(CLASSNAME, "Start date is "+startDate);
			//Log.d(CLASSNAME, "End date is "+endDate);
			return false;
		}

		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY && saturdays.equals("1"))
			return true;
		if (dayOfWeek == Calendar.SUNDAY && sundays.equals("1"))
			return true;
		if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY && weekdays.equals("1"))
			return true;

		return false;
	}

	@Override
	public String toString() {
		return "ServiceDate [id=" + id + ", weekdays=" + weekdays + ", saturdays=" + saturdays
				+ ", sundays=" + sundays
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", serviceAddedExceptions="
				+ serviceAddedExceptions + ", serviceRemovedExceptions=" + serviceRemovedExceptions
				+ "]";
	}
}