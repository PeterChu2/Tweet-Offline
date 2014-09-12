package chu.ForCHUApps.tweetoffline;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Locale;
import chu.ForCHUApps.tweetoffline.ConfirmDialogFragment.YesNoListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements YesNoListener{


	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	public static final String ROW_ID = "row_id"; // Intent extra key
	private ActionBar.TabListener tabListener;
	private SMSHelper smsHelper;
	private static String DATABASE_NAME;
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
		mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
//			preferenceFragment = new TwitterPreferenceFragment();
//	        getFragmentManager().beginTransaction().replace(android.R.id.content,
//	        		preferenceFragment).commit();
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class TwitterListFragment extends Fragment
	{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private DatabaseConnector database;
		private Cursor cursor;
		private SimpleCustomCursorAdapter customAdapter;
		private Activity activity;
		private String[] from = new String[] { "username" };
		private int[] to = new int[] { R.id.usernameTextView };
		private TwitterListListener multiListener;
		private ListView listView;
		private SMSReceiver smsReceiver;
		private IntentFilter intentFilter;


		public SimpleCustomCursorAdapter getCustomAdapter()
		{
			return customAdapter;
		}

		public static TwitterListFragment newInstance(int sectionNumber) {
			TwitterListFragment fragment = new TwitterListFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public TwitterListFragment(){
			intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
			// For Android versions <= 4.3. This will allow the app to stop the broadcast to the default SMS app
			intentFilter.setPriority(999);
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			this.activity = activity;
			Bundle bundle = this.getArguments();
			customAdapter = new SimpleCustomCursorAdapter(this.getActivity(),
					R.layout.list_item,
					null,
					from,
					to,
					0);

			int sectionNumber = bundle.getInt(ARG_SECTION_NUMBER);
			if(sectionNumber == 1)
			{
				DATABASE_NAME = "Following";
			}
			if(sectionNumber == 2)
			{
				DATABASE_NAME = "Followers";
			}
			if(sectionNumber == 3)
			{
				DATABASE_NAME = "Custom";
			}
			this.database = new DatabaseConnector(activity, DATABASE_NAME);
			multiListener = new TwitterListListener(database.getName(), getActivity(), customAdapter);
			smsReceiver = new SMSReceiver(DATABASE_NAME, null);
			activity.registerReceiver(smsReceiver, intentFilter);
		}

		@Override
		public void onDetach() {
			super.onDetach();
			activity.unregisterReceiver(smsReceiver);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final int section = getArguments().getInt(ARG_SECTION_NUMBER);
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			Button newButton = (Button) rootView.findViewById(R.id.newButton);
			ListView listView = (ListView) rootView
					.findViewById(R.id.twitterList);

			listView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					// TODO Auto-generated method stub

					getListView().setItemChecked(position, !customAdapter.isPositionChecked(position));
					return false;
				}
			});
			listView.setMultiChoiceModeListener(multiListener);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			populateListViewFromDB();
			listView.setAdapter(customAdapter);
			database.close();

			this.listView = listView;

			if(section == 1)
			{
				newButton.setText("Follow New User");
			}
			else if(section == 2)
			{
				newButton.setText("Add Follower");
			}
			else if(section == 3)
			{
				newButton.setText("Add User");
			}

			newButton.setOnClickListener(new OnClickListener() {

				Bundle bundle;
				@Override
				public void onClick(View v) {
					bundle = new Bundle();
					bundle.putInt("section", section);
					Intent addNewUser = 
							new Intent(activity, AddEditUser.class);
					addNewUser.putExtras(bundle);
					startActivityForResult(addNewUser, 0); // start AddEditContact Activity
				}

			});
			listView.setOnItemClickListener(new OnItemClickListener() 
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) 
				{
					// create an Intent to launch the ViewContact Activity
					Intent viewUser = 
							new Intent(activity, ViewUser.class);

					// pass the selected contact's row ID as an extra with the Intent
					viewUser.putExtra(ROW_ID, id);
					viewUser.putExtra("section", section);
					startActivityForResult(viewUser, 0); // start the ViewContact Activity
				} // end method onItemClick
			}); // end viewContactListener);
			return rootView;
		}

		public ListView getListView()
		{
			return listView;
		}

		public String getName()
		{
			return database.getName();
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		public void onActivityResult(int requestcode, int resultCode, Intent data)
		{
			populateListViewFromDB();
			database.close();
		}
		

		public void populateListViewFromDB() {
			database.open();
			cursor = database.getAllRecords();
			if(cursor != null)
			{
				customAdapter.changeCursor(cursor);
			}
			else
			{
				customAdapter.swapCursor(null);
			}

			customAdapter.notifyDataSetChanged();
		}

		private void deleteRecords(){
			database.open();
			database.deleteRecords();
			database.close();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
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
			currFragment.populateListViewFromDB();
			customAdapter.clearSelectionIDs();
		}
		if(tag == "remove_entries")
		{
			SimpleCustomCursorAdapter customAdapter = currFragment.getCustomAdapter();
			HashSet<Long> selectedIDs = customAdapter.getSelectedUserIDs();
			for(Long id : selectedIDs){
				DatabaseActions.deleteUser(this, currFragment.getName(), id, false, currFragment);
			}
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
		// TODO Auto-generated method stub
	}
	
}
