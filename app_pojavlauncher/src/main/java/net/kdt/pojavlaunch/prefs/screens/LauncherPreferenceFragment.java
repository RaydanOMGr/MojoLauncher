package net.kdt.pojavlaunch.prefs.screens;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.kdt.pojavlaunch.LauncherActivity;
import git.artdeell.mojo.R;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Preference for the main screen, any sub-screen should inherit this class for consistent behavior,
 * overriding only onCreatePreferences
 */
public class LauncherPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.setBackgroundColor(getResources().getColor(R.color.background_app));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_main);
        setupNotificationRequestPreference();
        setupLanguageSelectorPreference();
    }

    private void setupNotificationRequestPreference() {
        Preference mRequestNotificationPermissionPreference = requirePreference("notification_permission_request");
        Activity activity = getActivity();
        if(activity instanceof LauncherActivity) {
            LauncherActivity launcherActivity = (LauncherActivity)activity;
            mRequestNotificationPermissionPreference.setVisible(!launcherActivity.checkForNotificationPermission());
            mRequestNotificationPermissionPreference.setOnPreferenceClickListener(preference -> {
                launcherActivity.askForNotificationPermission(()->mRequestNotificationPermissionPreference.setVisible(false));
                return true;
            });
        }else{
            mRequestNotificationPermissionPreference.setVisible(false);
        }
    }

    private void setupLanguageSelectorPreference() {
        ListPreference languagesListPref = requirePreference("change_language", ListPreference.class);

        List<String> availableLanguages = new ArrayList<>();
        availableLanguages.add("default");
        availableLanguages.addAll(Tools.getLanguages(getContext()));

        List<String> languageNames = new ArrayList<>();
        for (String code : availableLanguages) {
            if(code.equals("default")) languageNames.add("System default");
            else languageNames.add(Tools.getLanguageName(Tools.localeFrom(code)));
        }
        if (languagesListPref != null) {
            languagesListPref.setEntries(languageNames.toArray(new String[0]));
            languagesListPref.setEntryValues(availableLanguages.toArray(new String[0]));
            languagesListPref.setDefaultValue(languageNames.get(0));

            languagesListPref.setOnPreferenceChangeListener((preference, newValue) -> {
                LauncherPreferences.DEFAULT_PREF.edit()
                        .putString("selected_language", (String) newValue)
                        .apply();
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if(sharedPreferences != null) sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        if(sharedPreferences != null) sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        LauncherPreferences.loadPreferences(getContext());
    }

    protected Preference requirePreference(CharSequence key) {
        Preference preference = findPreference(key);
        if(preference != null) return preference;
        throw new IllegalStateException("Preference "+key+" is null");
    }
    @SuppressWarnings("unchecked")
    protected <T extends Preference> T requirePreference(CharSequence key, Class<T> preferenceClass) {
        Preference preference = requirePreference(key);
        if(preferenceClass.isInstance(preference)) return (T)preference;
        throw new IllegalStateException("Preference "+key+" is not an instance of "+preferenceClass.getSimpleName());
    }
}
