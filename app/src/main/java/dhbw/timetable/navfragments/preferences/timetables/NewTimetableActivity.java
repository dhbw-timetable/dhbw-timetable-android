package dhbw.timetable.navfragments.preferences.timetables;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dhbw.timetable.R;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class NewTimetableActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_timetable);
        setupActionBar();

        final TextView urlView = (TextView) findViewById(R.id.new_timetable_url);

        urlView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().startsWith("https://rapla.dhbw-stuttgart.de/rapla?key=")) {
                    String trimmed = s.toString()
                            .substring("https://rapla.dhbw-stuttgart.de/rapla?key=".length());
                    int end = trimmed.indexOf("&");
                    if(end != -1) {
                        trimmed = trimmed.substring(0, end);
                    }
                    urlView.setText(trimmed);
                    Toast.makeText(NewTimetableActivity.this, "Some magic happened!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /** Enable the back button */
        if (id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
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

                Intent returnIntent = new Intent();
                returnIntent.putExtra("tt", url);
                setResult(Activity.RESULT_OK, returnIntent);
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
