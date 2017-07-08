package dhbw.timetable.navfragments.today;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedHashSet;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.CourseDetailsActivity;
import dhbw.timetable.DayDetailsActivity;
import dhbw.timetable.R;
import dhbw.timetable.data.AgendaAppointment;
import dhbw.timetable.data.Appointment;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
class AgendaAppointmentAdapter extends RecyclerView.Adapter<AgendaAppointmentAdapter.MyViewHolder> {

    private LinkedHashSet<AgendaAppointment> appointments;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView time, course;

        MyViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.courseTime);
            course = (TextView) view.findViewById(R.id.courseTitle);
        }
    }

    AgendaAppointmentAdapter(LinkedHashSet<AgendaAppointment> appointments) {
        this.appointments = appointments;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.agenda_list_row, parent, false);
        final RecyclerView mRecyclerView = (RecyclerView) parent;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View child) {
                int itemPos = mRecyclerView.getChildLayoutPosition(child);
                AgendaAppointment item = (AgendaAppointment) appointments.toArray()[itemPos];
                if (!item.isBreak()) {
                    Log.d("DEBUG", "Would fire details with: " + item);
                    Activity activity = ActivityHelper.getActivity();
                    if (activity != null) {
                        Intent detailsIntent = new Intent(activity, CourseDetailsActivity.class);
                        detailsIntent.putExtra("startTime", item.getStartTime());
                        detailsIntent.putExtra("endTime", item.getEndTime());
                        detailsIntent.putExtra("course", item.getCourse());
                        detailsIntent.putExtra("info", item.getInfo());
                        activity.startActivity(detailsIntent);
                    }
                }
            }
        });

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AgendaAppointment a = (AgendaAppointment) appointments.toArray()[position];
        // AgendaAppointment a = appointments.get(position);
        holder.time.setText(a.getStartTime());
        holder.course.setText(a.getCourse());

        holder.time.setTextAppearance(holder.time.getContext(),
                (position == 0 || position == (appointments.size()-1)) ?
                        R.style.AgendaTimeMain : R.style.AgendaTime);

        float scale = holder.course.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (10 * scale + 0.5f);
        holder.course.setBackgroundResource(a.isBreak() ?
                R.drawable.break_background : R.drawable.course_background);
        holder.course.setPadding(dpAsPixels, 0, dpAsPixels, 0);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }
}