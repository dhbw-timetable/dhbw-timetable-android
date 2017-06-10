package dhbw.timetable.navfragments.notifications.alarm;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
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

public class AlarmActivity extends AppCompatActivity {
    private Ringtone ringRing;

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
                close(Activity.RESULT_OK);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close(Activity.RESULT_CANCELED);
            }
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void close(int result) {
        // Intent returnIntent = new Intent();
        // setResult(result, returnIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ALARM-ACT", "Starting alarm activity!");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringRing = RingtoneManager.getRingtone(this, notification);
        ringRing.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ALARM-ACT", "Stopping alarm activity.");
        ringRing.stop();
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
        AlarmFragment.pendingIntent.cancel();
        Log.i("ALARM", "Destroyed");
    }
}
