package dhbw.timetable.navfragments.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dhbw.timetable.R;
import dhbw.timetable.dialogs.ListDialog;

public class AlarmFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_alarm, container, false);

        final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        final Switch aOFESwitch = (Switch) view.findViewById(R.id.AlarmOnFirstEventSwitch);

        final TextView toneView = (TextView) view.findViewById(R.id.AlarmTone);
        final TextView toneValueView = (TextView) view.findViewById(R.id.AlarmToneValue);

        final Switch shiftSwitch = (Switch) view.findViewById(R.id.ShiftSwitch);
        final TextView firstShiftView = (TextView) view.findViewById(R.id.FirstShiftTextField);
        final TextView firstShiftValueView = (TextView) view.findViewById(R.id.FirstShiftValue);

        final TextView secondShiftView = (TextView) view.findViewById(R.id.SecondShiftTextField);
        final TextView secondShiftValueView = (TextView) view.findViewById(R.id.SecondShiftValue);

        aOFESwitch.setChecked(sharedPref.getBoolean("alarmOnFirstEvent", false));
        aOFESwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("alarmOnFirstEvent", isChecked);
                editor.apply();
                toneView.setEnabled(isChecked);
                toneValueView.setEnabled(isChecked);
            }
        });

        View.OnClickListener onToneClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a tone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog)dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            // TODO Play the tone
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("alarmToneIndex", which);
                            editor.putString("alarmTone", checkedItem);
                            editor.apply();
                            toneValueView.setText(checkedItem);
                        }
                    }
                }, sharedPref.getInt("alarmToneIndex", 0), "Lalala", "Lululu", "Bububu")
                        .show(getActivity().getFragmentManager(), "alarm_tone");
            }
        };

        toneView.setEnabled(aOFESwitch.isChecked());
        toneView.setOnClickListener(onToneClick);

        toneValueView.setEnabled(aOFESwitch.isChecked());
        toneValueView.setText(sharedPref.getString("alarmTone", "Lalala"));
        toneValueView.setOnClickListener(onToneClick);

        shiftSwitch.setChecked(sharedPref.getBoolean("shift", false));
        shiftSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("shift", isChecked);
                firstShiftView.setEnabled(isChecked);
                firstShiftValueView.setEnabled(isChecked);
                secondShiftView.setEnabled(isChecked);
                secondShiftValueView.setEnabled(isChecked);
                editor.apply();
            }
        });

        View.OnClickListener onFirstShiftViewClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a shift", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog)dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("alarmFirstShiftIndex", which);
                            editor.putString("alarmFirstShift", checkedItem);
                            editor.apply();
                            if(which > 0) {
                                checkedItem += " before";
                            }
                            firstShiftValueView.setText(checkedItem);
                        }
                    }
                }, sharedPref.getInt("alarmFirstShiftIndex", 0),"Immediately",
                        "5min", "10min", "15min", "30min", "45min", "1h")
                        .show(getActivity().getFragmentManager(), "alarm_first_shift");
            }
        };

        firstShiftView.setEnabled(shiftSwitch.isChecked());
        firstShiftView.setOnClickListener(onFirstShiftViewClick);

        firstShiftValueView.setEnabled(shiftSwitch.isChecked());
        String fShift = sharedPref.getString("alarmFirstShift", "Immediately");
        firstShiftValueView.setText(fShift.equals("Immediately") ? fShift : fShift + " before" );
        firstShiftValueView.setOnClickListener(onFirstShiftViewClick);

        View.OnClickListener onSecondShiftViewClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.newInstance("Select a shift", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which >= 0) {
                                    ListView lw = ((AlertDialog)dialog).getListView();
                                    String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("alarmSecondShiftIndex", which);
                                    editor.putString("alarmSecondShift", checkedItem);
                                    editor.apply();
                                    if(which > 0) {
                                        checkedItem += " before";
                                    }
                                    secondShiftValueView.setText(checkedItem);
                                }
                            }
                        }, sharedPref.getInt("alarmSecondShiftIndex", 1),"Immediately",
                        "5min", "10min", "15min", "30min", "45min", "1h")
                        .show(getActivity().getFragmentManager(), "alarm_second_shift");
            }
        };

        secondShiftView.setEnabled(shiftSwitch.isChecked());
        secondShiftView.setOnClickListener(onSecondShiftViewClick);

        secondShiftValueView.setEnabled(shiftSwitch.isChecked());
        String sShift = sharedPref.getString("alarmSecondShift", "5min");
        secondShiftValueView.setText(sShift.equals("Immediately") ? sShift : sShift + " before" );
        secondShiftValueView.setOnClickListener(onSecondShiftViewClick);

        return view;
    }
}