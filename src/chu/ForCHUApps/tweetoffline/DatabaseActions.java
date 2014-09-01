package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

public class DatabaseActions {

	static DatabaseConnector databaseConnector;
	private Activity activity;

	public static void deleteUser(final Context context, String DATABASE_NAME, long rowID, final boolean finish)
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
				}
			} // end method onPostExecute
				}; // end new AsyncTask

				// execute the AsyncTask to delete contact at rowID
				deleteTask.execute(new Long[] { rowID });


	} // end method deleteContact
}
