package dhbw.timetable;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;

import dhbw.timetable.navfragments.notifications.NotificationsFragment;
import dhbw.timetable.navfragments.preferences.PreferencesActivity;
import dhbw.timetable.navfragments.today.TodayFragment;
import dhbw.timetable.navfragments.week.WeekFragment;
import dhbw.timetable.views.SideTimesView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment currFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_today);
        displayFragment(R.id.nav_today);

        getSupportActionBar().setElevation(0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean displayFragment(int id) {
        boolean changeNeeded = true;
        currFragment = null;
        switch(id) {
            case R.id.nav_week:
                currFragment = new WeekFragment();
                break;
            case R.id.nav_today:
                currFragment = new TodayFragment();
                break;
            case R.id.nav_notifications:
                currFragment = new NotificationsFragment();
                break;
            case R.id.nav_settings:
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                changeNeeded = false;
        }

        if(currFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, currFragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return changeNeeded;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return displayFragment(item.getItemId());
    }
}
