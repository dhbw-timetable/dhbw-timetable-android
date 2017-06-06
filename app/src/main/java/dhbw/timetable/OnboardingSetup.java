package dhbw.timetable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.navfragments.preferences.timetables.NewTimetableActivity;

public class OnboardingSetup extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button aBtn = (Button) findViewById(R.id.onboard_addTimetable);
        //Button tBtn = (Button) findViewById(R.id.onboard_tutorial);

        aBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OnboardingSetup.this, NewTimetableActivity.class);
                startActivityForResult(i, 2);
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("tt");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("onboardingSuccess", !result.isEmpty());
                setResult(Activity.RESULT_OK, returnIntent);

                TimetableManager.getInstance().updateGlobals(this.getApplication(), new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ONBOARD", "Onboarding timetable loaded.");
                        finish();
                    }
                });
                setContentView(R.layout.loading);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("ONBOARD", "Destroyed");
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
       // do nothing
    }
}
