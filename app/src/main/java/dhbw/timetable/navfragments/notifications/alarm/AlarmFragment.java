package dhbw.timetable.navfragments.notifications.alarm;

import android.app.AlertDialog;
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
                            firstShiftValueView.setText(checkedItem.equals("Immediately") ? checkedItem : checkedItem.concat(" before"));
                        } else {
                            // Update because shift has changed
                            AlarmSupervisor.getInstance().rescheduleAllAlarms(
                                    AlarmFragment.this.getActivity().getApplicationContext());
                        }
                    }
                }, sharedPref.getInt("alarmFirstShiftIndex", 0),
                        "Immediately", "15min", "30min", "45min", "1h", "1,5h", "2h")
                        .show(getActivity().getFragmentManager(), "alarm_first_shift");
            }
        };

        firstShiftView.setEnabled(aOFESwitch.isChecked());
        firstShiftView.setOnClickListener(onFirstShiftViewClick);

        firstShiftValueView.setEnabled(aOFESwitch.isChecked());
        String fShift = sharedPref.getString("alarmFirstShift", "15min");
        firstShiftValueView.setText(fShift.concat(" before"));
        firstShiftValueView.setOnClickListener(onFirstShiftViewClick);

        return view;
    }
}