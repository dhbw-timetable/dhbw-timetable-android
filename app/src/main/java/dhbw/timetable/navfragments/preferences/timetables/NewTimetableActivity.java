package dhbw.timetable.navfragments.preferences.timetables;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import dhbw.timetable.R;

public class NewTimetableActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_timetable);
        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /** Enable the back button */
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (id == R.id.action_apply_timetable) {
            String name =  ((TextView) findViewById(R.id.new_timetable_name)).getText().toString();
            String url = ((TextView) findViewById(R.id.new_timetable_url)).getText().toString();

            // If not empty
            if(name.length() > 0 && url.length() > 0) {
                SharedPreferences sharedPref = this.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                // TODO Check if timetable with same name already exists before overwriting
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("t#" + name, url);
                editor.apply();
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_timetable, menu);
        return true;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Disable shadow
            actionBar.setElevation(0);
        }
    }
}
