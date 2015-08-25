package chu.ForCHUApps.tweetoffline.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import chu.ForCHUApps.tweetoffline.sms.SMSHelper;
import chu.ForCHUApps.tweetoffline.ui.TwitterListFragment;

public class DatabaseActions {
    static DatabaseConnector sDatabaseConnector;

    public static void deleteUser(final Context context, final DatabaseOpenHelper.DatabaseName databaseName, long rowID,
                                  final boolean finishActivity, final TwitterListFragment fragment) {
        sDatabaseConnector = new DatabaseConnector(context, databaseName);
        // create an AsyncTask that deletes the contact in another
        // thread, then calls finish after the deletion
        AsyncTask<Long, Object, Object> deleteTask =
                new AsyncTask<Long, Object, Object>() {
                    @Override
                    protected Object doInBackground(Long... params) {
                        sDatabaseConnector.deleteRecord(params[0]);
                        return null;
                    } // end method doInBackground

                    @Override
                    protected void onPostExecute(Object result) {
                        if (finishActivity) {
                            ((Activity) context).setResult(Activity.RESULT_OK);
                            ((Activity) context).finish(); // return to the MainActivity
                            return;
                        }
                        fragment.populateListViewFromDB();
                    }
                };
        // execute the AsyncTask to delete contact at rowID
        deleteTask.execute(new Long[]{rowID});
    }

    public static void sendMessage(Context context, DatabaseOpenHelper.DatabaseName databaseName,
                                   final String command,
                                   final long rowID,
                                   final SMSHelper smsHelper) {
        sDatabaseConnector = new DatabaseConnector(context, databaseName);
        // create an AsyncTask that deletes the contact in another
        // thread, then calls finish after the deletion
        AsyncTask<Long, Object, Object> usernameTask =
                new AsyncTask<Long, Object, Object>() {
                    @Override
                    protected Object doInBackground(Long... params) {
                        sDatabaseConnector.open();
                        String username = sDatabaseConnector.getUsername(params[0]);
                        sDatabaseConnector.close();
                        return username;
                    } // end method doInBackground

                    @Override
                    protected void onPostExecute(Object username) {
                        smsHelper.sendSMS(command + (String) username);
                    } // end method onPostExecute

                }; // end new AsyncTask

        // execute the AsyncTask to delete contact at rowID
        usernameTask.execute(new Long[]{rowID});
    }

    // Update a user
    public static void updateUser(final ContentValues updateRow, Long rowID,
                                  Context context, DatabaseOpenHelper.DatabaseName databaseName) {
        sDatabaseConnector = new DatabaseConnector(context, databaseName);
        if (rowID != null) {
            // User selected an item with a rowID
            sDatabaseConnector.updateRecord(rowID, updateRow);
        } else {
            // Tweet received with username
            sDatabaseConnector.updateRecordByUsername(updateRow);
        }
        // Reload User info into the layout if the activity is .ViewUser
        if (((Activity) context).getClass().getName().equals(UserDetailActivity.class.getName())) {
            ((UserDetailActivity) context).loadContacts();
        }
    }
}
