package com.tumblr.railproboston.android.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Build;
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

	public List<V> getAll(List<K> keys) {
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

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			for (K k : keys)
				vals.addAll(get(k));
			return vals;
		}

		SQLiteDatabase db = getPopulatedReadableDatabase();
		String query = this.getUnionQuery(keys);
		Log.d(CLASSNAME, String.format("Query is: %s", query));
		Log.i(CLASSNAME, "About to make query");
		Cursor c = db.rawQuery(query, null);
		Log.i(CLASSNAME, "About to process query results");
		c.moveToFirst();
		Log.d(CLASSNAME, "There were this many results: " + c.getCount());

		while (!c.isAfterLast()) {
			vals.add(extractValue(c));
			c.moveToNext();
		}
		db.close();

		Log.d(CLASSNAME, "Done processing query results");
		return vals;
	}

	public List<V> get(K key) {
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private String getUnionQuery(List<K> keys) {
		SQLiteQueryBuilder b = new SQLiteQueryBuilder();
		b.setTables(getTableName());
		String[] subQueries = new String[keys.size()];
		for (int i = 0; i < keys.size(); i++)
			subQueries[i] = getUnionSubQuery(keys.get(i));
		return b.buildUnionQuery(subQueries, unionQuerySortOrder(), null);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected String getUnionSubQuery(K k) {
		//String selection = StopTimeEntry.COLUMN_NAME_TRIP_ID + "=" + "\"" + t.getTripId() + "\""; // SQL where clause
		String selection = selection(k);
		String[] columns = columns();
		Set<String> set = new HashSet<String>();
		set.addAll(Arrays.asList(columns));
		SQLiteQueryBuilder b = new SQLiteQueryBuilder();
		b.setTables(getTableName());
		return b.buildUnionSubQuery("typeDiscriminator", columns, set, 0, "typeDiscriminator",
				selection, null, null);
	}

	//protected abstract String getUnionSubQuery(K key);

	protected abstract String selection(K key);

	protected abstract String[] selectionArgs(K key);

	protected abstract String[] columns();

	//protected abstract String getUnionQuery(List<K> keys);

	protected abstract String getPluralName();

	protected abstract String getFileName();

	protected abstract V getValue(String line);

	protected abstract ContentValues getContentValues(V v);

	protected abstract String getTableName();

	protected abstract String sortOrder();

	protected abstract String unionQuerySortOrder();

	protected abstract V extractValue(Cursor c);
}