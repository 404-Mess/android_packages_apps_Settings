/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.provider.Settings;
import android.service.autofill.AutoFillService;
import android.service.autofill.AutofillService;
import android.service.autofill.AutofillServiceInfo;
import android.text.TextUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class DefaultAutofillPicker extends DefaultAppPickerFragment {

    static final String SETTING = Settings.Secure.AUTOFILL_SERVICE;
    // TODO(b/35956626): remove once clients migrated
    static final Intent OLD_AUTO_FILL_PROBE = new Intent(AutoFillService.OLD_SERVICE_INTERFACE);
    static final Intent AUTOFILL_PROBE = new Intent(AutofillService.SERVICE_INTERFACE);

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DEFAULT_AUTOFILL_PICKER;
    }

    @Override
    protected boolean shouldShowItemNone() {
        return true;
    }

    @Override
    protected List<DefaultAppInfo> getCandidates() {
        final List<DefaultAppInfo> candidates = new ArrayList<>();
        final List<ResolveInfo> resolveInfos = mPm.getPackageManager()
                .queryIntentServices(AUTOFILL_PROBE, PackageManager.GET_META_DATA);
        for (ResolveInfo info : resolveInfos) {
            candidates.add(new DefaultAppInfo(mUserId, new ComponentName(
                    info.serviceInfo.packageName, info.serviceInfo.name)));
        }
        final List<ResolveInfo> oldResolveInfos = mPm.getPackageManager()
                .queryIntentServices(OLD_AUTO_FILL_PROBE, PackageManager.GET_META_DATA);
        for (ResolveInfo info : oldResolveInfos) {
            candidates.add(new DefaultAppInfo(mUserId, new ComponentName(
                    info.serviceInfo.packageName, info.serviceInfo.name)));
        }
        return candidates;
    }

    @Override
    protected String getDefaultAppKey() {
        return Settings.Secure.getString(getContext().getContentResolver(), SETTING);
    }

    @Override
    protected String getConfirmationMessage(DefaultAppInfo appInfo) {
        if (appInfo == null) {
            return null;
        }
        final CharSequence appName = appInfo.loadLabel(mPm.getPackageManager());
        return getContext().getString(R.string.autofill_confirmation_message, appName);
    }

    @Override
    protected boolean setDefaultAppKey(String key) {
        Settings.Secure.putString(getContext().getContentResolver(), SETTING, key);
        return true;
    }

    /**
     * Provides Intent to setting activity for the specified auto-fill service.
     */
    static final class AutofillSettingIntentProvider implements SettingIntentProvider {

        private final String mSelectedKey;
        private final PackageManager mPackageManager;

        public AutofillSettingIntentProvider(PackageManager packageManager, String key) {
            mSelectedKey = key;
            mPackageManager = packageManager;
        }

        @Override
        public Intent getIntent() {
            final List<ResolveInfo> resolveInfos = mPackageManager.queryIntentServices(
                    AUTOFILL_PROBE, PackageManager.GET_META_DATA);

            for (ResolveInfo resolveInfo : resolveInfos) {
                final ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                final String flattenKey = new ComponentName(
                        serviceInfo.packageName, serviceInfo.name).flattenToString();
                if (TextUtils.equals(mSelectedKey, flattenKey)) {
                    final String settingsActivity = new AutofillServiceInfo(
                            mPackageManager, serviceInfo)
                            .getSettingsActivity();
                    if (TextUtils.isEmpty(settingsActivity)) {
                        return null;
                    }
                    return new Intent(Intent.ACTION_MAIN).setComponent(
                            new ComponentName(serviceInfo.packageName, settingsActivity));
                }
            }

            final List<ResolveInfo> oldResolveInfos = mPackageManager.queryIntentServices(
                    OLD_AUTO_FILL_PROBE, PackageManager.GET_META_DATA);

            for (ResolveInfo resolveInfo : oldResolveInfos) {
                final ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                final String flattenKey = new ComponentName(
                        serviceInfo.packageName, serviceInfo.name).flattenToString();
                if (TextUtils.equals(mSelectedKey, flattenKey)) {
                    final String settingsActivity = new AutofillServiceInfo(
                            mPackageManager, serviceInfo)
                            .getSettingsActivity();
                    if (TextUtils.isEmpty(settingsActivity)) {
                        return null;
                    }
                    return new Intent(Intent.ACTION_MAIN).setComponent(
                            new ComponentName(serviceInfo.packageName, settingsActivity));
                }
            }
            return null;
        }
    }
}
