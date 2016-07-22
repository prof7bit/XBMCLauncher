package com.gmail.prof7bit.kodilauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

public class LauncherSettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from XML
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();

            Context c = getActivity();

            /*
             * Every time the settings are showm we determine whether we are
             * currently the default launcher and grey out certain preferences.
             */
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo resolveInfo = c.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            String currentHomePackage = resolveInfo.activityInfo.packageName;
            enableClearDefaultPref(currentHomePackage.equals(c.getPackageName()));
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            Context c = getActivity();
            String key = preference.getKey();

            if (key.equals("clear_defaults")) {
                /*
                 * This works only when we are currently the default launcher:
                 * clear the default.
                 */
                PackageManager m = c.getPackageManager();
                m.clearPackagePreferredActivities(c.getPackageName());
                Toast.makeText(c, R.string.toast_kodi_removed_from_home_button, Toast.LENGTH_SHORT).show();
                enableClearDefaultPref(false);

            } else if (key.equals("open_launcher_settings")) {
                /*
                 * open the android settings/home screen
                 */
                Intent intentHomeSettings = new Intent();
                intentHomeSettings.setAction(Settings.ACTION_HOME_SETTINGS);
                startActivity(intentHomeSettings);
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        /**
         * grey out the "Home Settings" and enable the "Clear Launcher Defaults"
         * or vice versa. This is called after it has been determined who is the
         * default Launcher application.
         *
         * @param enabled enable "Clear Default" and disable "Home settings"
         */
        private void enableClearDefaultPref(boolean enabled) {
            Preference p = getPreferenceScreen().findPreference("clear_defaults");
            p.setEnabled(enabled);
            p = getPreferenceScreen().findPreference("open_launcher_settings");
            p.setEnabled(!enabled);
        }
    }
}
