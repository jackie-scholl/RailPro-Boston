package com.tumblr.railproboston.android.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.tumblr.railproboston.android.ui.DownloadZipDialog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
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

	@SuppressWarnings("deprecation")
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

	private synchronized SQLiteDatabase getPopulatedReadableDatabase() {
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
			BufferedReader br = getReader(getFileName());
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

	private File getGtfs() {
		File x = new File(ctx.getCacheDir(), "MBTA_GTFS");
		x.mkdirs();
		if (!x.isDirectory())
			Log.w(ScheduleEngine.CLASSNAME, "Unable to create gtfs directory: " + x);
		return x;
	}

	private File gtfsFile() {
		return new File(getGtfs(), "MBTA_GTFS.zip");
	}

	private ZipFile getGtfsZip() {
		Log.i(ScheduleEngine.CLASSNAME,
				"Is external storage writeable? " + BaseScheduleEngine.isExternalStorageWritable());
		if (ScheduleEngine.gtfs == null) {
			if (!gtfsFile().exists()) {
				boolean b = downloadFile("https://sites.google.com/site/rs0site/MBTA_GTFS-CR.zip?attredirects=0&d=1",
						gtfsFile());
				if (!b) {
					Log.w(ScheduleEngine.CLASSNAME, "Failed to download MBTA GTFS file");
					return null;
				}
			} else {
				Log.i(ScheduleEngine.CLASSNAME, "GTFS file already exists");
			}

			try {
				ScheduleEngine.gtfs = new ZipFile(gtfsFile());
			} catch (IOException e) {
				Log.w(ScheduleEngine.CLASSNAME, "Failed to initiate zip file: ", e);
			}
		}

		Log.d(ScheduleEngine.CLASSNAME, "gtfs = " + ScheduleEngine.gtfs);
		return ScheduleEngine.gtfs;
	}

	private boolean downloadFile(String sUrl, File downloadLoc) {
		Log.i(ScheduleEngine.CLASSNAME, "Trying to download MBTA GTFS file");
		Log.d(ScheduleEngine.CLASSNAME,
				"Is external storage writeable? " + BaseScheduleEngine.isExternalStorageWritable());

		//new DownloadZipDialogFragment().show(MainActivity.getInstance().getSupportFragmentManager(), "DownloadZipDialog");
		boolean b = true;
		/*boolean b = new DownloadZipDialog().call();
		if (!b) {
			Log.w(ScheduleEngine.CLASSNAME, "User denied file download");
			return false;
		}*/

		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(sUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.w(ScheduleEngine.CLASSNAME, "Bad response code " + connection.getResponseCode());
				return false;
			}

			// download the file
			input = connection.getInputStream();
			downloadLoc.createNewFile();
			output = new FileOutputStream(downloadLoc);

			byte data[] = new byte[4096];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
		} catch (IOException e) {
			Log.e(ScheduleEngine.CLASSNAME, "Failed to download MBTA GTFS file because of exception ", e);
			b = false;
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {}

			if (connection != null)
				connection.disconnect();
		}

		return b;
	}

	private BufferedReader getReader(String name) {
		return new BufferedReader(new InputStreamReader(getZipStream("MBTA_GTFS-CR/" + name)));
	}

	private InputStream getZipStream(String name) {
		try {
			Log.d(ScheduleEngine.CLASSNAME, "Attempting to open zip stream for name " + name);
			ZipFile gtfsZip = getGtfsZip();
			ZipEntry entry = gtfsZip.getEntry(name);
			return gtfsZip.getInputStream(entry);
		} catch (IOException e) {
			Log.e(ScheduleEngine.CLASSNAME, "Can't open zip stream " + name, e);
		}
		return null;
	}

	private static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
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