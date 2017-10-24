package dhbw.timetable.navfragments.notifications.alarm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import dhbw.timetable.R;
import dhbw.timetable.rapla.data.event.BackportAppointment;
import dhbw.timetable.rapla.data.time.TimelessDate;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class AlarmActivity extends AppCompatActivity {
    private boolean destroy = false; // false means snoozeAlarm
    private String title, time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ALARM-ACT", "Creating alarm activity.");
        setContentView(R.layout.alarm);

        Button snoozeBtn = (Button) findViewById(R.id.snoozeButton);
        Button stopBtn = (Button) findViewById(R.id.alarmStopButton);

        snoozeBtn.setOnClickListener(v -> {
            destroy = false;
            finish();
        });

        stopBtn.setOnClickListener(v -> {
            destroy = true;
            finish();
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        BackportAppointment appointment = AlarmSupervisor.getInstance().getCurrentAppointment(getApplication());
        if (appointment != null) {
            title = appointment.getTitle();
            time = appointment.getStartTime();

            TextView alarmTextInfo = (TextView) findViewById(R.id.alarmTextInfo);
            alarmTextInfo.setText(title + " at " + time);
        } else {
            Log.w("ALARM", "Tried launching alarm activity without appointment! :((");
            destroy = true;
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ALARM-ACT", "Starting alarm activity!");
        AlarmSupervisor.getInstance().playRingtone(this.getApplicationContext());
        AlarmSupervisor.getInstance().startVibrator(this.getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("ALARM-ACT", "Stopping alarm activity!");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmSupervisor.getInstance().stopRingtone(getApplicationContext());
        AlarmSupervisor.getInstance().stopVibrator(this.getApplicationContext());
        if (destroy) {
            AlarmSupervisor.getInstance().cancelAlarm(this.getApplicationContext(), new TimelessDate().hashCode());
        } else {
            AlarmSupervisor.getInstance().snoozeAlarm(this.getApplicationContext());
        }
        Log.i("ALARM", "Destroyed activity");
    }
}
