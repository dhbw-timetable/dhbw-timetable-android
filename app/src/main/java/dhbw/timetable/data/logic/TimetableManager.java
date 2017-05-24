package dhbw.timetable.data.logic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public final class TimetableManager {

    public final static ArrayList<Appointment> GLOBAL_TIMETABLES = new ArrayList<>();

    private TimetableManager() {}

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

    /**
     * Downloads timetable contents into GLOBAL_TIMETABLES and writes data to file system
     */
    public static void UpdateGlobals(final Application application, final Runnable updater) {
        GLOBAL_TIMETABLES.clear();
        new AsyncTask<Void, Void, Void>() {
            boolean success = false;

            @Override
            protected Void doInBackground(Void... noArgs) {
                // Get the first timetable
                String timetable = getActiveTimetable(application);
                if (timetable.equals("undefined")) {
                    Log.i("TTM", "There is currently no timetable specified.");
                    return null;
                }
                System.out.println("Loading online globals for " + timetable);

                // Get sync range from Preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);

                GregorianCalendar startDate = (GregorianCalendar) Calendar.getInstance();
                DateHelper.SubtractDays(startDate, Integer.parseInt(prefs.getString("sync_range_past", "0")) * 7);
                DateHelper.Normalize(startDate);

                GregorianCalendar endDate = (GregorianCalendar) Calendar.getInstance();
                DateHelper.AddDays(endDate, Integer.parseInt(prefs.getString("sync_range_future", "0")) * 7);
                DateHelper.Normalize(endDate);

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                System.out.println("Running algorithm for args: START=" + sdf.format(startDate.getTime()) + "; END=" + sdf.format(endDate.getTime()));

                // Run download algorithm for ArrayList GLOBAL_TIMETABLES
                DataImporter importer = new DataImporter(timetable);
                try {
                    importer.importAll(startDate, endDate);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                success = true;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(!success) {
                    Log.w("TTM", "Unable to receive online data");
                    // TODO Let the user know about this error
                    return;
                }
                SharedPreferences sharedPref = application.getSharedPreferences(
                        application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                Log.i("TTM", "Successfully updated global timetables: ");
                Log.i("TTM", TimetableManager.SerialRepresentation());
                // Update UI
                System.out.println("Updating UI...");
                updater.run();
                System.out.println("Done.");

                String changeCrit = sharedPref.getString("onChangeCrit", "None");
                Log.i("TTM", "Searching for changes. Criteria: " + changeCrit);
                switch (changeCrit) {
                    case "None":
                        break;
                    case "Every change":
                        try {
                            FileInputStream fis = application.openFileInput(
                                    application.getResources().getString(R.string.TIMETABLES_FILE));
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            String comp1 = sb.toString();
                            String comp2 = SerialRepresentation();
                            if(comp1.contains(comp2) || comp2.contains(comp1) || comp1.equals(comp2)) {
                                Log.i("RESULT", "EQUAL");
                            } else {
                                Log.i("RESULT", "UNEQUAL");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "One week ahead":
                        // TODO Check one week ahead
                        break;
                }
                // Update offline globals
                try {
                    FileOutputStream outputStream = application.openFileOutput(
                            application.getResources().getString(R.string.TIMETABLES_FILE), Context.MODE_PRIVATE);
                    outputStream.write(SerialRepresentation().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private static void checkForChanges() {

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

    /**
     * Loads the last downloaded timetables into GLOBAL_TIMETABLES
     */
    public static void LoadOfflineGlobals(Application application , Runnable updater) {
        // TODO If no OfflineGlobals were found, try to load them from online
        if(!secureFile(application)) {
            UpdateGlobals(application, updater);
            return;
        }
        System.out.println("Loading offline globals...");
        GLOBAL_TIMETABLES.clear();
        try {
            FileInputStream fis = application.openFileInput(
                    application.getResources().getString(R.string.TIMETABLES_FILE));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));

            String line;
            Appointment a;
            while ((line = bufferedReader.readLine()) != null) {
                String[] aData = line.split("\t");
                String[] date = aData[0].split("\\.");
                GregorianCalendar g = new GregorianCalendar();
                g.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
                g.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                g.set(Calendar.YEAR, Integer.parseInt(date[2]));

                a = new Appointment(aData[1], g, aData[2], aData[3]);

                GLOBAL_TIMETABLES.add(a);
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

    public static String SerialRepresentation() {
        StringBuilder sb = new StringBuilder();
        for (Appointment a : GLOBAL_TIMETABLES) sb.append(a.toString() + "\n");
        return sb.toString();
    }
}
