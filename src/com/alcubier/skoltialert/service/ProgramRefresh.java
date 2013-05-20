package com.alcubier.skoltialert.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.alcubier.skoltialert.BReceiver.RefreshBReceiver;

public class ProgramRefresh {
	
    public static void Schedule(Context context, SharedPreferences sharedPreferences, Boolean inmediato) {
    	
    	PendingIntent mAlarmSender;
        mAlarmSender = PendingIntent.getBroadcast(context,
        		0, new Intent(context, RefreshBReceiver.class), 0);

    	
        Boolean RefreshPref = sharedPreferences.getBoolean("toggle_refresh", false);
        if (RefreshPref) {
            // We want the alarm to go off 30 seconds from now.
            long firstTime = SystemClock.elapsedRealtime();
            long minutes = 60;

            Integer listRefreshPreference = Integer.parseInt(sharedPreferences.getString("list_refresh", "60"));
            
            switch(listRefreshPreference) {
            case 1: minutes = 1; break;
            case 5: minutes = 5; break;
            case 10: minutes = 10; break;
            case 30: minutes = 30; break;
            case 60: minutes = 60; break;
            case 180: minutes = 180; break;
            case 360: minutes = 360; break;
            case 720: minutes = 720; break;
            case 1440: minutes = 1440; break;
            default: minutes = 60; 
            }
            
            if (!inmediato) {
            	firstTime = firstTime + minutes*1000*60;
            }
            
            // Schedule the alarm!
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime, minutes*1000*60, mAlarmSender);

        } else {
            // And cancel the alarm.
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(mAlarmSender);
        }

    }
}
