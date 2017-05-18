package dhbw.timetable.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TimetableRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TimetableRestarterBroadcastReceiver.class.getSimpleName(),
                "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, TimetableSyncService.class));
    }
}
