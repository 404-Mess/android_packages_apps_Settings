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

package com.android.settings.applications;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArraySet;

import com.android.settings.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;
import com.android.settings.enterprise.DevicePolicyManagerWrapper;
import com.android.settings.testutils.ApplicationTestUtils;
import com.android.settings.testutils.shadow.ShadowUserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.Arrays;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ApplicationFeatureProviderImpl}.
 */
@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION,
        shadows = {ShadowUserManager.class})
public final class ApplicationFeatureProviderImplTest {

    private final int MAIN_USER_ID = 0;
    private final int MANAGED_PROFILE_ID = 10;

    private final int PER_USER_UID_RANGE = 100000;
    private final int APP_1_UID = MAIN_USER_ID * PER_USER_UID_RANGE + 1;
    private final int APP_2_UID = MANAGED_PROFILE_ID * PER_USER_UID_RANGE + 1;

    private final String APP_1 = "app1";
    private final String APP_2 = "app2";

    private final String PERMISSION = "some.permission";

    private @Mock UserManager mUserManager;
    private @Mock Context mContext;
    private @Mock PackageManagerWrapper mPackageManager;
    @Mock private IPackageManagerWrapper mPackageManagerService;
    @Mock private DevicePolicyManagerWrapper mDevicePolicyManager;

    private ApplicationFeatureProvider mProvider;

