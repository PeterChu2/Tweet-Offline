package chu.ForCHUApps.tweetoffline;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends Activity implements OnSharedPreferenceChangeListener {

	private static final String bioKey = "edittext_set_status";
	private static final String locationKey = "edittext_set_location";
	private static final String twitterNumberKey = "edittext_twitter_number";
	private SMSHelper smsHelper;
	private String twitterNumber;
	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new TwitterPreferenceFragment()).commit();
		smsHelper = new SMSHelper(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if( key.equals(twitterNumberKey ))
		{
			twitterNumber = sharedPreferences.getString(twitterNumberKey, null);
			smsHelper.setTwitterNumber(twitterNumber);
		}
		if( key.equals( locationKey ))
		{
			smsHelper.sendSMS("SET LOCATION " + sharedPreferences.getString(locationKey, null));
		}
		if( key.equals( bioKey ))
		{
			smsHelper.sendSMS("SET BIO " + sharedPreferences.getString(locationKey, null));
		}
	}

}
