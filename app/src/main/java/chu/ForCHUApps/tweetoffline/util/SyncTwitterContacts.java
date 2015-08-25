package chu.ForCHUApps.tweetoffline.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
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
public class SyncTwitterContacts extends AsyncTask<String, String, String> {
    /**
     * Before starting background thread Show Progress Dialog
     */
    Context context;
    private RateLimitStatus status;
    private final Object waitToken = new Object();
    private ProgressDialog pDialog;
    private SharedPreferences sharedPreferences;

    public SyncTwitterContacts(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Fetching User Contacts.\n " +
                "They will be fetched in the background if this dialog is dismissed");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values[0].equals("countdown")) {
            APILimitCountDownTimer myCounter = new APILimitCountDownTimer(
                    status.getSecondsUntilReset() * 1000 + 5000, 1000, waitToken, this);
            myCounter.start();
        } else {
            pDialog.setMessage(values[0]);
        }
    }

    protected String doInBackground(String... args) {
        DatabaseConnector followerDatabase = new DatabaseConnector(context,
                DatabaseOpenHelper.DatabaseName.FOLLOWERS);
        DatabaseConnector followingDatabase = new DatabaseConnector(context,
                DatabaseOpenHelper.DatabaseName.FOLLOWING);

        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);

            // Access Token
            String access_token = sharedPreferences.getString(Constants.PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = sharedPreferences.getString(Constants.PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            // Update status
            long cursor = -1;
            IDs follower_ids;
            IDs following_ids;
            Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
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
                if (status.getRemaining() == 0) {
                    // Show message with time remaining until reset
                    publishProgress("countdown");

                    // Wait until rate limit has been reset
                    synchronized (waitToken) {
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
                        "", user.getDescription(),
                        user.getBiggerProfileImageURL());
            }
            followerDatabase.close();

            for (long id : following_ids.getIDs()) {
                if (status.getRemaining() == 0) {
                    // Create message with countdown until sync
                    publishProgress("countdown");

                    // Wait until rate limit has been reset
                    synchronized (waitToken) {
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
                        "", user.getDescription(),
                        user.getBiggerProfileImageURL());
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
     **/
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after getting all products
        pDialog.dismiss();
        // updating UI from Background Thread
        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(),
                        "Synced Twitter Contacts Successfully!", Toast.LENGTH_SHORT)
                        .show();
                // Update both followers and following list with new synchronized data

                // Following
                TwitterListFragment currFragment = (TwitterListFragment) ((AppCompatActivity) context)
                        .getSupportFragmentManager().
                                findFragmentByTag("android:switcher:" + R.id.pager + ":0");
                if (currFragment != null) {
                    currFragment.populateListViewFromDB();
                }

                // Followers
                currFragment = (TwitterListFragment) ((AppCompatActivity) context)
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
     * @param progress A String describing the progress
     */
    public void showProgress(String progress) {
        publishProgress(progress);
    }
}
