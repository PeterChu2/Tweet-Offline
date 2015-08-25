package chu.ForCHUApps.tweetoffline.sms;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chu.ForCHUApps.tweetoffline.db.DatabaseActions;
import chu.ForCHUApps.tweetoffline.db.DatabaseOpenHelper;

public class SMSReceiver extends BroadcastReceiver {
    private String mBio;
    private String mName;
    private String recentTweet;
    private String username;
    private String message2;
    private DatabaseOpenHelper.DatabaseName mDatabaseName;
    private Long rowID;
    private Pattern p = Pattern.compile("(\\d+)");
    private Matcher m;
    private Date tweetDate;

    public SMSReceiver(DatabaseOpenHelper.DatabaseName databaseName, Long rowID) {
        this.mDatabaseName = databaseName;
        this.rowID = rowID;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    // This only works on Android <= 4.3
                    this.abortBroadcast();
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    if (phoneNumber.equals(SMSHelper.twitterNumber)) {
                        String message = currentMessage.getDisplayMessageBody();

                        // Check all possibilities of GET command, which returns the latest tweet of a user

                        if (message.startsWith("@")) {
                            String[] partTwoMessage = message.split(":[\\s]", 2);
                            username = partTwoMessage[0];
                            recentTweet = partTwoMessage[1];
                            ContentValues cv = new ContentValues();

                            String timeStamp = recentTweet.substring(recentTweet.lastIndexOf('('));

                            tweetDate = parseDate(timeStamp);

                            // Replace twitter's relative time stamp in the text with an a static date
                            recentTweet = recentTweet.replace(timeStamp, getFormattedDate(tweetDate));
                            // "\n(" + //tweetDate.format("%Y-%m-%d around %l:%M %p") +")" );

                            cv.put("recentTweet", recentTweet);
                            cv.put("tweetDate", tweetDate.getTime());
                            cv.put("username", username); //Also update username to ensure it has correct capitalization in the db
                            DatabaseActions.updateUser(cv, rowID, context, mDatabaseName);
                            recentTweet = null;
                        } else if (message.startsWith("1/2: @")) {
                            String[] partTwoMessage = message.split(":[\\s]", 3);
                            username = partTwoMessage[1];

                            if (message2 != null) {
                                recentTweet = partTwoMessage[2] + message2;
                                String timeStamp = recentTweet.substring(recentTweet.lastIndexOf('('));

                                tweetDate = parseDate(timeStamp);

                                recentTweet = recentTweet.replace(timeStamp, getFormattedDate(tweetDate));

                                ContentValues cv = new ContentValues();
                                cv.put("recentTweet", recentTweet);
                                cv.put("tweetDate", tweetDate.getTime());
                                cv.put("username", username);
                                DatabaseActions.updateUser(cv, rowID, context, mDatabaseName);
                                message2 = null;
                            } else {
                                recentTweet = partTwoMessage[2];
                            }

                        } else if (message.startsWith("2/2: ")) {
                            String[] partTwoMessage = message.split(":[\\s]", 2);
                            if (recentTweet != null) {
                                recentTweet += partTwoMessage[1];
                                String timeStamp = recentTweet.substring(recentTweet.lastIndexOf('('));

                                tweetDate = parseDate(timeStamp);

                                // Replace twitter's relative time stamp in the text with an a static date
                                recentTweet = recentTweet.replace(timeStamp, getFormattedDate(tweetDate));

                                ContentValues cv = new ContentValues();
                                cv.put("recentTweet", recentTweet);
                                cv.put("tweetDate", tweetDate.getTime());
                                cv.put("username", username);
                                DatabaseActions.updateUser(cv, rowID, context, mDatabaseName);
                                recentTweet = null;
                            } else if (mBio != null) // SMS is part 2 of fetch for mBio
                            {
                                mBio += partTwoMessage[1];
                                mBio = mBio.replaceAll("\n\nReply\\sw/.*", "");
                                ContentValues cv = new ContentValues();
                                cv.put("bio", mBio);
                                cv.put("name", mName);
                                DatabaseActions.updateUser(cv, rowID, context, mDatabaseName);
                                mBio = null;
                            } else // Part 2 of the SMS is received before Part 1
                            {
                                message2 = partTwoMessage[1];
                            }
                        } else {
                            // Check if text was the WHOIS command which returns name and bio
                            String[] biography = message.split(".\nBio: ");
                            if (biography.length > 1) {
                                // Get the first name
                                mName = biography[0].split(",")[0];
                                mBio = biography[1];
                            }
                            // Biography only spans one text
                            else if (message.contains(", since ")) {
                                mName = message.split(",", 2)[0];
                                // String before first occurrence of period is garbage
                                mBio = message.split("\\.\\n", 2)[1];
                                // Remove garbage text
                                mBio = mBio.replaceAll("\n\nReply\\sw/.*", "");
                            }
                            // Not a biography message, return
                            else {
                                return;
                            }

                            if (message.startsWith("1/2: ")) // Bio does not fit in one SMS
                            {
                                // Take remainder of message after the 1/2:
                                mName = mName.substring(5);
                                if (message2 != null) {
                                    mBio += message2;
                                    // Replace junk message at the end with an empty string
                                    mBio = mBio.replaceAll("\n\nReply\\sw/.*", "");
                                    ContentValues cv = new ContentValues();
                                    cv.put("name", mName);
                                    cv.put("bio", mBio);
                                    DatabaseActions.updateUser(cv, rowID, context, mDatabaseName);
                                }
                            } else {
                                ContentValues cv = new ContentValues();
                                cv.put("name", mName);
                                cv.put("bio", mBio);
                                DatabaseActions.updateUser(cv, rowID, context, mDatabaseName);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception in SMSReceiver: " + e);
        }
    }

    private Date parseDate(String timeStamp) {
        m = p.matcher(timeStamp);

        if (m.find()) {
            long conversionFactor = 1;
            // get current time
            Calendar calendar = Calendar.getInstance();
            long currentTimeInMs = calendar.getTime().getTime();

            if (timeStamp.contains("minute")) {
                conversionFactor = 60 * 1000;
            } else if (timeStamp.contains("second")) {
                conversionFactor = 1000;
            } else if (timeStamp.contains("hour")) {
                conversionFactor = 60 * 60 * 1000;
            } else if (timeStamp.contains("day")) {
                conversionFactor = 24 * 60 * 60 * 1000;
            } else if (timeStamp.contains("month")) {
                conversionFactor = 30l * 24l * 60l * 60l * 1000l;
            } else if (timeStamp.contains("year")) {
                conversionFactor = 365l * 30l * 24l * 60l * 60l * 1000l;
            }

            // original tweet date is current time minus time elapsed
            return new Date(currentTimeInMs
                    - conversionFactor * Integer.parseInt(m.group(0)));
        }
        return null;
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat customDateFormat = new SimpleDateFormat("yyyy-MMM-d");
        SimpleDateFormat customTimeFormat = new SimpleDateFormat("h:mm a");
        return String.format("\n(%s around %s)", customDateFormat.format(date),
                customTimeFormat.format(date));
    }
}


