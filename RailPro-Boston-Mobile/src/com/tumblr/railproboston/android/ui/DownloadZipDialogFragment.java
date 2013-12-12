package com.tumblr.railproboston.android.ui;

import jscholl.commuterrail.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DownloadZipDialogFragment extends DialogFragment {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

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
				.setPositiveButton(R.string.download_confirm,
						new DialogInterface.OnClickListener() {
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