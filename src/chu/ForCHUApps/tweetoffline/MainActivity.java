package chu.ForCHUApps.tweetoffline;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import twitter4j.conf.*; 
import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import chu.ForCHUApps.tweetoffline.ConfirmDialogFragment.YesNoListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements YesNoListener{

	private static final String TWITTER_CONSUMER_KEY = "ecIc8ctYPjZmcAhR8oc1CB5LF";
	private static final String TWITTER_CONSUMER_SECRET = "b8R632rpG6w4G3A6cyjAhm9cXVNPUlLeb762L9jb8JZISzM9Pj";
	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";
	static final String PREF_USERNAME = "usernameKey";

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	// Twitter
	private static Twitter twitter; // Use this to access Twitter API
	private static RequestToken requestToken;
	private String username;
	// Progress dialog
	ProgressDialog pDialog;

	// Internet Connection detector
	private ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	public static final String ROW_ID = "row_id"; // Intent extra key
	private ActionBar.TabListener tabListener;
	private SMSHelper smsHelper;
	private SharedPreferences sharedPreferences;
	private String twitterNumber;
	private static final String twitterNumberKey = "edittext_twitter_number";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Set the actionbar overflow
		getOverflowMenu();

		// Get sharedPreferences with the User's twitter short code if it exists
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		smsHelper = new SMSHelper(this);

		if(sharedPreferences.contains(twitterNumberKey))
		{
			twitterNumber = sharedPreferences.getString(twitterNumberKey, null);
			smsHelper.setTwitterNumber(twitterNumber);
		}
		else
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Configure Twitter shortcode");
			alert.setMessage("The app will not be able to do anything until" +
					" you set your SMS shortcode in the settings.");
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing, user confirmed seeing the message
				}
			});
			alert.show();
		}

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Check if twitter keys are set
		if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
			// Internet Connection is not present
			alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
			// stop executing code by return
			return;
		}
		tabListener = new ActionBar.TabListener(){
			@Override
			public void onTabSelected(ActionBar.Tab tab,
					FragmentTransaction fragmentTransaction) {
				// When the given tab is selected, switch to the corresponding page in
				// the ViewPager.
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(ActionBar.Tab tab,
					FragmentTransaction fragmentTransaction) {
			}

			@Override
			public void onTabReselected(ActionBar.Tab tab,
					FragmentTransaction fragmentTransaction) {
			}
		};


		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(tabListener));
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
	};
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		if (id == R.id.clearList)
		{
			Fragment currFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
			((TwitterListFragment)currFragment).deleteRecords();
			((TwitterListFragment)currFragment).populateListViewFromDB();
			return true;
		}
		if (id == R.id.composeTweet)
		{
			ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Send tweet:", true, 0);
			confirmDialog.show(getFragmentManager(), "compose");
		}
		if (id == R.id.notifsOFF)
		{
			ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Turn off all notifications", false, 0);
			confirmDialog.show(getFragmentManager(), "notifsOFF");
		}
		if (id == R.id.notifsON)
		{
			ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Turn on all notifications", false, 0);
			confirmDialog.show(getFragmentManager(), "notifsON");
		}
		if (id == R.id.syncList)
		{
			loginToTwitter();
			if(username != null)
			{
				new SyncTwitterContacts(this).execute(username);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return TwitterListFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	// Hacky way to show overflow in actionbar menu regardless of hardware hardware button
	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onYes(ConfirmDialogFragment dialog) {

		// Only confirm dialog is for a tweet in this activity; no need for dialog tags
		Dialog dialogView = dialog.getDialog();
		String text = "";
		String tag = dialog.getTag();
		EditText input;
		TwitterListFragment currFragment = (TwitterListFragment) getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());

		// Do different actions depending on what dialog is shown
		if(tag == "compose")
		{
			input = (EditText) dialogView.getCurrentFocus();
			text = input.getText().toString();
		}
		else if(tag == "notifsOFF")
		{
			text = "OFF";
		}
		else if(tag == "notifsON")
		{
			text = "ON";
		}
		else if(tag == "unfollow_entries")
		{
			SimpleCustomCursorAdapter customAdapter = currFragment.getCustomAdapter();
			HashSet<Long> selectedIDs = customAdapter.getSelectedUserIDs();
			for(Long id : selectedIDs){
				DatabaseActions.sendMessage(this, currFragment.getName(), "Unfollow ", id, smsHelper);
				if(currFragment.getName() == "Following")
				{
					DatabaseActions.deleteUser(this, currFragment.getName(), id, false, currFragment);
				}
			}
			// Refresh list
			currFragment.populateListViewFromDB();
			customAdapter.clearSelectionIDs();
		}
		if(tag == "remove_entries")
		{
			SimpleCustomCursorAdapter customAdapter = currFragment.getCustomAdapter();
			HashSet<Long> selectedIDs = customAdapter.getSelectedUserIDs();
			// Remove multiple entries
			for(Long id : selectedIDs){
				DatabaseActions.deleteUser(this, currFragment.getName(), id, false, currFragment);
			}
			// Refresh list
			currFragment.populateListViewFromDB();
			customAdapter.clearSelectionIDs();

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

	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	private void loginToTwitter() {

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {

			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					username = user.getScreenName();

					// Shared Preferences
					Editor e = sharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.putString(PREF_USERNAME, username);
					e.commit(); // save changes

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}

			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter
						.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {
			// user already logged into twitter
			username = sharedPreferences.getString(PREF_USERNAME, username);
			Toast.makeText(getApplicationContext(),
					"Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}


	// Class that uses Twitter API to fetch information of a user's contacts
	// This task will sync the follower and following list to the user's actual lists

	class SyncTwitterContacts extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		Context context;
		private RateLimitStatus status;
		private CountDownTimer myCounter;
		private Object waitToken = new Object();

		public SyncTwitterContacts(Context context)
		{
			this.context = context;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Fetching User Contacts.\n " +
					"They will be fetched in the background if this dialog is dismissed");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			if(values[0].equals("countdown"))
			{
				myCounter = new WaitCountDown(status.getSecondsUntilReset()*1000 + 5000, 1000, waitToken) {

					@Override
					public void onTick(long millisUntilFinished) {
						publishProgress("Rate Limit of 180"
								+ " has been reached. Your remaining contacts will be fetched in "+
								(millisUntilFinished/1000)/60 + " minutes, " +
								(millisUntilFinished/1000)%60 + " seconds.\n" +
								"Dismiss to load in background.");
						if(millisUntilFinished <= 2000)
						{
							synchronized (waitToken) {
								publishProgress("Fetching User Contacts \n" +
										"They will be fetched in the background if this dialog is dismissed");
								waitToken.notify();
							}
						}
					}

					@Override
					public void onFinish() {
					}
				};
				myCounter.start();
			}
			else
			{
				pDialog.setMessage(values[0]);
			}
		}

		protected String doInBackground(String... args) {
			DatabaseConnector followerDatabase = new DatabaseConnector(context, "Followers");
			DatabaseConnector followingDatabase = new DatabaseConnector(context, "Following");

			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token 
				String access_token = sharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = sharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

				// Update status
				long cursor = -1;
				IDs follower_ids;
				IDs following_ids;
				Map<String ,RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
				status = rateLimitStatus.get("/users/show/:id");

				if (0 < args.length) {
					follower_ids = twitter.getFollowersIDs(args[0], cursor);
					following_ids = twitter.getFriendsIDs(args[0], cursor);
				} else {
					follower_ids = twitter.getFollowersIDs(cursor);
					following_ids = twitter.getFriendsIDs(cursor);
				}

				for (long id : follower_ids.getIDs()) {
					// All 180 calls to API have been used
					if(status.getRemaining() == 0){
						// Show message with time remaining until reset
						publishProgress("countdown");

						// Wait until rate limit has been reset
						synchronized(waitToken){
							try {
								waitToken.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						// Get new rate limit after waiting
						rateLimitStatus = twitter.getRateLimitStatus();
						status = rateLimitStatus.get("/users/show/:id");
					}

					User user = twitter.showUser(id);
					followerDatabase.insertRecord(
							"@" + user.getScreenName(),
							user.getName(),
							"", user.getDescription());
				}
				followerDatabase.close();

				for (long id : following_ids.getIDs()) {
					if(status.getRemaining() == 0){
						// Create message with countdown until sync
						publishProgress("countdown");

						// Wait until rate limit has been reset
						synchronized(waitToken){
							try {
								waitToken.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							rateLimitStatus = twitter.getRateLimitStatus();
							status = rateLimitStatus.get("/users/show/:id");
						}
					}
					User user = twitter.showUser(id);
					followingDatabase.insertRecord(
							"@" + user.getScreenName(),
							user.getName(),
							"", user.getDescription());
				}
				followingDatabase.close();

			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}
		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Synced Twitter Contacts Successfully!", Toast.LENGTH_SHORT)
							.show();
					// Update both followers and following list with new synchronized data

					// Following
					TwitterListFragment currFragment = (TwitterListFragment) getSupportFragmentManager().
							findFragmentByTag("android:switcher:" + R.id.pager + ":1");
					if(currFragment != null)
					{
						currFragment.populateListViewFromDB();
					}

					// Followers
					currFragment = (TwitterListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":2");
					if(currFragment != null)
					{
						currFragment.populateListViewFromDB();
					}
				}
			});
		}
	}
}