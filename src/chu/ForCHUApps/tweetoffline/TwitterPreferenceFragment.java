package chu.ForCHUApps.tweetoffline;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

public class TwitterPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	private static final String bioKey = "edittext_set_status";
	private static final String locationKey = "edittext_set_location";
	private static final String twitterNumberKey = "edittext_twitter_number";
	private SMSHelper smsHelper;
	private String twitterNumber;
	private SharedPreferences sharedPreferences;
	private Preference signUpPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		smsHelper = new SMSHelper(getActivity());
		signUpPref = (Preference) findPreference("signUp");
		signUpPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				smsHelper.sendSMS("SIGNUP");
				return true;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
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
