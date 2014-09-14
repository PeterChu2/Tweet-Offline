package chu.ForCHUApps.tweetoffline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.SmsManager;

public class SMSHelper {
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

		// Ensure twitterNumber is configured
		if(twitterNumber == null)
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			alert.setTitle("Configure Twitter shortcode");
			alert.setMessage("Set your Twitter ShortCode in the app settings.\n" +
					"You can find your Short Code from the settings. The app cannot" +
					" perform any actions until the Short Code is configured.");
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing, user confirmed seeing the message
				}
			});
			alert.show();
		}
		else
		{
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(twitterNumber, null, message,null,null);// piSent, piDelivered);
		}
	}

}
