// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddEditUser extends Activity 
{
	private long rowID; // id of contact being edited, if any

	// EditTexts for contact information
	private EditText nameEditText;
	private EditText usernameEditText;
	private int section;
	private String DATABASE_NAME;
	private String username;



	// called when the Activity is first started
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		
		super.onCreate(savedInstanceState); // call super's onCreate
		setContentView(R.layout.add_user); // inflate the UI

		nameEditText = (EditText) findViewById(R.id.nameEditText);
		usernameEditText = (EditText) findViewById(R.id.usernameEditText);

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
	} // end method onCreate

	// responds to event generated when user clicks the Done Button
	OnClickListener saveButtonClicked = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			username = usernameEditText.getText().toString();
			if ((username.length() != 0)&&(username.charAt(0)== '@')&&!(containsWhitespace(username)))
			{


				AsyncTask<Object, Object, Object> saveContactTask = 
						new AsyncTask<Object, Object, Object>() 
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
						}; // end AsyncTask

						// save the contact to the database using a separate thread
						saveContactTask.execute((Object[]) null); 

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
				usernameEditText.getText().toString(),
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

	
} // end class AddEditContact
