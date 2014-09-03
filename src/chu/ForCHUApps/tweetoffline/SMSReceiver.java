package chu.ForCHUApps.tweetoffline;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver{

	private static final String TAG = "SmsReceiver";
	private String bio;
	private String name;
	private String recentTweet;
	private String[] biography;
	private String username;
	private String[] partTwoMessage;
	private String message;
	private String message2;
	private String DATABASE_NAME;
	private long rowID;

	public SMSReceiver(String DATABASE_NAME, long rowID) {
		this.DATABASE_NAME = DATABASE_NAME;
		this.rowID = rowID;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Retrieves a map of extended data from the intent.
		//			if(intent.getAction().equals(SMS_ACTION))
		final Bundle bundle = intent.getExtras();

		try {

			if (bundle != null) {

				final Object[] pdusObj = (Object[]) bundle.get("pdus");

				for (int i = 0; i < pdusObj.length; i++) {

					// This only works on Android <= 4.3
					this.abortBroadcast();

					SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
					String phoneNumber = currentMessage.getDisplayOriginatingAddress();
					if( phoneNumber.equals(SMSHelper.twitterNumber) )
					{
						message = currentMessage.getDisplayMessageBody();

						// Check all possibilities of GET command, which returns the latest tweet of a user

						if(message.startsWith("@"))
						{
							partTwoMessage = message.split(":[\\s]", 2);
							username = partTwoMessage[0];
							recentTweet = partTwoMessage[1];
							ContentValues cv = new ContentValues();
							cv.put("recentTweet", recentTweet);
							cv.put("username", username); //Also update username to ensure it has correct capitalization in the db
							DatabaseActions.updateUser(cv, rowID, context, DATABASE_NAME);
							recentTweet = null;
						}
						else if(message.startsWith("1/2: @"))
						{
							partTwoMessage = message.split(":[\\s]", 3);

							if(message2 != null)
							{
								recentTweet = partTwoMessage[2] + message2;
								ContentValues cv = new ContentValues();
								cv.put("recentTweet", recentTweet);
								DatabaseActions.updateUser(cv, rowID, context, DATABASE_NAME);
								message2 = null;
							}
							else{
								recentTweet = partTwoMessage[2];
							}

						}
						else if(message.startsWith("2/2: "))
						{
							partTwoMessage = message.split(":[\\s]", 2);
							if(recentTweet != null)
							{
								recentTweet += partTwoMessage[1];
								ContentValues cv = new ContentValues();
								cv.put("recentTweet", recentTweet);
								DatabaseActions.updateUser(cv, rowID, context, DATABASE_NAME);
								recentTweet = null;
							}
							else if(bio != null) // SMS is part 2 of fetch for bio
							{
								bio += partTwoMessage[1];
								bio = bio.replaceAll("\n\nReply\\sw/.*", "");
								ContentValues cv = new ContentValues();
								cv.put("bio", bio);
								cv.put("name", name);
								DatabaseActions.updateUser(cv, rowID, context, DATABASE_NAME);
								bio = null;
							}
							else // Part 2 of the SMS is received before Part 1
							{	
								message2 = partTwoMessage[1];
							}
						}

						else
						{
							// Check if text was the WHOIS command which returns name and bio
							biography =  message.split(".\nBio: ");
							if(biography.length > 1)
							{
								// Get the first name
								name = biography[0].split(",")[0];
								bio = biography[1];

								if(message.startsWith("1/2: ")) // Bio does not fit in one SMS
								{
									name = name.substring(5);
									if(message2 != null)
									{
										bio += message2;
										// Replace junk message at the end with an empty string
										bio = bio.replaceAll("\n\nReply\\sw/.*", "");
										ContentValues cv = new ContentValues();
										cv.put("name", name);
										cv.put("bio", bio);
										DatabaseActions.updateUser(cv, rowID, context, DATABASE_NAME);
									}
								}
								else
								{
									ContentValues cv = new ContentValues();
									cv.put("name", name);
									cv.put("bio", bio);
									DatabaseActions.updateUser(cv, rowID, context, DATABASE_NAME);
								}
							}
						}
					}
				} // end for loop
			} // bundle is null

		} catch (Exception e) {
			Log.e("SmsReceiver", "Exception SMSReceiver" + e);
		}
	}
}


