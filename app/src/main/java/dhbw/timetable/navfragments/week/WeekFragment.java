package dhbw.timetable.navfragments.week;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import dhbw.timetable.R;
import dhbw.timetable.data.ErrorCallback;
import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.dialogs.ErrorDialog;
import dhbw.timetable.dialogs.InfoDialog;
import dhbw.timetable.rablabla.data.BackportAppointment;
import dhbw.timetable.rablabla.data.DateUtilities;
import dhbw.timetable.rablabla.data.TimelessDate;
import dhbw.timetable.views.SideTimesView;
import dhbw.timetable.views.WeekdayView;


/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class WeekFragment extends Fragment {

    private TimelessDate weekToDisplay;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final Activity activity = getActivity();
        final Application application = activity.getApplication();
        final View view = WeekFragment.this.getView();
        if (id == R.id.action_refresh_week) {
            if(!TimetableManager.getInstance().isRunning()) {
                TimetableManager.getInstance().updateGlobals(application, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            applyGlobalContent(true, false, view, activity);
                            Snackbar.make(view, "Updated!", Snackbar.LENGTH_SHORT).show();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }, new ErrorCallback() {
                    @Override
                    public void onError(String string) {
                        ErrorDialog.newInstance("Error", "Unable to update timetable data.", string)
                                .show(WeekFragment.this.getActivity().getFragmentManager(), "WEEKDLERR2");
                    }
                });
            } else {
                Toast.makeText(activity, "I'm currently busy, sorry!", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_today_week) {
            if(!TimetableManager.getInstance().isRunning()) {
                weekToDisplay = new TimelessDate();
                displayWeek(view, activity, true);
            } else {
                Toast.makeText(activity, "I'm currently busy, sorry!", Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    private void pickWeek(final View view, final Activity activity) {
        if(!TimetableManager.getInstance().isRunning()) {
            DatePickerDialog.OnDateSetListener handler = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker dpView, int year, int month, int day) {
                    weekToDisplay.set(Calendar.YEAR, year);
                    weekToDisplay.set(Calendar.MONTH, month);
                    weekToDisplay.set(Calendar.DAY_OF_MONTH, day);
                    Log.i("DATE", "Picked date: " + day + "." + month + "." + year);
                    displayWeek(view, activity, false);
                }
            };

            DatePickerDialog dpd = new DatePickerDialog(getContext(), handler,
                    weekToDisplay.get(Calendar.YEAR),
                    weekToDisplay.get(Calendar.MONTH),
                    weekToDisplay.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        } else {
            Toast.makeText(activity, "I'm currently busy, sorry!", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayWeek(final View view, final Activity activity, final boolean today) {
        TimetableManager.getInstance().loadOfflineGlobals(activity.getApplication(), new Runnable() {
            @Override
            public void run() {
                applyGlobalContent(false, false, view, activity);
            }
        });
        if (applyGlobalContent(true, false, view, activity)) {
            TimetableManager.getInstance().updateGlobals(activity.getApplication(), new Runnable() {
                @Override
                public void run() {
                    try {
                        applyGlobalContent(false, false, view, activity);
                        Snackbar.make(view, today ? "Updated to today!" : "Updated!", Snackbar.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }, new ErrorCallback() {
                @Override
                public void onError(String string) {
                    ErrorDialog.newInstance("Warning", "Unable to update timetable data. The data may be not up to date.", string).show(WeekFragment.this.getActivity().getFragmentManager(), "WEEKDLERR");
                }
            });
        } else {
            if(!TimetableManager.getInstance().isRunning()) {
                TimetableManager.getInstance().reorderSpecialGlobals(activity.getApplication(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            applyGlobalContent(false, true, view, activity);
                            Snackbar.make(view, "Updated special date!", Snackbar.LENGTH_SHORT).show();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }, new ErrorCallback() {
                    @Override
                    public void onError(String string) {
                        ErrorDialog.newInstance("Error", "Unable to load specifiy week. This week is not in your sync range. There is no data for it.", string).show(WeekFragment.this.getActivity().getFragmentManager(), "WEEKDLERR");
                    }
                }, weekToDisplay);
            } else {
                Log.w("ASYNC", "Tried to sync while manager was busy.");
                Toast.makeText(activity, "I'm currently busy, sorry!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Applies timetables to UI. Return true if successful
    and false if the date requested from the UI would not
    match the loaded globals */
    public boolean applyGlobalContent(boolean firstTry, boolean special, final View view, final Activity activity) {
        LinearLayout body = (LinearLayout) view.findViewById(R.id.week_layout_body);
        RelativeLayout times = (RelativeLayout) view.findViewById(R.id.week_layout_times);

        // Prepare appointment data
        TimelessDate day = (TimelessDate) weekToDisplay.clone();
        DateUtilities.Backport.Normalize(day);
        String formattedDate = new SimpleDateFormat("EE dd.MM.yy", Locale.GERMANY).format(day.getTime());
        // activity.setTitle(formattedDate);
        TextView actTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        actTitle.setText(formattedDate);
        actTitle.setOnClickListener(v -> pickWeek(view, activity));

        ArrayList<BackportAppointment> weekAppointments = DateUtilities.Backport.GetWeekAppointments(day, TimetableManager.getInstance().getGlobalsAsList());
        Log.d("TTM", weekAppointments.size() + " week appointments for: " + formattedDate);
        if (weekAppointments.size() == 0 && firstTry) {
            return false;
        } else if (weekAppointments.size() == 0) {
            body.removeAllViews();
            times.removeAllViews();
            if (special) {
                InfoDialog.newInstance("Info", "No appointments found. Sync range to low or simply no appointments scheduled!").show(activity.getFragmentManager(), "Empty");
            }
            return true;
        }

        for(BackportAppointment a : weekAppointments) Log.d("TTM", a.toString());

        Integer[] borders = DateUtilities.Backport.GetBorders(weekAppointments);

        // If margin is possible
        int fExtensionFirst = borders[0] >= 30 ? borders[0] - 30 : borders[0];
        int fExtensionSecond = borders[1] <= 1410 ? borders[1] + 30 : borders[1];

        // Initialize side time view
        times.removeAllViews();
        SideTimesView sideTimesView = new SideTimesView(fExtensionFirst, fExtensionSecond, times, body);
        sideTimesView.setBackgroundColor(Color.parseColor("#F0F0F0"));
        times.addView(sideTimesView);
        // Initialize body content
        body.removeAllViews();
        WeekdayView dayElement;
        for (int i = 0; i < 5; i++) {
            dayElement = new WeekdayView(fExtensionFirst, fExtensionSecond, body,
                    DateUtilities.Backport.GetAppointmentsOfDay(day, weekAppointments), i == 4, new SimpleDateFormat("EE dd.MM.yyyy", Locale.GERMANY).format(day.getTime()));
            dayElement.setBackgroundColor(Color.parseColor("#FAFAFA"));
            body.addView(dayElement);

            DateUtilities.Backport.AddDays(day, 1);
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Activity activity = getActivity();
        AppBarLayout appBarLayout = (AppBarLayout) activity.findViewById(R.id.appbar);

        // Handle the tabs from navigation fragment
        if (appBarLayout.getChildCount() != 1) appBarLayout.removeViewAt(1);

        final View rootView = inflater.inflate(R.layout.content_week, container, false);

        // Reset to today
        weekToDisplay = new TimelessDate();
        int iDay = weekToDisplay.get(Calendar.DAY_OF_WEEK);
        if (iDay == Calendar.SATURDAY || iDay == Calendar.SUNDAY) {
            DateUtilities.Backport.NextWeek(weekToDisplay);
        }
        DateUtilities.Backport.Normalize(weekToDisplay);
        // activity.setTitle();
        TextView actTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        actTitle.setText(new SimpleDateFormat("EEEE dd.MM.yyyy", Locale.GERMANY).format(weekToDisplay.getTime()));
        actTitle.setOnClickListener(v -> pickWeek(rootView, activity));
        TimetableManager.getInstance().loadOfflineGlobals(activity.getApplication(), new Runnable() {
            @Override
            public void run() {
                Log.i("TTM", "Successfully loaded offline globals for week fragment.");
                applyGlobalContent(true, false, rootView, activity);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d("WEEKMENU", "Using menu. dpWidth=" + dpWidth);
        if(dpWidth >= 100) {
            inflater.inflate(R.menu.menu_week, menu);
        } else {
            inflater.inflate(R.menu.menu_week_small, menu);
        }
    }
}
