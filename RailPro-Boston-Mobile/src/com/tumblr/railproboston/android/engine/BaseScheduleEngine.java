package com.tumblr.railproboston.android.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public abstract class BaseScheduleEngine<K, V> {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	protected static String getStringByColumn(Cursor c, String column) {
		return c.getString(c.getColumnIndexOrThrow(column));
	}

	protected Context ctx;
	protected Map<K, List<V>> cache;

	private String name;

	public BaseScheduleEngine(Context ctx) {
		this.ctx = ctx;
		cache = null;
		name = getPluralName();
	}

	public void prepareDatabase() {
		getPopulatedReadableDatabase().close();
	}

	public List<V> getAll() {
		Log.d(CLASSNAME, String.format("About to get %s from database", name));
		String query = SQLiteQueryBuilder.buildQueryString(false, getTableName(), columns(), null,
				null, null, sortOrder(), null);
		return dbRawQuery(query);
	}

	public List<V> getList(List<K> keys) {
		Log.d(CLASSNAME, String.format("About to get %s from database", name));

		List<V> vals = new ArrayList<V>();

		// Try to use cache
		Set<K> cachedKeys = cache.keySet();
		Iterator<K> iterator = keys.iterator();
		while (iterator.hasNext()) {
			K k = iterator.next();
			if (cachedKeys.contains(k)) {
				vals.addAll(cache.get(k));
				iterator.remove();
			}
		}

		if (keys.size() == 0)
			return vals;

		String query = getUnionQuery(keys);

		vals.addAll(dbRawQuery(query));

		return vals;
	}

	public List<V> getList(K... keys) {
		return getList(Arrays.asList(keys));
	}

	@SuppressWarnings("unchecked")
	public List<V> get(K key) {
		return getList(key);
	}

	private List<V> dbRawQuery(String query) {
		List<V> vals = new ArrayList<V>();

		SQLiteDatabase db = getPopulatedReadableDatabase();
		Log.i(CLASSNAME, String.format("Query is: %s", query));
		Cursor c = db.rawQuery(query, null);
		c.moveToFirst();
		Log.i(CLASSNAME, "There were this many results: " + c.getCount());

		while (!c.isAfterLast()) {
			vals.add(extractValue(c));
			c.moveToNext();
		}
		db.close();

		Log.d(CLASSNAME, "Done processing query results");
		return vals;
	}

	private String getUnionQuery(List<K> keys) {
		SQLiteQueryBuilder b = new SQLiteQueryBuilder();
		b.setTables(this.getTableName());
		String[] subQueries = new String[keys.size()];
		for (int i = 0; i < keys.size(); i++)
			subQueries[i] = getUnionSubQuery(keys.get(i));
		return b.buildUnionQuery(subQueries, sortOrder(), null);
	}

	private String getUnionSubQuery(K k) {
		String selection = this.selection(k);
		String[] columns = this.columns();
		String sortOrder = this.sortOrder();
		SQLiteQueryBuilder b = new SQLiteQueryBuilder();
		b.setTables(getTableName());

		return b.buildQuery(columns, selection, null, null, null, sortOrder, null);
		//return b.buildUnionSubQuery("typeDiscriminator", columns, set, 0, "typeDiscriminator",
		//		selection, null, null);
	}

	public boolean buildDatabase() {
		Log.i(CLASSNAME, "Building database of " + name);
		List<V> values = getValuesFromFile();

		Log.i(CLASSNAME, String.format("Writing %s to database", name));
		SQLiteOpenHelper mDbHelper = ScheduleEngine.getDbHelper(ctx);
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		for (V v : values) {
			db.insert(getTableName(), null, getContentValues(v));
		}
		db.close();
		Log.i(CLASSNAME, String.format("Done writing %s to database", name));
		return true;
	}

	private SQLiteDatabase getPopulatedReadableDatabase() {
		SQLiteOpenHelper mDbHelper = ScheduleEngine.getDbHelper(ctx);

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		int count = db.query(getTableName(), null, null, null, null, null, null).getCount(); // Get number of rows in table
		if (count > 0)
			return db;
		db.close();
		buildDatabase();
		return mDbHelper.getReadableDatabase();
	}

	private List<V> getValuesFromFile() {
		List<V> stopTimes = new ArrayList<V>();

		try {
			BufferedReader br = ScheduleEngine.getReader(getFileName());
			br.readLine(); // Clears the line with the headers
			String line;
			while ((line = br.readLine()) != null) {
				V val = getValue(line);
				if (val != null)
					stopTimes.add(val);
			}
			br.close();
		} catch (IOException e) {
			Log.w(CLASSNAME, String.format("Failed reading %s file", name));
			return null;
		}

		return stopTimes;
	}

	protected static String equalsSelection(String columnName, String value) {
		return columnName + "=" + "\"" + value + "\"";
	}

	protected abstract String getPluralName();

	protected abstract String getFileName();

	protected abstract V getValue(String line);

	protected abstract ContentValues getContentValues(V v);

	protected abstract String selection(K key);

	protected String[] columns() {
		return null;
	}

	protected abstract String getTableName();

	protected abstract String sortOrder();

	protected abstract V extractValue(Cursor c);
}

/*public List<V> get(K key) {
Log.d(CLASSNAME, String.format("About to get %s", name));

List<V> values = new ArrayList<V>();
SQLiteDatabase db = getPopulatedReadableDatabase();

String sortOrder = this.sortOrder();
String selection = this.selection(key);
String[] selectionArgs = this.selectionArgs(key);

Log.d(CLASSNAME,
		String.format("Query is: %s with args %s", selection,
				Arrays.toString(selectionArgs)));
Log.i(CLASSNAME, "About to make query");

Cursor c = db.query(getTableName(), null, selection, selectionArgs, null, null, sortOrder);

Log.i(CLASSNAME, "About to process query results");
c.moveToFirst();
Log.d(CLASSNAME, "There were this many results: " + c.getCount());

while (!c.isAfterLast()) {
	values.add(extractValue(c));
	c.moveToNext();
}
db.close();
Log.d(CLASSNAME, "Done processing query results");
return values;
}*/