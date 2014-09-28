package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

//Fragments representing the lists
public class TwitterListFragment extends Fragment
{
	private DatabaseConnector database;
	private Cursor cursor;
	private SimpleCustomCursorAdapter customAdapter;
	private Activity activity;
	private String[] from;
	private int[] to = new int[] { R.id.usernameTextView };
	private TwitterListListener multiListener;
	private ListView listView;
	private SMSReceiver smsReceiver;
	private IntentFilter intentFilter;
	private String DATABASE_NAME;
	private String sortBy;

	public SimpleCustomCursorAdapter getCustomAdapter()
	{
		return customAdapter;
	}

	public static TwitterListFragment newInstance(int sectionNumber) {
		TwitterListFragment fragment = new TwitterListFragment();
		Bundle args = new Bundle();
		args.putInt(Constants.ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public TwitterListFragment(){
		intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		// For Android versions <= 4.3. This will allow the app to stop the broadcast to the default SMS app
		intentFilter.setPriority(999);
		// Default view option is username
		sortBy = "username";
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		Bundle bundle = this.getArguments();
		// Get Options for sorting list entries
		sortBy = PreferenceManager.getDefaultSharedPreferences(
				activity).getString("sort_by", "username");
		from = new String[] { sortBy };

		Log.d("DEBUG", "testing, "+sortBy);
		// Map layout items to data
		customAdapter = new SimpleCustomCursorAdapter(this.getActivity(),
				R.layout.list_item,
				null,
				from,
				to,
				0);

		int sectionNumber = bundle.getInt(Constants.ARG_SECTION_NUMBER);
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
		database.close();
		activity.unregisterReceiver(smsReceiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final int section = getArguments().getInt(Constants.ARG_SECTION_NUMBER);
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		Button newButton = (Button) rootView.findViewById(R.id.newButton);
		ListView listView = (ListView) rootView
				.findViewById(R.id.twitterList);

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				getListView().setItemChecked(position, !customAdapter.isPositionChecked(position));
				return false;
			}
		});
		// Allow for selecting multiple entries in contextual menu
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
				viewUser.putExtra(Constants.ROW_ID, id);
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

	// Refresh list when returning from another activity
	public void onActivityResult(int requestcode, int resultCode, Intent data)
	{
		populateListViewFromDB();
		database.close();
	}

	// Refreshes the ListView from the database
	public void populateListViewFromDB() {
		database.open();
		cursor = database.getAllRecords(sortBy);
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

	// Deletes all users in the current list
	public void deleteRecords(){
		database.open();
		database.deleteRecords();
		database.close();
	}
}
