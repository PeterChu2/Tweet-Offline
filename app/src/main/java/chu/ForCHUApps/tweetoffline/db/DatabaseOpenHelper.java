package chu.ForCHUApps.tweetoffline.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private DatabaseName mDatabaseName;
    public enum DatabaseName {
        FOLLOWERS, FOLLOWING, CUSTOM
    }

    // public constructor
    public DatabaseOpenHelper(Context context, DatabaseName name,
                              CursorFactory factory, int version) {
        super(context, name.name(), factory, version);
        mDatabaseName = name;
    } // end DatabaseOpenHelper constructor

    // creates the contacts table when the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        // query to create a new table named contacts
        String createQuery = String.format(
                "CREATE TABLE %s(_id integer primary key autoincrement," +
                        "username TEXT UNIQUE, name TEXT," +
                        " recentTweet TEXT, bio TEXT, tweetDate INTEGER, pic TEXT);", mDatabaseName.name());
        db.execSQL(createQuery); // execute the query
    } // end method onCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
}