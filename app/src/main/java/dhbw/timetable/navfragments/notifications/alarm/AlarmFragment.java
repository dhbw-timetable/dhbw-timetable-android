package dhbw.timetable.navfragments.notifications.alarm;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        final Switch aOFESwitch = view.findViewById(R.id.AlarmOnFirstEventSwitch);

        final TextView toneView = view.findViewById(R.id.AlarmTone);
        final TextView toneValueView = view.findViewById(R.id.AlarmToneValue);

        final TextView vibrationView = view.findViewById(R.id.VibrationTextField);
        final TextView vibrationValueView = view.findViewById(R.id.AlarmVibrationView);

        final TextView firstShiftView = view.findViewById(R.id.ShiftTextField);
        final TextView firstShiftValueView = view.findViewById(R.id.FirstShiftValue);

        aOFESwitch.setChecked(sharedPref.getBoolean("alarmOnFirstEvent", false));
        aOFESwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!permissionCheck()) {
                    aOFESwitch.setChecked(false);
                    return;
                }
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("alarmOnFirstEvent", isChecked);
            editor.apply();

            AlarmSupervisor.getInstance().rescheduleAllAlarms(
                    AlarmFragment.this.getActivity().getApplicationContext());
            toneView.setEnabled(isChecked);
            toneValueView.setEnabled(isChecked);

            vibrationView.setEnabled(isChecked);
            vibrationValueView.setEnabled(isChecked);

            firstShiftView.setEnabled(isChecked);
            firstShiftValueView.setEnabled(isChecked);
        });

        View.OnClickListener onToneClick = v -> {
            final MediaPlayer mMediaPlayer = new MediaPlayer();
            final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ListDialog.newInstance("Select a tone", (dialog, which) -> {
                if (which >= 0) {
                    if (!permissionCheck()) {
                        Log.w("ALARM", "Permissions invalid. Can not safely play sounds!");
                        return;
                    }
                    ListView lw = ((AlertDialog) dialog).getListView();
                    String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());

                    if (mMediaPlayer.isPlaying() || mMediaPlayer.isLooping()) {
                        mMediaPlayer.reset();
                    }

                    try {
                        mMediaPlayer.setDataSource(AlarmFragment.this.getActivity(), sound);
                        final AudioManager audioManager = (AudioManager) AlarmFragment.this.getActivity().getSystemService(Context.AUDIO_SERVICE);
                        if (audioManager != null) {
                            Log.d("ALARM", "Playing sound now...");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                                if (mNotificationManager != null) {
                                    mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                                } else {
                                    Log.w("ALARM", "Can't access system service NotificationManager");
                                }
                            } else {
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            }
                            Log.d("ALARM", "Set ringer mode to normal.");
                            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
                                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                                mMediaPlayer.prepare();
                                mMediaPlayer.start();
                            }
                        } else {
                            Log.w("ALARM" ,"Can't get system service AudioManager");
                        }
                    } catch (IOException | IllegalStateException e) {
                        e.printStackTrace();
                        mMediaPlayer.reset();
                    }

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("alarmToneIndex", which);
                    editor.putString("alarmTone", checkedItem);
                    editor.apply();
                    toneValueView.setText(checkedItem);
                } else {
                    mMediaPlayer.reset();
                }
            }, sharedPref.getInt("alarmToneIndex", 0), "Default")
                    .show(getActivity().getFragmentManager(), "alarm_tone");
        };

        toneView.setEnabled(aOFESwitch.isChecked());
        toneView.setOnClickListener(onToneClick);

        toneValueView.setEnabled(aOFESwitch.isChecked());
        toneValueView.setText(sharedPref.getString("alarmTone", "Default"));
        toneValueView.setOnClickListener(onToneClick);


        View.OnClickListener onVibrationClick = v -> ListDialog.newInstance("Select a pattern", (dialog, which) -> {
            // Get instance of Vibrator from current Context
            Vibrator v1 = (Vibrator) AlarmFragment.this.getActivity()
                    .getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (which >= 0) {
                ListView lw = ((AlertDialog) dialog).getListView();
                String checkedItem = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());

                // Start without a delay
                // Vibrate for 1000 milliseconds
                // Sleep for 100 milliseconds
                long[] pattern1 = {0, 1000, 100, 1000, 100, 1000, 100, 1000, 100};

                // Alternative pattern
                long[] pattern2 = {0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
                long[] chosenPattern = null;
                switch (which) {
                    case 1:
                        chosenPattern = pattern1;
                        break;
                    case 2:
                        chosenPattern = pattern2;
                        break;
                }

                if (chosenPattern != null && v1 != null) {
                    // The '-1' here means to repeat ONCE
                    v1.vibrate(chosenPattern, -1);
                }

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("alarmVibrationIndex", which);
                editor.putString("alarmVibration", checkedItem);
                editor.apply();
                vibrationValueView.setText(checkedItem);
            } else {
                if (v1 != null) {
                    v1.cancel();
                }
            }
        }, sharedPref.getInt("alarmVibrationIndex", 0), "None", "Default", "Alternative")
                .show(getActivity().getFragmentManager(), "alarm_vibration");

        vibrationView.setEnabled(aOFESwitch.isChecked());
        vibrationView.setOnClickListener(onVibrationClick);

        vibrationValueView.setEnabled(aOFESwitch.isChecked());
        vibrationValueView.setText(sharedPref.getString("alarmVibration", "None"));
        vibrationValueView.setOnClickListener(onVibrationClick);


        View.OnClickListener onFirstShiftViewClick = v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmFragment.this.getContext(), (view1, hourOfDay, minute) -> {
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

                AlarmSupervisor.getInstance().rescheduleAllAlarms(AlarmFragment.this.getActivity().getApplicationContext());
            },
                    sharedPref.getInt("alarmFirstShiftHour", 0),
                    sharedPref.getInt("alarmFirstShiftMinute", 0),
                    true
            );
            timePickerDialog.show();
        };

        firstShiftView.setEnabled(aOFESwitch.isChecked());
        firstShiftView.setOnClickListener(onFirstShiftViewClick);

        firstShiftValueView.setEnabled(aOFESwitch.isChecked());
        firstShiftValueView.setText(sharedPref.getString("alarmFirstShift", "None"));
        firstShiftValueView.setOnClickListener(onFirstShiftViewClick);

        return view;
    }

    private boolean permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("ALARM", "Checking permissions for alarm...");
            NotificationManager notificationManager = (NotificationManager)
                    getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                Log.d("ALARM", "Notifications permissions not set. Requesting...");
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
                return false;
            }
            if (!Settings.System.canWrite(getContext())) {
                Log.d("ALARM", "Write permissions not set. Requesting...");
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return false;
            }
            Log.d("ALARM", "All permissions granted.");
        } else {
            Log.d("ALARM", "Permissions check not needed!");
        }
        return true;
    }
}