package com.alcubier.skoltialert.BReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.alcubier.skoltialert.service.RefreshService;

public class RefreshBReceiver extends BroadcastReceiver {
	 
	final String TAG = getClass().getName();

	private Handler handler=new Handler() {
		@Override
		public void handleMessage(Message msg) {

		}
	};
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Recurring alarm; requesting download service.");
        // start the download
        
    	Bundle bundle = new Bundle();
    	bundle.putString("type", "all");
        Intent refresh = new Intent(context, RefreshService.class);
		refresh.putExtras(bundle);
		refresh.putExtra(RefreshService.EXTRA_MESSENGER, new Messenger(handler));
        context.startService(refresh);   
    }
 
}