    private int mAppCount = -1;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getSystemService(Context.USER_SERVICE)).thenReturn(mUserManager);

        mProvider = new ApplicationFeatureProviderImpl(mContext, mPackageManager,
                mPackageManagerService, mDevicePolicyManager);
    }

    private void verifyCalculateNumberOfInstalledApps(boolean async) {
        setUpUsersAndInstalledApps();

        when(mPackageManager.getInstallReason(APP_1, new UserHandle(MAIN_USER_ID)))
                .thenReturn(PackageManager.INSTALL_REASON_UNKNOWN);
        when(mPackageManager.getInstallReason(APP_2, new UserHandle(MANAGED_PROFILE_ID)))
                .thenReturn(PackageManager.INSTALL_REASON_POLICY);

        // Count all installed apps.
        mAppCount = -1;
        mProvider.calculateNumberOfInstalledApps(ApplicationFeatureProvider.IGNORE_INSTALL_REASON,
                async, (num) -> mAppCount = num);
        if (async) {
            ShadowApplication.runBackgroundTasks();
        }
        assertThat(mAppCount).isEqualTo(2);

        // Count apps with specific install reason only.
        mAppCount = -1;
        mProvider.calculateNumberOfInstalledApps(PackageManager.INSTALL_REASON_POLICY, async,
                (num) -> mAppCount = num);
        if (async) {
            ShadowApplication.runBackgroundTasks();
        }
        assertThat(mAppCount).isEqualTo(1);
    }

    @Test
    public void testCalculateNumberOfInstalledAppsSync() {
        verifyCalculateNumberOfInstalledApps(false /* async */);
    }

    @Test
    public void testCalculateNumberOfInstalledAppsAsync() {
        verifyCalculateNumberOfInstalledApps(true /* async */);
    }

    private void verifyCalculateNumberOfAppsWithAdminGrantedPermissions(boolean async)
            throws Exception {
        setUpUsersAndInstalledApps();

        when(mDevicePolicyManager.getPermissionGrantState(null, APP_1, PERMISSION))
                .thenReturn(DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
        when(mDevicePolicyManager.getPermissionGrantState(null, APP_2, PERMISSION))
                .thenReturn(DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED);
        when(mPackageManagerService.checkUidPermission(PERMISSION, APP_1_UID))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        when(mPackageManagerService.checkUidPermission(PERMISSION, APP_2_UID))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mPackageManager.getInstallReason(APP_1, new UserHandle(MAIN_USER_ID)))
                .thenReturn(PackageManager.INSTALL_REASON_UNKNOWN);
        when(mPackageManager.getInstallReason(APP_2, new UserHandle(MANAGED_PROFILE_ID)))
                .thenReturn(PackageManager.INSTALL_REASON_POLICY);

        mAppCount = -1;
        mProvider.calculateNumberOfAppsWithAdminGrantedPermissions(new String[] {PERMISSION}, async,
                (num) -> mAppCount = num);
        if (async) {
            ShadowApplication.runBackgroundTasks();
        }
        assertThat(mAppCount).isEqualTo(2);

    }

    @Test
    public void testCalculateNumberOfAppsWithAdminGrantedPermissionsSync() throws Exception {
        verifyCalculateNumberOfAppsWithAdminGrantedPermissions(false /* async */);
    }

    @Test
    public void testCalculateNumberOfAppsWithAdminGrantedPermissionsAsync() throws Exception {
        verifyCalculateNumberOfAppsWithAdminGrantedPermissions(true /* async */);
    }

    @Test
    public void testFindPersistentPreferredActivities() throws Exception {
        when(mUserManager.getUserProfiles()).thenReturn(Arrays.asList(new UserHandle(MAIN_USER_ID),
                new UserHandle(MANAGED_PROFILE_ID)));

        final Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        final Intent editIntent = new Intent(Intent.ACTION_EDIT);
        final Intent sendIntent = new Intent(Intent.ACTION_SEND);

        final ResolveInfo app1 = createResolveInfo(APP_1);
        final ResolveInfo app2 = createResolveInfo(APP_2);
        when(mPackageManagerService.findPersistentPreferredActivity(viewIntent, MAIN_USER_ID))
                .thenReturn(app1);
        when(mPackageManagerService.findPersistentPreferredActivity(viewIntent, MANAGED_PROFILE_ID))
                .thenReturn(app1);
        when(mPackageManagerService.findPersistentPreferredActivity(editIntent, MAIN_USER_ID))
                .thenReturn(null);
        when(mPackageManagerService.findPersistentPreferredActivity(editIntent, MANAGED_PROFILE_ID))
                .thenReturn(app2);
        when(mPackageManagerService.findPersistentPreferredActivity(sendIntent, MAIN_USER_ID))
                .thenReturn(app1);
        when(mPackageManagerService.findPersistentPreferredActivity(sendIntent, MANAGED_PROFILE_ID))
                .thenReturn(null);

        final Set<ApplicationFeatureProvider.PersistentPreferredActivityInfo> expectedActivities
                = new ArraySet<>();
        expectedActivities.add(new ApplicationFeatureProvider.PersistentPreferredActivityInfo(APP_1,
                MAIN_USER_ID));
        expectedActivities.add(new ApplicationFeatureProvider.PersistentPreferredActivityInfo(APP_1,
                MANAGED_PROFILE_ID));
        expectedActivities.add(new ApplicationFeatureProvider.PersistentPreferredActivityInfo(APP_2,
                MANAGED_PROFILE_ID));

        assertThat(mProvider.findPersistentPreferredActivities(
                new Intent[] {viewIntent, editIntent, sendIntent})).isEqualTo(expectedActivities);
    }

    private void setUpUsersAndInstalledApps() {
        when(mUserManager.getUsers(true)).thenReturn(Arrays.asList(
                new UserInfo(MAIN_USER_ID, "main", UserInfo.FLAG_ADMIN),
                new UserInfo(MANAGED_PROFILE_ID, "managed profile", 0)));

        when(mPackageManager.getInstalledApplicationsAsUser(PackageManager.GET_DISABLED_COMPONENTS
                | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS
                | PackageManager.MATCH_ANY_USER,
                MAIN_USER_ID)).thenReturn(Arrays.asList(
                        ApplicationTestUtils.buildInfo(APP_1_UID, APP_1, 0 /* flags */,
                                Build.VERSION_CODES.M)));
        when(mPackageManager.getInstalledApplicationsAsUser(PackageManager.GET_DISABLED_COMPONENTS
                | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS,
                MANAGED_PROFILE_ID)).thenReturn(Arrays.asList(
                        ApplicationTestUtils.buildInfo(APP_2_UID, APP_2, 0 /* flags */,
                                Build.VERSION_CODES.LOLLIPOP)));
    }

    private ResolveInfo createResolveInfo(String packageName) {
        final ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.packageName = packageName;
        final ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.activityInfo = activityInfo;
        return resolveInfo;
    }
}
