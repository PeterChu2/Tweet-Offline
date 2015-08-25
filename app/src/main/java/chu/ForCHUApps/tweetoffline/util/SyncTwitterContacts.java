package chu.ForCHUApps.tweetoffline.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chu.ForCHUApps.tweetoffline.R;
import chu.ForCHUApps.tweetoffline.db.DatabaseConnector;
import chu.ForCHUApps.tweetoffline.db.DatabaseOpenHelper;
import chu.ForCHUApps.tweetoffline.ui.TwitterListFragment;
import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Class that uses Twitter API to fetch information of a user's contacts
 * This task will sync the follower and following list to the user's actual lists
 */
public class SyncTwitterContacts extends AsyncTask<String, String, Void> {
    private Context mContext;
    private RateLimitStatus mStatus;
    private final Object mWaitToken = new Object();
    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPreferences;

    public SyncTwitterContacts(Context context, ProgressDialog pDialog) {
        mContext = context;
        mProgressDialog = pDialog;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.setMessage("Fetching User Contacts.\n " +
                "They will be fetched in the background if this dialog is dismissed");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(true);
        if(!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values[0].equals("countdown")) {
            APILimitCountDownTimer myCounter = new APILimitCountDownTimer(
                    mStatus.getSecondsUntilReset() * 1000 + 5000, 1000, mWaitToken, this);
            myCounter.start();
        } else {
            mProgressDialog.setMessage(values[0]);
        }
    }

    @Override
    protected Void doInBackground(String... args) {
        DatabaseConnector followerDatabase = new DatabaseConnector(mContext,
                DatabaseOpenHelper.DatabaseName.FOLLOWERS);
        DatabaseConnector followingDatabase = new DatabaseConnector(mContext,
                DatabaseOpenHelper.DatabaseName.FOLLOWING);
        DatabaseConnector customDatabase = new DatabaseConnector(mContext,
                DatabaseOpenHelper.DatabaseName.CUSTOM);

        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);

            // Access Token
            String access_token = mSharedPreferences.getString(Constants.PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = mSharedPreferences.getString(Constants.PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            // Update status
            long cursor = -1;
            IDs follower_ids;
            IDs following_ids;
            Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
            mStatus = rateLimitStatus.get("/users/show/:id");

            if (0 < args.length) {
                follower_ids = twitter.getFollowersIDs(args[0], cursor);
                following_ids = twitter.getFriendsIDs(args[0], cursor);
            } else {
                follower_ids = twitter.getFollowersIDs(cursor);
                following_ids = twitter.getFriendsIDs(cursor);
            }

            // First go through the custom list to update pictures and biographies
            List<String> customListUsernames = new ArrayList<String>();
            customDatabase.open();
            Cursor customDatabaseCursor = customDatabase.getAllRecords("username");
            if (customDatabaseCursor.moveToFirst()) {
                while (!customDatabaseCursor.isAfterLast()) {
                    String username = customDatabaseCursor.getString(customDatabaseCursor
                            .getColumnIndex("username"));
                    // remove the @ sign
                    customListUsernames.add(username.substring(1));
                    customDatabaseCursor.moveToNext();
                }
            }
            customDatabaseCursor.close();
            for (String username : customListUsernames) {
                rateLimitStatus = twitter.getRateLimitStatus();
                mStatus = rateLimitStatus.get("/users/show/:id");
                if (mStatus.getRemaining() <= 0) {
                    // Create message with countdown until sync
                    publishProgress("countdown");

                    // Wait until rate limit has been reset
                    synchronized (mWaitToken) {
                        try {
                            mWaitToken.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                User user = twitter.showUser(username);
                chu.ForCHUApps.tweetoffline.models.User contact = new chu.ForCHUApps.tweetoffline.models.User(
                        "@" + user.getScreenName(), user.getName(), "",
                        user.getDescription(), user.getBiggerProfileImageURL());
                customDatabase.insertRecord(contact);
            }
            customDatabase.close();

            // next sync the followers database
            for (long id : follower_ids.getIDs()) {
                rateLimitStatus = twitter.getRateLimitStatus();
                mStatus = rateLimitStatus.get("/users/show/:id");
                // All 180 calls to API have been used
                if (mStatus.getRemaining() <= 0) {
                    // Show message with time remaining until reset
                    publishProgress("countdown");

                    // Wait until rate limit has been reset
                    synchronized (mWaitToken) {
                        try {
                            mWaitToken.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                User user = twitter.showUser(id);
                chu.ForCHUApps.tweetoffline.models.User contact = new chu.ForCHUApps.tweetoffline.models.User(
                        "@" + user.getScreenName(), user.getName(), "",
                        user.getDescription(), user.getBiggerProfileImageURL());
                followerDatabase.insertRecord(contact);
            }
            followerDatabase.close();

            // sync the following database
            for (long id : following_ids.getIDs()) {
                rateLimitStatus = twitter.getRateLimitStatus();
                mStatus = rateLimitStatus.get("/users/show/:id");
                if (mStatus.getRemaining() <= 0) {
                    // Create message with countdown until sync
                    publishProgress("countdown");

                    // Wait until rate limit has been reset
                    synchronized (mWaitToken) {
                        try {
                            mWaitToken.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                User user = twitter.showUser(id);
                chu.ForCHUApps.tweetoffline.models.User contact = new chu.ForCHUApps.tweetoffline.models.User(
                        "@" + user.getScreenName(), user.getName(), "",
                        user.getDescription(), user.getBiggerProfileImageURL());
                followingDatabase.insertRecord(contact);
            }
            followingDatabase.close();

        } catch (TwitterException e) {
            // Error in updating status
            Log.e("SyncTwitterContacts", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // dismiss the dialog after getting all products
        if(mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        // updating UI from Background Thread
        ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext.getApplicationContext(),
                        "Synced Twitter Contacts Successfully!", Toast.LENGTH_SHORT)
                        .show();
                // Update both followers and following list with new synchronized data

                // Following
                TwitterListFragment currFragment = (TwitterListFragment) ((AppCompatActivity) mContext)
                        .getSupportFragmentManager().
                                findFragmentByTag("android:switcher:" + R.id.pager + ":0");
                if (currFragment != null) {
                    currFragment.populateListViewFromDB();
                }

                // Followers
                currFragment = (TwitterListFragment) ((AppCompatActivity) mContext)
                        .getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.pager + ":1");
                if (currFragment != null) {
                    currFragment.populateListViewFromDB();
                }
            }
        });
    }

    /**
     * Hacky way to show progress from another class because of protected final access of publishProgress
     *
     * @param progress A String describing the progress
     */
    public void showProgress(String progress) {
        publishProgress(progress);
    }
}
