package dhbw.timetable.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import dhbw.timetable.data.TimetableManager;

public class TimetableSyncService extends Service {

    // NEEDED
    private static TimetableSyncService INSTANCE;

    private Timer timer;
    private TimerTask timerTask;

    public TimetableSyncService(Context context) {
        super();
        Log.d("SYNC", "Sync service obj created.");
        INSTANCE = this;
    }

    // NEEDED
    public TimetableSyncService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i("SYNC", "onStartCommand(intent=" + intent + ", getAction()=" + (intent != null ? intent.getAction() : "NULL"));
        // Do not trust android
        if (intent != null) {
            startTimer(intent.getIntExtra("freq", -1));
        } else {
            Log.w("SYNC", "Android did not pass intent to background service");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SYNC", "Destroyed sync service!");
        sendBroadcast(new Intent(".services.TimetableRestarterBroadcastReceiver"));
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startTimer(int freq) {
        timer = new Timer();
        initializeTimerTask();

        // Schedule the timer, to wake up every x MILLI seconds
        if (freq > -1) {
            Log.i("SYNC", "Background sync successfully scheduled. Sync now every " + freq + "ms.");
            timer.schedule(timerTask, 1000, freq);
        }
    }

    public void stopTimer() {
        Log.i("SYNC", "Background sync stop requested.");
        // Stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            Log.i("SYNC", "Successfully stopped");
            timer = null;
        } else {
            Log.i("SYNC", "Service already stopped!");
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if (!TimetableManager.getInstance().isBusy()) {
                    TimetableManager.getInstance().updateGlobals(TimetableSyncService.this.getApplication(), () -> {
                        Log.i("SYNC", "Background sync finished.");
                        // TODO Refresh activities ?
                        // Check onChange preference and possibly fire notifications done
                    }, string -> Log.e("SYNC", "Background sync FAILED: " + string));
                    Log.i("SYNC", "Background sync now running");
                } else {
                    Log.w("SYNC", "Tried asynchronous sync");
                }
            }
        };
    }
}
