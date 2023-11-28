package org.max.budgetcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.IZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {
    boolean connectionProblems;

    boolean settingsCompleteAndChecked;

    public String token;

    public URL url;

    Pattern tokenPattern;

    Pattern urlPattern;

    private boolean backAcquired;

    public SettingsActivity() {
        super();
        settingsCompleteAndChecked = false;
        tokenPattern = Pattern.compile("[^0-9a-zA-Z]");
        urlPattern = Pattern.compile("[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");

    }

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
                    .replace(R.id.settings, new SettingsFragment(this))
                    .commit();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void onBackPressed() {
        if (settingsCompleteAndChecked)
            super.onBackPressed();
        else {
            backAcquired = true;
            checkConnection();
        }
    }

    void checkConnection() {
        if (url != null && token != null) {
            ZenMoneyClient client = new ZenMoneyClient(url, token, new CheckConnectionHandler());
            client.checkConnection();
        }
    }

    public boolean assign(@NonNull String key, @NonNull Object value) {
        boolean ret = false;
        String buff;
        if (key.equals("token")) {
            buff = value.toString();
            ret = checkFormat(tokenPattern, buff);
            token = ret ? buff : null;
        } else if (key.equals("url")) {
            buff = value.toString();
            try {
                if (checkFormat(urlPattern, buff)) {
                    ret = false;
                    url = null;
                } else {
                    ret = true;
                    url = new URL(buff);
                }
            } catch (MalformedURLException e) {
                ret = false;
                url = null;
                e.printStackTrace();
            }
        }
        return ret;
    }

    private boolean checkFormat(Pattern pattern, String buffer) {
        if (buffer.trim().length() == 0)
            return false;
        else {
            Matcher m = pattern.matcher(buffer);
            if (m.find())
                return false;
            else
                return true;
        }
    }

    private class CheckConnectionHandler implements IZenClientResponseHandler {
        @Override
        public void onNon200Code(Response responze) {
            Log.w(this.getClass().getName(), "[onNon200Code] HTTP " + responze.code());
            settingsCompleteAndChecked = false;
        }

        @Override
        public void onResponseReceived(JSONObject jObject) throws JSONException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(this.getClass().getName(), "[onResponseReceived] Connected successfully");
                    settingsCompleteAndChecked = true;
                    if (backAcquired) {
                        Log.i(this.getClass().getName(), "[onResponseReceived] Exit activity");
                        SettingsActivity.this.onBackPressed();
                    }
                }
            });
        }

        @Override
        public void processError(Exception e) {
            Log.e(this.getClass().getName(), "[processError] Exception " + e.getMessage());
            e.printStackTrace();
            settingsCompleteAndChecked = false;
        }
    }
}