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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.FontInfo;
import android.content.IFontService;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.settings.display.FontDialogPreference;

import com.android.settings.R;
import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.settings.du.AccentPicker;
import com.dirtyunicorns.tweaks.fragments.ThemePicker;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;

import com.dirtyunicorns.support.colorpicker.ColorPickerPreference;
import com.dirtyunicorns.support.preferences.CustomSeekBarPreference;
import com.dirtyunicorns.support.preferences.SecureSettingSwitchPreference;

public class Interfaces extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    public static final String TAG = "Interfaces";

    private String KEY_ACCENT_PICKER = "accent_picker";
    private String KEY_THEME_PICKER = "theme_picker";

    private static final String QS_HEADER_STYLE = "qs_header_style";
    private static final String QS_TILE_STYLE = "qs_tile_style";
    private static final String QS_PANEL_BG_ALPHA = "qs_panel_bg_alpha";
    private static final String QS_PANEL_COLOR = "qs_panel_color";
    private static final String SYSTEM_THEME_STYLE = "system_theme_style";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";
    private static final String SWITCH_STYLE = "switch_style";
    private static final String KEY_FONT_PICKER_FRAGMENT_PREF = "custom_font";
    private static final String PREF_KEY_CUTOUT = "cutout_settings";

    private SecureSettingSwitchPreference mRoundedFwvals;
    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private ListPreference mSwitchStyle;
    private ListPreference mQsHeaderStyle;
    private ListPreference mSystemThemeStyle;
    private Preference mQsTileStyle;
    private Preference mAccentPicker;
    private Preference mThemePicker;
    private FontDialogPreference mFontPreference;
    private ColorPickerPreference mQsPanelColor;
    private CustomSeekBarPreference mQsPanelAlpha;

    Context mContext;

    IFontService mFontService = IFontService.Stub.asInterface(ServiceManager.getService("dufont"));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interfaces);

        ContentResolver resolver = getActivity().getContentResolver();

        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        mCornerRadius.setOnPreferenceChangeListener(this);
        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int cornerRadius = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                (int) (res.getDimension(resourceIdRadius) / density));
        mCornerRadius.setValue(cornerRadius / 1);

        // Rounded Content Padding
        mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        mContentPadding.setOnPreferenceChangeListener(this);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int contentPadding = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                (int) (res.getDimension(resourceIdPadding) / density));
        mContentPadding.setValue(contentPadding / 1);

        // Rounded use Framework Values
        mRoundedFwvals = (SecureSettingSwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);

        mSystemThemeStyle = (ListPreference) findPreference(SYSTEM_THEME_STYLE);
        int systemThemeStyle = Settings.System.getInt(resolver,
                Settings.System.SYSTEM_THEME_STYLE, 0);
        int themeValueIndex = mSystemThemeStyle.findIndexOfValue(String.valueOf(systemThemeStyle));
        mSystemThemeStyle.setValueIndex(themeValueIndex >= 0 ? themeValueIndex : 0);
        mSystemThemeStyle.setSummary(mSystemThemeStyle.getEntry());
        mSystemThemeStyle.setOnPreferenceChangeListener(this);

        mAccentPicker = findPreference(KEY_ACCENT_PICKER);

        mThemePicker = findPreference(KEY_THEME_PICKER);

        // set qs tile style
        mQsTileStyle = (Preference) findPreference(QS_TILE_STYLE);

        mSwitchStyle = (ListPreference) findPreference(SWITCH_STYLE);
        int switchStyle = Settings.System.getInt(resolver,
                Settings.System.SWITCH_STYLE, 2);
        int switchValueIndex = mSwitchStyle.findIndexOfValue(String.valueOf(switchStyle));
        mSwitchStyle.setValueIndex(switchValueIndex >= 0 ? switchValueIndex : 0);
        mSwitchStyle.setSummary(mSwitchStyle.getEntry());
        mSwitchStyle.setOnPreferenceChangeListener(this);

        mFontPreference = (FontDialogPreference) findPreference(KEY_FONT_PICKER_FRAGMENT_PREF);
        mFontPreference.setSummary(getCurrentFontInfo().fontName.replace("_", " "));

        // set qs header style
        mQsHeaderStyle = (ListPreference) findPreference(QS_HEADER_STYLE);
        int qsHeaderStyle = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_STYLE, 0);
        int headerValueIndex = mQsHeaderStyle.findIndexOfValue(String.valueOf(qsHeaderStyle));
        mQsHeaderStyle.setValueIndex(headerValueIndex >= 0 ? headerValueIndex : 0);
        mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntry());
        mQsHeaderStyle.setOnPreferenceChangeListener(this);

        mQsPanelAlpha = (CustomSeekBarPreference) findPreference(QS_PANEL_BG_ALPHA);
        int qsPanelAlpha = Settings.System.getIntForUser(resolver,
                Settings.System.QS_PANEL_BG_ALPHA, 255, UserHandle.USER_CURRENT);
        mQsPanelAlpha.setValue(qsPanelAlpha);
        mQsPanelAlpha.setOnPreferenceChangeListener(this);

        mQsPanelColor = (ColorPickerPreference) findPreference(QS_PANEL_COLOR);
        int QsColor = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_BG_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mQsPanelColor.setNewPreviewColor(QsColor);
        mQsPanelColor.setOnPreferenceChangeListener(this);

        Preference mCutoutPref = (Preference) findPreference(PREF_KEY_CUTOUT);
        if (!hasPhysicalDisplayCutout(getContext()))
            getPreferenceScreen().removePreference(mCutoutPref);

        updateThemePicker(systemThemeStyle);
    }

    public void updateThemePicker(int systemThemeStyle) {
        switch(systemThemeStyle){ 
            case 0:
                mThemePicker.setEnabled(false);
                break;
            case 1:
                mThemePicker.setEnabled(false);
                break;
            case 2:
                mThemePicker.setEnabled(false);
                break;
            case 3:
                mThemePicker.setEnabled(true);
                break;
            case 4:
                mThemePicker.setEnabled(true);
                break;
            default:
                break;
        }
    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = getContext().getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = res.getIdentifier("com.android.systemui:dimen/rounded_corner_radius", null, null);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        mCornerRadius.setValue((int) (res.getDimension(resourceIdRadius) / density));
        mContentPadding.setValue((int) (res.getDimension(resourceIdPadding) / density));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        ContentResolver resolver = getActivity().getContentResolver();
	 if (preference == mCornerRadius) {
            Settings.Secure.putInt(resolver, Settings.Secure.SYSUI_ROUNDED_SIZE,
                    ((int) newValue) * 1);
            return true;
        } else if (preference == mContentPadding) {
            Settings.Secure.putInt(resolver, Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                    ((int) newValue) * 1);
            return true;
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
            return true;
        } else if (preference == mSwitchStyle) {
            String value = (String) newValue;
            Settings.System.putInt(resolver, Settings.System.SWITCH_STYLE, Integer.valueOf(value));
            int valueIndex = mSwitchStyle.findIndexOfValue(value);
            mSwitchStyle.setSummary(mSwitchStyle.getEntries()[valueIndex]);
            return true;
        } else if (preference == mSystemThemeStyle) {
            String value = (String) newValue;
            Settings.System.putInt(resolver, Settings.System.SYSTEM_THEME_STYLE, Integer.valueOf(value));
            int systemThemeStyle = mSystemThemeStyle.findIndexOfValue(value);
            mSystemThemeStyle.setSummary(mSystemThemeStyle.getEntries()[systemThemeStyle]);
            updateThemePicker(systemThemeStyle);
            return true;
        } else if (preference == mQsHeaderStyle) {
            String value = (String) newValue;
            Settings.System.putInt(resolver, Settings.System.QS_HEADER_STYLE, Integer.valueOf(value));
            int valueIndex = mQsHeaderStyle.findIndexOfValue(value);
            mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntries()[valueIndex]);
            return true;
        } else if (preference == mQsPanelColor) {
            int bgColor = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_BG_COLOR, bgColor,
                    UserHandle.USER_CURRENT);
        } else if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_BG_ALPHA, bgAlpha,
                    UserHandle.USER_CURRENT);
            return true;
	}
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mAccentPicker) {
            AccentPicker.show(this);
        } else if (preference == mQsTileStyle) {
            QsTileStyles.show(this);
        } else if (preference == mThemePicker) {
            ThemePicker.show(this);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private FontInfo getCurrentFontInfo() {
        try {
            return mFontService.getFontInfo();
        } catch (RemoteException e) {
            return FontInfo.getDefaultFontInfo();
        }
    }

    public void stopProgress() {
    	mFontPreference.stopProgress();
    }

    private static boolean hasPhysicalDisplayCutout(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_physicalDisplayCutout);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.interfaces;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
