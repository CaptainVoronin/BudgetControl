package org.max.budgetcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {
    static final Pattern p;
    static {
        p = Pattern.compile("[а-яА-Я]");
    }
    boolean connectionProblems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
            connectionProblems = extras.getBoolean(MainActivity.CONNECTION_PROBLEM, false);

        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(connectionProblems))
                    .commit();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        boolean connectionProblems;

        public SettingsFragment(boolean connectionProblems) {
            this.connectionProblems = connectionProblems;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            EditTextPreference etp = ( EditTextPreference)  findPreference( "token" );
            etp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    String val = newValue.toString();
                    if( val.trim().length() == 0 )
                        return false;
                    else {
                        Matcher m = p.matcher( val );
                        if( m.find() )
                            return false;
                        else
                            return true;
                    }
                }
            });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.idCancel) {
            exitActivity();
            return true;
        } else if (item.getItemId() == R.id.idSave) {
            saveChanges();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {

    }

    private void exitActivity() {

    }
}