package chu.ForCHUApps.tweetoffline.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import chu.ForCHUApps.tweetoffline.util.Constants;
import chu.ForCHUApps.tweetoffline.R;
import chu.ForCHUApps.tweetoffline.sms.SMSHelper;

public class TwitterPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
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
		if( key.equals( Constants.twitterNumberKey ))
		{
			twitterNumber = sharedPreferences.getString(Constants.twitterNumberKey, null);
			smsHelper.setTwitterNumber(twitterNumber);
		}
		if( key.equals( Constants.locationKey ))
		{
			smsHelper.sendSMS("SET LOCATION " + sharedPreferences.getString(Constants.locationKey, null));
		}
		if( key.equals( Constants.bioKey ))
		{
			smsHelper.sendSMS("SET BIO " + sharedPreferences.getString(Constants.locationKey, null));
		}
	}
}
