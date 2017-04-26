/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

/**
 * Common base class for things that need to show the battery usage graph.
 */
public abstract class PowerUsageBase extends DashboardFragment {

    // +1 to allow ordering for PowerUsageSummary.
    @VisibleForTesting
    static final int MENU_STATS_REFRESH = Menu.FIRST + 1;

    protected BatteryStatsHelper mStatsHelper;
    protected UserManager mUm;
    private BatteryBroadcastReceiver mBatteryBroadcastReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);
        mStatsHelper = new BatteryStatsHelper(activity, true);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mStatsHelper.create(icicle);
        setHasOptionsMenu(true);

        mBatteryBroadcastReceiver = new BatteryBroadcastReceiver(getContext());
        mBatteryBroadcastReceiver.setBatteryChangedListener(() -> {
            if (!mHandler.hasMessages(MSG_REFRESH_STATS)) {
                mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mStatsHelper.clearStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        BatteryStatsHelper.dropFile(getActivity(), BatteryHistoryDetail.BATTERY_HISTORY_FILE);
        mBatteryBroadcastReceiver.register();
        if (mHandler.hasMessages(MSG_REFRESH_STATS)) {
            mHandler.removeMessages(MSG_REFRESH_STATS);
            mStatsHelper.clearStats();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBatteryBroadcastReceiver.unRegister();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeMessages(MSG_REFRESH_STATS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            mStatsHelper.storeState();
        }
    }

    protected void refreshStats() {
        mStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, mUm.getUserProfiles());
    }

    protected void updatePreference(BatteryHistoryPreference historyPref) {
        historyPref.setStats(mStatsHelper);
    }

    static final int MSG_REFRESH_STATS = 100;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_STATS:
                    mStatsHelper.clearStats();
                    refreshStats();
                    break;
            }
        }
    };

}
