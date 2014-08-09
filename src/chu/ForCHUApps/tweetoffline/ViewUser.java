//// Activity for viewing a single contact.
package chu.ForCHUApps.tweetoffline;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
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
	private TextView usernameTextView; // displays contact's username
	private TextView recentTweetTextView; // displays user's latest tweet
	private TextView bioTextView; // display's user's bio
	private Button retweetButton;
	private Button replyButton;
	private Button dmButton;
	private Button favoriteButton;
	private Button fetchButton;
	private Button whoButton;


	private int section;
	private String DATABASE_NAME;
	private static int MAX_SMS_MESSAGE_LENGTH = 160;
	private static String twitterNumber = "21212";
	private static String SENT = "SMS_SENT";
	private static String DELIVERED = "SMS_DELIVERED";
	private SMSReceiver smsReceiver = new SMSReceiver(this);
	private String user;
	private IntentFilter intentFilter;


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user); // inflate GUI

		// get the TextViews
		nameTextView = (TextView) findViewById(R.id.nameTextView);
		usernameTextView = (TextView) findViewById(R.id.usernameTextView);
		recentTweetTextView = (TextView) findViewById(R.id.latestTweetTextView);
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

		intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(smsReceiver, intentFilter);

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

	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(smsReceiver);
	}

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
			int recentTweetIndex = result.getColumnIndex("recentTweet");
			int bioIndex = result.getColumnIndex("bio");

			// fill TextViews with the retrieved data
			nameTextView.setText(result.getString(nameIndex));
			usernameTextView.setText(result.getString(usernameIndex));
			recentTweetTextView.setText(result.getString(recentTweetIndex));
			bioTextView.setText(result.getString(bioIndex));
			//         cityTextView.setText(result.getString(cityIndex));
			user = result.getString(usernameIndex);
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
		if(DATABASE_NAME == "Followers")
		{
			inflater.inflate(R.menu.follower_menu, menu);
		}
		else if(DATABASE_NAME == "Following")
		{
			inflater.inflate(R.menu.following_menu, menu);
		}
		else
		{
			inflater.inflate(R.menu.custom_menu, menu);
		}
		return true;
	} // end method onCreateOptionsMenu

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) // switch based on selected MenuItem's ID
		{
		case R.id.unfollow:
			AlertDialog.Builder builder = new AlertDialog.Builder(ViewUser.this);
			builder.setTitle(R.string.confirmTitle);
			builder.setNegativeButton(R.string.button_cancel, null);
			builder.setMessage("Unfollow "+user+"?");
			builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int button)
				{
//					sendSMS(twitterNumber, "UNFOLLOW "+user);
					deleteContact();
				}
			});
			builder.show();
			
			break;
		case R.id.unsubscribe:
			createDialog("Unsubscribe to "+user+"'s updates?", "OFF "+user, true);
			break;
		case R.id.subscribe:
			createDialog("Subscribe to "+user+"'s updates?", "ON "+user, true);
			break;
		} // end switch

		return super.onOptionsItemSelected(item);
	} // end method onOptionsItemSelected

	// delete a contact
	private void deleteContact()
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
				finish(); // return to the MainActivity
			} // end method onPostExecute
				}; // end new AsyncTask

				// execute the AsyncTask to delete contact at rowID
				deleteTask.execute(new Long[] { rowID });    

	} // end method deleteContact

	@Override
	public void onClick(View v) {
		String message = "";
		switch(v.getId()) {
		case R.id.retweetButton:
			createDialog("Retweet "+user+"'s latest tweet?", "RETWEET " + user, true);
			break;
		case R.id.replyButton:
			createDialog("Send direct message to " + user + ":\n" + message, user + message, true);
			break;
		case R.id.dmButton:
			createDialog("Send direct message to " + user + ":\n" + message, "D " + user + message, true);
			break;
		case R.id.favoriteButton:
			createDialog("Favorite "+user+"'s latest tweet?", "FAV " + user, true);
			break;
		case R.id.whoButton:
			createDialog(twitterNumber, "WHOIS " + user, false);
			break;
		case R.id.fetchButton:
			createDialog(twitterNumber, "GET " + user, false);
			break;
		}
	}
	// ---sends an SMS message to another device---
	private void sendSMS(String phoneNumber, String message) {

		PendingIntent piSent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0,new Intent(DELIVERED), 0);

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message,null,null);// piSent, piDelivered);

	}

	private void createDialog(String message, final String text, boolean confirm)
	{
		if(confirm){
			AlertDialog.Builder builder = new AlertDialog.Builder(ViewUser.this);
			builder.setTitle(R.string.confirmTitle); // title bar string
			//	    	builder.setTitle
			builder.setNegativeButton(R.string.button_cancel, null);
			;

			builder.setMessage(message);
			builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int button)
				{
					sendSMS(twitterNumber, text);
				}
			});
			builder.show(); // display the Dialog
		}
		else
		{
			sendSMS(twitterNumber, text);
		}
	}

}

