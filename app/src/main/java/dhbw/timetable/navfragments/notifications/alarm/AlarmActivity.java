package dhbw.timetable.navfragments.notifications.alarm;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import dhbw.timetable.R;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class AlarmActivity extends AppCompatActivity {
    private boolean dispose = false; // false means snooze

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ALARM-ACT", "Creating alarm activity.");
        setContentView(R.layout.alarm);

        Button snoozeBtn = (Button) findViewById(R.id.snoozeButton);
        Button stopBtn = (Button) findViewById(R.id.alarmStopButton);

        snoozeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispose = false;
                finish();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispose = true;
                finish();
            }
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ALARM-ACT", "Starting alarm activity!");
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AlarmSupervisor.getInstance().setRingtone(this, sound);
        AlarmSupervisor.getInstance().playRingtone();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ALARM-ACT", "Stopping alarm activity.");
        AlarmSupervisor.getInstance().stopRingtone();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ALARM-ACT", "onPause");
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
        if(dispose) {
            AlarmSupervisor.getInstance().dispose();
        } else {
            AlarmSupervisor.getInstance().snooze(this.getApplicationContext());
        }
        Log.i("ALARM", "Destroyed activity");
    }
}
