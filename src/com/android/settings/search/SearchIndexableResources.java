/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import android.provider.SearchIndexableResource;
import android.support.annotation.DrawableRes;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.XmlRes;
import com.android.settings.DateTimeSettings;
import com.android.settings.DevelopmentSettings;
import com.android.settings.DeviceInfoSettings;
import com.android.settings.DisplaySettings;
import com.android.settings.LegalSettings;
import com.android.settings.PrivacySettings;
import com.android.settings.R;
import com.android.settings.ScreenPinningSettings;
import com.android.settings.SecuritySettings;
import com.android.settings.WallpaperTypeSettings;
import com.android.settings.WirelessSettings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.accounts.UserAndAccountDashboardFragment;
import com.android.settings.applications.AdvancedAppSettings;
import com.android.settings.applications.AppAndNotificationDashboardFragment;
import com.android.settings.applications.SpecialAccessSettings;
import com.android.settings.backup.BackupSettingsFragment;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment;
import com.android.settings.datausage.DataUsageMeteredSettings;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.deviceinfo.StorageDashboardFragment;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.display.ScreenZoomSettings;
import com.android.settings.enterprise.EnterprisePrivacySettings;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.fuelgauge.PowerUsageDetail;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.gestures.DoubleTapPowerSettings;
import com.android.settings.gestures.DoubleTapScreenSettings;
import com.android.settings.gestures.DoubleTwistGestureSettings;
import com.android.settings.gestures.GestureSettings;
import com.android.settings.gestures.PickupGestureSettings;
import com.android.settings.gestures.SwipeToNotificationSettings;
import com.android.settings.inputmethod.AvailableVirtualKeyboardFragment;
import com.android.settings.inputmethod.InputAndGestureSettings;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.inputmethod.PhysicalKeyboardFragment;
import com.android.settings.inputmethod.VirtualKeyboardFragment;
import com.android.settings.location.LocationSettings;
import com.android.settings.location.ScanningSettings;
import com.android.settings.network.NetworkDashboardFragment;
import com.android.settings.nfc.PaymentSettings;
import com.android.settings.notification.ConfigureNotificationSettings;
import com.android.settings.notification.OtherSoundSettings;
import com.android.settings.notification.SoundSettings;
import com.android.settings.notification.ZenModePrioritySettings;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.notification.ZenModeVisualInterruptionSettings;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.sim.SimSettings;
import com.android.settings.system.SystemDashboardFragment;
import com.android.settings.tts.TtsEnginePreferenceFragment;
import com.android.settings.tts.TtsSlidersFragment;
import com.android.settings.users.UserSettings;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;

import java.util.Collection;
import java.util.HashMap;

public final class SearchIndexableResources {

    @XmlRes
    public static final int NO_DATA_RES_ID = 0;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    static final HashMap<String, SearchIndexableResource> sResMap = new HashMap<>();

    @VisibleForTesting
    static void addIndex(Class<?> indexClass, @XmlRes int xmlResId,
            @DrawableRes int iconResId) {
        String className = indexClass.getName();
        int rank = Ranking.getRankForClassName(className);
        sResMap.put(className, new SearchIndexableResource(rank, xmlResId, className, iconResId));
    }

