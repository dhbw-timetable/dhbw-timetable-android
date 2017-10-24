package dhbw.timetable.navfragments.preferences.timetables;

import android.content.Context;
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

import dhbw.timetable.R;
import dhbw.timetable.dialogs.InfoDialog;
import dhbw.timetable.rapla.network.NetworkUtilities;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class EditTimetableActivity extends AppCompatActivity {
    private String nameBefore;
    private TextView nameView;
    private TextView urlView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_timetable);
        setupActionBar();

        nameView = (TextView) findViewById(R.id.edit_timetable_name);
        urlView = (TextView) findViewById(R.id.edit_timetable_url);

        // Load this timetable
        nameView.setText(getIntent().getExtras().getString("name"));
        urlView.setText(getIntent().getExtras().getString("url"));

        urlView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().startsWith("https://rapla.dhbw-stuttgart.de/rapla?key=")) {
                    String trimmed = s.toString()
                            .substring("https://rapla.dhbw-stuttgart.de/rapla?key=".length());
                    int end = trimmed.indexOf("&");
                    if (end != -1) {
                        trimmed = trimmed.substring(0, end);
                    }
                    urlView.setText(trimmed);
                    Toast.makeText(EditTimetableActivity.this, "Some magic happened!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        nameBefore = nameView.getText().toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /** Enable the back button */
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (id == R.id.action_edit_apply_timetable) {
            String name = nameView.getText().toString();
            String url = urlView.getText().toString();
            // If not empty
            if (name.length() > 0 && NetworkUtilities.URLIsValid(url)) {
                SharedPreferences sharedPref = this.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                // Check if name changed, if yes delete old pref
                if (!nameBefore.equals(name)) {
                    editor.remove("t#" + nameBefore);
                }
                editor.putString("t#" + name, url);
                editor.apply();
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else {
                InfoDialog.newInstance("Invalid input",
                        "Please ensure you have entered a valid name and a reachable link.")
                        .show(getFragmentManager(), "INPUT");
            }
        } else if (id == R.id.action_edit_delete_timetable) {
            SharedPreferences sharedPref = this.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("t#" + nameBefore);
            // TODO YN Dialog: Are you sure?
            editor.apply();
            finish();
            overridePendingTransition(0, 0);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_timetable, menu);
        return true;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Disable shadow
            // actionBar.setElevation(0);
        }
    }
}
