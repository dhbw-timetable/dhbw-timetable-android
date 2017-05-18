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
        Log.i("EXIT", "Destroyed sync service!");
        sendBroadcast(new Intent(".services.TimetableRestarterBroadcastReceiver"));
        stopTask();
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
            System.out.println("Background sync started. Sync every " + freq + "ms...");
            timer.schedule(timerTask, 1000, freq);
        }
    }

    public void stopTask() {
        System.out.println("Background sync service stop requested...");
        // Stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            System.out.println("Stopped");
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                // TODO Implement sync
                Log.i("SYNC", "Would like to sync now. Please implement me! :-)");
            }
        };
    }
}
