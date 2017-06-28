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

import dhbw.timetable.navfragments.notifications.NotificationsFragment;
import dhbw.timetable.navfragments.notifications.alarm.AlarmSupervisor;
import dhbw.timetable.navfragments.preferences.PreferencesActivity;
import dhbw.timetable.navfragments.today.TodayFragment;
import dhbw.timetable.navfragments.week.WeekFragment;
import dhbw.timetable.services.TimetableSyncService;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment currFragment;
    private Intent mServiceIntent;
    private TimetableSyncService mService;

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

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("INFO", "onStart MAIN ACT");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final int defaultSelectedItem = PreferenceManager.getDefaultSharedPreferences(this).
                getString("standardView", "0").equals("0") ?
                R.id.nav_today : R.id.nav_week;

        navigationView.setCheckedItem(defaultSelectedItem);
        displayFragment(defaultSelectedItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Reconfiguring background sync...");
        stopSyncService();
        startSyncService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
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
        } else {
            Log.w("ACT-RES", "Received unknown activity result{" + resultCode + "} from " + requestCode);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmSupervisor.getInstance().initialize(this.getApplicationContext());
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
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAIN", "onDestroy!");
        super.onDestroy();
    }

    void applyGlobalContent() {
        if(currFragment instanceof WeekFragment) {
            ((WeekFragment)currFragment).applyGlobalContent(false);
        } else if(currFragment instanceof TodayFragment) {
            TodayFragment frag = ((TodayFragment)currFragment);
            frag.applyGlobalContent(frag.getView());
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
