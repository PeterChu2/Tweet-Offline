package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ConfirmDialogFragment extends DialogFragment{

	private static InputFilter[] filterArray = new InputFilter[1];
	private static int MAX_SMS_MESSAGE_LENGTH = 160;

	private EditText contents;

	public interface YesNoListener {
		void onYes(ConfirmDialogFragment confirmDialogFragment);
		void onNo();
	}

	public static ConfirmDialogFragment newInstance(String message, boolean requiredInput) {
		ConfirmDialogFragment frag = new ConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putString("message", message);
		args.putBoolean("requiredInput", requiredInput);
		frag.setArguments(args);
		filterArray[0] = new InputFilter.LengthFilter(MAX_SMS_MESSAGE_LENGTH);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof YesNoListener)) {
			throw new ClassCastException(activity.toString() + " must implement YesNoListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final String message = getArguments().getString("message");
		final Boolean requiresInput = getArguments().getBoolean("requiredInput");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.confirmTitle);
		builder.setMessage(message);
		if(requiresInput)
		{
			LayoutInflater i = getActivity().getLayoutInflater();
			View view = i.inflate(R.layout.dialog_input, null);
			contents = (EditText) view.findViewById(R.id.dialogInput);
			contents.setFilters(filterArray);
			builder.setView(contents);
			builder.setView(view);
			contents.requestFocus();
		}

		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((YesNoListener) getActivity()).onYes(ConfirmDialogFragment.this);
			}
		});
		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
				((YesNoListener) getActivity()).onNo();
			}
		});
		Dialog dialog= builder.create();
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

}
