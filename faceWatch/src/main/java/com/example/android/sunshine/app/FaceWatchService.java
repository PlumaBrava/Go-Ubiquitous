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

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class FaceWatchService extends CanvasWatchFaceService {
    private static final String TAG = "FaceWatchService";
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;


    // Weather variables
    Bitmap mbitmap;//icon
    String mMaxTemp="sdM";
    String mMinTemp="sdm";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<FaceWatchService.Engine> mWeakReference;

        public EngineHandler(FaceWatchService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            FaceWatchService.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine
            implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mMaxTempPaint;
        Paint mMinTempPaint;
        Paint mbitMapPaint;

        float mLineHeight;

        Paint mTextPaint;
        boolean mAmbient;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        int mTapCount;

        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(FaceWatchService.this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Wearable.API)
            .build();

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(FaceWatchService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = FaceWatchService.this.getResources();

            //read the dimentions settings
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mLineHeight = resources.getDimension(R.dimen.digital_line_height);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mMinTempPaint = new Paint();
            mMinTempPaint = createTextPaint(resources.getColor(R.color.digital_text_maxTemp));

            mMaxTempPaint = new Paint();
            mMaxTempPaint = createTextPaint(resources.getColor(R.color.digital_text_minTemp));

            mbitMapPaint = new Paint();
            mbitMapPaint = createTextPaint(resources.getColor(R.color.digital_text_bitMap));
            // borrar, se pone para testear el facewatch
            mbitmap= BitmapFactory.decodeResource(getResources(), R.drawable.ic_clear);

            mTime = new Time();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }

                // Whether the timer should be running depends on whether we're visible (as well as
                // whether we're in ambient mode), so we may need to start or stop the timer.
                updateTimer();
            }
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            FaceWatchService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            FaceWatchService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = FaceWatchService.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = FaceWatchService.this.getResources();
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    mTapCount++;
                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
                            R.color.background : R.color.background2));
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);


            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
// Only render the day of week and date if there is no peek card, so they do not bleed
                // into each other in ambient mode.
                if (getPeekCardPosition().isEmpty()) {

                    // TemMax
                    canvas.drawText(
                            mMaxTemp,
                            mXOffset, mYOffset + mLineHeight, mMaxTempPaint);
                    // TemMax
                    canvas.drawText(
                            mMinTemp,
                            mXOffset, mYOffset + mLineHeight * 2, mMaxTempPaint);
                    // bitMap
                    if (mbitmap != null) {
                        canvas.drawBitmap(mbitmap, mXOffset+mLineHeight * 3, mYOffset + mLineHeight * 1, mbitMapPaint);
                    }


                    // Day of week
//                canvas.drawText(
//                        mDayOfWeekFormat.format(mDate),
//                        mXOffset, mYOffset + mLineHeight, mDatePaint);
//                // Date
//                canvas.drawText(
//                        mDateFormat.format(mDate),
//                        mXOffset, mYOffset + mLineHeight * 2, mDatePaint);
//            }
                }


            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
            String text = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }


        /*
        * Extracts {@link android.graphics.Bitmap} data from the
        * {@link com.google.android.gms.wearable.Asset}
        */
        private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(Asset... params) {

                if (params.length > 0) {

                    Asset asset = params[0];

                    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                            mGoogleApiClient, asset).await().getInputStream();

                    if (assetInputStream == null) {
                        Log.w(TAG, "Requested an unknown Asset.");
                        return null;
                    }
                    return BitmapFactory.decodeStream(assetInputStream);

                } else {
                    Log.e(TAG, "Asset must be non-null");
                    return null;
                }


            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {

                if (bitmap != null) {
//                    LOGD(TAG, "Setting background image on second page..");
//                    moveToPage(1);
//                    mAssetFragment.setBackgroundImage(bitmap);
                    mbitmap=bitmap;
                }
            }
        }

        @Override // DataApi.DataListener
        public void onDataChanged(DataEventBuffer dataEvents) {
            Log.d(TAG, "onDataChanged: ");
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {


                    continue;
                }

                DataItem dataItem = dataEvent.getDataItem();

                //sacar el dato
                DataItem item = dataEvent.getDataItem();
//                    Log.i(LOG, "getItemUri :" + item.getUri().getPath().toString());
//                    Log.i(LOG, "getItemUri.compareTO :" + item.getUri().getPath().compareTo("/envionumero"));
                if (item.getUri().getPath().compareTo("/weatherdata") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    mMaxTemp=dataMap.getString("maxtemp");
                    mMinTemp=dataMap.getString("mintemp");
                    Asset weatherImageAsset = dataMap.getAsset("weatherImage");
//                    mbitmap= BitmapFactory.decodeResource(getResources(), R.drawable.ic_clear);



                    // Loads image on background thread.
                    new LoadBitmapAsyncTask().execute(weatherImageAsset);






//                    mbitmap = loadBitmapFromAsset(profileAsset);
//                        mTextView.setText("int: " + dataMap.getInt("numero"));
//                        Log.i(LOG, "setText_numero :" + dataMap.getInt("numero"));
//                    mTextView.append("   ---   long:" + dataMap.getLong(KEYB));
                }
//                    Log.i(LOG, "NO Compara");


//              Lee la configuracion del reloj que se envia desde el telefono
//                if (!dataItem.getUri().getPath().equals(
//                        DigitalWatchFaceUtil.PATH_WITH_FEATURE)) {
//                    continue;
//                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Config DataItem updated:" + config);
                }
//                updateUiForConfigDataMap(config); estudiar.....
            }
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnected: " + connectionHint);
            }
            Log.d(TAG, "onConnected: " + connectionHint);
            mMaxTemp="148 conect";
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
//          updateConfigDataItemAndUiOnStartup(); levanta la configuracion default
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnectionSuspended: " + cause);
            }
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult result) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnectionFailed: " + result);
            }
            Log.d(TAG, "onConnectionFailed: " + result);
        }


    }


}
