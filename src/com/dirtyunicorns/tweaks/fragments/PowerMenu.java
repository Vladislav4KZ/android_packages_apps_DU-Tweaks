/*
 * Copyright (C) 2017-2018 The Dirty Unicorns Project
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

package com.dirtyunicorns.tweaks.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.du.Utils;
import com.android.internal.widget.LockPatternUtils;

import java.util.ArrayList;
import java.util.List;

public class PowerMenu extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_LOCKDOWN_IN_POWER_MENU = "lockdown_in_power_menu";
    private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
    private static final String KEY_POWERMENU_TORCH = "powermenu_torch";

    private static final int MY_USER_ID = UserHandle.myUserId();

    private SwitchPreference mPowermenuTorch;
    private SwitchPreference mPowerMenuLockDown;
    private PreferenceCategory mAdvancedCategory;
    private ListPreference mPowerMenuAnimations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.powermenu);

        final PreferenceScreen prefSet = getPreferenceScreen();
        final LockPatternUtils lockPatternUtils = new LockPatternUtils(getActivity());
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mPowerMenuLockDown = (SwitchPreference) findPreference(KEY_LOCKDOWN_IN_POWER_MENU);
        mAdvancedCategory = (PreferenceCategory) findPreference("powermenu_advanced_category");

        if (lockPatternUtils.isSecure(MY_USER_ID)) {
            mPowerMenuLockDown.setChecked((Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCKDOWN_IN_POWER_MENU, 0) == 1));
            mPowerMenuLockDown.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mPowerMenuLockDown);
            prefSet.removePreference(mAdvancedCategory);
        }

        mPowerMenuAnimations = (ListPreference) findPreference(POWER_MENU_ANIMATIONS);
        mPowerMenuAnimations.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.POWER_MENU_ANIMATIONS, 0)));
        mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
        mPowerMenuAnimations.setOnPreferenceChangeListener(this);

        mPowermenuTorch = (SwitchPreference) findPreference(KEY_POWERMENU_TORCH);
        mPowermenuTorch.setOnPreferenceChangeListener(this);
        if (!Utils.deviceHasFlashlight(getActivity())) {
            prefScreen.removePreference(mPowermenuTorch);
        } else {
        mPowermenuTorch.setChecked((Settings.System.getInt(resolver,
                Settings.System.POWERMENU_TORCH, 0) == 1));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPowerMenuLockDown) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCKDOWN_IN_POWER_MENU, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuAnimations) {
            Settings.System.putInt(getContentResolver(), Settings.System.POWER_MENU_ANIMATIONS,
                    Integer.valueOf((String) objValue));
            mPowerMenuAnimations.setValue(String.valueOf(objValue));
            mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            return true;
        } else if (preference == mPowermenuTorch) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWERMENU_TORCH, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }
}
