package dhbw.timetable.navfragments.week;

import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.logic.DateHelper;
import dhbw.timetable.data.logic.TimetableManager;
import dhbw.timetable.navfragments.today.TodayFragment;
import dhbw.timetable.views.SideTimesView;
import dhbw.timetable.views.WeekdayView;

public class WeekFragment extends Fragment {

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
                    Toast.makeText(application, "Finished!", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else if (id == R.id.action_search_week) {
            // Toast.makeText(getActivity(), "Not implemented yet.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void applyGlobalContent() {
        View view = this.getView();
        LinearLayout body = (LinearLayout) view.findViewById(R.id.week_layout_body);
        RelativeLayout times = (RelativeLayout) view.findViewById(R.id.week_layout_times);

        // Prepare appointment data
        GregorianCalendar day = (GregorianCalendar) Calendar.getInstance();
        DateHelper.Normalize(day);
        ArrayList<Appointment> weekAppointments = DateHelper.GetWeekAppointments(day, TimetableManager.GLOBAL_TIMETABLES);
        Pair<Integer, Integer> borders = DateHelper.GetBorders(weekAppointments);

        // Initialize side time view
        times.removeAllViews();
        SideTimesView sideTimesView = new SideTimesView(borders.first, borders.second, times, body);
        sideTimesView.setBackgroundColor(Color.parseColor("#F0F0F0"));
        // FIXME: App stucks when adding view (on week nav fragment as default).
        times.addView(sideTimesView);

        // Initialize body content
        body.removeAllViews();
        WeekdayView dayElement;
        for (int i = 0; i < 5; i++) {
            dayElement = new WeekdayView(borders.first, borders.second, body,
                    DateHelper.GetAppointmentsOfDay(day, weekAppointments));
            dayElement.setBackgroundColor(Color.parseColor("#FAFAFA"));
            body.addView(dayElement);

            DateHelper.AddDays(day, 1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        TimetableManager.LoadOfflineGlobals(getActivity().getApplication(), new Runnable() {
            @Override
            public void run() {
                System.out.println("Successfully loaded offline globals for week fragment.");
            }
        });
        applyGlobalContent();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Week");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_week, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_week, menu);
    }
}
