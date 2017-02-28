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

package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.SeekBarVolumizer;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
import com.android.settings.core.PreferenceController;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundSettings extends DashboardFragment {
    private static final String TAG = "SoundSettings";

    private static final String SELECTED_PREFERENCE_KEY = "selected_preference";
    private static final int REQUEST_CODE = 200;

    private static final int SAMPLE_CUTOFF = 2000;  // manually cap sample playback at 2 seconds

    private final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();
    private final H mHandler = new H();

    private WorkSoundPreferenceController mWorkSoundController;
    private RingtonePreference mRequestPreference;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mProgressiveDisclosureMixin.setTileLimit(6);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SOUND;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String selectedPreference = savedInstanceState.getString(SELECTED_PREFERENCE_KEY, null);
            if (!TextUtils.isEmpty(selectedPreference)) {
                mRequestPreference = (RingtonePreference) findPreference(selectedPreference);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mVolumeCallback.stopSample();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RingtonePreference) {
            mRequestPreference = (RingtonePreference) preference;
            mRequestPreference.onPrepareRingtonePickerIntent(mRequestPreference.getIntent());
            startActivityForResult(preference.getIntent(), REQUEST_CODE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return mDashboardFeatureProvider.isEnabled()
                ? R.xml.ia_sound_settings : R.xml.sound_settings;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        final List<PreferenceController> controllers = new ArrayList<>();
        Lifecycle lifecycle = getLifecycle();
        if (!mDashboardFeatureProvider.isEnabled()) {
            controllers.add(new CastPreferenceController(context));
        }
        controllers.add(new ZenModePreferenceController(context));
        controllers.add(new EmergencyBroadcastPreferenceController(context));
        controllers.add(new VibrateWhenRingPreferenceController(context));

        // === Volumes ===
        controllers.add(new AlarmVolumePreferenceController(context, mVolumeCallback, lifecycle));
        controllers.add(new MediaVolumePreferenceController(context, mVolumeCallback, lifecycle));
        controllers.add(
                new NotificationVolumePreferenceController(context, mVolumeCallback, lifecycle));
        controllers.add(new RingVolumePreferenceController(context, mVolumeCallback, lifecycle));

        // === Phone & notification ringtone ===
        controllers.add(new PhoneRingtonePreferenceController(context));
        controllers.add(new AlarmRingtonePreferenceController(context));
        controllers.add(new NotificationRingtonePreferenceController(context));

        // === Work Sound Settings ===
        mWorkSoundController = new WorkSoundPreferenceController(context, this, getLifecycle());
        controllers.add(mWorkSoundController);

        // === Other Sound Settings ===
        if (mDashboardFeatureProvider.isEnabled()) {
            controllers.add(new DialPadTonePreferenceController(context, this, lifecycle));
            controllers.add(new ScreenLockSoundPreferenceController(context, this, lifecycle));
            controllers.add(new ChargingSoundPreferenceController(context, this, lifecycle));
            controllers.add(new DockingSoundPreferenceController(context, this, lifecycle));
            controllers.add(new TouchSoundPreferenceController(context, this, lifecycle));
            controllers.add(new VibrateOnTouchPreferenceController(context, this, lifecycle));
            controllers.add(new DockAudioMediaPreferenceController(context, this, lifecycle));
            controllers.add(new BootSoundPreferenceController(context));
            controllers.add(new EmergencyTonePreferenceController(context, this, lifecycle));
        }

        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mRequestPreference != null) {
            mRequestPreference.onActivityResult(requestCode, resultCode, data);
            mRequestPreference = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRequestPreference != null) {
            outState.putString(SELECTED_PREFERENCE_KEY, mRequestPreference.getKey());
        }
    }

    // === Volumes ===

    final class VolumePreferenceCallback implements VolumeSeekBarPreference.Callback {
        private SeekBarVolumizer mCurrent;

        @Override
        public void onSampleStarting(SeekBarVolumizer sbv) {
            if (mCurrent != null && mCurrent != sbv) {
                mCurrent.stopSample();
            }
            mCurrent = sbv;
            if (mCurrent != null) {
                mHandler.removeMessages(H.STOP_SAMPLE);
                mHandler.sendEmptyMessageDelayed(H.STOP_SAMPLE, SAMPLE_CUTOFF);
            }
        }

        @Override
        public void onStreamValueChanged(int stream, int progress) {
            // noop
        }

        public void stopSample() {
            if (mCurrent != null) {
                mCurrent.stopSample();
            }
        }
    }

    ;

    // === Callbacks ===


    private final class H extends Handler {
        private static final int STOP_SAMPLE = 1;

        private H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_SAMPLE:
                    mVolumeCallback.stopSample();
                    break;
            }
        }
    }

    // === Indexing ===

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.sound_settings;
                    return Arrays.asList(sir);
                }

                public List<String> getNonIndexableKeys(Context context) {
                    final ArrayList<String> rt = new ArrayList<String>();
                    new NotificationVolumePreferenceController(
                            context, null /* Callback */,
                            null /* Lifecycle */).updateNonIndexableKeys(rt);
                    new RingVolumePreferenceController(
                            context, null /* Callback */,
                            null /* Lifecycle */).updateNonIndexableKeys(rt);
                    new PhoneRingtonePreferenceController(context).updateNonIndexableKeys(rt);
                    new VibrateWhenRingPreferenceController(context).updateNonIndexableKeys(rt);
                    new EmergencyBroadcastPreferenceController(context).updateNonIndexableKeys(rt);
                    if (FeatureFactory.getFactory(context).getDashboardFeatureProvider(context)
                            .isEnabled()) {
                        new DialPadTonePreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new ScreenLockSoundPreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new ChargingSoundPreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new DockingSoundPreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new TouchSoundPreferenceController(context, null /*
                        SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new VibrateOnTouchPreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new DockAudioMediaPreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                        new BootSoundPreferenceController(context).updateNonIndexableKeys(rt);
                        new EmergencyTonePreferenceController(context,
                                null /* SettingsPreferenceFragment */,
                                null /* Lifecycle */).updateNonIndexableKeys(rt);
                    } else {
                        new CastPreferenceController(context).updateNonIndexableKeys(rt);
                    }

                    return rt;
                }
            };

    // === Work Sound Settings ===

    void enableWorkSync() {
        if (mWorkSoundController != null) {
            mWorkSoundController.enableWorkSync();
        }
    }
}
