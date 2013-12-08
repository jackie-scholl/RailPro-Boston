package com.tumblr.railproboston.android.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.tumblr.railproboston.android.engine.RoutesReaderContract.RouteEntry;
import com.tumblr.railproboston.android.engine.types.Route;

public class RoutesEngine extends BaseNoKeyScheduleEngine<Route> {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	public RoutesEngine(Context ctx) {
		super(ctx);
	}

	protected ContentValues getContentValues(Route x) {
		ContentValues values = new ContentValues();
		values.put(RouteEntry.COLUMN_NAME_ROUTE_ID, x.routeId);
		values.put(RouteEntry.COLUMN_NAME_SHORT_NAME, x.shortName);
		values.put(RouteEntry.COLUMN_NAME_LONG_NAME, x.longName);
		return values;
	}

	protected String getTableName() {
		return RouteEntry.TABLE_NAME;
	}

	protected String sortBy() {
		return RouteEntry.COLUMN_NAME_ROUTE_ID;
	}

	public static SqlContract getContract() {
		return new RoutesReaderContract();
	}

	@Override
	protected String[] columns() {
		return new String[] { RouteEntry.COLUMN_NAME_ROUTE_ID, RouteEntry.COLUMN_NAME_SHORT_NAME,
				RouteEntry.COLUMN_NAME_SHORT_NAME };
	}

	@Override
	protected String getPluralName() {
		return "routes";
	}

	@Override
	protected String getFileName() {
		return ScheduleEngine.ROUTES;
	}

	@Override
	protected Route getValue(String line) {
		String[] rowData = line.split(",");
		if (rowData[0].substring(1, 3).equals("CR"))
			return new Route(rowData);
		else
			return null;
	}

	@Override
	protected Route extractValue(Cursor c) {
		String routeId = getStringByColumn(c, RouteEntry.COLUMN_NAME_ROUTE_ID);
		String shortName = getStringByColumn(c, RouteEntry.COLUMN_NAME_SHORT_NAME);
		String longName = getStringByColumn(c, RouteEntry.COLUMN_NAME_LONG_NAME);
		return new Route(routeId, shortName, longName);
	}
}

final class RoutesReaderContract implements SqlContract {
	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public RoutesReaderContract() {}

	/* Inner class that defines the table contents */
	public static abstract class RouteEntry implements BaseColumns {
		public static final String TABLE_NAME = "route";
		public static final String COLUMN_NAME_ROUTE_ID = "routeid";
		public static final String COLUMN_NAME_SHORT_NAME = "shortName";
		public static final String COLUMN_NAME_LONG_NAME = "longName";
	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + RouteEntry.TABLE_NAME + "  (" +
			RouteEntry._ID + " INTEGER PRIMARY KEY," +
			RouteEntry.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP +
			RouteEntry.COLUMN_NAME_SHORT_NAME + TEXT_TYPE + COMMA_SEP +
			RouteEntry.COLUMN_NAME_LONG_NAME + TEXT_TYPE + " )";

	static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME;

	public String getSqlCreateTable() {
		return SQL_CREATE_ENTRIES;
	}

	public String getSqlDeleteTable() {
		return SQL_DELETE_ENTRIES;
	}
}
