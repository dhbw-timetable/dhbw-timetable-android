package dhbw.timetable;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.navfragments.notifications.NotificationsFragment;
import dhbw.timetable.navfragments.notifications.alarm.AlarmSupervisor;
import dhbw.timetable.navfragments.preferences.PreferencesActivity;
import dhbw.timetable.navfragments.today.TodayFragment;
import dhbw.timetable.navfragments.week.WeekFragment;
import dhbw.timetable.services.TimetableSyncService;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment currFragment;
    private Intent mServiceIntent;
    private TimetableSyncService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        AlarmSupervisor.getInstance().initialize();
        setContentView(R.layout.activity_main);

        // Onboarding check
        if (!getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).getBoolean("onboardingDone", false)) {
            Log.i("ONBOARD", "Haven't done onboarding. Starting...");
            Intent i = new Intent(this, OnboardingSetup.class);
            startActivityForResult(i, 1);
            overridePendingTransition(0, 0);
        }

        startSyncService();

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
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAIN", "onDestroy!");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            Log.i("ACT-RES", "Onboarding activity has a result");
            boolean on = data.getBooleanExtra("onboardingSuccess", false);
            if (on) {
                Log.i("ONBOARD", "Saving onboarding as done");
                SharedPreferences sharedPref = this.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("onboardingDone", true);
                editor.apply();

                applyGlobalContent();
            }
        } else if (requestCode == 2) {
            Log.i("ACT-RES", "Settings activity has a result");
            Log.i("SYNC", "Reconfiguring background sync...");
            stopSyncService();
            startSyncService();
        } else {
            Log.w("ACT-RES", "Received unknown activity result{" + resultCode + "} from " + requestCode);
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return displayFragment(item.getItemId());
    }

    void applyGlobalContent() {
        if(currFragment instanceof WeekFragment) {
            WeekFragment frag = ((WeekFragment) currFragment);
            frag.applyGlobalContent(false, false, frag.getView(), this);
        } else if(currFragment instanceof TodayFragment) {
            TodayFragment frag = ((TodayFragment) currFragment);
            frag.applyGlobalContent(frag.getView());
        }
    }

    public boolean displayFragment(int id) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        boolean changeNeeded = true;
        if (!TimetableManager.getInstance().isRunning()) {
            switch (id) {
                case R.id.nav_week:
                    currFragment = new WeekFragment();
                    break;
                case R.id.nav_today:
                    currFragment = new TodayFragment();
                    break;
                case R.id.nav_notifications:
                    if (currFragment instanceof NotificationsFragment) {
                        drawer.closeDrawer(GravityCompat.START);
                        return false;
                    }
                    currFragment = new NotificationsFragment();
                    break;
                case R.id.nav_settings:
                    Intent i = new Intent(this, PreferencesActivity.class);
                    startActivityForResult(i, 2);
                    overridePendingTransition(0, 0);
                    changeNeeded = false;
            }

            if (currFragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, currFragment);
                ft.commit();
            }
        } else {
            changeNeeded = false;
            Toast.makeText(this, "I'm currently busy, sorry!", Toast.LENGTH_SHORT).show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return changeNeeded;
    }

    private void startSyncService() {
        mService = new TimetableSyncService(this);
        mServiceIntent = new Intent(this, mService.getClass());
        String syncFreq = PreferenceManager.
                getDefaultSharedPreferences(this).getString("sync_frequency_list", "-1");
        if(!syncFreq.equals("-1")) {
            int msFreq = (int) (Double.parseDouble(syncFreq) * 360000);
            mServiceIntent.putExtra("freq", msFreq);
            if (!isMyServiceRunning(mService.getClass())) {
                startService(mServiceIntent);
            }
        }
    }

    private void stopSyncService() {
        stopService(mServiceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                //Log.i ("isMyServiceRunning?", true + "");
                return true;
            }
        }
        //Log.i ("isMyServiceRunning?", false + "");
        return false;
    }
}
