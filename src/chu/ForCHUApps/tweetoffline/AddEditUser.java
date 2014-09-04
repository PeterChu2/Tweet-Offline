// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package chu.ForCHUApps.tweetoffline;

import chu.ForCHUApps.tweetoffline.ConfirmDialogFragment.YesNoListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddEditUser extends Activity implements YesNoListener
{
	private long rowID; // id of contact being edited, if any

	// EditTexts for contact information
	private EditText nameEditText;
	private EditText usernameEditText;
	private int section;
	private String DATABASE_NAME;
	private String username;
	private SMSHelper smsHelper;
	private IntentFilter intentFilter;
	private SMSReceiver smsReceiver;

	// called when the Activity is first started
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{

		super.onCreate(savedInstanceState); // call super's onCreate
		setContentView(R.layout.add_user); // inflate the UI

		nameEditText = (EditText) findViewById(R.id.nameEditText);
		usernameEditText = (EditText) findViewById(R.id.usernameEditText);

		smsHelper = new SMSHelper(this);
//		smsReceiver = new SMSReceiver(DATABASE_NAME, null);

		Bundle extras = getIntent().getExtras(); // get Bundle of extras
		section = extras.getInt("section");

		// if there are extras, use them to populate the EditTexts
		if (extras != null)
		{
			rowID = extras.getLong("row_id");
			nameEditText.setText(extras.getString("name"));  
			usernameEditText.setText(extras.getString("username"));
		} // end if

		// set event listener for the Save Contact Button
		Button saveContactButton = 
				(Button) findViewById(R.id.saveButton);
		saveContactButton.setOnClickListener(saveButtonClicked);
		
		intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		// For Android versions <= 4.3. This will allow the app to stop the broadcast to the default SMS app
		intentFilter.setPriority(999);
		
//		registerReceiver(smsReceiver, intentFilter);
		
		
	} // end method onCreate

	// responds to event generated when user clicks the Done Button
	OnClickListener saveButtonClicked = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			username = usernameEditText.getText().toString();
			if(username.charAt(0) != '@')
			{
				username = "@" + username;
			}
			if ((username.length() != 0)&&!(containsWhitespace(username)))
			{
				if(section == 1)
				{
					DATABASE_NAME = "Following";
					ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Follow " + usernameEditText.getText().toString() +"?", false, 0);
					confirmDialog.show(getFragmentManager(), "follow");
				}
				else
				{
					// save the contact to the database using a separate thread
					(new saveContactTask()).execute((Object[]) null); 
				}

			} // end if
			else
			{
				// create a new AlertDialog Builder
				AlertDialog.Builder builder = 
						new AlertDialog.Builder(AddEditUser.this);

				// set dialog title & message, and provide Button to dismiss
				builder.setTitle(R.string.errorTitle); 
				builder.setMessage(R.string.errorMessage);
				builder.setPositiveButton(R.string.errorButton, null); 
				builder.show(); // display the Dialog
			} // end else
		} // end method onClick
	}; // end OnClickListener saveContactButtonClicked

	// saves contact information to the database
	private void save() 
	{
		// get DatabaseConnector to interact with the SQLite database
		if(section == 1)
		{
			DATABASE_NAME = "Following";
		}
		else if(section == 2)
		{
			DATABASE_NAME = "Followers";
		}
		else if(section == 3)
		{
			DATABASE_NAME = "Custom";
		}


		DatabaseConnector databaseConnector = new DatabaseConnector(this, DATABASE_NAME);

		// insert the contact information into the database
		// initially latest tweet and bio are empty strings
		databaseConnector.insertRecord(
				username,
				nameEditText.getText().toString(),
				"", "");

	} // end class saveContact

	public static boolean containsWhitespace(String str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	public static boolean hasLength(String str) {
		return (str != null && str.length() > 0);
	}

	@Override
	public void onYes(ConfirmDialogFragment confirmDialogFragment) {
		// Only dialog in this activity using SMS is adding to following list
		smsHelper.sendSMS("FOLLOW " + usernameEditText.getText().toString());
		(new saveContactTask()).execute((Object[]) null); 
	}

	@Override
	public void onNo() {
		// TODO Auto-generated method stub

	}

	private class saveContactTask extends AsyncTask<Object, Object, Object>
	{
		@Override
		protected Object doInBackground(Object... params) 
		{
			save(); // save contact to the database
			return null;
		} // end method doInBackground

		@Override
		protected void onPostExecute(Object result) 
		{
			setResult(Activity.RESULT_OK);
			finish(); // return to the previous Activity
		} // end method onPostExecute
	}

} // end class AddEditContact
