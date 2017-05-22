package dhbw.timetable.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import dhbw.timetable.data.logic.TimetableManager;

public class TimetableSyncService extends Service {

    private static TimetableSyncService INSTANCE;

    private Timer timer;
    private TimerTask timerTask;

    public TimetableSyncService(Context context) {
        super();
        Log.i("SYNC", "Sync service created.");
        INSTANCE = this;
    }

    // NEEDED
    public TimetableSyncService() {}

    public static TimetableSyncService GetInstance() {
        return INSTANCE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer(intent.getIntExtra("freq", -1));
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

        // Schedule the timer, to wake up every x MILI seconds
        if (freq > -1) {
            Log.i("SYNC", "Background sync started. Sync every " + freq + "ms.");
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
                TimetableManager.UpdateGlobals(TimetableSyncService.this.getApplication(), new Runnable() {
                    @Override
                    public void run() {
                        Log.i("SYNC", "Background sync finished.");
                        // TODO Refresh activities
                        // TODO Check onChange preference and possibly fire notifications
                    }
                });
                Log.i("SYNC", "Would like to sync now. Please implement me! :-)");
            }
        };
    }
}
