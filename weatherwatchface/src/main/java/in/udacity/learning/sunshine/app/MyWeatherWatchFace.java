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

package in.udacity.learning.sunshine.app;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class MyWeatherWatchFace extends CanvasWatchFaceService implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final String WEARABLE_DATA_PATH = "/wearable_data";
    public static String RECEIVE_ACTION = "receive";

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
    /**
     * Update rate in milliseconds for mute mode. We update every minute, like in ambient mode.
     */
    private static final long MUTE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);
    long mInteractiveUpdateRateMs = INTERACTIVE_UPDATE_RATE_MS;

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;
    private static final String TAG = MyWeatherWatchFace.class.getName();

    /**
     * Alpha value for drawing time when in mute mode.
     */
    static final int MUTE_ALPHA = 100;

    /**
     * Alpha value for drawing time when not in mute mode.
     */
    static final int NORMAL_ALPHA = 255;

    private String maxTemp = "";
    private String minTemp = "";
    private String descTemp = "";
    private Bitmap tempIcon;

    private GoogleApiClient mGoogleApiClient;
    Engine engine;

    @Override
    public Engine onCreateEngine() {
        engine = new Engine();
        return engine;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected: ");
        sendSignalForLatestTemprature();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWeatherWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWeatherWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWeatherWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;
    private Paint mAmPmPaint;
    private Paint mColonPaint;
    private Paint mColonPaintSeconds;
    private Paint mTextPaint;
    private Paint mMinTextPaint;
    private Paint mHighTemp;
    private Paint mLowTemp;
    private Paint mGraphics;
    private Paint mBackgroundPaint;

    private float mColonWidth;

    private Calendar mCalendar;

    private float mXOffset;
    private float mXDistanceOffset;
    private float mYOffset;
    private float mLineHeight;

    private String mAmString;
    private String mPmString;
    private static final String COLON_STRING = ":";

    private class Engine extends CanvasWatchFaceService.Engine {

        private static final int BACKGROUND_COLOR = Color.BLACK;
        private static final int TEXT_HOURS_MINS_COLOR = Color.WHITE;
        private static final int TEXT_SECONDS_COLOR = Color.GRAY;
        private static final int TEXT_AM_PM_COLOR = Color.GRAY;
        private static final int TEXT_COLON_COLOR = Color.GRAY;
        private static final int TEXT_HIGH_COLOR = Color.GRAY;
        private static final int TEXT_LOW_COLOR = Color.GRAY;
        private static final int TEXT_DISTANCE_COUNT_COLOR = Color.GRAY;

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equalsIgnoreCase(RECEIVE_ACTION)) {
                    mCalendar.setTimeZone(TimeZone.getDefault());
                } else {

                    collectValues();
                    if (ListenService.asset != null) {
                        new LoadBitmapAsyncTask().execute(ListenService.asset);
                    }
                }
                invalidate();
            }
        };

        private void collectValues() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyWeatherWatchFace.this);
            maxTemp = sharedPreferences.getString("max", null);
            minTemp = sharedPreferences.getString("min", null);
            descTemp = sharedPreferences.getString("desc", null);

