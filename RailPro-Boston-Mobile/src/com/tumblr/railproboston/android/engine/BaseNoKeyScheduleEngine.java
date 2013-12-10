package com.tumblr.railproboston.android.engine;

import android.content.Context;

public abstract class BaseNoKeyScheduleEngine<V> extends
		BaseScheduleEngine<String, V> {

	public BaseNoKeyScheduleEngine(Context ctx) {
		super(ctx);
	}

	@Override
	protected String selection(String key) {
		return null;
	}

	@Override
	protected String sortOrder() {
		return null;
	}
}
