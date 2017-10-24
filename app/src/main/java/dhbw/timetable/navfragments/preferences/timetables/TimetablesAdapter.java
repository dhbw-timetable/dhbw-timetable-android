package dhbw.timetable.navfragments.preferences.timetables;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dhbw.timetable.R;
import dhbw.timetable.data.Timetable;

class TimetablesAdapter extends RecyclerView.Adapter<TimetablesAdapter.MyViewHolder> {

    private List<Timetable> timetablesList;
    private View.OnClickListener onClick;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, key;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            key = (TextView) view.findViewById(R.id.key);
        }
    }

    TimetablesAdapter(List<Timetable> timetables, View.OnClickListener onItemClick) {
        this.timetablesList = timetables;
        this.onClick = onItemClick;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timetable_list_row, parent, false);

        itemView.setOnClickListener(onClick);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Timetable timetable = timetablesList.get(position);
        holder.name.setText(timetable.getName());
        holder.key.setText(timetable.getKey());
    }

    @Override
    public int getItemCount() {
        return timetablesList.size();
    }
}