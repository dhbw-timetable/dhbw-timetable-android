package dhbw.timetable.data;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.R;
import dhbw.timetable.dialogs.ErrorDialog;
import dhbw.timetable.navfragments.notifications.alarm.AlarmSupervisor;

import static dhbw.timetable.ActivityHelper.getActivity;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public final class TimetableManager {

    private final static TimetableManager INSTANCE = new TimetableManager();

    private final Map<TimelessDate, ArrayList<Appointment>> globalTimetables = new HashMap<>();
    private final Map<TimelessDate, ArrayList<Appointment>> localTimetables  = new HashMap<>();
    private boolean busy = false;

    private TimetableManager() {}

    public static TimetableManager getInstance() {
        return INSTANCE;
    }

    private static String getActiveTimetable(Application a) {
        SharedPreferences sharedPref = a.getSharedPreferences(
                a.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        for(String key : sharedPref.getAll().keySet()) {
            // filter for timetables
            if(key.startsWith("t#")) {
                return sharedPref.getString(key, "undefined");
            }
        }
        return "undefined";
    }

    private boolean areAppointmentsEqual(ArrayList<Appointment> l1, ArrayList<Appointment> l2) {
        if(l1.size() == l2.size()) {
            Appointment a1, a2;
            for (int i = 0; i < l1.size(); i++) {
                a1 = l1.get(i);
                a2 = l2.get(i);
                if (!a1.equals(a2)) return false;
            }
        }
        return true;
    }

    private boolean notificationNeeded(Application application, SharedPreferences sharedPref) {
        if(!secureFile(application)) {
            Log.i("TTM", "No offline globals to compare.");
            return false;
        }
        String changeCrit = sharedPref.getString("onChangeCrit", "None");
        Log.i("TTM", "Searching for changes. Criteria: " + changeCrit);
        Map<TimelessDate, ArrayList<Appointment>> offlineTimetables;
        switch (changeCrit) {
            case "None":
                return false;
            case "Every change":
                offlineTimetables = loadOfflineGlobalsIntoList(application);

                for(TimelessDate date : offlineTimetables.keySet()) {
                    // Can only compare if available
                    if(globalTimetables.containsKey(date)) {
                        Log.i("COMP", "Comparing week " + new SimpleDateFormat("dd.MM.yyyy").format(date.getTime()));
                        if(!areAppointmentsEqual(offlineTimetables.get(date),
                                globalTimetables.get(date))) {
                            return true;
                        }
                    }
                }
                return false;
            case "One week ahead":
                TimelessDate thisWeek = new TimelessDate();
                TimelessDate nextWeek = new TimelessDate();
                DateHelper.NextWeek(nextWeek);

                offlineTimetables = loadOfflineGlobalsIntoList(application);

                if(offlineTimetables.containsKey(thisWeek)) {
                    if(!areAppointmentsEqual(offlineTimetables.get(thisWeek),
                            globalTimetables.get(thisWeek))) {
                        return true;
                    }
                }
                if(offlineTimetables.containsKey(nextWeek)) {
                    if(!areAppointmentsEqual(offlineTimetables.get(nextWeek),
                            globalTimetables.get(nextWeek))) {
                        return true;
                    }
                }
                return false;
        }
        Log.e("TTM", "Error! Wrong change crit: " + changeCrit);
        return false;
    }

    private static Uri getTone(SharedPreferences sharedPreferences) {
        String tone = sharedPreferences.getString("onChangeTone", "None");
        switch(tone) {
            case "None":
                return null;
            case "Default":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Log.w("TONE", "Warning, invalid ringtone for on change notification: " + tone);
        return null;
    }

    private void handleChangePolicies(Application application) {
        SharedPreferences sharedPref = application.getSharedPreferences(
                application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Uri sound = getTone(sharedPref);
        if (notificationNeeded(application, sharedPref)) {
            Log.i("TTM", "Changes found. Would fire!");
            switch (sharedPref.getString("onChangeForm", "Banner")) {
                case "None":
                    if (sound != null) {
                        Ringtone r = RingtoneManager.getRingtone(application.getApplicationContext(), sound);
                        r.play();
                    }
                    break;
                case "Banner":
                    fireBanner(sound);
                    break;
            }

        } else {
            Log.i("TTM", "Change check negative -> Won't fire any notification for change.");
        }
    }

    private static void fireBanner(Uri sound) {
        Activity curr = getActivity();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(curr)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setSound(sound)
                        .setLargeIcon(BitmapFactory.decodeResource(curr.getResources(),R.mipmap.ic_launcher_large))
                        .setContentTitle(curr.getResources().getString(R.string.app_name))
                        .setContentText("Your timetable changed!");

        if(sound != null) {
            mBuilder.setSound(sound);
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(curr, curr.getClass());

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(curr);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(curr.getClass());
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager)
                curr.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1337, mBuilder.build());
    }

    private static boolean secureFile(Application application) {
        try {
            FileInputStream fis = application.openFileInput(application.getResources().getString(R.string.TIMETABLES_FILE));
            fis.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isBusy() {
        return busy;
    }

    public Map<TimelessDate, ArrayList<Appointment>> getGlobals() {
        return globalTimetables;
    }

    public ArrayList<Appointment> getGlobalsAsList() {
        ArrayList<Appointment> weeks = new ArrayList<>();
        for(ArrayList<Appointment> week : globalTimetables.values()) weeks.addAll(week);
        return weeks;
    }

    private ArrayList<Appointment> getLocalsAsList() {
        ArrayList<Appointment> weeks = new ArrayList<>();
        for(ArrayList<Appointment> week : localTimetables.values()) weeks.addAll(week);
        return weeks;
    }

    /**
     * Downloads timetable contents from only on day into existing GLOBAL_TIMETABLES and writes
     * complete global data to file system
     */
    public void reorderSpecialGlobals(final Application application, final Runnable updater, final TimelessDate date) {
        // DO NOT CLEAR GLOBALS ONLY LOCALS
        globalTimetables.keySet().removeAll(localTimetables.keySet());
        localTimetables.clear();
        new AsyncTask<Void, Void, Void>() {
            boolean success = false;
            String errMSG;

            @Override
            protected Void doInBackground(Void... noArgs) {
                if(TimetableManager.this.busy) {
                    Log.e("ASYNC", "Critical warning: trying to update globals asynchronous!");
                }
                TimetableManager.this.busy = true;

                // Get the first timetable
                String timetable = getActiveTimetable(application);
                if (timetable.equals("undefined")) {
                    Log.w("TTM", "There is currently no timetable specified.");
                    return null;
                }
                Log.i("TTM", "Loading SPECIAL online globals for " + timetable);

                // Same start and end date
                TimelessDate startDate = (TimelessDate) date.clone();
                DateHelper.Normalize(startDate);

                TimelessDate endDate = (TimelessDate) startDate.clone();

                Log.i("TTM", "REORDER algorithm for " + new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(startDate.getTime()));

                // Run download algorithm for ArrayList LOCAL_TIMETABLES
                DataImporter importer = new DataImporter(timetable, false);
                try {
                    importer.importAll(startDate, endDate);
                    success = true;
                    globalTimetables.putAll(localTimetables);
                } catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);

                    errMSG = e.getMessage() + "\n" + sw.toString();
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(!success) {
                    Log.w("TTM", "Unable to receive online data");
                    Activity activity = ActivityHelper.getActivity();
                    if(activity != null) {
                        ErrorDialog.newInstance("ERROR", "Unable to receive online data", "")
                                .show(activity.getFragmentManager(), "DLSERROR");
                    }
                    return;
                }

                Log.i("TTM", "Successfully REORDERED SPECIAL global timetables");
                Log.d("TTM", TimetableManager.getInstance().serialRepresentation());
                // Update UI
                Log.i("TTM", "Updating UI...");
                updater.run();
                Log.i("TTM", "Updated UI!");

                TimetableManager.this.busy = false;
            }
        }.execute();
    }

    /**
     * Downloads timetable contents into cleared GLOBAL_TIMETABLES and writes data to file system
     */
    public void updateGlobals(final Application application, final Runnable updater) {
        globalTimetables.clear();
        localTimetables.clear();
        new AsyncTask<Void, Void, Void>() {
            boolean success = false, timetablePresent = true;
            String errMSG;

            @Override
            protected Void doInBackground(Void... noArgs) {
                if(TimetableManager.this.busy) {
                    Log.e("ASYNC", "Critical warning: trying to update globals asynchronous!");
                }
                TimetableManager.this.busy = true;
                // Get the first timetable
                String timetable = getActiveTimetable(application);
                if (timetable.equals("undefined")) {
                    Log.w("TTM", "There is currently no timetable specified.");
                    timetablePresent = false;
                    return null;
                }
                Log.i("TTM", "Loading online globals for " + timetable);

                // Get sync range from Preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);

                TimelessDate startDate = new TimelessDate();
                DateHelper.SubtractDays(startDate, Integer.parseInt(prefs.getString("sync_range_past", "1")) * 7);
                DateHelper.Normalize(startDate);

                TimelessDate endDate = new TimelessDate();
                DateHelper.AddDays(endDate, Integer.parseInt(prefs.getString("sync_range_future", "1")) * 7);
                DateHelper.Normalize(endDate);

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                Log.i("TTM", "Running algorithm from " + sdf.format(startDate.getTime()) + " to " + sdf.format(endDate.getTime()));

                // Run download algorithm for ArrayList GLOBAL_TIMETABLES
                DataImporter importer = new DataImporter(timetable, true);
                try {
                    importer.importAll(startDate, endDate);
                    success = true;
                } catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);

                    errMSG = e.getMessage() + "\n" + sw.toString();
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(!success) {
                    Log.w("TTM", "Unable to receive online data");
                    // If user is on board
                    if(timetablePresent) {
                        // let him know about this error
                        Activity activity = ActivityHelper.getActivity();
                        if (activity != null) {
                            ErrorDialog.newInstance("ERROR", "Unable to receive online data", errMSG)
                                    .show(activity.getFragmentManager(), "DLERROR");
                        }
                    }
                    return;
                }

                Log.i("TTM", "Successfully updated global timetables [" + globalTimetables.size() + "][" + getGlobalsAsList().size() + "]:");
                Log.d("TTM", serialRepresentation());
                // Update UI
                Log.i("TTM", "Updating UI...");
                updater.run();
                Log.i("TTM", "Updated UI!");

                AlarmSupervisor.getInstance().rescheduleAllAlarms(application);

                handleChangePolicies(application);

                // Update offline globals
                try {
                    FileOutputStream outputStream = application.openFileOutput(
                            application.getResources().getString(R.string.TIMETABLES_FILE), Context.MODE_PRIVATE);
                    outputStream.write(serialRepresentation().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);

                    errMSG = e.getMessage() + "\n" + sw.toString();
                    e.printStackTrace();

                    // let user know about this error
                    Activity activity = ActivityHelper.getActivity();
                    if (activity != null) {
                        ErrorDialog.newInstance("ERROR", "Unable to update offline data", errMSG)
                                .show(activity.getFragmentManager(), "OFFERROR");
                    }
                }

                TimetableManager.this.busy = false;
            }
        }.execute();
    }

    /**
     * Loads the last downloaded timetables into GLOBAL_TIMETABLES
     */
    public void loadOfflineGlobals(Application application , Runnable updater) {
        // If no OfflineGlobals were found, try to load them from online
        if(!secureFile(application)) {
            Log.i("TTM", "No offline globals were found, checking online.");
            if(!TimetableManager.getInstance().isBusy()) {
                updateGlobals(application, updater);
            } else {
                Log.i("ASYNC", "Tried to sync while manager was busy");
            }
            return;
        }
        Log.i("TTM", "Loading offline globals...");
        globalTimetables.clear();
        try {
            FileInputStream fis = application.openFileInput(
                    application.getResources().getString(R.string.TIMETABLES_FILE));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

            String line;
            Appointment a;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.isEmpty()) continue;

                String[] aData = line.split("\t");
                String[] date = aData[0].split("\\.");
                TimelessDate g = new TimelessDate();
                g.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
                g.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                g.set(Calendar.YEAR, Integer.parseInt(date[2]));

                a = new Appointment(aData[1], g, aData[2], aData[3]);

                TimetableManager.getInstance().insertAppointment(globalTimetables, (TimelessDate) g.clone(), a);
            }
            Log.i("TTM", "Success!");
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();

            Log.e("TTM", "FAILED!");
        }
        Log.i("TTM", "Updating UI...");
        updater.run();
        Log.i("TTM", "Done");
    }

    Map<TimelessDate, ArrayList<Appointment>> getLocals() {
        return localTimetables;
    }

    void insertAppointment(Map<TimelessDate, ArrayList<Appointment>> globals, GregorianCalendar date, Appointment a) {
        TimelessDate week = new TimelessDate(date);
        DateHelper.Normalize(week);

        if(!globals.containsKey(week)) globals.put(week, new ArrayList<Appointment>());
        globals.get(week).add(a);
    }

    private Map<TimelessDate, ArrayList<Appointment>> loadOfflineGlobalsIntoList(
            Application application) {
        Log.i("TTM", "Accessing offline globals...");
        Map<TimelessDate, ArrayList<Appointment>> offlineAppointments = new HashMap<>();
        try {
            FileInputStream fis = application.openFileInput(
                    application.getResources().getString(R.string.TIMETABLES_FILE));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

            String line;
            Appointment a;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.isEmpty()) continue;

                String[] aData = line.split("\t");
                String[] date = aData[0].split("\\.");
                TimelessDate g = new TimelessDate();
                g.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
                g.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                g.set(Calendar.YEAR, Integer.parseInt(date[2]));

                a = new Appointment(aData[1], g, aData[2], aData[3]);

                TimetableManager.getInstance().insertAppointment(offlineAppointments, (TimelessDate) g.clone(), a);
            }
            Log.i("TTM", "Success!");
            bufferedReader.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String errMSG = e.getMessage() + "\n" + sw.toString();
            e.printStackTrace();

            // let user know about this error
            Activity activity = ActivityHelper.getActivity();
            if (activity != null) {
                ErrorDialog.newInstance("ERROR", "Unable to import offline data. Is it corrupt?", errMSG)
                        .show(activity.getFragmentManager(), "OFFLOADERROR");
            }
            Log.e("TTM", "FAILED!");
        }
        return offlineAppointments;
    }

    private String serialRepresentation() {
        StringBuilder sb = new StringBuilder();
        for(TimelessDate week : globalTimetables.keySet()) {
            for(Appointment a : globalTimetables.get(week)) {
                sb.append(a.toString()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
