package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

public class ConfirmDialogFragment extends DialogFragment{

	private static InputFilter[] filterArray = new InputFilter[1];
	// Each text message is limited to 160 characters
	private static int MAX_SMS_MESSAGE_LENGTH = 160;
	private static int usedChars = 0;

	private EditText contents;
	private TextView characterCount;
	
	private final TextWatcher mTextEditorWatcher = new TextWatcher() {
		
		@Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        	// Set the count to the number of characters the user is still able to enter
           characterCount.setText(String.valueOf(160 - usedChars - s.length()));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
	};

	public interface YesNoListener {
		void onYes(ConfirmDialogFragment confirmDialogFragment);
		void onNo();
	}

	public static ConfirmDialogFragment newInstance(String message, boolean requiredInput, int usedCharacters) {
		ConfirmDialogFragment frag = new ConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putString("message", message);
		args.putBoolean("requiredInput", requiredInput);
		frag.setArguments(args);
		// Set max length on the message the user can enter
		filterArray[0] = new InputFilter.LengthFilter(MAX_SMS_MESSAGE_LENGTH - usedCharacters);
		usedChars = usedCharacters;
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
			// Set up layout for input box and set the alert dialog builder to the view
			
			LayoutInflater i = getActivity().getLayoutInflater();
			View view = i.inflate(R.layout.dialog_input, null);
			contents = (EditText) view.findViewById(R.id.dialogInput);
			contents.setFilters(filterArray);
			characterCount = (TextView) view.findViewById(R.id.characterCount);
			characterCount.setText(String.valueOf(160 - usedChars));
			contents.addTextChangedListener(mTextEditorWatcher);
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
		// Show keyboard for user input immediately
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}

}
