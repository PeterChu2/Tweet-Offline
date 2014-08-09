package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver{

	final SmsManager smsManager = SmsManager.getDefault();
	private Activity activity;
	private static final String TAG = "SmsReceiver";
	private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";



	public SMSReceiver(Activity activity) {
		this.activity = activity;
	}
//	 @Override
//	    public void onReceive(Context context, Intent intent) {
//	        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
//	            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//	                String messageBody = smsMessage.getMessageBody();
//	            }
//	        }
//	    }
	
//	 for (int i = 0; i &lt; messages.length; i++) {
//         SmsMessage message = messages[i];
//         String phNum = message.getOriginatingAddress();
//         if (phNum.equals("000000")) {
//            this.abortBroadcast();
//            // do something
//         } else {
//            // do something else
//         }
//
//     }
	
//	 if (intent.getAction().equals(ACTION)) {
//	        StringBuilder buf = new StringBuilder();
//	        Bundle bundle = intent.getExtras();
//	        if (bundle != null) {
//	            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
//	            for (int i = 0; i &lt; messages.length; i++) {
//	                SmsMessage message = messages[i];
//	                String phNum = message.getDisplayOriginatingAddress();
//	                if ("xxx-xxx-xxxx".equals(phNum))
//	                {// Do your thing }
//	            }
//	        }
//
//	    }
	
	
	
	
	
	
	
	
//	StringBuilder smsBuilder = new StringBuilder();
//    final String SMS_URI_INBOX = "content://sms/inbox"; 
//     final String SMS_URI_ALL = "content://sms/";  
//     try {  
//         Uri uri = Uri.parse(SMS_URI_INBOX);  
//         String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };  
//         Cursor cur = getContentResolver().query(uri, projection, "address='123456789'", null, "date desc");
//          if (cur.moveToFirst()) {  
//             int index_Address = cur.getColumnIndex("address");  
//             int index_Person = cur.getColumnIndex("person");  
//             int index_Body = cur.getColumnIndex("body");  
//             int index_Date = cur.getColumnIndex("date");  
//             int index_Type = cur.getColumnIndex("type");         
//             do {  
//                 String strAddress = cur.getString(index_Address);  
//                 int intPerson = cur.getInt(index_Person);  
//                 String strbody = cur.getString(index_Body);  
//                 long longDate = cur.getLong(index_Date);  
//                 int int_Type = cur.getInt(index_Type);  
//
//                 smsBuilder.append("[ ");  
//                 smsBuilder.append(strAddress + ", ");  
//                 smsBuilder.append(intPerson + ", ");  
//                 smsBuilder.append(strbody + ", ");  
//                 smsBuilder.append(longDate + ", ");  
//                 smsBuilder.append(int_Type);  
//                 smsBuilder.append(" ]\n\n");  
//             } while (cur.moveToNext());  
//
//             if (!cur.isClosed()) {  
//                 cur.close();  
//                 cur = null;  
//             }  
//         } else {  
//             smsBuilder.append("no result!");  
//         } // end if  
//         }
//     } catch (SQLiteException ex) {  
//         Log.d("SQLiteException", ex.getMessage());  
//     }  
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		// Retrieves a map of extended data from the intent.
//		if(intent.getAction().equals(SMS_ACTION))
		final Bundle bundle = intent.getExtras();

		try {

			if (bundle != null) {

				final Object[] pdusObj = (Object[]) bundle.get("pdus");

				for (int i = 0; i < pdusObj.length; i++) {

					this.abortBroadcast();
					
					SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
//					 SmsMessage msgs = SmsMessage.createFromPdu((byte[]) smsExtra[smsExtra.length - 1]); // Get the newest message
//					String senderPhoneNumber=msgs[0].getOriginatingAddress ();  
					String phoneNumber = currentMessage.getDisplayOriginatingAddress();

					String senderNum = phoneNumber;
					String message = currentMessage.getDisplayMessageBody();

					Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);


					// Show Alert
					int duration = Toast.LENGTH_LONG;
					Toast toast = Toast.makeText(context, 
							"senderNum: "+ senderNum + ", message: " + message, duration);
					toast.show();
					
					
		            
				} // end for loop
			} // bundle is null

		} catch (Exception e) {
			Log.e("SmsReceiver", "Exception smsReceiver" +e);

		}
	}

}
