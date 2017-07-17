package dhbw.timetable.navfragments.notifications;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dhbw.timetable.R;
import dhbw.timetable.navfragments.notifications.alarm.AlarmFragment;
import dhbw.timetable.navfragments.notifications.changes.ChangesFragment;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class NotificationsFragment extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter = null;
    private ViewPager mViewPager = null;
    private TabLayout mTabLayout = null;

    // Mandatory Constructor
    public NotificationsFragment() {}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // getActivity().setTitle("Notifications");
        TextView actTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        actTitle.setText("Notifications");
        actTitle.setOnClickListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_notifications,container, false);

        AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.appbar);

        if(appBarLayout.getChildCount() == 1) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

            mViewPager = (ViewPager) rootView.findViewById(R.id.tab_container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            mTabLayout = new TabLayout(getActivity());
            mTabLayout.setTabTextColors(
                    getResources().getColor(R.color.disabledGrey),
                    getResources().getColor(R.color.white)
            );
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            mTabLayout.setupWithViewPager(mViewPager);

            appBarLayout.addView(mTabLayout);
        }

        return rootView;
    }





    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return new AlarmFragment();
                case 1:
                    return new ChangesFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ALARM";
                case 1:
                    return "CHANGES";
            }
            return null;
        }
    }
}
