package in.udacity.learning.shunshine.app.fragment;

/**
 * Created by Lokesh on 06-09-2015.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.sync.SunshineSyncAdapter;
import in.udacity.learning.utility.Utility;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        Preference list_preference = findPreference(getString(R.string.pref_keys_unit_type));
        bindPreferenceSummaryToValue(list_preference);

        Preference edit_preference = findPreference(getString(R.string.pref_keys_location));
        bindPreferenceSummaryToValue(edit_preference);

        Preference edit_icon_pack = findPreference(getString(R.string.pref_key_icon));
        bindPreferenceSummaryToValue(edit_icon_pack);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onResume();
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

        } else if (preference.getKey().equalsIgnoreCase(getString(R.string.pref_keys_location))) {
            @SunshineSyncAdapter.locationStatus int status = Utility.getLocationStatus(getActivity());
            switch (status) {
                case SunshineSyncAdapter.LOCATION_STATUS_OK:
                    preference.setSummary(stringValue);
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN:
                    preference.setSummary(getString(R.string.pref_location_unknown_description, value.toString()));
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                    preference.setSummary(getString(R.string.pref_location_error_description, value.toString()));
                    break;
                default:
                    // Note --- if the server is down we still assume the value is valid
                    preference.setSummary(stringValue);
            }
        } else {
            // For other pref_general, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase(getString(R.string.pref_keys_location))) {
            Utility.resetLocationStatus(getActivity());
            SunshineSyncAdapter.syncImmediately(getActivity());
        } else if (key.equalsIgnoreCase(getString(R.string.pref_keys_location_key_status))) {
            // our location status has changed.  Update the summary accordingly
            Preference locationPreference = findPreference(getString(R.string.pref_keys_location));
            bindPreferenceSummaryToValue(locationPreference);
        } else if (key.equals(getString(R.string.pref_keys_unit_type))) {
            // units have changed. update lists of weather entries accordingly
            getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }else if ( key.equals(getString(R.string.pref_key_icon)) ) {
            // art pack have changed. update lists of weather entries accordingly
            getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }
    }
}