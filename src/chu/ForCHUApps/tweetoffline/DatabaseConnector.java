package chu.ForCHUApps.tweetoffline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseConnector {

	private SQLiteDatabase database; // database object
	private DatabaseOpenHelper databaseOpenHelper; // database helper
	private String DATABASE_NAME;

	// public constructor for DatabaseConnector
	public DatabaseConnector(Context context, String DATABASE_NAME) 
	{
		this.DATABASE_NAME = DATABASE_NAME;
		// create a new DatabaseOpenHelper
		databaseOpenHelper = 
				new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
	} // end DatabaseConnector constructor

	// open the database connection
	public void open() throws SQLException 
	{
		// create or open a database for reading/writing
		database = databaseOpenHelper.getWritableDatabase();
	} // end method open

	// close the database connection
	public void close() 
	{
		if (database != null)
			database.close(); // close the database connection
	} // end method close

	// inserts a new user in the database
	public void insertRecord(String username, String name, String recentTweet, String bio)
	{
		ContentValues newRecord = new ContentValues();
		newRecord.put("username", username);
		newRecord.put("name", name);
		newRecord.put("recentTweet", recentTweet);
		newRecord.put("bio", bio);
		open();
		database.insert(DATABASE_NAME, null, newRecord);
		close();
	} // end method insertRecord

	// return a Cursor with all contact information in the database
	public Cursor getAllRecords() 
	{
		return database.query(DATABASE_NAME, null, 
				null, null, null, null, "username COLLATE NOCASE");
	} // end method getAllRecords

	// get a Cursor containing all information about the record specified
	// by the given id
	public Cursor getOneRecord(long id) 
	{
		return database.query(
				DATABASE_NAME, new String[]{"username", "name", "recentTweet", "bio"}, "_id=" + id, null, null, null, null);
	} // end method getOneRecord

	public void deleteRecords()
	{
		open();
		database.delete(DATABASE_NAME,null,null);
		close();
	}

	// delete the user specified by the given String name
	public void deleteRecord(long id)
	{
		open();
		database.delete(DATABASE_NAME, "_id=" + id, null);
		close();
	} // end method deleteContact

	public void updateUser(long id, ContentValues cv) 
	{
		
		open(); // open the database
		database.update(DATABASE_NAME, cv, "_id=" + id, null);
		close(); // close the database
	} // end method updateContact

} // end class DatabaseConnector

