package dhbw.timetable.navfragments.today;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dhbw.timetable.MainActivity;
import dhbw.timetable.R;
import dhbw.timetable.data.AgendaAppointment;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.DateHelper;
import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.views.TodaySummaryRect;

public class TodayFragment extends Fragment {
    private RecyclerView recyclerView;
    private AgendaAppointmentAdapter aAdapter;
    private List<AgendaAppointment> agendaAppointmentsList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Today");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_today, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclingAgenda);

        aAdapter = new AgendaAppointmentAdapter(agendaAppointmentsList);
        RecyclerView.LayoutManager aLayoutManager = new LinearLayoutManager(view.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(aLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(aAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        TimetableManager.getInstance().loadOfflineGlobals(getActivity().getApplication(), new Runnable() {
            @Override
            public void run() {
                applyGlobalContent(getView());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_today, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_today) {
            if(!TimetableManager.getInstance().isBusy()) {
                TimetableManager.getInstance().updateGlobals(getActivity().getApplication(), new Runnable() {
                    @Override
                    public void run() {
                        applyGlobalContent(getView());
                        Toast.makeText(TodayFragment.this.getActivity().getApplication(), "Updated!", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            } else {
                Log.w("ASYNC", "Tried to sync while manager was busy");
            }
        }

        return false;
    }

    private void applyGlobalContent(View view) {
        applyAgenda(view);
        applyTomorrow(view);
        applyWeekSummary(view);
    }

    private void applyAgenda(View view) {
        agendaAppointmentsList.clear();
        String currDate = DateHelper.GetCurrentDate();
        for(Appointment a : TimetableManager.getInstance().getGlobals()) {
            if(a.getDate().equals(currDate)) {
                agendaAppointmentsList.add(new AgendaAppointment(a.getStartTime(), a.getEndTime(), a.getCourse(), false));
            }
        }
        int size = agendaAppointmentsList.size();
        TextView placeholder = (TextView) view.findViewById(R.id.agendaEmptyPlaceholder);
        if(size > 0) {
            placeholder.setText("");
            Map<Integer, AgendaAppointment> breaks = new HashMap<>();
            for (int i = 0; i < size; i++) {
                if(i != (agendaAppointmentsList.size() - 1)) {
                    AgendaAppointment aa = agendaAppointmentsList.get(i);
                    AgendaAppointment following = agendaAppointmentsList.get(i+1);
                    // If break is present
                    if (!aa.getEndTime().equals(following.getStartTime())) {
                        breaks.put(i, new AgendaAppointment(aa.getEndTime(), "DONOTUSE", "PAUSE", true));
                    }
                }
            }

            for (int i : breaks.keySet()) agendaAppointmentsList.add(i + 1, breaks.get(i));

            String endTime = agendaAppointmentsList.get(agendaAppointmentsList.size() - 1).getEndTime();
            agendaAppointmentsList.add(new AgendaAppointment(endTime, "", "FEIERABEND", true));
            aAdapter.notifyDataSetChanged();
        } else {
            placeholder.setText("Keine Vorlesungen");
        }
    }

    private void applyTomorrow(View view) {
        GregorianCalendar tomorrow = (GregorianCalendar) Calendar.getInstance();
        DateHelper.AddDays(tomorrow, 1);
        ArrayList<Appointment> tomorrowAppointments = DateHelper.GetAppointmentsOfDay(tomorrow, TimetableManager.getInstance().getGlobals());
        TextView beginView = (TextView) view.findViewById(R.id.beginTime);
        TextView tomorrowSummaryView = (TextView) view.findViewById(R.id.tomorrowSummary);
        if(tomorrowAppointments.size() > 0) {
            beginView.setText("Begin: " + tomorrowAppointments.get(0).getStartTime());
            StringBuilder sb = new StringBuilder();
            for(Appointment a : tomorrowAppointments) sb.append(a.getCourse() + ",\n");
            // Delete last comma
            sb.deleteCharAt(sb.length() - 2);
            tomorrowSummaryView.setText(sb.toString());
        } else {
            beginView.setText("Keine Vorlesungen");
            tomorrowSummaryView.setText("");
        }
    }

    // Apply
    private void applyWeekSummary(final View view) {
        GregorianCalendar day = (GregorianCalendar) Calendar.getInstance();
        DateHelper.Normalize(day);

        ArrayList<Appointment> weekAppointments = DateHelper.GetWeekAppointments(day,
                TimetableManager.getInstance().getGlobals());

        String startTime, endTime;
        int startID = -1, endID = -1;
        Appointment startA, endA;
        ArrayList<ArrayList<Appointment>> wData = new ArrayList<>();
        for(int d = 0; d < 5; d++) {
            startA = endA = null;
            ArrayList<Appointment> dayAppointments = DateHelper.GetAppointmentsOfDay(day, weekAppointments);
            wData.add(dayAppointments);
            if (dayAppointments.size() > 0){
                startA = dayAppointments.get(0);
                endA = dayAppointments.get(dayAppointments.size() - 1);
            }
            switch (d) {
                case 0:
                    startID = R.id.moStart;
                    endID = R.id.moEnd;
                    break;
                case 1:
                    startID = R.id.diStart;
                    endID = R.id.diEnd;
                    break;
                case 2:
                    startID = R.id.miStart;
                    endID = R.id.miEnd;
                    break;
                case 3:
                    startID = R.id.doStart;
                    endID = R.id.doEnd;
                    break;
                case 4:
                    startID = R.id.frStart;
                    endID = R.id.frEnd;
                    break;
            }
            if (startA != null) {
                startTime = startA.getStartTime();
                endTime = endA.getEndTime();
            } else {
                startTime = endTime = "00:00";
            }
            ((TextView) view.findViewById(startID)).setText(startTime);
            ((TextView) view.findViewById(endID)).setText(endTime);

            DateHelper.AddDays(day, 1);
        }
        GridLayout gl = (GridLayout) view.findViewById(R.id.weekGrid);
        gl.removeAllViews();
        Pair<Integer, Integer> borders = DateHelper.GetBorders(weekAppointments);
        TodaySummaryRect ra = new TodaySummaryRect(borders.first, borders.second, gl, wData);
        ra.setBackgroundColor(Color.WHITE);
        ra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).displayFragment(R.id.nav_week);
                NavigationView navigationView = (NavigationView) TodayFragment.this.getActivity().findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_week);
            }
        });
        gl.addView(ra);
    }
}
