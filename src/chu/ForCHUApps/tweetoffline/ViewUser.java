//// Activity for viewing a single user
package chu.ForCHUApps.tweetoffline;

import chu.ForCHUApps.tweetoffline.ConfirmDialogFragment.YesNoListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
	private SMSReceiver smsReceiver;
	private String user;
	private IntentFilter intentFilter;
	private ConfirmDialogFragment confirmDialog;
	private ScrollingMovementMethod scrolling;
	private SMSHelper smsHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user); // inflate GUI

		smsHelper = new SMSHelper(this);


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


		// get the selected contact's row ID
		Bundle extras = getIntent().getExtras();
		rowID = extras.getLong(Constants.ROW_ID); 
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
		smsReceiver = new SMSReceiver(DATABASE_NAME, rowID);
		
	} // end method onCreate
	//
	// called when the activity is first created
	@Override
	protected void onResume()
	{
		super.onResume();
		registerReceiver(smsReceiver, intentFilter);
		// create new LoadContactTask and execute it 
		new LoadContactTask().execute(rowID);

	} // end method onResume
	
	@Override
	protected void onStop()
	{
		super.onStop();
		unregisterReceiver(smsReceiver);
	}

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
			smsHelper.sendSMS(text);
			break;
		case R.id.fetchButton:
			text = "GET " + user;
			smsHelper.sendSMS(text);
			break;
		}
	}

	@Override
	public void onYes(ConfirmDialogFragment dialog) {
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
				DatabaseActions.deleteUser(this, DATABASE_NAME, rowID, true, null);
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
			smsHelper.sendSMS(text);
		}
	}

	@Override
	public void onNo() {
		// NOOP
	}

	public void loadContacts()
	{
		new LoadContactTask().execute(rowID);
	} // end method onResume

	// performs database query outside GUI thread
	private class LoadContactTask extends AsyncTask<Long, Object, Object> 
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
		protected void onPostExecute(Object result)
		{
			super.onPostExecute(result);
			((Cursor) result).moveToFirst(); // move to the first item 

			// get the column index for each data item
			int nameIndex = ((Cursor) result).getColumnIndex("name");
			int usernameIndex = ((Cursor) result).getColumnIndex("username");
			int recentTweetIndex = ((Cursor) result).getColumnIndex("recentTweet");
			int bioIndex = ((Cursor) result).getColumnIndex("bio");

			// fill TextViews with the retrieved data
			nameTextView.setText(((Cursor) result).getString(nameIndex));
			usernameTextView.setText(((Cursor) result).getString(usernameIndex));
			recentTweetTextView.setText(((Cursor) result).getString(recentTweetIndex));
			bioTextView.setText(((Cursor) result).getString(bioIndex));
			user = ((Cursor) result).getString(usernameIndex);
			((Cursor) result).close(); // close the result cursor
			databaseConnector.close(); // close database connection

		} // end method onPostExecute
	} // end class LoadContactTask

}
