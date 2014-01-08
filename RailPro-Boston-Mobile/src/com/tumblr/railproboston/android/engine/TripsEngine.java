package com.tumblr.railproboston.android.engine;

import java.util.Locale;

import com.tumblr.railproboston.android.engine.types.Route;
import com.tumblr.railproboston.android.engine.types.Trip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

public class TripsEngine extends BaseScheduleEngine<Route, Trip> {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();
	public static final String FILE_NAME = "trips.txt";

	public TripsEngine(Context ctx) {
		super(ctx);
	}

	protected String getPluralName() {
		return "trips";
	}

	protected String getFileName() {
		return TripsEngine.FILE_NAME;
	}

	protected Trip getValue(String line) {
		String[] rowData = line.split(",");
		if (rowData[0].substring(1, 3).equals("CR"))
			return new Trip(rowData);
		else
			return null;
	}

	protected String sortOrder() {
		return TripEntry.COLUMN_NAME_TRIP_ID + " ASC";
	}

	protected String selection(Route r) {
		return equalsSelection(TripEntry.COLUMN_NAME_ROUTE_ID, r.getRouteId());
	}

	protected String[] columns() {
		return columns;
	}

	protected Trip extractValue(Cursor c) {
		String routeId = getStringByColumn(c, TripEntry.COLUMN_NAME_ROUTE_ID);
		String serviceId = getStringByColumn(c, TripEntry.COLUMN_NAME_SERVICE_ID);
		String tripId = getStringByColumn(c, TripEntry.COLUMN_NAME_TRIP_ID);
		String directionId = getStringByColumn(c, TripEntry.COLUMN_NAME_DIRECTION_ID);
		String headsign = getStringByColumn(c, TripEntry.COLUMN_NAME_TRIP_HEADSIGN);
		String blockId = getStringByColumn(c, TripEntry.COLUMN_NAME_BLOCK_ID);
		String shapeId = getStringByColumn(c, TripEntry.COLUMN_NAME_SHAPE_ID);
		return new Trip(routeId, serviceId, tripId, directionId, headsign, blockId, shapeId);
	}

	protected Trip extractValue2(Cursor c) {
		String[] columns = columns();
		String[] values = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			values[i] = getStringByColumn(c, columns[i]);
		}
		return new Trip(columns);
	}

	protected String getTableName() {
		return TripEntry.TABLE_NAME;
	}

	protected ContentValues getContentValues(Trip v) {
		ContentValues cv = new ContentValues();
		cv.put(TripEntry.COLUMN_NAME_ROUTE_ID, v.tripId);
		cv.put(TripEntry.COLUMN_NAME_SERVICE_ID, v.serviceId);
		cv.put(TripEntry.COLUMN_NAME_TRIP_ID, v.tripId);
		cv.put(TripEntry.COLUMN_NAME_TRIP_HEADSIGN, v.headsign);
		cv.put(TripEntry.COLUMN_NAME_DIRECTION_ID, v.direction);
		cv.put(TripEntry.COLUMN_NAME_BLOCK_ID, v.blockId);
		cv.put(TripEntry.COLUMN_NAME_SHAPE_ID, v.shapeId);
		return cv;
	}

	/*public static SqlContract getContract() {
		return new SqlContract() {
			private static final String TEXT_TYPE = " TEXT";
			private static final String COMMA_SEP = ",";

			public String getSqlCreateTable() {
				String columnList = "";
				for (int i = 0; i < columns.length; i++) {
					String c = columns[i] + TEXT_TYPE;
					if (i != columns.length - 1)
						c += COMMA_SEP;
					columnList += c;
				}

				return String.format(Locale.US, "CREATE TABLE %s (%s INTEGER PRIMARY KEY,%s )", TripEntry.TABLE_NAME,
						TripEntry._ID, columnList);
			}

			public String getSqlDeleteTable() {
				return "DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME;
			}
		};
	}*/

	public static ScheduleEngineContract getContract2() {
		return new ScheduleEngineContract() {
			public String getTableName() {
				return TripEntry.TABLE_NAME;
			}

			public String getID() {
				return TripEntry._ID;
			}

			public String[] getColumns() {
				return columns;
			}

		};
	}

	//CONTRACT:

	private static String[] columns = new String[] { TripEntry.COLUMN_NAME_ROUTE_ID,
			TripEntry.COLUMN_NAME_SERVICE_ID,
			TripEntry.COLUMN_NAME_TRIP_ID,
			TripEntry.COLUMN_NAME_TRIP_HEADSIGN,
			TripEntry.COLUMN_NAME_DIRECTION_ID,
			TripEntry.COLUMN_NAME_BLOCK_ID,
			TripEntry.COLUMN_NAME_SHAPE_ID };

	/* Inner class that defines the table contents */
	private static abstract class TripEntry implements BaseColumns {
		private static final String TABLE_NAME = "trip";
		private static final String COLUMN_NAME_ROUTE_ID = "routeid";
		private static final String COLUMN_NAME_SERVICE_ID = "serviceid";
		private static final String COLUMN_NAME_TRIP_ID = "tripid";
		private static final String COLUMN_NAME_TRIP_HEADSIGN = "tripheadsign";
		private static final String COLUMN_NAME_DIRECTION_ID = "directionid";
		private static final String COLUMN_NAME_BLOCK_ID = "blockid";
		private static final String COLUMN_NAME_SHAPE_ID = "shapeid";
	}

	/*"CREATE TABLE " + TripEntry.TABLE_NAME + "  (" +
			TripEntry._ID + " INTEGER PRIMARY KEY," +
			TripEntry.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP +
			TripEntry.COLUMN_NAME_SERVICE_ID + TEXT_TYPE + COMMA_SEP +
			TripEntry.COLUMN_NAME_TRIP_ID + TEXT_TYPE + COMMA_SEP +
			TripEntry.COLUMN_NAME_TRIP_HEADSIGN + TEXT_TYPE + COMMA_SEP +
			TripEntry.COLUMN_NAME_DIRECTION_ID + TEXT_TYPE + COMMA_SEP +
			TripEntry.COLUMN_NAME_BLOCK_ID + TEXT_TYPE + COMMA_SEP +
			TripEntry.COLUMN_NAME_SHAPE_ID + TEXT_TYPE + " )";*/

}

/*final class TripsReaderContract implements SqlContract {
	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public TripsReaderContract() {}

	// Inner class that defines the table contents
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
}*/