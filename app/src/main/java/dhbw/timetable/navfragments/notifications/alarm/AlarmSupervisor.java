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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.DateHelper;
import dhbw.timetable.data.TimelessDate;
import dhbw.timetable.data.TimetableManager;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
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

    public Ringtone getRingtone() {
        return ringtone;
    }

    public void playRingtone() {
        ringtone.play();
    }

    public void stopRingtone() {
        ringtone.stop();
    }

    public void rescheduleAllAlarms(Context context) {
        Log.i("ALARM", "Rescheduling all alarms...");
        cancelAllAlarms();
        Map<TimelessDate, ArrayList<Appointment>> globals = TimetableManager.getInstance().getGlobals();
        ArrayList<Appointment> appointmentsOfWeek;
        TimelessDate tempDay;
        Appointment firstAppointment;
        for(TimelessDate week : globals.keySet()) {
            appointmentsOfWeek = globals.get(week);
            for(int day = 0; day < 5; day++) {
                tempDay = (TimelessDate) week.clone();
                DateHelper.AddDays(tempDay, day);
                firstAppointment = DateHelper.GetFirstAppointmentOfDay(appointmentsOfWeek, tempDay);
                // only if there are appointments
                if(firstAppointment != null) {
                    Log.d("ALARM", "");
                    scheduleAlarm(firstAppointment.getStartDate(), context);
                }
            }
        }
        Log.i("ALARM", "Rescheduled " + alarms.size() + " alarms");
    }

    public void scheduleAlarm(GregorianCalendar date, Context context) {
        Log.d("ALARM", "Scheduling alarm...");
        PendingIntent p = PendingIntent.getBroadcast(context,
                0, new Intent(context, AlarmReceiver.class), 0);
        alarms.put(new TimelessDate(date), p);
        manager.setExact(AlarmManager.RTC_WAKEUP,
                date.getTimeInMillis(),
                p);

        Log.d("ALARM", "Alarm ready for "
                + new SimpleDateFormat("dd.MM.yyyy").format(date.getTime()));
    }

    public void cancelAlarm(GregorianCalendar date) {
        Log.i("ALARM", "Canceling " + new SimpleDateFormat("dd.MM.yyyy").format(date.getTime()) +  "...");
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

    void cancelAllAlarms() {
        Log.i("ALARM", "Canceling all alarms...");
        for(TimelessDate d : alarms.keySet()) alarms.get(d).cancel();
        alarms.clear();
        Log.i("ALARM", "All alarms canceled");
    }

    @Deprecated
    boolean isShowing() {
        return ActivityHelper.getActivity() instanceof AlarmActivity;
    }
}
