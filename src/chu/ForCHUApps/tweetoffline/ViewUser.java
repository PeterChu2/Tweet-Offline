//// Activity for viewing a single user
package chu.ForCHUApps.tweetoffline;

import chu.ForCHUApps.tweetoffline.ConfirmDialogFragment.YesNoListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ViewUser extends Activity implements OnClickListener, YesNoListener
{
	private long rowID; // selected contact's row ID in the database
	private TextView nameTextView; // displays contact's name 
	private TextView usernameTextView; // displays contact's username
	private TextView recentTweetTextView; // displays user's latest tweet
	private TextView bioTextView; // displays user's bio
	private Button retweetButton;
	private Button replyButton;
	private Button dmButton;
	private Button favoriteButton;
	private Button fetchButton;
	private Button whoButton;

	private int section;
	private String DATABASE_NAME;
	private static String twitterNumber = "21212";
	private static String SENT = "SMS_SENT";
	private static String DELIVERED = "SMS_DELIVERED";
	private SMSReceiver smsReceiver = new SMSReceiver();
	private String user;
	private IntentFilter intentFilter;
	private ConfirmDialogFragment confirmDialog;
	private ScrollingMovementMethod scrolling;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user); // inflate GUI
		
		scrolling = new ScrollingMovementMethod();

		// get the TextViews
		nameTextView = (TextView) findViewById(R.id.nameTextView);
		usernameTextView = (TextView) findViewById(R.id.usernameTextView);
		recentTweetTextView = (TextView) findViewById(R.id.latestTweetTextView);
		recentTweetTextView.setMovementMethod(scrolling);
		bioTextView = (TextView) findViewById(R.id.bioTextView);
		bioTextView.setMovementMethod(scrolling);

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
		// For Android versions <= 4.3. This will allow the app to stop the broadcast to the default SMS app
		intentFilter.setPriority(999);
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
			confirmDialog = ConfirmDialogFragment.newInstance("Unfollow " + user + "?", false, 0);
			confirmDialog.show(getFragmentManager(), "unfollow");
			break;
		case R.id.unsubscribe:
			confirmDialog = ConfirmDialogFragment.newInstance("Unsubscribe to "+user+"'s updates?", false, 0);
			confirmDialog.show(getFragmentManager(), "unsubscribe");
			break;
		case R.id.subscribe:
			confirmDialog = ConfirmDialogFragment.newInstance("Subscribe to "+user+"'s updates?", false, 0);
			confirmDialog.show(getFragmentManager(), "subscribe");
			break;
		} // end switch

		return super.onOptionsItemSelected(item);
	} // end method onOptionsItemSelected



	@Override
	public void onClick(View v) {
		String text;
		switch(v.getId()) {
		case R.id.retweetButton:
			confirmDialog = ConfirmDialogFragment.newInstance("Retweet "+user+"'s latest tweet?", false, 0);
			confirmDialog.show(getFragmentManager(), "retweet");
			break;
		case R.id.replyButton:
			confirmDialog = ConfirmDialogFragment.newInstance("Reply to " + user + ":", true, user.length() + 1);
			confirmDialog.show(getFragmentManager(), "reply");
			break;
		case R.id.dmButton:
			confirmDialog = ConfirmDialogFragment.newInstance("Send direct message to " + user + ":", true, user.length() + 3);
			confirmDialog.show(getFragmentManager(), "dm");
			break;
		case R.id.favoriteButton:
			confirmDialog = ConfirmDialogFragment.newInstance("Favorite "+user+"'s latest tweet?", false, 0);
			confirmDialog.show(getFragmentManager(), "favorite");
			break;
		case R.id.whoButton:
			text = "WHOIS " + user;
			sendSMS(twitterNumber, text);
			break;
		case R.id.fetchButton:
			text = "GET " + user;
			sendSMS(twitterNumber, text);
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

	private class SMSReceiver extends BroadcastReceiver{

		private static final String TAG = "SmsReceiver";
		private String bio;
		private String name;
		private String recentTweet;
		private String[] biography;
		private String username;
		private String[] partTwoMessage;
		private String message;
		private String message2;

		public SMSReceiver() {

		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Retrieves a map of extended data from the intent.
			//			if(intent.getAction().equals(SMS_ACTION))
			final Bundle bundle = intent.getExtras();

			try {

				if (bundle != null) {

					final Object[] pdusObj = (Object[]) bundle.get("pdus");

					for (int i = 0; i < pdusObj.length; i++) {

						// This only works on Android <= 4.3
						this.abortBroadcast();

						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
						String phoneNumber = currentMessage.getDisplayOriginatingAddress();
						if( phoneNumber.equals(twitterNumber) )
						{
							message = currentMessage.getDisplayMessageBody();

							// Check all possibilities of GET command, which returns the latest tweet of a user

							if(message.startsWith("@"))
							{
								partTwoMessage = message.split(":[\\s]", 2);
								username = partTwoMessage[0];
								recentTweet = partTwoMessage[1];
								ContentValues cv = new ContentValues();
								cv.put("recentTweet", recentTweet);
								cv.put("username", username); //Also update username to ensure it has correct capitalization in the db
								updateUser(cv);
								recentTweet = null;
							}
							else if(message.startsWith("1/2: @"))
							{
								partTwoMessage = message.split(":[\\s]", 3);

								if(message2 != null)
								{
									recentTweet = partTwoMessage[2] + message2;
									ContentValues cv = new ContentValues();
									cv.put("recentTweet", recentTweet);
									updateUser(cv);
									message2 = null;
								}
								else{
									recentTweet = partTwoMessage[2];
								}

							}
							else if(message.startsWith("2/2: "))
							{
								partTwoMessage = message.split(":[\\s]", 2);
								if(recentTweet != null)
								{
									recentTweet += partTwoMessage[1];
									ContentValues cv = new ContentValues();
									cv.put("recentTweet", recentTweet);
									updateUser(cv);
									recentTweet = null;
								}
								else if(bio != null) // SMS is part 2 of fetch for bio
								{
									bio += partTwoMessage[1];
									bio = bio.replaceAll("\n\nReply\\sw/.*", "");
									ContentValues cv = new ContentValues();
									cv.put("bio", bio);
									cv.put("name", name);
									updateUser(cv);
									bio = null;
								}
								else // Part 2 of the SMS is received before Part 1
								{	
									message2 = partTwoMessage[1];
								}
							}

							else
							{
								// Check if text was the WHOIS command which returns name and bio
								biography =  message.split(".\nBio: ");
								if(biography.length > 1)
								{
									// Get the first name
									name = biography[0].split(",")[0];
									bio = biography[1];

									if(message.startsWith("1/2: ")) // Bio does not fit in one SMS
									{
										name = name.substring(5);
										if(message2 != null)
										{
											bio += message2;
											// Replace junk message at the end with an empty string
											bio = bio.replaceAll("\n\nReply\\sw/.*", "");
											ContentValues cv = new ContentValues();
											cv.put("name", name);
											cv.put("bio", bio);
											Log.d(TAG, bio+ "tesr");
											updateUser(cv);
										}
									}
									else
									{
										ContentValues cv = new ContentValues();
										cv.put("name", name);
										cv.put("bio", bio);
										updateUser(cv);
									}
								}
							}
						}
					} // end for loop
				} // bundle is null

			} catch (Exception e) {
				Log.e("SmsReceiver", "Exception SMSReceiver" +e);
			}
		}
	}

	// delete a contact
	private void deleteUser()
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
				setResult(Activity.RESULT_OK);
				finish(); // return to the MainActivity
			} // end method onPostExecute
				}; // end new AsyncTask

				// execute the AsyncTask to delete contact at rowID
				deleteTask.execute(new Long[] { rowID });    

	} // end method deleteContact

	// Update a user
	private void updateUser(ContentValues updateRow)
	{
		final DatabaseConnector databaseConnector = 
				new DatabaseConnector(ViewUser.this, DATABASE_NAME);

		// create an AsyncTask that updates the contact in another 
		AsyncTask<ContentValues, Object, Object> updateTask =
				new AsyncTask<ContentValues, Object, Object>()
				{
			@Override
			protected Object doInBackground(ContentValues... params)
			{
				databaseConnector.updateUser(rowID, params[0]);
				return null;
			} // end method doInBackground

			@Override
			protected void onPostExecute(Object result)
			{
				new LoadContactTask().execute(rowID);
			} // end method onPostExecute
				}; // end new AsyncTask

				// execute the AsyncTask to delete contact at rowID
				updateTask.execute(new ContentValues[] { updateRow });    

	} // end method deleteContact
	
	@Override
	public void onYes(ConfirmDialogFragment dialog) {
		// TODO Auto-generated method stub
		Dialog dialogView = dialog.getDialog();
		String text = "";
		
		String tag = dialog.getTag();
		
		// Do different actions depending on what dialog is shown
		if(tag == "follow")
		{
			text = "FOLLOW " + user;
		}
		else if(tag == "unfollow")
		{
			text = "UNFOLLOW " + user;
			if( section == 1 )
			{
				deleteUser();
			}
		}
		else if(tag == "subscribe")
		{
			text = "ON " + user;
		}
		else if(tag == "unsubscribe")
		{
			text = "OFF " + user;
		}
		else if(tag == "favorite")
		{
			text = "FAVORITE " + user;
		}
		else if(tag == "retweet")
		{
			text = "RETWEET " + user;
		}
		else if(tag == "reply")
		{
			EditText input = (EditText) dialogView.getCurrentFocus();
			text = user + " " + input.getText().toString();
		}
		else if(tag == "dm")
		{
			EditText input = (EditText) dialogView.getCurrentFocus();
			text = "D " + user + " " + input.getText().toString();
		}
		if(text.isEmpty() == false)
		{
			sendSMS(twitterNumber, text);
		}
	}
	
	@Override
	public void onNo() {
		// TODO Auto-generated method stub

	}

}
