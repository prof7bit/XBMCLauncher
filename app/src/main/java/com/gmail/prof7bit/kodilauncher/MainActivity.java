package com.gmail.prof7bit.kodilauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;


public class MainActivity extends Activity {

    private static final String TAG = "KODILauncher";
    private static final int WAIT_TIME = 15;
    private int counter;
    private SharedPreferences settings;
    private Toast toast;
    private Handler handler = new Handler();


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void toast_show(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void launch() {
        // Load preferences
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
                toast_show("Kodi launcher couldn't execute su, couldn't fix permissions, needs root!");

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

        counter = WAIT_TIME;
        try_launch();
    }

    public void try_launch() {
        if (isNetworkAvailable() || (counter == 0)) {
            final String xbmcPackage = settings.getString("xbmc_variant", getString(R.string.xbmc_package_default));

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(xbmcPackage);
            if (launchIntent != null) {
                Log.d(TAG, "starting " + launchIntent.toString());
                startActivity(launchIntent);
            } else {
                Log.e(TAG, "could not find " + xbmcPackage + ", opening launcher settings instead");
                Toast.makeText(getApplicationContext(), "Kodi launcher couldn't find " + xbmcPackage + ", please check the selected Kodi variant", Toast.LENGTH_SHORT).show();
                Intent launchSettings = new Intent(this, LauncherSettingsActivity.class);
                startActivity(launchSettings);
            }
        } else {
            if (counter < WAIT_TIME - 1) {
                toast_show("waiting for network " + counter);
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try_launch();
                }
            }, 1000);
            counter -= 1;
        }
    }

    public void onResume() {
        super.onResume();
        launch();
    }
}
