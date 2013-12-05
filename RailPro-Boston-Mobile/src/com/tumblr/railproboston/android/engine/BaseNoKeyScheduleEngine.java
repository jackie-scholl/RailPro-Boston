package com.tumblr.railproboston.android.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public abstract class BaseNoKeyScheduleEngine<V> extends
		BaseScheduleEngine<Object, V> {

	public BaseNoKeyScheduleEngine(Context ctx) {
		super(ctx);
	}

	@Override
	protected String selection(Object key) {
		return null;
	}

	@Override
	protected String[] selectionArgs(Object key) {
		return null;
	}

	@Override
	protected String sortOrder() {
		return null;
	}

	@Override
	protected String unionQuerySortOrder() {
		return null;
	}
}
