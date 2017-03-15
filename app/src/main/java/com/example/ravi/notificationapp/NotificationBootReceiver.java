package com.example.ravi.notificationapp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ravi on 05-Dec-16.
 */

public class NotificationBootReceiver extends BroadcastReceiver
{
    NotificationAlarmReceiver alarm = new NotificationAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
