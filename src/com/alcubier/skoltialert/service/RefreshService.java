package com.alcubier.skoltialert.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.SkoltiAlert;
import com.alcubier.skoltialert.db.DatabaseHelper;

public class RefreshService extends IntentService {
public static final String EXTRA_MESSENGER="com.alcubier.skoltialert.service.refreshservice.EXTRA_MESSENGER";
Context context;
DatabaseHelper dbHelper;
boolean hayAlarma;
boolean showAlarma;
final String TAG = getClass().getName();
NotificationManager mNM;
Notification notification;
PendingIntent contentIntent;
RemoteViews contentView;



public RefreshService() {
	super("RefreshService");
}

@Override
public void onCreate() {
	super.onCreate();
	context = getApplicationContext();
	dbHelper=new DatabaseHelper(context);
	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	showAlarma = prefs.getBoolean("notification_refresh", false); 
	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
}

private void showNotification() {
    // In this sample, we'll use the same text for the ticker and the expanded notification
    CharSequence text = getText(R.string.alarm_service_started);

    // Set the icon, scrolling text and timestamp
    notification = new Notification(R.drawable.ic_stat_refresh, text,
            System.currentTimeMillis());
    
//  notification.defaults |= Notification.DEFAULT_SOUND;

    // The PendingIntent to launch our activity if the user selects this notification
    contentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, SkoltiAlert.class), 0);

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
                   text, contentIntent);
    


    // Send the notification.
    // We use a layout id because it is a unique number.  We use it later to cancel.
    mNM.notify(R.string.alarm_service_started, notification);
}


@Override
public void onDestroy() {
	super.onDestroy();
	


    // Tell the user we stopped.
    if (showAlarma) {
    	mNM.cancel(R.string.alarm_service_started);
    	if (hayAlarma) {
    		CharSequence text = getText(R.string.alarm_service_hayalarma);
    	    // Set the icon, scrolling text and timestamp
    	    notification = new Notification(R.drawable.stat_notify_alarm, text,
    	            System.currentTimeMillis());
    	    // Set the icon, scrolling text and timestamp
    		notification.defaults |= Notification.DEFAULT_SOUND;
    		notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	    notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
                    text, contentIntent);
    	    
    		mNM.notify(R.string.alarm_service_hayalarma, notification);
    	}
	}	
}

@Override	
public void onHandleIntent(Intent i) {

	int result=Activity.RESULT_CANCELED;

	Bundle extras=i.getExtras();
	String searchTrack;
	int idTrack;

	String type = extras.getString("type");
	Log.i(getClass().getName(), type);
	
	
	Toast.makeText(getApplicationContext(), type, Toast.LENGTH_LONG).show();
	
	if (type.equals("all")) {
		if (showAlarma) showNotification();
		searchTrack = extras.getString("track");
		hayAlarma = dbHelper.UpdateTracks(context, "");
	} else {
		if (type.equals("delete")) {
			showAlarma = false;
			idTrack = extras.getInt("idtrack");
			dbHelper.CleanMentionsByTrack(context, idTrack, true);
		} else {
			if (showAlarma) showNotification();
			searchTrack = extras.getString("track");
			hayAlarma = dbHelper.UpdateTracks(context, searchTrack);
		}
		
	}
	Log.i(TAG,"RefreshService ended");		
    dbHelper.close();
        
    result=Activity.RESULT_OK;
	
	if (type.equals("refreshone")) {
		Messenger messenger=(Messenger)extras.get(EXTRA_MESSENGER);
		Message msg=Message.obtain();
		
		msg.arg1=result;
		
		try {
			messenger.send(msg);
		}
		catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}
	}
}
}