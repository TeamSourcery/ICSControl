
package com.sourcery.icscontrol.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Spannable;
import android.widget.EditText;

import com.sourcery.icscontrol.R;
import com.sourcery.icscontrol.SettingsPreferenceFragment;
import com.sourcery.icscontrol.util.CMDProcessor;
import com.sourcery.icscontrol.util.Helpers;

public class UserInterface extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";

    private static final String PREF_CRT_ON = "crt_on";
    private static final String PREF_CRT_OFF = "crt_off";
private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
    private static final String PREF_IME_SWITCHER = "ime_switcher";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
    private static final String PREF_ROTATION_ANIMATION = "rotation_animation_delay";
    private static final String PREF_180 = "rotate_180";

    CheckBoxPreference mCrtOnAnimation;
    CheckBoxPreference mCrtOffAnimation;
CheckBoxPreference mEnableVolumeOptions;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mLongPressToKill;
    CheckBoxPreference mAllow180Rotation;
    CheckBoxPreference mHorizontalAppSwitcher;
    Preference mCustomLabel;
    ListPreference mAnimationRotationDelay;
    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mDisableBootAudio;
    CheckBoxPreference mDisableBugMailer;

    String mCustomLabelText = null;
    int newDensityValue;

  

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        PreferenceScreen prefs = getPreferenceScreen();

        mCrtOffAnimation = (CheckBoxPreference) findPreference(PREF_CRT_OFF);
        mCrtOffAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CRT_OFF_ANIMATION, 1) == 1);

        mCrtOnAnimation = (CheckBoxPreference) findPreference(PREF_CRT_ON);
        mCrtOnAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CRT_ON_ANIMATION, 0) == 1);

 mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
        mEnableVolumeOptions.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ENABLE_VOLUME_OPTIONS, 0) == 1);


        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mShowImeSwitcher.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, 1) == 1);

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mLongPressToKill = (CheckBoxPreference) findPreference(PREF_LONGPRESS_TO_KILL);
        mLongPressToKill.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);

        mAnimationRotationDelay = (ListPreference) findPreference(PREF_ROTATION_ANIMATION);
        mAnimationRotationDelay.setOnPreferenceChangeListener(this);
        mAnimationRotationDelay.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                200) + "");

        mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
        mAllow180Rotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));

        mHorizontalAppSwitcher = (CheckBoxPreference) findPreference("horizontal_recents_task_panel");
        mHorizontalAppSwitcher.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(),
                Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, 0) == 1);

              
        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        if (mDisableBootAnimation.isChecked())
            mDisableBootAnimation.setSummary(R.string.disable_bootanimation_summary);

       mDisableBootAudio = (CheckBoxPreference) findPreference("disable_bootaudio");
        
        if(!new File("/system/media/boot_audio.mp3").exists() &&
                !new File("/system/media/boot_audio.unicorn").exists() ) {
            mDisableBootAudio.setEnabled(false);
            mDisableBootAudio.setSummary(R.string.disable_bootaudio_summary_disabled);
        } else {
            mDisableBootAudio.setChecked(!new File("/system/media/boot_audio.mp3").exists());
            if (mDisableBootAudio.isChecked())
                mDisableBootAudio.setSummary(R.string.disable_bootaudio_summary);
        }

        mDisableBugMailer = (CheckBoxPreference) findPreference("disable_bugmailer");
        mDisableBugMailer.setChecked(!new File("/system/bin/bugmailer.sh").exists());

        if (!getResources().getBoolean(com.android.internal.R.bool.config_enableCrtAnimations)) {
            prefs.removePreference((PreferenceGroup) findPreference("crt"));
        } else {
            // can't get this working in ICS just yet
            ((PreferenceGroup) findPreference("crt")).removePreference(mCrtOnAnimation);
        }
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null) {
            mCustomLabel
                    .setSummary("Custom label currently not set. Once you specify a custom one, there's no way back without doing a data wipe.");
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mCrtOffAnimation) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CRT_OFF_ANIMATION, checked ? 1 : 0);
            return true;

        } else if (preference == mCrtOnAnimation) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CRT_ON_ANIMATION, checked ? 1 : 0);
            return true;

        } else if (preference == mEnableVolumeOptions) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ENABLE_VOLUME_OPTIONS, checked ? 1 : 0);
            return true;

        } else if (preference == mShowImeSwitcher) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER, checked ? 1 : 0);
            return true;

        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("Custom Carrier Label");
            alert.setMessage("Please enter a new one!");

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else if (preference == mLongPressToKill) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.KILL_APP_LONGPRESS_BACK, checked ? 1 : 0);
            return true;

        } else if (preference == mAllow180Rotation) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8)
                            : (1 | 2 | 8));
            return true;

        } else if (preference == mHorizontalAppSwitcher) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, checked ? 1
                            : 0);
            Helpers.restartSystemUI();
            return true;

        } else if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.backup");
                Helpers.getMount("ro");
                preference.setSummary(R.string.disable_bootanimation_summary);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.backup /system/media/bootanimation.zip");
                Helpers.getMount("ro");
                preference.setSummary("");
            }
            return true;

         } else if (preference == mDisableBootAudio) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/boot_audio.mp3 /system/media/boot_audio.disabled");
                Helpers.getMount("ro");
                preference.setSummary(R.string.disable_bootaudio_summary);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/boot_audio.disabled /system/media/boot_audio.mp3");
                Helpers.getMount("ro");
                preference.setSummary("");
            }
            return true;

        } else if (preference == mDisableBugMailer) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/bugmailer.sh /system/bin/bugmailer.sh.backup");
                Helpers.getMount("ro");
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/bugmailer.sh.backup /system/bin/bugmailer.sh");
                Helpers.getMount("ro");
            }
            return true;
        
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAnimationRotationDelay) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                    Integer.parseInt((String) newValue));

            return true;
        }
        return false;
    }

   
    public static void addButton(Context context, String key) {
        ArrayList<String> enabledToggles = Navbar
                .getButtonsStringArray(context);
        enabledToggles.add(key);
        Navbar.setButtonsFromStringArray(context, enabledToggles);
    }

    public static void removeButton(Context context, String key) {
        ArrayList<String> enabledToggles = Navbar
                .getButtonsStringArray(context);
        enabledToggles.remove(key);
        Navbar.setButtonsFromStringArray(context, enabledToggles);
    }

}
