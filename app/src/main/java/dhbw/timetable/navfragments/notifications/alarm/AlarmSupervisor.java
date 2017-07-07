package dhbw.timetable.navfragments.notifications.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.DateHelper;
import dhbw.timetable.data.TimelessDate;
import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.dialogs.ErrorDialog;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public final class AlarmSupervisor {

    private static final AlarmSupervisor INSTANCE = new AlarmSupervisor();
    private static final String sTagAlarms = ":alarms";
    private static final int SNOOZE_DURATION = 1000 * 60 * 5; // ms = 5min

    private AlarmManager manager;
    private Ringtone ringtone;
    private boolean rescheduling;

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

    void playRingtone() {
        ringtone.play();
    }

    void stopRingtone() {
        ringtone.stop();
    }

     Appointment getCurrentAppointment() {
        TimelessDate today = new TimelessDate();
        TimelessDate monday = new TimelessDate(today);
        DateHelper.Normalize(monday);
        ArrayList<Appointment> week = TimetableManager.getInstance().getGlobals().get(monday);
        return week != null ? DateHelper.GetFirstAppointmentOfDay(week, today) : null;
    }

    public void rescheduleAllAlarms(Context context) {
        if(rescheduling) {
            Log.i("ALARM", "Request denied. Already rescheduling...");
            return;
        }
        rescheduling = true;
        Log.i("ALARM", "Rescheduling all alarms...");
        cancelAllAlarms(context);

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(sharedPref.getBoolean("alarmOnFirstEvent", false)) {
            Map<TimelessDate, ArrayList<Appointment>> globals = TimetableManager.getInstance().getGlobals();
            ArrayList<Appointment> appointmentsOfWeek;
            TimelessDate tempDay;
            Appointment firstAppointment;
            for (TimelessDate week : globals.keySet()) {
                appointmentsOfWeek = globals.get(week);
                for (int day = 0; day < 5; day++) {
                    tempDay = (TimelessDate) week.clone();
                    DateHelper.AddDays(tempDay, day);
                    firstAppointment = DateHelper.GetFirstAppointmentOfDay(appointmentsOfWeek, tempDay);
                    // Only if there are appointments
                    if (firstAppointment != null) {
                        // apply shifting
                        int shifter = 0;
                        shifter += sharedPref.getInt("alarmFirstShiftHour", 0) * 60 * 60 * 1000;
                        shifter += sharedPref.getInt("alarmFirstShiftMinute", 0) * 60 * 1000;

                        GregorianCalendar afterShift = (GregorianCalendar) firstAppointment.getStartDate().clone();
                        afterShift.setTimeInMillis(afterShift.getTimeInMillis() - shifter);
                        // If is not over
                        GregorianCalendar today = (GregorianCalendar) Calendar.getInstance();
                        if (today.getTimeInMillis() < afterShift.getTimeInMillis()) {
                            scheduleAlarm(context, afterShift);
                        }
                    }
                }
            }
            Log.i("ALARM", "Rescheduled " + getAlarmIds(context).size() + " alarms");
        } else {
            Log.i("ALARM", "Not needed. Alarm not active.");
        }
        rescheduling = false;
    }

    private void scheduleAlarm(Context context, GregorianCalendar date) {
        Log.d("ALARM", "Scheduling alarm...");
        TimelessDate td = new TimelessDate(date);
        int notificationId = td.hashCode();
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(context, notificationId, i, PendingIntent.FLAG_CANCEL_CURRENT);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        date.getTimeInMillis(),
                        p);
            } else {
                manager.setWindow(AlarmManager.RTC_WAKEUP,
                        date.getTimeInMillis(),
                        1,
                        p);
            }

            serializeAlarm(context, notificationId);
        } catch(SecurityException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String errMSG = e.getMessage() + "\n" + sw.toString();
            e.printStackTrace();
            Activity act = ActivityHelper.getActivity();
            if(act != null) {
                ErrorDialog.newInstance("ERROR", "Failed to schedule alarm. Did some alarms crash?", errMSG)
                        .show(act.getFragmentManager(), "DLSERROR");
            }
        }
        Log.d("ALARM", "Alarm ready for " + new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.GERMANY).format(date.getTime()));
    }

    void snooze(Context context) {
        Log.i("ALARM", "Snoozing current alarm...");
        TimelessDate today = new TimelessDate();
        Intent intent = new Intent(context, AlarmActivity.class);
        PendingIntent p = getAlarm(context, intent, today.hashCode());
        if(p == null) {
            Log.e("ALARM", "Unable to find todays intent for "
                    + new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(today.getTime()));
            return;
        }
        manager.cancel(p);

        // Reschedule
        GregorianCalendar later = (GregorianCalendar) Calendar.getInstance() ;
        later.setTimeInMillis(later.getTimeInMillis() + SNOOZE_DURATION);
        scheduleAlarm(context, later);

        Log.i("ALARM", "Alarm snoozed");
    }

    void dispose(Context context) {
        Log.i("ALARM", "Disposing current alarm...");
        TimelessDate today = new TimelessDate();
        PendingIntent p = getAlarm(context, new Intent(context, AlarmActivity.class), today.hashCode());
        if(p == null) {
            Log.e("ALARM", "Unable to find todays intent for "
                    + new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(today.getTime()));
            return;
        }
        manager.cancel(p);
        Log.i("ALARM", "Alarm disposed");
    }

    private PendingIntent getAlarm(Context context, Intent intent, int notificationId) {
        return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void cancelAlarm(Context context, Intent intent, int notificationId) {
        Log.i("ALARM", "Canceling " + notificationId);
        PendingIntent p = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.cancel(p);
        p.cancel();

        deserializeAlarm(context,notificationId);
        Log.i("ALARM", "Alarm canceled");
    }

    private void cancelAllAlarms(Context context) {
        Log.i("ALARM", "Canceling all alarms...");
        for (int idAlarm : getAlarmIds(context)) {
            cancelAlarm(context, new Intent(context, AlarmActivity.class), idAlarm);
        }
        Log.i("ALARM", "All alarms canceled");
    }

    private void serializeAlarm(Context context, int notificationId) {
        List<Integer> idsAlarms = getAlarmIds(context);

        if (idsAlarms.contains(notificationId)) return;

        idsAlarms.add(notificationId);

        saveIdsInPreferences(context, idsAlarms);
    }

    private void deserializeAlarm(Context context, int notificationId) {
        List<Integer> idsAlarms = getAlarmIds(context);

        for (int i = 0; i < idsAlarms.size(); i++) {
            if (idsAlarms.get(i) == notificationId)
                idsAlarms.remove(i);
        }

        saveIdsInPreferences(context, idsAlarms);
    }

    private List<Integer> getAlarmIds(Context context) {
        List<Integer> ids = new ArrayList<>();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            JSONArray jsonArray2 = new JSONArray(prefs.getString(sTagAlarms, "[]"));

            for (int i = 0; i < jsonArray2.length(); i++) {
                ids.add(jsonArray2.getInt(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    private static void saveIdsInPreferences(Context context, List<Integer> lstIds) {
        JSONArray jsonArray = new JSONArray();
        for (Integer idAlarm : lstIds) {
            jsonArray.put(idAlarm);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(sTagAlarms, jsonArray.toString());

        editor.apply();
    }

    @Deprecated
    boolean isShowing() {
        return ActivityHelper.getActivity() instanceof AlarmActivity;
    }
}
