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

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;

import com.dirtyunicorns.support.preferences.SystemSettingSwitchPreference;
import com.dirtyunicorns.support.preferences.CustomSeekBarPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.internal.util.du.Utils;

import java.util.ArrayList;
import java.util.List;

public class NavigationOptions extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_BUTTON_SWAP_KEYS = "swap_navigation_keys";

    private SystemSettingSwitchPreference mSwapKeysPreference;
    private CustomSeekBarPreference mNavigationBarHeight;
    private CustomSeekBarPreference mNavigationBarHeightLandscape;
    private CustomSeekBarPreference mNavigationBarWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_options);
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        int nav_height = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_HEIGHT, 48, UserHandle.USER_CURRENT);
        mNavigationBarHeight = (CustomSeekBarPreference) findPreference("navigation_bar_height");
        mNavigationBarHeight.setTitle(Utils.isPhone(getContext()) ?
                R.string.navigation_bar_height_title :
                R.string.navigation_bar_height_tablets_portrait_title);
        mNavigationBarHeight.setMin(24);
        mNavigationBarHeight.setMax(100);
        mNavigationBarHeight.setValue(nav_height);
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        int nav_height_land = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, 48, UserHandle.USER_CURRENT);
        mNavigationBarHeightLandscape = (CustomSeekBarPreference) findPreference(
                "navigation_bar_height_landscape");
        if (Utils.isPhone(getActivity())) {
            prefSet.removePreference(mNavigationBarHeightLandscape);
            mNavigationBarHeightLandscape = null;
        } else {
            mNavigationBarHeightLandscape.setTitle(
                    R.string.navigation_bar_height_tablets_landscape_title);
            mNavigationBarHeightLandscape.setMin(24);
            mNavigationBarHeightLandscape.setMax(100);
            mNavigationBarHeightLandscape.setValue(nav_height_land);
            mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);
        }

        int nav_width = Settings.System.getIntForUser(resolver,
                Settings.System.NAVIGATION_BAR_WIDTH, 48, UserHandle.USER_CURRENT);
        mNavigationBarWidth = (CustomSeekBarPreference) findPreference("navigation_bar_width");
        if (!Utils.isPhone(getActivity())) {
            prefSet.removePreference(mNavigationBarWidth);
            mNavigationBarWidth = null;
        } else {
            mNavigationBarWidth.setTitle(R.string.navigation_bar_width_title);
            mNavigationBarWidth.setMin(24);
            mNavigationBarWidth.setMax(100);
            mNavigationBarWidth.setValue(nav_width);
            mNavigationBarWidth.setOnPreferenceChangeListener(this);
        }

        mSwapKeysPreference = (SystemSettingSwitchPreference) prefScreen.findPreference(
		KEY_BUTTON_SWAP_KEYS);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNavigationBarHeight) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNavigationBarHeightLandscape) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNavigationBarWidth) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH, val,
                    UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }
}
