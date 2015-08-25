package chu.ForCHUApps.tweetoffline.db;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import chu.ForCHUApps.tweetoffline.R;
import chu.ForCHUApps.tweetoffline.sms.SMSHelper;
import chu.ForCHUApps.tweetoffline.sms.SMSReceiver;
import chu.ForCHUApps.tweetoffline.ui.ConfirmDialogFragment;
import chu.ForCHUApps.tweetoffline.ui.ConfirmDialogFragment.ConfirmationListener;
import chu.ForCHUApps.tweetoffline.util.Constants;

public class UserDetailActivity extends Activity implements OnClickListener, ConfirmationListener {
    private long mRowId; // selected contact's row ID in the database
    private TextView mNameTv; // displays contact's name
    private TextView mUsernameTv; // displays contact's username
    private TextView mRecentTweetTv; // displays user's latest tweet
    private TextView mBioTv; // displays user's bio
    private int mSection;
    private DatabaseOpenHelper.DatabaseName mDatabaseName;
    private SMSReceiver smsReceiver;
    private String user;
    private IntentFilter intentFilter;
    private ConfirmDialogFragment confirmDialog;
    private SMSHelper smsHelper;
    private ImageView profilePic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_user); // inflate GUI
        smsHelper = new SMSHelper(this);
        ScrollingMovementMethod scrolling = new ScrollingMovementMethod();

        // get the TextViews
        mNameTv = (TextView) findViewById(R.id.nameTextView);
        mUsernameTv = (TextView) findViewById(R.id.usernameTextView);
        mRecentTweetTv = (TextView) findViewById(R.id.latestTweetTextView);
        mRecentTweetTv.setMovementMethod(scrolling);
        mBioTv = (TextView) findViewById(R.id.bioTextView);
        mBioTv.setMovementMethod(scrolling);

        profilePic = (ImageView) findViewById(R.id.twitterProfileIcon);

        //get the Buttons
        Button retweetBtn = (Button) findViewById(R.id.retweetButton);
        Button replyBtn = (Button) findViewById(R.id.replyButton);
        Button dmBtn = (Button) findViewById(R.id.dmButton);
        Button favoriteBtn = (Button) findViewById(R.id.favoriteButton);
        Button fetchBtn = (Button) findViewById(R.id.fetchButton);
        Button getBioBtn = (Button) findViewById(R.id.whoButton);
        retweetBtn.setOnClickListener(this);
        replyBtn.setOnClickListener(this);
        dmBtn.setOnClickListener(this);
        favoriteBtn.setOnClickListener(this);
        fetchBtn.setOnClickListener(this);
        getBioBtn.setOnClickListener(this);

        intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        // For Android versions <= 4.3. This will allow the app to stop the broadcast to the default SMS app
        intentFilter.setPriority(999);

        // get the selected contact's row ID
        Bundle extras = getIntent().getExtras();
        mRowId = extras.getLong(Constants.ROW_ID);
        mSection = extras.getInt("section");

        if (mSection == 1) {
            mDatabaseName = DatabaseOpenHelper.DatabaseName.FOLLOWING;
        }
        if (mSection == 2) {
            mDatabaseName = DatabaseOpenHelper.DatabaseName.FOLLOWERS;
        }
        if (mSection == 3) {
            mDatabaseName = DatabaseOpenHelper.DatabaseName.CUSTOM;
        }
        smsReceiver = new SMSReceiver(mDatabaseName, mRowId);

    } // end method onCreate

    // called when the activity is first created
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(smsReceiver, intentFilter);
        // create new LoadContactTask and execute it
        new LoadContactTask().execute(mRowId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsReceiver);
    }

    // create the Activity's menu from a menu resource XML file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        if (mDatabaseName.equals("Followers")) {
            inflater.inflate(R.menu.follower_menu, menu);
        } else if (mDatabaseName.equals("Following")) {
            inflater.inflate(R.menu.following_menu, menu);
        } else {
            inflater.inflate(R.menu.custom_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) // switch based on selected MenuItem's ID
        {
            case R.id.unfollow:
                confirmDialog = ConfirmDialogFragment
                        .newInstance("Unfollow " + user + "?", false, 0);
                confirmDialog.show(getFragmentManager(), "unfollow");
                break;
            case R.id.unsubscribe:
                confirmDialog = ConfirmDialogFragment
                        .newInstance("Unsubscribe to " + user + "'s updates?", false, 0);
                confirmDialog.show(getFragmentManager(), "unsubscribe");
                break;
            case R.id.subscribe:
                confirmDialog = ConfirmDialogFragment
                        .newInstance("Subscribe to " + user + "'s updates?", false, 0);
                confirmDialog.show(getFragmentManager(), "subscribe");
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        String text;
        switch (v.getId()) {
            case R.id.retweetButton:
                confirmDialog = ConfirmDialogFragment.newInstance(String
                        .format("Retween %s's latest tweet?", user), false, 0);
                confirmDialog.show(getFragmentManager(), "retweet");
                break;
            case R.id.replyButton:
                confirmDialog = ConfirmDialogFragment.newInstance(String.format("Reply to %s?", user)
                        , true, user.length() + 1);
                confirmDialog.show(getFragmentManager(), "reply");
                break;
            case R.id.dmButton:
                confirmDialog = ConfirmDialogFragment.newInstance(String
                        .format("Send direct message to %s?", user), true, user.length() + 3);
                confirmDialog.show(getFragmentManager(), "dm");
                break;
            case R.id.favoriteButton:
                confirmDialog = ConfirmDialogFragment.newInstance(String
                        .format("Favorite %s's latest tweet?", user), false, 0);
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
    public void onConfirm(ConfirmDialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        String text = "";

        String tag = dialog.getTag();

        // Do different actions depending on what dialog is shown
        if (tag == "follow") {
            text = "FOLLOW " + user;
        } else if (tag == "unfollow") {
            text = "UNFOLLOW " + user;
            if (mSection == 1) {
                DatabaseActions.deleteUser(this, mDatabaseName, mRowId, true, null);
            }
        } else if (tag == "subscribe") {
            text = "ON " + user;
        } else if (tag == "unsubscribe") {
            text = "OFF " + user;
        } else if (tag == "favorite") {
            text = "FAVORITE " + user;
        } else if (tag == "retweet") {
            text = "RETWEET " + user;
        } else if (tag == "reply") {
            EditText input = (EditText) dialogView.getCurrentFocus();
            text = user + " " + input.getText().toString();
        } else if (tag == "dm") {
            EditText input = (EditText) dialogView.getCurrentFocus();
            text = "D " + user + " " + input.getText().toString();
        }
        if (text.isEmpty() == false) {
            smsHelper.sendSMS(text);
        }
    }

    public void loadContacts() {
        new LoadContactTask().execute(mRowId);
    } // end method onResume

    // performs database query outside GUI thread
    private class LoadContactTask extends AsyncTask<Long, Object, Object> {

        DatabaseConnector databaseConnector =
                new DatabaseConnector(UserDetailActivity.this, mDatabaseName);

        // perform the database access
        @Override
        protected Cursor doInBackground(Long... params) {
            databaseConnector.open();

            //          get a cursor containing all data on given entry
            return databaseConnector.getOneRecord(params[0]);
        } // end method doInBackground

        // use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            ((Cursor) result).moveToFirst(); // move to the first item

            // get the column index for each data item
            int nameIndex = ((Cursor) result).getColumnIndex("name");
            int usernameIndex = ((Cursor) result).getColumnIndex("username");
            int recentTweetIndex = ((Cursor) result).getColumnIndex("recentTweet");
            int bioIndex = ((Cursor) result).getColumnIndex("bio");
            int picIndex = ((Cursor) result).getColumnIndex("pic");

            String bigger_pic_URL = ((Cursor) result).getString(picIndex);
            Ion.with(profilePic)
                    .error(R.drawable.tweet_offline_logo)
                    .load(bigger_pic_URL);

            // fill TextViews with the retrieved data
            mNameTv.setText(((Cursor) result).getString(nameIndex));
            mUsernameTv.setText(((Cursor) result).getString(usernameIndex));
            mRecentTweetTv.setText(((Cursor) result).getString(recentTweetIndex));
            mBioTv.setText(((Cursor) result).getString(bioIndex));
            user = ((Cursor) result).getString(usernameIndex);
            ((Cursor) result).close(); // close the result cursor
            databaseConnector.close(); // close database connection
        }
    }
}
