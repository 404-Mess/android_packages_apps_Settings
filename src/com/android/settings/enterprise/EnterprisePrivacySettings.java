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

import com.android.settings.R;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.PreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;

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
    protected String getCategoryKey() {
        return null;
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
        return controllers;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(
                    Context context, boolean enabled) {
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.enterprise_privacy_settings;
                return Arrays.asList(sir);
            }
        };
}
