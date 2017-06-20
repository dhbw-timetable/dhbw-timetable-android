package dhbw.timetable;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

public class ActivityHelper {

    public static Activity getActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field breakField = activityRecordClass.getDeclaredField("paused");
                breakField.setAccessible(true);
                if (!breakField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch(Exception e) {
            Log.e("ERROR", "Cant get current activity :( " + e.getClass());
        }
        return null;
    }
}
