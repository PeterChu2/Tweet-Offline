package chu.ForCHUApps.tweetoffline.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import chu.ForCHUApps.tweetoffline.models.User;

public class DatabaseConnector {
    private SQLiteDatabase database; // database object
    private DatabaseOpenHelper databaseOpenHelper; // database helper
    private DatabaseOpenHelper.DatabaseName mDatabaseName;

    // public constructor for DatabaseConnector
    public DatabaseConnector(Context context, DatabaseOpenHelper.DatabaseName databaseName) {
        this.mDatabaseName = databaseName;
        // create a new DatabaseOpenHelper
        databaseOpenHelper =
                new DatabaseOpenHelper(context, databaseName, null, 1);
    }

    // open the database connection
    public void open() throws SQLException {
        // create or open a database for reading/writing
        database = databaseOpenHelper.getWritableDatabase();
    }

    // close the database connection
    public void close() {
        if (database != null)
            database.close(); // close the database connection
    }

    // inserts a new user in the database
    public void insertRecord(User user) {
        ContentValues newRecord = new ContentValues();
        newRecord.put("username", user.getUsername());
        newRecord.put("name", user.getName());
        newRecord.put("recentTweet", user.getRecentTweet());
        newRecord.put("bio", user.getBio());
        newRecord.put("pic", user.getProfilePicURL());
        open();
        database.insertWithOnConflict(
                mDatabaseName.name(), null, newRecord, SQLiteDatabase.CONFLICT_REPLACE);
        close();
    }

    // return a Cursor with all contact information in the database
    public Cursor getAllRecords(String sortBy) {
        return database.query(mDatabaseName.name(), null,
                null, null, null, null, sortBy + " COLLATE NOCASE");
    }

    // get a Cursor containing all information about the record specified
    // by the given id
    public Cursor getOneRecord(long id) {
        return database.query(
                mDatabaseName.name(), new String[]{"username", "name", "recentTweet", "bio", "pic"},
                "_id=" + id, null, null, null, null);
    }

    public String getUsername(long id) {
        Cursor result = database.query(
                mDatabaseName.name(),
                new String[]{"username"},
                "_id=" + id,
                null, null, null, null);
        result.moveToFirst();

        return result.getString(result.getColumnIndex("username"));
    }

    public void deleteRecords() {
        open();
        database.delete(mDatabaseName.name(), null, null);
        close();
    }

    // delete the user specified by the given String name
    public void deleteRecord(long id) {
        open();
        database.delete(mDatabaseName.name(), "_id=" + id, null);
        close();
    }

    // Update user data by row ID
    public void updateRecord(long id, ContentValues cv) {
        open(); // open the database
        database.update(mDatabaseName.name(), cv, "_id=?", new String[]{String.valueOf(id)});
        close(); // close the database
    }

    public DatabaseOpenHelper.DatabaseName getName() {
        return mDatabaseName;
    }

    // Update user data by username in text
    public void updateRecordByUsername(ContentValues cv) {
        String username;
        username = cv.getAsString("username");
        if (username != null) {
            open();
            database.update(mDatabaseName.name(), cv, "username=?", new String[]{username});
            close();
        }
    }
}

