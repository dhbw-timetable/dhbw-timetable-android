package dhbw.timetable.navfragments.notifications.alarm;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.widget.TimePicker;

import java.io.IOException;

import dhbw.timetable.R;
import dhbw.timetable.dialogs.ListDialog;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
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

        final TextView firstShiftView = (TextView) view.findViewById(R.id.ShiftTextField);
        final TextView firstShiftValueView = (TextView) view.findViewById(R.id.FirstShiftValue);

        aOFESwitch.setChecked(sharedPref.getBoolean("alarmOnFirstEvent", false));
        aOFESwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("alarmOnFirstEvent", isChecked);
                editor.apply();

                AlarmSupervisor.getInstance().rescheduleAllAlarms(
                        AlarmFragment.this.getActivity().getApplicationContext());
                toneView.setEnabled(isChecked);
                toneValueView.setEnabled(isChecked);

                firstShiftView.setEnabled(isChecked);
                firstShiftValueView.setEnabled(isChecked);
            }
        });

        View.OnClickListener onToneClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer mMediaPlayer = new MediaPlayer();
                final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                ListDialog.newInstance("Select a tone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0) {
                            ListView lw = ((AlertDialog)dialog).getListView();
                            String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());

                            // TODO handle checked Item

                            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();

                            try {
                                mMediaPlayer.setDataSource(AlarmFragment.this.getActivity(), sound);
                                final AudioManager audioManager = (AudioManager) AlarmFragment.this
                                        .getActivity().getSystemService(Context.AUDIO_SERVICE);
                                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                                    mMediaPlayer.prepare();
                                    mMediaPlayer.start();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("alarmToneIndex", which);
                            editor.putString("alarmTone", checkedItem);
                            editor.apply();
                            toneValueView.setText(checkedItem);
                        } else {
                            mMediaPlayer.stop();
                        }
                    }
                }, sharedPref.getInt("alarmToneIndex", 0), "Default")
                        .show(getActivity().getFragmentManager(), "alarm_tone");
            }
        };

        toneView.setEnabled(aOFESwitch.isChecked());
        toneView.setOnClickListener(onToneClick);

        toneValueView.setEnabled(aOFESwitch.isChecked());
        toneValueView.setText(sharedPref.getString("alarmTone", "Default"));
        toneValueView.setOnClickListener(onToneClick);

        View.OnClickListener onFirstShiftViewClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmFragment.this.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("alarmFirstShiftHour", hourOfDay);
                        editor.putInt("alarmFirstShiftMinute", minute);

                        String time;
                        if (hourOfDay == 0 && minute == 0) {
                            time = "None";
                        } else {
                            time = hourOfDay + "h " + minute + "m";
                        }
                        editor.putString("alarmFirstShift", time);

                        editor.apply();

                        firstShiftValueView.setText(time);

                        AlarmSupervisor.getInstance().rescheduleAllAlarms(AlarmFragment.this.getContext());
                    }
                },
                    sharedPref.getInt("alarmFirstShiftHour", 0),
                    sharedPref.getInt("alarmFirstShiftMinute", 0),
                    true
                );
                timePickerDialog.show();
            }
        };

        firstShiftView.setEnabled(aOFESwitch.isChecked());
        firstShiftView.setOnClickListener(onFirstShiftViewClick);

        firstShiftValueView.setEnabled(aOFESwitch.isChecked());
        firstShiftValueView.setText(sharedPref.getString("alarmFirstShift", "None"));
        firstShiftValueView.setOnClickListener(onFirstShiftViewClick);

        return view;
    }
}