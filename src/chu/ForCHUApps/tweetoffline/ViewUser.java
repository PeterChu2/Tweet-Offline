//// Activity for viewing a single contact.
package chu.ForCHUApps.tweetoffline;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ViewUser extends Activity implements OnClickListener
{
	//   private static final String TAG = ViewUser.class.getName();
	//   
	//   // Intent request code used to start an Activity that returns a result
	//   private static final int REQUEST_CONNECT_DEVICE = 1;
	//
	//   private BluetoothAdapter bluetoothAdapter = null; // Bluetooth adapter
	private Handler handler; // for displaying Toasts in the GUI thread

	private long rowID; // selected contact's row ID in the database
	private TextView nameTextView; // displays contact's name 
	private TextView usernameTextView; // displays contact's phone
	private TextView latestTweetTextView;
	private TextView bioTextView;
	private Button retweetButton;
	private Button replyButton;
	private Button dmButton;
	private Button favoriteButton;
	private Button fetchButton;
	private Button whoButton;

	private int section;
	private String DATABASE_NAME;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user); // inflate GUI

		// get the TextViews
		nameTextView = (TextView) findViewById(R.id.nameTextView);
		usernameTextView = (TextView) findViewById(R.id.usernameTextView);
		latestTweetTextView = (TextView) findViewById(R.id.latestTweetTextView);
		bioTextView = (TextView) findViewById(R.id.bioTextView);

		//get the Buttons
		retweetButton = (Button) findViewById(R.id.retweetButton);
		replyButton = (Button) findViewById(R.id.replyButton);
		dmButton = (Button) findViewById(R.id.dmButton);
		favoriteButton = (Button) findViewById(R.id.favoriteButton);
		fetchButton = (Button) findViewById(R.id.fetchButton);
		whoButton = (Button) findViewById(R.id.whoButton);
		retweetButton.setOnClickListener(this);
		replyButton.setOnClickListener(this);
		dmButton.setOnClickListener(this);
		favoriteButton.setOnClickListener(this);
		fetchButton.setOnClickListener(this);
		whoButton.setOnClickListener(this);


		// get the selected contact's row ID
		Bundle extras = getIntent().getExtras();
		rowID = extras.getLong(MainActivity.ROW_ID); 
		section = extras.getInt("section");

		if( section == 1 )
		{
			DATABASE_NAME = "Following";
		}
		if( section == 2 )
		{
			DATABASE_NAME = "Followers";
		}
		if( section == 3 )
		{
			DATABASE_NAME = "Custom";
		}
		//      
		handler = new Handler(); // create the Handler
	} // end method onCreate
	//
	// called when the activity is first created
	@Override
	protected void onResume()
	{
		super.onResume();

		// create new LoadContactTask and execute it 
		new LoadContactTask().execute(rowID);
	} // end method onResume
	//   
	// performs database query outside GUI thread
	private class LoadContactTask extends AsyncTask<Long, Object, Cursor> 
	{

		DatabaseConnector databaseConnector = 
				new DatabaseConnector(ViewUser.this, DATABASE_NAME);

		// perform the database access
		@Override
		protected Cursor doInBackground(Long... params)
		{
			databaseConnector.open();

			//          get a cursor containing all data on given entry
			return databaseConnector.getOneRecord(params[0]);
		} // end method doInBackground

		// use the Cursor returned from the doInBackground method
		@Override
		protected void onPostExecute(Cursor result)
		{
			super.onPostExecute(result);

			result.moveToFirst(); // move to the first item 

			// get the column index for each data item
			int nameIndex = result.getColumnIndex("name");
			int usernameIndex = result.getColumnIndex("username");

			// fill TextViews with the retrieved data
			nameTextView.setText(result.getString(nameIndex));
			usernameTextView.setText(result.getString(usernameIndex));
			//         emailTextView.setText(result.getString(emailIndex));
			//         streetTextView.setText(result.getString(streetIndex));
			//         cityTextView.setText(result.getString(cityIndex));

			result.close(); // close the result cursor
			databaseConnector.close(); // close database connection
		} // end method onPostExecute
	} // end class LoadContactTask
	//      
	// create the Activity's menu from a menu resource XML file
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_user_menu, menu);
		return true;
	} // end method onCreateOptionsMenu

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) // switch based on selected MenuItem's ID
		{
		case R.id.unfollow:
			break;
		case R.id.unsubscribe:
			break;
		case R.id.subscribe:
			break;
		} // end switch

		return super.onOptionsItemSelected(item);
	} // end method onOptionsItemSelected

	// delete a contact
	private void deleteContact()
	{
		// create a new AlertDialog Builder
		AlertDialog.Builder builder = 
				new AlertDialog.Builder(ViewUser.this);

		builder.setTitle(R.string.confirmTitle); // title bar string
		builder.setMessage(R.string.confirmMessage); // message to display

		// provide an OK button that simply dismisses the dialog
		builder.setPositiveButton(R.string.button_delete,
				new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int button)
			{
				final DatabaseConnector databaseConnector = 
						new DatabaseConnector(ViewUser.this, DATABASE_NAME);

				// create an AsyncTask that deletes the contact in another 
				// thread, then calls finish after the deletion
				AsyncTask<Long, Object, Object> deleteTask =
						new AsyncTask<Long, Object, Object>()
						{
					@Override
					protected Object doInBackground(Long... params)
					{
						databaseConnector.deleteRecord(params[0]); 
						return null;
					} // end method doInBackground

					@Override
					protected void onPostExecute(Object result)
					{
						finish(); // return to the AddressBook Activity
					} // end method onPostExecute
						}; // end new AsyncTask

						// execute the AsyncTask to delete contact at rowID
						deleteTask.execute(new Long[] { rowID });               
			} // end method onClick
		} // end anonymous inner class
				); // end call to method setPositiveButton

		builder.setNegativeButton(R.string.button_cancel, null);
		builder.show(); // display the Dialog
	} // end method deleteContact

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.retweetButton:
			break;
		case R.id.replyButton:
			break;
		case R.id.dmButton:
			break;
		case R.id.favoriteButton:
			break;
		case R.id.whoButton:
			break;
		case R.id.fetchButton:
			break;
		}
	}
}

