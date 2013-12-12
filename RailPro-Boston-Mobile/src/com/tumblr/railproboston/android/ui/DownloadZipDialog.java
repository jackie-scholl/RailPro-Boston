package com.tumblr.railproboston.android.ui;

import com.tumblr.railproboston.android.MainActivityA;
import com.tumblr.railproboston.android.ui.DownloadZipDialogFragment.DownloadZipDialogListener;

import android.util.Log;

public class DownloadZipDialog implements DownloadZipDialogListener {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	private DownloadZipDialogFragment fragment;

	private Boolean b = null;
	private Object lock = new Object();

	@Override
	public void onConfirm() {
		Log.i(CLASSNAME, "User confirmed download request");
		b = Boolean.TRUE;
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public void onDeny() {
		Log.i(CLASSNAME, "User denied download request");
		b = Boolean.FALSE;
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public DownloadZipDialog() {
		fragment = new DownloadZipDialogFragment();
		fragment.setListener(this);
		fragment.show(MainActivityA.getInstance().getSupportFragmentManager(), "DownloadZipDialog");
	}

	public boolean call() {
		synchronized (lock) {
			while (b == null) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					Log.e(CLASSNAME, "interrupted while waiting for dialog result", e);
				}
			}
		}
		return b;
	}
}