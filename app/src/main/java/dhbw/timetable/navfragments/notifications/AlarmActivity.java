package dhbw.timetable.navfragments.notifications;

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
import android.widget.Button;

import dhbw.timetable.R;

public class AlarmActivity extends AppCompatActivity {
    private Ringtone ringRing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void close(int result) {
        Log.i("ONBOARD", "Onboarding timetable loaded.");
        Intent returnIntent = new Intent();
        setResult(result, returnIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ALARM", "FIRING ALARM VIA ACTIVITY !!!");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringRing = RingtoneManager.getRingtone(this, notification);
        ringRing.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ringRing.stop();
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
        Log.i("ALARM", "Destroyed");
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
