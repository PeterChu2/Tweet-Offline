package chu.ForCHUApps.tweetoffline;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	//	LoaderManager lm = getSupportLoaderManager();
	//	lm.initLoader(0, this);

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	public static final String ROW_ID = "row_id"; // Intent extra key
	private ActionBar.TabListener tabListener;
	private static int MAX_SMS_MESSAGE_LENGTH = 160;
	private SmsManager smsManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		smsManager = SmsManager.getDefault();

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
			return true;
		}
		if (id == R.id.clearList)
		{

			Fragment currFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
			((PlaceholderFragment)currFragment).deleteRecords();
			((PlaceholderFragment)currFragment).populateListViewFromDB();
			return true;

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
			return PlaceholderFragment.newInstance(position + 1);
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
	public static class PlaceholderFragment extends Fragment
	{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private DatabaseConnector database;
		private Cursor cursor;
		private SimpleCursorAdapter customAdapter;
		private Activity activity;
		private String[] from = new String[] { "username" };
		private int[] to = new int[] { R.id.usernameTextView };


		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment(){

		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			this.activity = activity;
			Bundle bundle = this.getArguments();
			customAdapter = new SimpleCursorAdapter(this.getActivity(), R.layout.list_item, null, from, to, 0);
			int sectionNumber = bundle.getInt(ARG_SECTION_NUMBER);
			if(sectionNumber == 1)
			{
				if (this.database == null)
				{
					this.database = new DatabaseConnector(activity, "Following");
				}
			}
			if(sectionNumber == 2)
			{
				if (this.database == null)
				{this.database = new DatabaseConnector(activity, "Followers");
				}
			}
			if(sectionNumber == 3)
			{
				if (this.database == null)
				{
					this.database = new DatabaseConnector(activity, "Custom");
				}
			}
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
			populateListViewFromDB();
			listView.setAdapter(customAdapter);
			database.close();

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
				public void onItemClick(AdapterView<?> parent, View view, int id,
						long position) 
				{
					// create an Intent to launch the ViewContact Activity
					Intent viewUser = 
							new Intent(activity, ViewUser.class);

					// pass the selected contact's row ID as an extra with the Intent
					viewUser.putExtra(ROW_ID, position);
					viewUser.putExtra("section", section);
					startActivityForResult(viewUser, 0); // start the ViewContact Activity
				} // end method onItemClick
			}); // end viewContactListener);
			return rootView;
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

		private void populateListViewFromDB() {
			database.open();
			cursor = database.getAllRecords();
			customAdapter.changeCursor(cursor);
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
}
