package com.tumblr.railproboston.android.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.tumblr.railproboston.android.engine.CalendarReaderContract.CalendarEntry;

public class CalendarEngine extends BaseScheduleEngine<String, ServiceDate> {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

	public CalendarEngine(Context ctx) {
		super(ctx);
	}

	public String getServiceIDs(Date d) {
		Log.d(CLASSNAME, "Getting service ids for " + d);
		List<ServiceDate> serviceDates = getAll();
		List<String> serviceIDs = new ArrayList<String>();
		for (ServiceDate sd : serviceDates) {
			if (sd.isWithinService(d))
				serviceIDs.add(sd.id);
		}

		if (serviceIDs.size() != 1)
			Log.e(CLASSNAME, "Wrong number of service ids. All service ids: " + serviceIDs);
		Log.d(CLASSNAME, "Service ID is: " + serviceIDs.get(0));

		return serviceIDs.get(0);
	}

	@Override
	protected String getPluralName() {
		return "service dates";
	}

	@Override
	protected String getFileName() {
		return ScheduleEngine.CALENDAR;
	}

	@Override
	protected ServiceDate getValue(String line) {
		String[] rowData = line.split(",");
		if (rowData[0].substring(1, 3).equals("CR"))
			return new ServiceDate(rowData);
		return null;
	}

	@Override
	protected ContentValues getContentValues(ServiceDate v) {
		ContentValues values = new ContentValues();
		values.put(CalendarEntry.COLUMN_NAME_SERVICE_ID, v.id);
		values.put(CalendarEntry.COLUMN_NAME_WEEKDAYS, v.weekdays);
		values.put(CalendarEntry.COLUMN_NAME_SATURDAYS, v.saturdays);
		values.put(CalendarEntry.COLUMN_NAME_SUNDAYS, v.sundays);
		values.put(CalendarEntry.COLUMN_NAME_START_DATE, v.startDate);
		values.put(CalendarEntry.COLUMN_NAME_END_DATE, v.endDate);
		return values;
	}

	@Override
	protected String selection(String key) {
		return equalsSelection(CalendarEntry.COLUMN_NAME_SERVICE_ID, key);
	}

	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return CalendarEntry.TABLE_NAME;
	}

	@Override
	protected String sortOrder() {
		// TODO Auto-generated method stub
		return CalendarEntry.COLUMN_NAME_SERVICE_ID + " ASC";
	}

	@Override
	protected ServiceDate extractValue(Cursor c) {
		String serviceId = c.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_SERVICE_ID));
		String weekdays = c.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_WEEKDAYS));
		String saturdays = c.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_SATURDAYS));
		String sundays = c.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_SUNDAYS));
		String startDate = c.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_START_DATE));
		String endDate = c.getString(c.getColumnIndexOrThrow(CalendarEntry.COLUMN_NAME_END_DATE));
		ServiceDate sid = new ServiceDate(serviceId, weekdays, saturdays, sundays, startDate, endDate);
		return sid;
	}
}

final class CalendarReaderContract {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public CalendarReaderContract() {}

	/* Inner class that defines the table contents */
	static abstract class CalendarEntry implements BaseColumns {
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
		String formattedDate = String.format(Locale.US, "%04d%02d%02d", c.get(Calendar.YEAR),
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