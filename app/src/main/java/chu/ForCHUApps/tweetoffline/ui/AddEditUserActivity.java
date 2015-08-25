// AddEditContact.java
// Activity for adding a new entry to or  
// editing an existing entry in the address book.
package chu.ForCHUApps.tweetoffline.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import chu.ForCHUApps.tweetoffline.R;
import chu.ForCHUApps.tweetoffline.db.DatabaseConnector;
import chu.ForCHUApps.tweetoffline.db.DatabaseOpenHelper;
import chu.ForCHUApps.tweetoffline.models.User;
import chu.ForCHUApps.tweetoffline.sms.SMSHelper;
import chu.ForCHUApps.tweetoffline.sms.SMSReceiver;
import chu.ForCHUApps.tweetoffline.ui.ConfirmDialogFragment.ConfirmationListener;

public class AddEditUserActivity extends Activity implements ConfirmationListener {
    // EditTexts for contact information
    private EditText nameEditText;
    private EditText usernameEditText;
    private int section;
    private DatabaseOpenHelper.DatabaseName mDatabaseName;
    private String mUsername;
    private SMSHelper mSmsHelper;
    private IntentFilter mIntentFilter;
    private SMSReceiver mSmsReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user);

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);

        mSmsHelper = new SMSHelper(this);

        Bundle extras = getIntent().getExtras(); // get Bundle of extras

        // if there are extras, use them to populate the EditTexts
        if (extras != null) {
            section = extras.getInt("section");
            nameEditText.setText(extras.getString("name"));
            usernameEditText.setText(extras.getString("username"));
        }

        //Determine what list the activity is using
        if (section == 1) {
            mDatabaseName = DatabaseOpenHelper.DatabaseName.FOLLOWING;
        } else if (section == 2) {
            mDatabaseName = DatabaseOpenHelper.DatabaseName.FOLLOWERS;
        } else if (section == 3) {
            mDatabaseName = DatabaseOpenHelper.DatabaseName.CUSTOM;
        }

        // set event listener for the Save Contact Button
        Button saveContactButton =
                (Button) findViewById(R.id.saveButton);
        saveContactButton.setOnClickListener(saveButtonClicked);

        mIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        // For Android versions <= 4.3. This will allow the app to stop the broadcast to the default SMS app
        mIntentFilter.setPriority(999);

        mSmsReceiver = new SMSReceiver(mDatabaseName, null);

    } // end method onCreate

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mSmsReceiver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mSmsReceiver);
    }

    // responds to event generated when user clicks the Done Button
    OnClickListener saveButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mUsername = usernameEditText.getText().toString();
            if (mUsername.charAt(0) != '@') {
                mUsername = "@" + mUsername;
            }
            if ((mUsername.length() != 0) && !(containsWhitespace(mUsername))) {
                if (section == 1) {
                    mDatabaseName = DatabaseOpenHelper.DatabaseName.FOLLOWING;
                    ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                            .newInstance("Follow " + usernameEditText.getText().toString() + "?", false, 0);
                    confirmDialog.show(getFragmentManager(), "follow");
                } else {
                    // save the contact to the database using a separate thread
                    (new saveContactTask()).execute((Object[]) null);
                }

            } // end if
            else {
                // create a new AlertDialog Builder
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(AddEditUserActivity.this);

                // set dialog title & message, and provide Button to dismiss
                builder.setTitle(R.string.errorTitle);
                builder.setMessage(R.string.errorMessage);
                builder.setPositiveButton(R.string.errorButton, null);
                builder.show(); // display the Dialog
            } // end else
        } // end method onClick
    }; // end OnClickListener saveContactButtonClicked

    // saves contact information to the database
    private void save() {
        // get DatabaseConnector to interact with the SQLite database
        DatabaseConnector databaseConnector = new DatabaseConnector(this, mDatabaseName);

        // insert the contact information into the database
        // initially latest tweet and bio are empty strings
        databaseConnector.insertRecord(new User(mUsername,  nameEditText.getText().toString()));

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
    public void onConfirm(ConfirmDialogFragment confirmDialogFragment) {
        // Only dialog in this activity using SMS is adding to following list
        mSmsHelper.sendSMS(String.format("FOLLOW %s", usernameEditText.getText().toString()));
        (new saveContactTask()).execute((Object[]) null);
    }

    private class saveContactTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            save(); // save contact to the database
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            setResult(Activity.RESULT_OK);
            finish(); // return to the previous Activity
        }
    }
}