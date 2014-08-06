//package chu.ForCHUApps.tweetoffline;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
//
//public class DatabaseOpenHelper extends SQLiteOpenHelper {
//		
//		private String name;
//		// public constructor
//		public DatabaseOpenHelper(Context context, String name,
//				CursorFactory factory, int version) 
//		{
//			setName(name);
//			super(context, name, factory, version);
//		} // end DatabaseOpenHelper constructor
//		
//		public DatabaseOpenHelper(Context context) {
//		    super(context, "Followers", null, 1);
//		 }
//		
//
//		// creates the contacts table when the database is created
//		@Override
//		public void onCreate(SQLiteDatabase db) 
//		{
//			// query to create a new table named contacts
//			String createQuery = "CREATE TABLE " + "Followers" +
//					"(_id integer primary key autoincrement," +
//					"username TEXT UNIQUE, name TEXT,"+" recentTweet TEXT);";
//			db.execSQL(createQuery); // execute the query
//		} // end method onCreate
//
//		@Override
//		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			// TODO Auto-generated method stub
//		}
//		public void setName(String name)
//		{
//			
//		}
//}