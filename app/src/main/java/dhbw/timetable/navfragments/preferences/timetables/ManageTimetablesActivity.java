package dhbw.timetable.navfragments.preferences.timetables;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import dhbw.timetable.R;
import dhbw.timetable.data.Timetable;
import dhbw.timetable.dialogs.InfoDialog;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class ManageTimetablesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TimetablesAdapter tAdapter;
    List<Timetable> timetableList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetables);
        setupActionBar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        tAdapter = new TimetablesAdapter(timetableList, v -> {
            Timetable item = timetableList.get(recyclerView.getChildLayoutPosition(v));
            Intent i = new Intent(ManageTimetablesActivity.this, EditTimetableActivity.class);
            i.putExtra("url", item.getURL());
            i.putExtra("name", item.getName());
            startActivity(i);
            overridePendingTransition(0, 0);
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tAdapter);
    }

    private void loadTimetableData() {
        timetableList.clear();
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        for(String key : sharedPref.getAll().keySet()) {
            // filter for timetables
            if(key.startsWith("t#")) {
                timetableList.add(new Timetable(key.substring(2), sharedPref.getString(key, "")));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadTimetableData();
        tAdapter.notifyDataSetChanged();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Enable the back button
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (id == R.id.action_add_timetable) {
            if(timetableList.size() > 0) {
                InfoDialog.newInstance(
                                "No multiple timetables", "You already have a timetable." +
                                " Delete or edit it to get different data.")
                        .show(getFragmentManager(), "noTimetableAdd");
                return false;
            }
            Intent i = new Intent(this, NewTimetableActivity.class);
            startActivity(i);
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetables, menu);
        return true;
    }
}
