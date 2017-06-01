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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.R;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public final class TimetableManager {

    private final static TimetableManager INSTANCE = new TimetableManager();

    private final ArrayList<Appointment> globalTimetables = new ArrayList<>();
    private final ArrayList<Appointment> localTimetables  = new ArrayList<>();
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

    private boolean notificationNeeded(Application application, SharedPreferences sharedPref) {
        if(!secureFile(application)) {
            Log.i("TTM", "No offline globals to compare.");
            return false;
        }
        String changeCrit = sharedPref.getString("onChangeCrit", "None");
        Log.i("TTM", "Searching for changes. Criteria: " + changeCrit);
        switch (changeCrit) {
            case "None":
                return false;
            case "Every change":
                // Simply check if serial representation of globals and offline globals would match
                try {
                    FileInputStream fis = application.openFileInput(
                            application.getResources().getString(R.string.TIMETABLES_FILE));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) sb.append(line + "\n");
                    return !sb.toString().equals(serialRepresentation());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            case "One week ahead":
                GregorianCalendar thisWeek = (GregorianCalendar) Calendar.getInstance();
                GregorianCalendar nextWeek = (GregorianCalendar) thisWeek.clone();
                DateHelper.NextWeek(nextWeek);
                return !getPartialRepresentation(application, thisWeek, nextWeek)
                        .equals(weekRepresentation(thisWeek) + weekRepresentation(nextWeek));
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
        if(notificationNeeded(application, sharedPref)) {
            Log.i("TTM", "Changes found. Would fire!");
            switch(sharedPref.getString("onChangeForm", "None")) {
                case "None":
                    if(sound != null) {
                        Ringtone r = RingtoneManager.getRingtone(application.getApplicationContext(), sound);
                        r.play();
                    }
                    break;
                case "Banner":
                    fireBanner(sound);
                    break;
            }

        } else {
            Log.i("TTM", "No relevant changes found. Won't fire any notification for change.");
        }
    }

    private static void fireBanner(Uri sound) {
        Activity curr = ActivityHelper.getActivity();

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

    public ArrayList<Appointment> getGlobals() {
        return globalTimetables;
    }

    public ArrayList<Appointment> getLocals() {
        return localTimetables;
    }

    /**
     * Downloads timetable contents from only on day into existing GLOBAL_TIMETABLES and writes
     * complete global data to file system
     */
    public void reorderSpecialGlobals(final Application application, final Runnable updater, final GregorianCalendar date) {
        // DO NOT CLEAR GLOBALS ONLY LOCALS
        globalTimetables.removeAll(localTimetables);
        localTimetables.clear();
        new AsyncTask<Void, Void, Void>() {
            boolean success = false;

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
                GregorianCalendar startDate = (GregorianCalendar) date.clone();
                DateHelper.Normalize(startDate);

                GregorianCalendar endDate = (GregorianCalendar) startDate.clone();

                Log.i("TTM", "REORDER algorithm for " + new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(startDate.getTime()));

                // Run download algorithm for ArrayList LOCAL_TIMETABLES
                DataImporter importer = new DataImporter(timetable, false);
                try {
                    importer.importAll(startDate, endDate);
                    success = true;
                    globalTimetables.addAll(localTimetables);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(!success) {
                    Log.w("TTM", "Unable to receive online data");
                    // TODO Let the user know about this error
                    return;
                }

                Log.i("TTM", "Successfully REORDERED SPECIAL global timetables");
                Log.d("TTM", TimetableManager.getInstance().serialRepresentation());
                // Update UI
                Log.i("TTM", "Updating UI...");
                updater.run();
                Log.i("TTM", "Updated UI!");

                TimetableManager.this.busy = false;

                // handleChangePolicies(application);

                /* Update offline globals
                try {
                    FileOutputStream outputStream = application.openFileOutput(
                            application.getResources().getString(R.string.TIMETABLES_FILE), Context.MODE_PRIVATE);
                    outputStream.write(SerialRepresentation().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
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
            boolean success = false;

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
                Log.i("TTM", "Loading online globals for " + timetable);

                // Get sync range from Preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);

                GregorianCalendar startDate = (GregorianCalendar) Calendar.getInstance();
                DateHelper.SubtractDays(startDate, Integer.parseInt(prefs.getString("sync_range_past", "0")) * 7);
                DateHelper.Normalize(startDate);

                GregorianCalendar endDate = (GregorianCalendar) Calendar.getInstance();
                DateHelper.AddDays(endDate, Integer.parseInt(prefs.getString("sync_range_future", "0")) * 7);
                DateHelper.Normalize(endDate);

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                Log.i("TTM", "Running algorithm from " + sdf.format(startDate.getTime()) + " to " + sdf.format(endDate.getTime()));

                // Run download algorithm for ArrayList GLOBAL_TIMETABLES
                DataImporter importer = new DataImporter(timetable, true);
                try {
                    importer.importAll(startDate, endDate);
                    success = true;
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(!success) {
                    Log.w("TTM", "Unable to receive online data");
                    // TODO Let the user know about this error
                    return;
                }

                Log.i("TTM", "Successfully updated global timetables");
                Log.d("TTM", serialRepresentation());
                // Update UI
                Log.i("TTM", "Updating UI...");
                updater.run();
                Log.i("TTM", "Updated UI!");

                handleChangePolicies(application);

                // Update offline globals
                try {
                    FileOutputStream outputStream = application.openFileOutput(
                            application.getResources().getString(R.string.TIMETABLES_FILE), Context.MODE_PRIVATE);
                    outputStream.write(serialRepresentation().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
        System.out.println("Loading offline globals...");
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
                GregorianCalendar g = new GregorianCalendar();
                g.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
                g.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                g.set(Calendar.YEAR, Integer.parseInt(date[2]));

                a = new Appointment(aData[1], g, aData[2], aData[3]);

                globalTimetables.add(a);
            }
            System.out.println("Success!");
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILED!");
        }
        System.out.println("Updating UI...");
        updater.run();
        System.out.println("Done");
    }

    private String weekRepresentation(GregorianCalendar g) {
        StringBuilder sb = new StringBuilder();
        for (Appointment a : DateHelper.GetWeekAppointments(g, globalTimetables))
            sb.append(a.toString()).append("\n");
        return sb.toString();
    }

    private String serialRepresentation() {
        StringBuilder sb = new StringBuilder();
        Appointment before = null;
        for (Appointment a : globalTimetables) {
            sb.append(a.toString()).append("\n");
            if(before != null && !DateHelper.IsSameWeek(a.getStartDate(), before.getStartDate())) {
                sb.append("\n");
            }
            before = a;
        }
        return sb.toString();
    }

    /**
     * Get partial representation within two weeks
     */
    private String getPartialRepresentation(Application application, GregorianCalendar startG, GregorianCalendar endG) {
        if(!DateHelper.IsSameWeek(startG, endG)) {
            try {
                FileInputStream fis = application.openFileInput(
                        application.getResources().getString(R.string.TIMETABLES_FILE));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

                StringBuilder partialBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(line.isEmpty()) continue;
                    String[] date = line.split("\t")[0].split("\\.");
                    GregorianCalendar g = new GregorianCalendar();
                    g.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
                    g.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                    g.set(Calendar.YEAR, Integer.parseInt(date[2]));
                    if(DateHelper.IsSameWeek(g, startG) || DateHelper.IsSameWeek(g, endG)) {
                        partialBuilder.append(line).append("\n");
                    }
                }
                bufferedReader.close();
                return partialBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
