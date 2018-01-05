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
public class CourseDetailsActivity extends AppCompatActivity {

    private String startTime, endTime, title, info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        this.startTime = getIntent().getStringExtra("startTime");
        this.endTime = getIntent().getStringExtra("endTime");
        this.title = getIntent().getStringExtra("title");
        this.info = getIntent().getStringExtra("info");

        TextView timeView = (TextView) findViewById(R.id.day_details_time);
        timeView.setText(String.format("%s - %s", startTime, endTime));

        TextView titleView = (TextView) findViewById(R.id.day_details_course);
        titleView.setText(title);

        TextView infoView = (TextView) findViewById(R.id.day_details_info);
        infoView.setText(info);

        Log.d("DETAILS", "Creating day details activity with \n" + startTime + "-" + endTime + "; " + title + "; " + info);
        setupActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("Details");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /* Enable the back button */
        if (id == android.R.id.home) {
            finish();
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
