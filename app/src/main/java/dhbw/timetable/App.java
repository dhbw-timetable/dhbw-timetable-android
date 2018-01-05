package dhbw.timetable;

import android.app.Application;
import android.util.Log;

/**
 * Created by Hendrik Ulbrich (c) 2017
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("APP", "Application terminated.");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i("APP", "Application terminated.");
    }

}