    static {
        addIndex(WifiSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_wireless);
        addIndex(NetworkDashboardFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_wireless);
        addIndex(ConfigureWifiSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_wireless);
        addIndex(SavedAccessPointsWifiSettings.class, NO_DATA_RES_ID,
                R.drawable.ic_settings_wireless);
        addIndex(BluetoothSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_bluetooth);
        addIndex(SimSettings.class, NO_DATA_RES_ID, R.drawable.ic_sim_sd);
        addIndex(DataUsageSummary.class, NO_DATA_RES_ID, R.drawable.ic_settings_data_usage);
        addIndex(DataUsageMeteredSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_data_usage);
        addIndex(WirelessSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_more);
        addIndex(ScreenZoomSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_display);
        addIndex(DisplaySettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_display);
        addIndex(WallpaperTypeSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_display);
        addIndex(ConfigureNotificationSettings.class,
                R.xml.configure_notification_settings, R.drawable.ic_settings_notifications);
        addIndex(AppAndNotificationDashboardFragment.class, NO_DATA_RES_ID,
                R.drawable.ic_settings_applications);
        addIndex(SoundSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_sound);
        addIndex(OtherSoundSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_sound);
        addIndex(ZenModeSettings.class,
                R.xml.zen_mode_settings, R.drawable.ic_settings_notifications);
        addIndex(ZenModePrioritySettings.class,
                R.xml.zen_mode_priority_settings, R.drawable.ic_settings_notifications);
        addIndex(StorageSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_storage);
        addIndex(PowerUsageSummary.class,
                R.xml.power_usage_summary, R.drawable.ic_settings_battery);
        addIndex(PowerUsageDetail.class, NO_DATA_RES_ID, R.drawable.ic_settings_battery);
        addIndex(BatterySaverSettings.class,
                R.xml.battery_saver_settings, R.drawable.ic_settings_battery);
        addIndex(AdvancedAppSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_applications);
        addIndex(SpecialAccessSettings.class,
                R.xml.special_access, R.drawable.ic_settings_applications);
        addIndex(UserSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_multiuser);
        addIndex(GestureSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_gestures);
        addIndex(PickupGestureSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_gestures);
        addIndex(DoubleTapScreenSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_gestures);
        addIndex(DoubleTapPowerSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_gestures);
        addIndex(DoubleTwistGestureSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_gestures);
        addIndex(SwipeToNotificationSettings.class, NO_DATA_RES_ID,
                R.drawable.ic_settings_gestures);
        addIndex(InputAndGestureSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_language);
        addIndex(LocationSettings.class, R.xml.location_settings, R.drawable.ic_settings_location);
        addIndex(ScanningSettings.class, R.xml.location_scanning, R.drawable.ic_settings_location);
        addIndex(SecuritySettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_security);
        addIndex(ScreenPinningSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_security);
        addIndex(AccountSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_accounts);
        addIndex(UserAndAccountDashboardFragment.class, NO_DATA_RES_ID,
                R.drawable.ic_settings_accounts);
        addIndex(InputMethodAndLanguageSettings.class,
                NO_DATA_RES_ID, R.drawable.ic_settings_language);
        addIndex(VirtualKeyboardFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_language);
        addIndex(AvailableVirtualKeyboardFragment.class,
                NO_DATA_RES_ID, R.drawable.ic_settings_language);
        addIndex(PhysicalKeyboardFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_language);
        addIndex(PrivacySettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_backup);
        addIndex(BackupSettingsFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_backup);
        addIndex(DateTimeSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_date_time);
        addIndex(AccessibilitySettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_accessibility);
        addIndex(PrintSettingsFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_print);
        addIndex(DevelopmentSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_development);
        addIndex(DeviceInfoSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_about);
        addIndex(LegalSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_about);
        addIndex(ZenModeVisualInterruptionSettings.class,
                R.xml.zen_mode_visual_interruptions_settings,
                R.drawable.ic_settings_notifications);
        addIndex(SystemDashboardFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_about);
        addIndex(StorageDashboardFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_storage);
        addIndex(ConnectedDeviceDashboardFragment.class, NO_DATA_RES_ID, R.drawable.ic_bt_laptop);
        addIndex(EnterprisePrivacySettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_about);
        addIndex(PaymentSettings.class, NO_DATA_RES_ID, R.drawable.ic_settings_nfc_payment);
        addIndex(
                TtsEnginePreferenceFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_language);
        addIndex(TtsSlidersFragment.class, NO_DATA_RES_ID, R.drawable.ic_settings_language);
    }

    private SearchIndexableResources() {
    }

    public static int size() {
        return sResMap.size();
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }
}
