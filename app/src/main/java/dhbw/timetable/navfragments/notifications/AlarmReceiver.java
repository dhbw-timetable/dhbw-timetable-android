package dhbw.timetable.navfragments.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ALARM", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();

            // For our recurring task, we'll just display a message
            Toast.makeText(context, "Alaaaaaaaaarm!!!!", Toast.LENGTH_LONG).show();
        }
}
