package chu.ForCHUApps.tweetoffline;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DatabaseOpenHelper extends SQLiteOpenHelper 
{
	private String DATABASE_NAME;
	// public constructor
	public DatabaseOpenHelper(Context context, String name,
			CursorFactory factory, int version) 
	{

		super(context, name, factory, version);
		DATABASE_NAME = name;
	} // end DatabaseOpenHelper constructor

	// creates the contacts table when the database is created
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// query to create a new table named contacts
		String createQuery = "CREATE TABLE " + DATABASE_NAME +
				"(_id integer primary key autoincrement," +
				"username TEXT UNIQUE, name TEXT," +
				" recentTweet TEXT, bio TEXT, tweetDate INTEGER, pic TEXT);";
		db.execSQL(createQuery); // execute the query
	} // end method onCreate

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
} // end class DatabaseOpenHelper