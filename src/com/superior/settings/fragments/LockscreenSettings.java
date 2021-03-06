/*
 * Copyright (C) 2018 The Superior OS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.superior.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.superior.settings.preferences.SecureSettingMasterSwitchPreference;
import com.superior.settings.preferences.SystemSettingSwitchPreference;

import com.superior.settings.R;

public class LockscreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker_category";
    private static final String AMBIENT_VISUALIZER_ENABLED = "ambient_visualizer_enabled";
    private static final String FOD_ANIMATION = "fod_anim";

    private SecureSettingMasterSwitchPreference mVisualizerEnabled;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private PreferenceCategory mFODIconPickerCategory;
    private SystemSettingSwitchPreference mAmbientVisualizerEnabled;
    private Preference mFODAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.superior_settings_lockscreen);
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();
        Context mContext = getContext();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

        mAmbientVisualizerEnabled = (SystemSettingSwitchPreference) findPreference(AMBIENT_VISUALIZER_ENABLED);
        updateAmbientVisualizer(visualizerEnabled == 1);

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null
                && !getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView)) {
            prefScreen.removePreference(mFODIconPickerCategory);
        }

        boolean showFODAnimationPicker = mContext.getResources().getBoolean(R.bool.showFODAnimationPicker);
        mFODAnimation = (Preference) findPreference(FOD_ANIMATION);
        if ((mFODIconPickerCategory != null && mFODAnimation != null &&
             !getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView)) ||
                (mFODIconPickerCategory != null && mFODAnimation != null && !showFODAnimationPicker)) {
            mFODIconPickerCategory.removePreference(mFODAnimation);
        }
    }

    private void updateAmbientVisualizer(boolean value) {
        if (!value) {
            mAmbientVisualizerEnabled.setChecked(false);
        }
        mAmbientVisualizerEnabled.setEnabled(value);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SUPERIOR;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            updateAmbientVisualizer(value);
            return true;
        }
        return false;
    }
}
