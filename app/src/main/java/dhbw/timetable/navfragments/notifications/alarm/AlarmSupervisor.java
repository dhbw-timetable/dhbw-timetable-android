package dhbw.timetable.navfragments.notifications.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.data.TimelessDate;

public final class AlarmSupervisor {
    private static final AlarmSupervisor INSTANCE = new AlarmSupervisor();
    private static final int SNOOZE_DURATION = 1000 * 60 * 5; // ms = 5min

    private AlarmManager manager;
    private Map<TimelessDate, PendingIntent> alarms = new HashMap<>();
    private Ringtone ringtone;

    private AlarmSupervisor() {}

    public static AlarmSupervisor getInstance() {
        return INSTANCE;
    }

    public void initialize(Context context) {
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setRingtone(Activity activity, Uri notification) {
        ringtone = RingtoneManager.getRingtone(activity, notification);
    }

    public void playRingtone() {
        ringtone.play();
    }

    public void stopRingtone() {
        ringtone.stop();
    }

    public void scheduleAlarm(GregorianCalendar date, Context context) {
        Log.i("ALARM", "Scheduling alarm...");
        PendingIntent p = PendingIntent.getBroadcast(context,
                0, new Intent(context, AlarmReceiver.class), 0);
        alarms.put(new TimelessDate(date), p);
        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                date.getTimeInMillis(),
                SNOOZE_DURATION,
                p);

        Log.i("ALARM", "Alarm ready");
    }

    public void cancelAlarm(GregorianCalendar date) {
        Log.i("ALARM", "Canceling...");
        PendingIntent p = alarms.get(new TimelessDate(date));
        if(p == null) {
            Log.e("ALARM", "Unable to find intent for "
                    + new SimpleDateFormat("dd.MM.yyyy").format(date.getTime()));
            return;
        }
        manager.cancel(p);
        Log.i("ALARM", "Alarm canceled");
    }

    public void dispose() {
        Log.i("ALARM", "Disposing current alarm...");
        TimelessDate today = new TimelessDate();
        PendingIntent p = alarms.get(today);
        if(p == null) {
            Log.e("ALARM", "Unable to find todays intent for "
                    + new SimpleDateFormat("dd.MM.yyyy").format(today.getTime()));
            return;
        }
        manager.cancel(p);
        Log.i("ALARM", "Alarm disposed");
    }

    @Deprecated
    boolean isShowing() {
        return ActivityHelper.getActivity() instanceof AlarmActivity;
    }
}
