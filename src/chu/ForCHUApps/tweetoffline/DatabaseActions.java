package chu.ForCHUApps.tweetoffline;

import chu.ForCHUApps.tweetoffline.MainActivity.TwitterListFragment;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DatabaseActions {

	private static final String TAG = "DEBUG";
	static DatabaseConnector databaseConnector;
	public static void deleteUser(final Context context, String DATABASE_NAME, long rowID, final boolean finish, final TwitterListFragment fragment)
	{
		databaseConnector = new DatabaseConnector(context, DATABASE_NAME);

		// create an AsyncTask that deletes the contact in another 
		// thread, then calls finish after the deletion
		AsyncTask<Long, Object, Object> deleteTask =
				new AsyncTask<Long, Object, Object>()
				{
			@Override
			protected Object doInBackground(Long... params)
			{
				databaseConnector.deleteRecord(params[0]); 
				return null;
			} // end method doInBackground

			@Override
			protected void onPostExecute(Object result)
			{
				if(finish == true)
				{
					((Activity) context).setResult(Activity.RESULT_OK);
					((Activity) context).finish(); // return to the MainActivity
					return;
				}
				fragment.populateListViewFromDB();
			} // end method onPostExecute
				}; // end new AsyncTask

				// execute the AsyncTask to delete contact at rowID
				deleteTask.execute(new Long[] { rowID });


	} // end method deleteUser

	public static void sendMessage(Context context, String DATABASE_NAME,
			final String command,
			final long rowID,
			final SMSHelper smsHelper)
	{
		databaseConnector = new DatabaseConnector(context, DATABASE_NAME);

		// create an AsyncTask that deletes the contact in another 
		// thread, then calls finish after the deletion
		AsyncTask<Long, Object, Object> usernameTask =
				new AsyncTask<Long, Object, Object>()
				{
			@Override
			protected Object doInBackground(Long... params)
			{
				databaseConnector.open();
				String username = databaseConnector.getUsername(params[0]);
				databaseConnector.close();
				return username;
			} // end method doInBackground

			@Override
			protected void onPostExecute(Object username)
			{
				smsHelper.sendSMS(command + (String)username);
			} // end method onPostExecute

				}; // end new AsyncTask

				// execute the AsyncTask to delete contact at rowID
				usernameTask.execute(new Long[] { rowID });
	}

	// Update a user
	public static void updateUser(final ContentValues updateRow, Long rowID, Context context, String DATABASE_NAME)
	{
		databaseConnector = new DatabaseConnector(context, DATABASE_NAME);
		if(rowID != null)
		{
			databaseConnector.updateUser(rowID, updateRow);
		}
		else
		{
			databaseConnector.updateUserByUsername(updateRow);
		}
		if(((Activity) context).getClass().getName().equals("chu.ForCHUApps.tweetoffline.ViewUser"))
		{
			((ViewUser) context).loadContacts();
		}
	} // end method updateUser

}
