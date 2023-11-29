package org.max.budgetcontrol;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    SettingsActivity activity;

    public SettingsFragment(SettingsActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        EditTextPreference etp = findPreference("token");
        etp.setOnPreferenceChangeListener(new PreferenceChangeListener("token"));
        String buff = etp.getText();
        if (buff != null)
            activity.assign("token", buff);
        etp = findPreference("url");
        etp.setOnPreferenceChangeListener(new PreferenceChangeListener("url"));
        buff = etp.getText();
        if (buff != null)
            activity.assign("url", buff);
    }

    class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final String key;

        PreferenceChangeListener(String key) {
            this.key = key;
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            return activity.assign(key, newValue.toString());
        }
    }
}