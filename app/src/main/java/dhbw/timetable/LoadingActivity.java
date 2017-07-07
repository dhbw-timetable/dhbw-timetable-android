package dhbw.timetable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import dhbw.timetable.data.TimetableManager;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        TimetableManager.getInstance().updateGlobals(getApplication(), new Runnable() {
            @Override
            public void run() {
                Log.i("FILE", "Hard reset timetable loaded.");
                LoadingActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}
