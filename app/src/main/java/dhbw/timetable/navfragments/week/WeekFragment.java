package dhbw.timetable.navfragments.week;

import android.app.Application;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.DateHelper;
import dhbw.timetable.data.TimelessDate;
import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.dialogs.InfoDialog;
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
        final Application application = getActivity().getApplication();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_week) {

            if(!TimetableManager.getInstance().isBusy()) {
                TimetableManager.getInstance().updateGlobals(application, new Runnable() {
                    @Override
                    public void run() {
                        applyGlobalContent(true);
                        // Toast.makeText(application, "Updated!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(WeekFragment.this.getView(), "Updated!", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        } else if (id == R.id.action_pick_week) {
            DatePickerDialog.OnDateSetListener handler = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    weekToDisplay.set(Calendar.YEAR, year);
                    weekToDisplay.set(Calendar.MONTH, month);
                    weekToDisplay.set(Calendar.DAY_OF_MONTH, day);
                    Log.i("DATE", "Picked date: " + day + "." + month + "." + year);
                    displayWeek();
                }
            };

            DatePickerDialog dpd = new DatePickerDialog(getContext(), handler,
                    weekToDisplay.get(Calendar.YEAR),
                    weekToDisplay.get(Calendar.MONTH),
                    weekToDisplay.get(Calendar.DAY_OF_MONTH));
            dpd.show();
            return true;
        }

        return false;
    }

    private void displayWeek() {
            if (applyGlobalContent(true)) {
                TimetableManager.getInstance().updateGlobals(this.getActivity().getApplication(), new Runnable() {
                    @Override
                    public void run() {
                        applyGlobalContent(false);
                        // Toast.makeText(WeekFragment.this.getActivity(), "Updated!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(WeekFragment.this.getView(), "Updated!", Snackbar.LENGTH_SHORT).show();
                    }
                });
            } else {
                if(!TimetableManager.getInstance().isBusy()) {
                    TimetableManager.getInstance().reorderSpecialGlobals(this.getActivity().getApplication(), new Runnable() {
                        @Override
                        public void run() {
                            applyGlobalContent(false);
                            // Toast.makeText(WeekFragment.this.getActivity(), "Updated special date!", Toast.LENGTH_SHORT).show();
                            Snackbar.make(WeekFragment.this.getView(), "Updated special date!!", Snackbar.LENGTH_SHORT).show();
                        }
                    }, weekToDisplay);
                } else {
                    Log.w("ASYNC", "Tried to sync while manager was busy");
                }
            }
    }


    /** Applies timetables to UI. Return true if successful
    and false if the date requested from the UI would not
    match the loaded globals*/
    public boolean applyGlobalContent(boolean firstTry) {
        View view = this.getView();
        LinearLayout body = (LinearLayout) view.findViewById(R.id.week_layout_body);
        RelativeLayout times = (RelativeLayout) view.findViewById(R.id.week_layout_times);

        // Prepare appointment data
        TimelessDate day = (TimelessDate) weekToDisplay.clone();
        DateHelper.Normalize(day);
        String formattedDate = new SimpleDateFormat("EEEE dd.MM.yyyy", Locale.GERMANY).format(day.getTime());
        getActivity().setTitle(formattedDate);

        ArrayList<Appointment> weekAppointments = DateHelper.GetWeekAppointments(day, TimetableManager.getInstance().getGlobalsAsList());
        Log.d("TTM", weekAppointments.size() + " week appointments for: " + formattedDate);
        if(weekAppointments.size() == 0 && firstTry) {
            return false;
        } else if (weekAppointments.size() == 0) {
            body.removeAllViews();
            times.removeAllViews();
            InfoDialog.newInstance("Error", "No appointments found.").show(getActivity().getFragmentManager(), "Empty");
            return true;
        }

        for(Appointment a : weekAppointments) Log.d("TTM", a.toString());
        Pair<Integer, Integer> borders = DateHelper.GetBorders(weekAppointments);

        // If margin is possible
        int fExtensionFirst = borders.first >= 30 ? borders.first - 30 : borders.first;
        int fExtensionSecond = borders.second <= 1410 ? borders.second + 30 : borders.second;

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
                    DateHelper.GetAppointmentsOfDay(day, weekAppointments), i == 4);
            dayElement.setBackgroundColor(Color.parseColor("#FAFAFA"));
            body.addView(dayElement);

            DateHelper.AddDays(day, 1);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Reset to today
        weekToDisplay = new TimelessDate();
        DateHelper.Normalize(weekToDisplay);
        getActivity().setTitle(new SimpleDateFormat("EEEE dd.MM.yyyy", Locale.GERMANY).format(weekToDisplay.getTime()));
        TimetableManager.getInstance().loadOfflineGlobals(getActivity().getApplication(), new Runnable() {
            @Override
            public void run() {
                Log.i("TTM", "Successfully loaded offline globals for week fragment.");
                applyGlobalContent(true);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_week, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_week, menu);
    }
}
