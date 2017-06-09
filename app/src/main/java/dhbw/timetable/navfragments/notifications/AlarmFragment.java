package dhbw.timetable.navfragments.notifications;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Calendar;

import dhbw.timetable.R;
import dhbw.timetable.dialogs.ListDialog;

public class AlarmFragment extends Fragment {

    static PendingIntent pendingIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_alarm, container, false);

        final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        final Switch aOFESwitch = (Switch) view.findViewById(R.id.AlarmOnFirstEventSwitch);

        final TextView toneView = (TextView) view.findViewById(R.id.AlarmTone);
        final TextView toneValueView = (TextView) view.findViewById(R.id.AlarmToneValue);

        final TextView shiftView = (TextView) view.findViewById(R.id.ShiftTextField);
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
                shiftView.setEnabled(isChecked);
                shiftSwitch.setEnabled(isChecked);

                firstShiftView.setEnabled(isChecked && shiftSwitch.isChecked());
                firstShiftValueView.setEnabled(isChecked && shiftSwitch.isChecked());
                secondShiftView.setEnabled(isChecked && shiftSwitch.isChecked());
                secondShiftValueView.setEnabled(isChecked && shiftSwitch.isChecked());

                if(isChecked) {
                    activateAlarm(getActivity());
                } else {
                    deactivateAlarm(getActivity());
                }
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

        shiftView.setEnabled(aOFESwitch.isChecked());

        shiftSwitch.setEnabled(aOFESwitch.isChecked());
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
                }, sharedPref.getInt("alarmFirstShiftIndex", 0),
                        "15min", "30min", "45min", "1h", "1,5h", "2h")
                        .show(getActivity().getFragmentManager(), "alarm_first_shift");
            }
        };

        firstShiftView.setEnabled(shiftSwitch.isChecked() && shiftSwitch.isEnabled());
        firstShiftView.setOnClickListener(onFirstShiftViewClick);

        firstShiftValueView.setEnabled(shiftSwitch.isChecked() && shiftSwitch.isEnabled());
        String fShift = sharedPref.getString("alarmFirstShift", "Immediately");
        firstShiftValueView.setText(fShift.equals("Immediately") || fShift.equals("None") ? fShift : fShift + " before" );
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
                        }, sharedPref.getInt("alarmSecondShiftIndex", 1), "None",
                        "15min", "30min", "45min", "1h", "1,5h", "2h")
                        .show(getActivity().getFragmentManager(), "alarm_second_shift");
            }
        };

        secondShiftView.setEnabled(shiftSwitch.isChecked() && shiftSwitch.isEnabled());
        secondShiftView.setOnClickListener(onSecondShiftViewClick);

        secondShiftValueView.setEnabled(shiftSwitch.isChecked() && shiftSwitch.isEnabled());
        String sShift = sharedPref.getString("alarmSecondShift", "None");
        secondShiftValueView.setText(sShift.equals("Immediately") || sShift.equals("None") ? sShift : sShift + " before" );
        secondShiftValueView.setOnClickListener(onSecondShiftViewClick);

        return view;
    }

    public static void activateAlarm(Context context) {
        Log.i("ALARM", "Initializing alarm...");
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        /*manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        60 * 1000, pendingIntent);*/

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 34);

        // setRepeating() lets you specify a precise custom interval--in this case,
        // 20 minutes.
        manager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                1000 * 60 * 5,
                pendingIntent);

        /* manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                8000,
                pendingIntent); */
        WakeLocker.acquire(context);
        Log.i("ALARM", "Alarm initialized");
    }

    public static void deactivateAlarm(Context context) {
        Log.i("ALARM", "Canceling...");
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Log.i("ALARM", "Alarm canceled");
        WakeLocker.release();
    }
}