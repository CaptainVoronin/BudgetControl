package org.max.budgetcontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.max.budgetcontrol.datasource.AZenClientResponseHandler;
import org.max.budgetcontrol.datasource.ZenMoneyClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    boolean connectionProblems;

    boolean settingsCompleteAndChecked;

    public String token;

    public URL url;

    Pattern tokenPattern;

    Pattern urlPattern;

    private boolean backAcquired;

    SettingsFragment settingsFragment;

    AlertDialog dlgCheckConnection;

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
        settingsFragment = new SettingsFragment(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }

       Toolbar tb = findViewById( R.id.toolbar );
       setSupportActionBar( tb );
       ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle( R.string.settings_avtivity_title );
        }
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
            CheckConnectionHandler handler = new CheckConnectionHandler();
            ZenMoneyClient client = new ZenMoneyClient(url, token, handler);

            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setMessage("Check connection").setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                handler.cancelRequest();
                dialogInterface.cancel();
            });
            dlg.setCancelable( false );
            dlgCheckConnection = dlg.create();
            dlgCheckConnection.show();

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

    private boolean checkFormat(@NonNull Pattern pattern, @NonNull String buffer) {
        if (buffer.trim().length() == 0)
            return false;
        else {
            Matcher m = pattern.matcher(buffer);
            return !m.find();
        }
    }

    private class CheckConnectionHandler extends AZenClientResponseHandler {
        @Override
        public void onNon200Code(@NonNull Response response) {
            Log.w(this.getClass().getName(), "[onNon200Code] HTTP " + response.code());
            runOnUiThread( () -> {
                settingsCompleteAndChecked = false;
                //EditTextPreference preference =  settingsFragment.findPreference("token");
                closeDialog();
                String message;

                switch (response.code()) {
                    case 401:
                        message = getApplicationContext().getString(R.string.authentication_failed);
                        break;
                    case 500:
                        message = getApplicationContext().getString( R.string.message_internal_server_error );
                        break;
                    default:
                        message = getApplicationContext().getString(R.string.common_http_error) + response.code();
                }

                AlertDialog.Builder dlg = new AlertDialog.Builder(SettingsActivity.this);
                dlg.setMessage(message).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    dialogInterface.cancel();
                });
                dlg.show();
            });
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
                    closeDialog();
                }
            });
        }

        @Override
        public void processError(Exception e) {
            runOnUiThread( ()-> {
                Log.e(this.getClass().getName(), "[processError] Exception " + e.getMessage());
                e.printStackTrace();
                settingsCompleteAndChecked = false;
                closeDialog();
                String message = getApplicationContext().getString(R.string.enternal_request_error)
                        + " " + e.getMessage();
                AlertDialog.Builder dlg = new AlertDialog.Builder(SettingsActivity.this);
                dlg.setMessage(message).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    dialogInterface.cancel();
                });
                dlg.show();
            });
        }
    }

    void closeDialog() {
        dlgCheckConnection.dismiss();
        dlgCheckConnection.cancel();
    }
}