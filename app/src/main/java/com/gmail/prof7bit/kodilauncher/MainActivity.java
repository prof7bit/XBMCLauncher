package com.gmail.prof7bit.kodilauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;

public class MainActivity extends Activity {

    private static final String TAG = "KODILauncher";


    private void launch() {
        // Load preferences
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean("fix_permissions", true)) {
            Process process = null;
            DataOutputStream dataOutputStream = null;

            try {
                process = Runtime.getRuntime().exec("su");
                dataOutputStream = new DataOutputStream(process.getOutputStream());
                dataOutputStream.writeBytes("chmod 666 /sys/class/display/mode\n");
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
                process.waitFor();

            } catch (Exception e) {
                Log.e(TAG, "could not execute su");
                Toast.makeText(getApplicationContext(), "Kodi launcher couldn't execute su, couldn't fix permissions, needs root!", Toast.LENGTH_LONG).show();

            } finally {
                try {
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    process.destroy();

                } catch (Exception e) {
                }
            }
        }

        String xbmcPackage = settings.getString("xbmc_variant", getString(R.string.xbmc_package_default));

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(xbmcPackage);
        if (launchIntent != null) {
            Log.d(TAG, "starting " + launchIntent.toString());
            startActivity(launchIntent);
        } else {
            Log.e(TAG, "could not find " + xbmcPackage + ", opening launcher settings instead");
            Toast.makeText(getApplicationContext(), "Kodi launcher couldn't find " + xbmcPackage + ", please check the selected Kodi variant", Toast.LENGTH_LONG).show();
            Intent launchSettings = new Intent(this, LauncherSettingsActivity.class);
            startActivity(launchSettings);
        }
    }

    public void onResume() {
        super.onResume();
        launch();
    }
}
