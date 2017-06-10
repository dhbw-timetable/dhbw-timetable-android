package dhbw.timetable.navfragments.notifications.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // TODO Reschedule all alarms
            // AlarmFragment.activateAlarm(context);
        }
    }
}