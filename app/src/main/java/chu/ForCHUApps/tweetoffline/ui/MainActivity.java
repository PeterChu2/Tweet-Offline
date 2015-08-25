package chu.ForCHUApps.tweetoffline.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Locale;
import chu.ForCHUApps.tweetoffline.R;
import chu.ForCHUApps.tweetoffline.db.DatabaseActions;
import chu.ForCHUApps.tweetoffline.sms.SMSHelper;
import chu.ForCHUApps.tweetoffline.ui.ConfirmDialogFragment.ConfirmationListener;
import chu.ForCHUApps.tweetoffline.util.ConnectionDetector;
import chu.ForCHUApps.tweetoffline.util.Constants;
import chu.ForCHUApps.tweetoffline.util.SimpleCustomCursorAdapter;
import chu.ForCHUApps.tweetoffline.util.SyncTwitterContacts;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity implements ConfirmationListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    private SMSHelper mSmsHelper;

    // Twitter
    private static Twitter twitter; // Use this to access Twitter API
    private static RequestToken requestToken;
    private String username;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set the actionbar overflow
        getOverflowMenu();

        // Get sharedPreferences with the User's twitter short code if it exists
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSmsHelper = new SMSHelper(this);

        if (sharedPreferences.contains(Constants.twitterNumberKey)) {
            String twitterNumber = sharedPreferences.getString(Constants.twitterNumberKey, null);
            mSmsHelper.setTwitterNumber(twitterNumber);
        } else {
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
        if (Constants.TWITTER_CONSUMER_KEY.trim().length() == 0 || Constants.TWITTER_CONSUMER_SECRET.trim().length() == 0) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
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
        handleIntent(getIntent());

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Refresh list when returning from another activity
    @Override
    public void onActivityResult(int requestcode, int resultCode, Intent data) {
        super.onActivityResult(requestcode, resultCode, data);
        // Refresh the twitter lists, synchronizing changes made by other activities
        for (int i = 0; i <= 2; i++) {
            TwitterListFragment currFragment = (TwitterListFragment) (
                    getSupportFragmentManager().
                            findFragmentByTag("android:switcher:" + R.id.pager + ":" + i)
            );
            if (currFragment != null) {
                currFragment.populateListViewFromDB();
            }
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
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        if (id == R.id.clearList) {
            Fragment currFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
            ((TwitterListFragment) currFragment).deleteRecords();
            ((TwitterListFragment) currFragment).populateListViewFromDB();
            return true;
        }
        if (id == R.id.composeTweet) {
            ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Send tweet:", true, 0);
            confirmDialog.show(getFragmentManager(), "compose");
        }
        if (id == R.id.notifsOFF) {
            ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Turn off all notifications", false, 0);
            confirmDialog.show(getFragmentManager(), "notifsOFF");
        }
        if (id == R.id.notifsON) {
            ConfirmDialogFragment confirmDialog = ConfirmDialogFragment.newInstance("Turn on all notifications", false, 0);
            confirmDialog.show(getFragmentManager(), "notifsON");
        }
        if (id == R.id.syncList) {
            loginToTwitter();
            if (username != null) {
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
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfirm(ConfirmDialogFragment dialog) {
        // Only confirm dialog is for a tweet in this activity; no need for dialog tags
        Dialog dialogView = dialog.getDialog();
        String text = "";
        String tag = dialog.getTag();
        EditText input;
        TwitterListFragment currFragment = (TwitterListFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
        // Do different actions depending on what dialog is shown
        if ("compose".equals(tag)) {
            input = (EditText) dialogView.getCurrentFocus();
            text = input.getText().toString();
        } else if ("notifsOFF".equals(tag)) {
            text = "OFF";
        } else if ("notifsON".equals(tag)) {
            text = "ON";
        } else if ("unfollow_entries".equals(tag)) {
            SimpleCustomCursorAdapter customAdapter = currFragment.getCustomAdapter();
            HashSet<Long> selectedIDs = customAdapter.getSelectedUserIDs();
            for (Long id : selectedIDs) {
                DatabaseActions.sendMessage(this, currFragment.getName(), "Unfollow ", id, mSmsHelper);
                if ("Following".equals(currFragment.getName().name())) {
                    DatabaseActions.deleteUser(this, currFragment.getName(), id, false, currFragment);
                }
            }
            // Refresh list
            currFragment.populateListViewFromDB();
            customAdapter.clearSelectionIDs();
        }
        if ("remove_entries".equals(tag)) {
            SimpleCustomCursorAdapter customAdapter = currFragment.getCustomAdapter();
            HashSet<Long> selectedIDs = customAdapter.getSelectedUserIDs();
            // Remove multiple entries
            for (Long id : selectedIDs) {
                DatabaseActions.deleteUser(this, currFragment.getName(), id, false, currFragment);
            }
            // Refresh list
            currFragment.populateListViewFromDB();
            customAdapter.clearSelectionIDs();
        }
        if (!text.isEmpty()) {
            mSmsHelper.sendSMS(text);
        }
    }

    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return sharedPreferences.getBoolean(Constants.PREF_KEY_TWITTER_LOGIN, false);
    }

    private void loginToTwitter() {
        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
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
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
            try {
                requestToken = twitter
                        .getOAuthRequestToken(Constants.TWITTER_CALLBACK_URL);
                this.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())), 500);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            username = sharedPreferences.getString(Constants.PREF_USERNAME, username);
            Toast.makeText(getApplicationContext(),
                    "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    // Manually handle returning from intent to authenticate with twitter
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && uri.toString().startsWith(Constants.TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = uri
                        .getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
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
                    e.putString(Constants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                    e.putString(Constants.PREF_KEY_OAUTH_SECRET,
                            accessToken.getTokenSecret());
                    // Store login status - true
                    e.putBoolean(Constants.PREF_KEY_TWITTER_LOGIN, true);
                    e.putString(Constants.PREF_USERNAME, username);
                    e.commit(); // save changes
                } catch (Exception e) {
                    // Check log for login errors
                    Log.e("MainActivity", String.format("Twitter Login Error: %s", e.getMessage()));
                }
            }
            // Update the database with the newly authenticated user's contacts
            new SyncTwitterContacts(this).execute(username);
        }
    }
}