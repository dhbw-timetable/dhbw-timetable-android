package dhbw.timetable.navfragments.week;

import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.logic.DateHelper;
import dhbw.timetable.data.logic.TimetableManager;
import dhbw.timetable.views.SideTimesView;
import dhbw.timetable.views.WeekdayView;

public class WeekFragment extends Fragment {

    private GregorianCalendar weekToDisplay;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final Application application = getActivity().getApplication();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_week) {
            TimetableManager.UpdateGlobals(application, new Runnable() {
                @Override
                public void run() {
                    applyGlobalContent();
                    Toast.makeText(application, "Updated!", Toast.LENGTH_SHORT).show();
                }
            });
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
        applyGlobalContent();
        TimetableManager.UpdateGlobals(this.getActivity().getApplication(), new Runnable() {
            @Override
            public void run() {
                applyGlobalContent();
                Toast.makeText(WeekFragment.this.getActivity(), "Updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void applyGlobalContent() {
        View view = this.getView();
        LinearLayout body = (LinearLayout) view.findViewById(R.id.week_layout_body);
        RelativeLayout times = (RelativeLayout) view.findViewById(R.id.week_layout_times);

        // Prepare appointment data
        GregorianCalendar day = (GregorianCalendar) weekToDisplay.clone();
        DateHelper.Normalize(day);
        String formattedDate = new SimpleDateFormat("EEEE dd.MM.yyyy", Locale.GERMANY).format(day.getTime());
        getActivity().setTitle(formattedDate);

        ArrayList<Appointment> weekAppointments = DateHelper.GetWeekAppointments(day, TimetableManager.GLOBAL_TIMETABLES);
        Log.i("TTM", "Week appointments for :" + formattedDate);
        for(Appointment a : weekAppointments) Log.i("TTM", a.toString());
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
    }

    @Override
    public void onStart() {
        super.onStart();
        // Reset to today
        weekToDisplay = (GregorianCalendar) Calendar.getInstance();
        DateHelper.Normalize(weekToDisplay);
        getActivity().setTitle(new SimpleDateFormat("EEEE dd.MM.yyyy", Locale.GERMANY).format(weekToDisplay.getTime()));
        TimetableManager.LoadOfflineGlobals(getActivity().getApplication(), new Runnable() {
            @Override
            public void run() {
                Log.i("TTM", "Successfully loaded offline globals for week fragment.");
                applyGlobalContent();
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
