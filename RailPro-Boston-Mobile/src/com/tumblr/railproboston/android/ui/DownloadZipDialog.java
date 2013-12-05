package com.tumblr.railproboston.android.ui;

import com.tumblr.railproboston.android.MainActivityA;
import com.tumblr.railproboston.android.ui.DownloadZipDialogFragment.DownloadZipDialogListener;

import jscholl.commuterrail.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class DownloadZipDialog implements DownloadZipDialogListener {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
	
	private DownloadZipDialogFragment fragment;
	
	private Boolean b = null;
	private Object lock = new Object();

	@Override
	public void onConfirm() {
		Log.i(CLASSNAME, "User confirmed download request");
		b = Boolean.TRUE;
		synchronized(lock) {
			lock.notifyAll();
		}
	}

	@Override
	public void onDeny() {
		Log.i(CLASSNAME, "User denied download request");
		b = Boolean.FALSE;
		synchronized(lock) {
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

class DownloadZipDialogFragment extends DialogFragment {
	private static final String CLASSNAME = new Object(){}.getClass().getEnclosingClass().getSimpleName();
	
	public interface DownloadZipDialogListener {
		public void onConfirm();
		public void onDeny();
	}
	
	private DownloadZipDialogListener listener;
	
	public void setListener(DownloadZipDialogListener listener) {
		this.listener = listener;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_download_zip)
               .setPositiveButton(R.string.download_confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       listener.onConfirm();
                   }
               })
               .setNegativeButton(R.string.download_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       listener.onDeny();
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}