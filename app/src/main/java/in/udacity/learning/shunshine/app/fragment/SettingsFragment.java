package in.udacity.learning.shunshine.app.fragment;

/**
 * Created by Lokesh on 06-09-2015.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.sync.SunshineSyncAdapter;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        Preference list_preference = findPreference(getString(R.string.pref_keys_unit_type));
        bindPreferenceSummaryToValue(list_preference);


        Preference edit_preference = findPreference(getString(R.string.pref_location_key));
        bindPreferenceSummaryToValue(edit_preference);
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's current value.
        String strPref = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "");
        onPreferenceChange(preference, strPref);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list pref_general, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }

            // No need to sync from server , Only changing unit that can be calculated offline

        } else {
            // For other pref_general, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);

            // As soon as Location changes sync immediately
            //SunshineSyncAdapter.syncImmediately(getActivity());
        }
        return true;
    }
}