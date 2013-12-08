package com.tumblr.railproboston.android.ui;

import com.tumblr.railproboston.android.engine.types.StopTime;

public interface OnPredictionFoundListener {
	public void onPredictionFound(StopTime prediction);
}