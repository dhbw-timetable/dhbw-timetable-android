package dhbw.timetable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import dhbw.timetable.data.ErrorCallback;
import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.dialogs.ErrorDialog;

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
        }, new ErrorCallback() {
            @Override
            public void onError(String string) {
                ErrorDialog.newInstance("Error", "Can't load timetable. Is it corrupt?", string).show(LoadingActivity.this.getFragmentManager(), "HARDRESDLERR");
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
