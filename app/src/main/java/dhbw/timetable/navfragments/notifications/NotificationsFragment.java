package dhbw.timetable.navfragments.notifications;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import dhbw.timetable.navfragments.notifications.AlarmFragment;
import dhbw.timetable.navfragments.notifications.ChangesFragment;
import dhbw.timetable.R;

public class NotificationsFragment extends Fragment implements TabHost.OnTabChangeListener {

    private FragmentTabHost mTabHost;
    private TabWidget tabWidget;
    private static String selected = "fragment_alarm";

    // Mandatory Constructor
    public NotificationsFragment() {}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Notifications");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_notifications,container, false);

        mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("fragment_alarm").setIndicator("Alarm"),
                AlarmFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("fragment_changes").setIndicator("Changes"),
                ChangesFragment.class, null);

        mTabHost.setCurrentTab(selected.equals("fragment_alarm") ? 0 : 1);

        tabWidget = mTabHost.getTabWidget();
        mTabHost.setOnTabChangedListener(this);

        applyTextColor();

        return rootView;
    }

    /**
      Set text color to white
     */
    private void applyTextColor() {
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            View tab = tabWidget.getChildAt(i);
            TextView tv = (TextView) tab.findViewById(android.R.id.title);
            int alpha;
            if (tab.isSelected()) {
                alpha = 255;
            } else {
                alpha = 191;
            }
            tv.setTextColor(Color.argb(alpha, 255, 255, 255));
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        selected = tabId;
        applyTextColor();
    }
}
