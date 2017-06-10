package dhbw.timetable.navfragments.today;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import dhbw.timetable.R;
import dhbw.timetable.data.AgendaAppointment;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
class AgendaAppointmentAdapter extends RecyclerView.Adapter<AgendaAppointmentAdapter.MyViewHolder> {

    private List<AgendaAppointment> appointments;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time, course;

        public MyViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.courseTime);
            course = (TextView) view.findViewById(R.id.courseTitle);
        }
    }

    AgendaAppointmentAdapter(List<AgendaAppointment> appointments) {
        this.appointments = appointments;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AgendaAppointment a = appointments.get(position);
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