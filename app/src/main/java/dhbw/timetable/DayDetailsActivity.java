package dhbw.timetable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class DayDetailsActivity extends AppCompatActivity {

    private String day, agenda;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        this.day = getIntent().getStringExtra("day") + "\n";
        this.agenda = getIntent().getStringExtra("agenda");

        TextView dayView = (TextView) findViewById(R.id.details_header);
        dayView.setText(day);

        TextView agendaView = (TextView) findViewById(R.id.details_agenda);
        agendaView.setText(agenda);

        Log.d("DETAILS", "Creating details activity with\n" + day + "\n" + agenda);
        setupActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("Agenda");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /** Enable the back button */
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
