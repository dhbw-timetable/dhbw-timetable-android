package dhbw.timetable.navfragments.notifications.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ALARM-REC", "Firing alarm!");
        if (AlarmSupervisor.getInstance().isShowing()) {
            Log.w("ALARM-REC", "Already have an alarm activity showing! Request denied.");
            return;
        }
        Log.d("ALARM-REC", "Activity creation granted.");
        // Does trigger on locked screen
        Intent i = new Intent(context, AlarmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        i.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        context.startActivity(i);
    }
}
