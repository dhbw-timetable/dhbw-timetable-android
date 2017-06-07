package dhbw.timetable.navfragments.preferences;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import dhbw.timetable.R;
import dhbw.timetable.dialogs.InfoDialog;
import dhbw.timetable.dialogs.YNDialog;
import dhbw.timetable.navfragments.preferences.timetables.ManageTimetablesActivity;


public class PreferencesActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || PrefsFragment.class.getName().equals(fragmentName);
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            final SwitchPreference resetSwitch = (SwitchPreference) findPreference("reset_switch");
            final Preference resetButton = findPreference("reset_button");
            final Preference tutorialButton = findPreference("tutorial_button");
            final Preference infoButton = findPreference("info_button");
            final Preference timetablesButton = findPreference("manage_timetables_button");
            final EditTextPreference syncRangeFuture = (EditTextPreference) findPreference("sync_range_future");
            final EditTextPreference syncRangePast = (EditTextPreference) findPreference("sync_range_past");

            timetablesButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), ManageTimetablesActivity.class);
                    startActivity(i);
                    getActivity().overridePendingTransition(0, 0);
                    return true;
                }
            });

            resetButton.setEnabled(resetSwitch.isChecked());
            resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    YNDialog dialog = YNDialog.newInstance("Are you sure?", "Yes", "Ehm...",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Delete all application data
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Cancel
                                }
                            }
                    );

                    dialog.show(getFragmentManager(), "reset");
                    return true;
                }
            });
            tutorialButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO Start a tutorial
                    InfoDialog.newInstance("Tutorial", "Currently there is no tutorial available. Coming soon.").show(getFragmentManager(), "tutorial");
                    return true;
                }
            });
            infoButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    InfoDialog.newInstance("About DHBW Timetable", "This app is a project from students of the DHBW Stuttgart.\n\nIt's deployed with\n\nNO WARRANTY\n\nfor correctness or availability.\n\nHendrik Ulbrich (C) 2017").show(getFragmentManager(), "info");
                    return true;
                }
            });
            resetSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    resetButton.setEnabled(resetSwitch.isChecked());
                    return true;
                }
            });

            syncRangeFuture.setSummary(Integer.parseInt(syncRangeFuture.getText()) + " weeks");
            syncRangeFuture.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    syncRangeFuture.setSummary(newValue + " weeks");
                    onSyncRangeChange();
                    return true;
                }
            });

            syncRangePast.setSummary(Integer.parseInt(syncRangePast.getText()) + " weeks");
            syncRangePast.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    syncRangePast.setSummary(newValue + " weeks");
                    onSyncRangeChange();
                    return true;
                }
            });

            setHasOptionsMenu(true);
        }

        private void onSyncRangeChange() {
            Application application = this.getActivity().getApplication();
            if(application.deleteFile(application.getResources().getString(R.string.TIMETABLES_FILE))) {
                Log.i("FILE", "Successfully deleted timetables file.");
            } else {
                Log.w("FILE", "Unable to delete timetables file! Do you have one?");
            }
        }

        /** Enable the back button */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
