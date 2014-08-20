package chu.ForCHUApps.tweetoffline;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SMSHelper {
	private static String SENT = "SMS_SENT";
	private static String DELIVERED = "SMS_DELIVERED";
	public static String twitterNumber;
	private Context context;
	
	public SMSHelper(Context context)
	{
		this.context = context;
	}
	
	public void setTwitterNumber(String twitterNumber) {
		SMSHelper.twitterNumber = twitterNumber;
	}

	// ---sends an SMS message to another device---
	public void sendSMS(String message) {

		PendingIntent piSent = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
		PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0,new Intent(DELIVERED), 0);

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(twitterNumber, null, message,null,null);// piSent, piDelivered);

	}

}
