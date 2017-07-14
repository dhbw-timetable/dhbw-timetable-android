package dhbw.timetable.navfragments.notifications.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
    private MediaPlayer mMediaPlayer;
    private AudioManager audioManager;
    private boolean rescheduling;

    private AlarmSupervisor() {}

    public static AlarmSupervisor getInstance() {
        return INSTANCE;
    }

    public void initialize(Context context) {
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mMediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    void startVibrator(Context context) {
        // Get instance of Vibrator from current Context
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        long[][] patterns = {
                {0, 1000, 100, 1000, 100, 1000, 100, 1000, 100},
                {0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100}};
        int patternIndex = sharedPreferences.getInt("alarmVibrationIndex", 0);

        switch(patternIndex) {
            case 0:
                return;
            case 1:
            case 2:
                vibrator.vibrate(patterns[patternIndex-1], -1);
                break;
        }
    }

    void stopVibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }

    void playRingtone(Context context) {
        if(!mMediaPlayer.isPlaying() && !mMediaPlayer.isLooping()) {
            try {
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                mMediaPlayer.setDataSource(context, sound);
                final int before = audioManager.getRingerMode();
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            audioManager.setRingerMode(before);
                        }
                    });
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
                mMediaPlayer.reset();
            }
        }
    }

    void stopRingtone() {
        mMediaPlayer.reset();
    }

     Appointment getCurrentAppointment() {
        TimelessDate today = new TimelessDate();
        TimelessDate monday = new TimelessDate(today);
        DateHelper.Normalize(monday);
        ArrayList<Appointment> week = TimetableManager.getInstance().getGlobals().get(monday);
        return week != null ? DateHelper.GetFirstAppointmentOfDay(week, today) : null;
    }

    private static PendingIntent getAlarm(Context context, int notificationId) {
        return PendingIntent.getBroadcast(context, notificationId, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE);
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
                            addAlarm(context, afterShift);
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

    private void addAlarm(Context context, GregorianCalendar date) {
        int notificationId = new TimelessDate(date).hashCode();
        Log.d("ALARM", "Scheduling alarm " + notificationId);
        PendingIntent p = PendingIntent.getBroadcast(
                context,
                notificationId,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                manager.setAlarmClock(new AlarmManager.AlarmClockInfo(date.getTimeInMillis(), p), p);
            } else {
                manager.setWindow(AlarmManager.RTC_WAKEUP,
                        date.getTimeInMillis(),
                        1,
                        p);
            }

            serializeAlarm(context, notificationId);
        } catch (SecurityException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String errMSG = e.getMessage() + "\n" + sw.toString();
            e.printStackTrace();

            Activity act = ActivityHelper.getActivity();
            if(act != null) {
                ErrorDialog.newInstance("ERROR", "Failed to schedule alarm. Did some alarms crash?", errMSG)
                        .show(act.getFragmentManager(), "ALSECERROR");
            }
        }
        Log.d("ALARM", "Alarm ready for "
                + new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.GERMANY).format(date.getTime()));
    }

    void cancelAlarm(Context context, int notificationId) {
        Log.d("ALARM", "Canceling " + notificationId);
        PendingIntent p = getAlarm(context, notificationId);
        if (p == null) {
            Log.w("ALARM", "Could not find alarm " + notificationId + "!");
            return;
        }
        manager.cancel(p);
        deserializeAlarm(context,notificationId);
        Log.d("ALARM", "Alarm stopped");
    }

    void snoozeAlarm(Context context) {
        Log.i("ALARM", "Snoozing current alarm...");
        cancelAlarm(context, new TimelessDate().hashCode());

        // Reschedule
        GregorianCalendar later = (GregorianCalendar) Calendar.getInstance() ;
        later.setTimeInMillis(later.getTimeInMillis() + SNOOZE_DURATION);
        addAlarm(context, later);

        Log.i("ALARM", "Alarm snoozed");
    }

    private void cancelAllAlarms(Context context) {
        Log.i("ALARM", "Canceling all alarms...");
        for (int idAlarm : getAlarmIds(context)) {
            cancelAlarm(context, idAlarm);
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
