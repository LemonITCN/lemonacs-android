package cn.lemage_scanlib.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author zhaoguangyang
 */
public final class InactivityTimer {

    private static final String TAG = InactivityTimer.class.getSimpleName();
    private static final long INACTIVITY_DELAY_MS = 300000L;
    private final Activity activity;
    private final BroadcastReceiver powerStatusReceiver;
    private boolean registered;
    private AsyncTask<Object, Object, Object> inactivityTask;

    public InactivityTimer(Activity activity) {
        this.activity = activity;
        this.powerStatusReceiver = new InactivityTimer.PowerStatusReceiver();
        this.registered = false;
        this.onActivity();
    }

    @SuppressLint({"NewApi"})
    public synchronized void onActivity() {
        this.cancel();
        this.inactivityTask = new InactivityTimer.InactivityAsyncTask();
        this.inactivityTask.execute(new Object[0]);
    }

    public synchronized void onPause() {
        this.cancel();
        if (this.registered) {
            this.activity.unregisterReceiver(this.powerStatusReceiver);
            this.registered = false;
        } else {
            Log.w(TAG, "PowerStatusReceiver was never registered?");
        }

    }

    public synchronized void onResume() {
        if (this.registered) {
            Log.w(TAG, "PowerStatusReceiver was already registered?");
        } else {
            this.activity.registerReceiver(this.powerStatusReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            this.registered = true;
        }

        this.onActivity();
    }

    private synchronized void cancel() {
        AsyncTask<?, ?, ?> task = this.inactivityTask;
        if (task != null) {
            task.cancel(true);
            this.inactivityTask = null;
        }

    }

    public void shutdown() {
        this.cancel();
    }

    private final class InactivityAsyncTask extends AsyncTask<Object, Object, Object> {
        private InactivityAsyncTask() {
        }

        protected Object doInBackground(Object... objects) {
            try {
                Thread.sleep(300000L);
                Log.i(InactivityTimer.TAG, "Finishing activity due to inactivity");
                InactivityTimer.this.activity.finish();
            } catch (InterruptedException var3) {
                ;
            }

            return null;
        }
    }

    private final class PowerStatusReceiver extends BroadcastReceiver {
        private PowerStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                boolean onBatteryNow = intent.getIntExtra("plugged", -1) <= 0;
                if (onBatteryNow) {
                    InactivityTimer.this.onActivity();
                } else {
                    InactivityTimer.this.cancel();
                }
            }

        }
    }
}
