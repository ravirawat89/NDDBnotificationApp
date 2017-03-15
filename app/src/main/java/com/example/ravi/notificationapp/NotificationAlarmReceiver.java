package com.example.ravi.notificationapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

/**
 * Created by Ravi on 05-Dec-16.
 */

public class NotificationAlarmReceiver extends WakefulBroadcastReceiver
{
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // BEGIN_INCLUDE(alarm_onreceive)
        /*
         * If your receiver intent includes extras that need to be passed along to the
         * service, use setComponent() to indicate that the service should handle the
         * receiver's intent. For example:
         *
         * ComponentName comp = new ComponentName(context.getPackageName(),
         *      MyService.class.getName());
         *
         * // This intent passed in this call will include the wake lock extra as well as
         * // the receiver intent contents.
         * startWakefulService(context, (intent.setComponent(comp)));
         *
         * In this example, we simply create a new intent to deliver to the service.
         * This intent holds an extra identifying the wake lock.
         */
        Intent service = new Intent(context, NotificationSchedulingService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
        // END_INCLUDE(alarm_onreceive)
    }
    public void setAlarm(Context context) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //Calendar calendar = Calendar.getInstance();
      //  calendar.setTimeInMillis(System.currentTimeMillis());
       // calendar.set(Calendar.HOUR_OF_DAY, 0 );
       // calendar.set(Calendar.MINUTE, 5);



       // alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(),1000 * 60 * 1, alarmIntent);

         //Wake up the device to fire a one-time alarm in one minute.
           //alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60*1000, alarmIntent);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+60*1000, 1000*60, alarmIntent);

           /*Wake up the device to fire the alarm in 30 minutes, and every 30 minutes after that.
         alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                 AlarmManager.INTERVAL_HALF_HOUR,
                 AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);*/



        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, NotificationBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    // BEGIN_INCLUDE(cancel_alarm)
    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, NotificationBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(cancel_alarm)

}