//            File f = Environment.getExternalStorageDirectory();
//            String path = f.getAbsolutePath() + File.separator + "mausam.png";
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            tempIcon = BitmapFactory.decodeFile(path, options);
        }

        int mTapCount;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWeatherWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            mGoogleApiClient = new GoogleApiClient.Builder(MyWeatherWatchFace.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(MyWeatherWatchFace.this)
                    .addOnConnectionFailedListener(MyWeatherWatchFace.this)
                    .build();
            mGoogleApiClient.connect();

            Resources resources = MyWeatherWatchFace.this.getResources();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mYOffset = resources.getDimension(R.dimen.fit_y_offset);
            mLineHeight = resources.getDimension(R.dimen.fit_line_height);
            mAmString = resources.getString(R.string.fit_am);
            mPmString = resources.getString(R.string.fit_pm);

            mHourPaint = createTextPaint(TEXT_HOURS_MINS_COLOR, BOLD_TYPEFACE);
            mMinutePaint = createTextPaint(TEXT_HOURS_MINS_COLOR);
            mSecondPaint = createTextPaint(TEXT_SECONDS_COLOR);
            mAmPmPaint = createTextPaint(TEXT_AM_PM_COLOR);
            mColonPaint = createTextPaint(TEXT_COLON_COLOR);
            mColonPaintSeconds = createTextPaint(TEXT_COLON_COLOR);
            mTextPaint = createTextPaint(TEXT_DISTANCE_COUNT_COLOR);
            mMinTextPaint = createTextPaint(TEXT_DISTANCE_COUNT_COLOR);
            mHighTemp = createTextPaint(TEXT_HIGH_COLOR);
            mLowTemp = createTextPaint(TEXT_LOW_COLOR);
            mGraphics = createTextPaint(TEXT_LOW_COLOR);

            mCalendar = Calendar.getInstance();
        }

        private Paint createTextPaint(int color) {
            return createTextPaint(color, NORMAL_TYPEFACE);
        }

        private Paint createTextPaint(int color, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            Log.d(TAG, "onApplyWindowInsets: " + (insets.isRound() ? "round" : "square"));

            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWeatherWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound ? R.dimen.fit_x_offset_round : R.dimen.fit_x_offset);
            mXDistanceOffset = resources.getDimension(isRound ? R.dimen.fit_steps_or_distance_x_offset_round : R.dimen.fit_steps_or_distance_x_offset);
            float textSize = resources.getDimension(isRound ? R.dimen.fit_text_size_round : R.dimen.fit_text_size);
            float secondsSize = resources.getDimension(isRound ? R.dimen.fit_seconds_text_size_round : R.dimen.fit_seconds_text_size);
            float amPmSize = resources.getDimension(isRound ? R.dimen.fit_am_pm_size_round : R.dimen.fit_am_pm_size);

            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mSecondPaint.setTextSize(secondsSize);
            mColonPaintSeconds.setTextSize(secondsSize);
            mAmPmPaint.setTextSize(amPmSize);
            mColonPaint.setTextSize(textSize);
            mHighTemp.setTextSize(textSize);
            mLowTemp.setTextSize(textSize);
            mTextPaint.setTextSize(resources.getDimension(R.dimen.fit_steps_or_distance_text_size));
            mMinTextPaint.setTextSize(resources.getDimension(R.dimen.text_min_size));

            mColonWidth = mColonPaint.measureText(COLON_STRING);

            if (!Utility.isValidText(maxTemp)) {
                collectValues();
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mHourPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);

            Log.d(TAG, "onPropertiesChanged: burn-in protection = " + burnInProtection
                    + ", low-bit ambient = " + mLowBitAmbient);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mAmPmPaint.setAntiAlias(antiAlias);
                mColonPaint.setAntiAlias(antiAlias);
                mColonPaintSeconds.setAntiAlias(antiAlias);
                mTextPaint.setAntiAlias(antiAlias);
            }
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {

            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE;
            // We only need to update once a minute in mute mode.
            setInteractiveUpdateRateMs(inMuteMode ? MUTE_UPDATE_RATE_MS : INTERACTIVE_UPDATE_RATE_MS);
            invalidate();
        }

        public void setInteractiveUpdateRateMs(long updateRateMs) {
            if (updateRateMs == mInteractiveUpdateRateMs) {
                return;
            }
            mInteractiveUpdateRateMs = updateRateMs;

            // Stop and restart the timer so the new update rate takes effect immediately.
            if (shouldTimerBeRunning()) {
                updateTimer();
            }
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = MyWeatherWatchFace.this.getResources();
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

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        private String getAmPmString(int amPm) {
            return amPm == Calendar.AM ? mAmString : mPmString;
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            boolean is24Hour = DateFormat.is24HourFormat(MyWeatherWatchFace.this);

            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
            }

            // Draw the hours.
            float x = mXOffset;
            String hourString;
            if (is24Hour) {
                hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
            } else {
                int hour = mCalendar.get(Calendar.HOUR);
                if (hour == 0) {
                    hour = 12;
                }
                hourString = String.valueOf(hour);
            }
            canvas.drawText(hourString, x, mYOffset, mHourPaint);
            x += mHourPaint.measureText(hourString);

            // Draw first colon (between hour and minute).
            canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);

            x += mColonWidth;

            // Draw the minutes.
            String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
            canvas.drawText(minuteString, x, mYOffset, mMinutePaint);
            x += mMinutePaint.measureText(minuteString);

            // In interactive mode, draw a second colon followed by the seconds.
            // Otherwise, if we're in 12-hour mode, draw AM/PM
            if (!isInAmbientMode()) {
                canvas.drawText(COLON_STRING, x, mYOffset, mColonPaintSeconds);

                x += mColonWidth;
                canvas.drawText(formatTwoDigitNumber(
                        mCalendar.get(Calendar.SECOND)), x, mYOffset, mSecondPaint);
            } else if (!is24Hour) {
                x += mColonWidth;
                canvas.drawText(getAmPmString(
                        mCalendar.get(Calendar.AM_PM)), x, mYOffset, mAmPmPaint);
            }

            // Only render distance if there is no peek card, so they do not bleed into each other
            // in ambient mode.
            if (getPeekCardPosition().isEmpty()) {
                if (Utility.isValidText(maxTemp))
                    canvas.drawText(
                            maxTemp,
                            mXDistanceOffset,
                            mYOffset + mLineHeight,
                            mTextPaint);

                if (Utility.isValidText(minTemp))
                    canvas.drawText(
                            " " + minTemp,
                            mXDistanceOffset + mTextPaint.measureText(maxTemp + " "),
                            mYOffset + mLineHeight,
                            mMinTextPaint);

                if (Utility.isValidText(descTemp))
                    canvas.drawText(
                            descTemp,
                            mXDistanceOffset,
                            mYOffset + mLineHeight + mLineHeight,
                            mTextPaint);

                if (tempIcon != null) {

                    Rect src = new Rect(0, 0, tempIcon.getWidth(), tempIcon.getHeight());
                    int yOff =(int)(mYOffset + mLineHeight + mLineHeight);
                    Rect dst = new Rect(140, 160, tempIcon.getWidth() + 240, tempIcon.getHeight() + 240);
                    canvas.drawBitmap(tempIcon, src, dst, mGraphics);
                }
            }


        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone and date formats, in case they changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());

            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(RECEIVE_ACTION);
            MyWeatherWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWeatherWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
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
                long delayMs = mInteractiveUpdateRateMs - (timeMs % mInteractiveUpdateRateMs);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    private void sendSignalForLatestTemprature() {
        // Create a DataMap object and send it to the data layer
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());

        Log.i(TAG, "sendData: " + dataMap.toString());
        // Construct a DataRequest and send over the data layer
        String signal = "/signal";
        PutDataMapRequest putDMR = PutDataMapRequest.create(signal);
        putDMR.getDataMap().putAll(dataMap);
        PutDataRequest request = putDMR.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    Log.v(TAG, "DataMap: " + " sent successfully to data layer ");
                } else {
                    // Log an error
                    Log.v(TAG, "ERROR: failed to send DataMap to data layer");
                }
            }
        });
    }

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

                Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);
                return bitmap;
            } else {
                Log.e(TAG, "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {

//                File f = Environment.getExternalStorageDirectory();
//                try {
//                    f = new File(f.getAbsolutePath(), "mausam.png");
//                    OutputStream out = new FileOutputStream(f);
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                    out.flush();
//                    out.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                tempIcon = bitmap;
            }
        }
    }
}
