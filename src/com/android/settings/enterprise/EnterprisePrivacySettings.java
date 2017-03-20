/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.enterprise;

import android.content.Context;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.core.PreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnterprisePrivacySettings extends DashboardFragment {

    static final String TAG = "EnterprisePrivacySettings";

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ENTERPRISE_PRIVACY_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.enterprise_privacy_settings;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        final List controllers = new ArrayList<PreferenceController>();
        controllers.add(new InstalledPackagesPreferenceController(context));
        controllers.add(new NetworkLogsPreferenceController(context));
        controllers.add(new BugReportsPreferenceController(context));
        controllers.add(new SecurityLogsPreferenceController(context));
        controllers.add(new EnterpriseInstalledPackagesPreferenceController(context));
        controllers.add(new AdminGrantedLocationPermissionsPreferenceController(context));
        controllers.add(new AdminGrantedMicrophonePermissionPreferenceController(context));
        controllers.add(new AdminGrantedCameraPermissionPreferenceController(context));
        controllers.add(new EnterpriseSetDefaultAppsPreferenceController(context));
        controllers.add(new AlwaysOnVpnPrimaryUserPreferenceController(context));
        controllers.add(new AlwaysOnVpnManagedProfilePreferenceController(context));
        controllers.add(new GlobalHttpProxyPreferenceController(context));
        controllers.add(new CaCertsCurrentUserPreferenceController(context));
        controllers.add(new CaCertsManagedProfilePreferenceController(context));
        controllers.add(new FailedPasswordWipePrimaryUserPreferenceController(context));
        controllers.add(new FailedPasswordWipeManagedProfilePreferenceController(context));
        controllers.add(new ImePreferenceController(context));
        return controllers;
    }

    public static boolean isPageEnabled(Context context) {
        return FeatureFactory.getFactory(context)
                .getEnterprisePrivacyFeatureProvider(context)
                .hasDeviceOwner();
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return isPageEnabled(context);
                }

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.enterprise_privacy_settings;
                    return Arrays.asList(sir);
                }
            };
}
