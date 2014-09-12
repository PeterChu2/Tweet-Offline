package chu.ForCHUApps.tweetoffline;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class TwitterPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}