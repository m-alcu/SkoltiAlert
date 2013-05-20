package com.alcubier.skoltialert.mentions;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utils {
	
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    public static boolean hasInternet(Activity a) {
        boolean hasConnectedWifi = false;
        boolean hasConnectedMobile = false;
     
        ConnectivityManager cm = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("wifi"))
                if (ni.isConnected())
                    hasConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("mobile"))
                if (ni.isConnected())
                    hasConnectedMobile = true;
        }
        return hasConnectedWifi || hasConnectedMobile;
    }
    
    public static Date String2Date(String date, String formatoFecha) {
    	
    	Date dataParsed;
		SimpleDateFormat formatter = new SimpleDateFormat(formatoFecha);
		try {
			dataParsed = (Date)formatter.parse(date);
		} catch (ParseException e) {
			 Date data = new Date(); 
			 Log.i("String2Date", " Error parse:" + date);
			 return data;
			 
			// TODO Auto-generated catch block
		}
		return dataParsed;
    }
    
    public static String getNameDatabase(Context context) {

    	SharedPreferences prefs;
    	final String DATABASE_NAME = "skoltialertDB";

    	prefs = PreferenceManager.getDefaultSharedPreferences(context);

    	if (prefs.getBoolean("sd_card", false)) {
    		Log.i("Utils", "sd_card");
    		File sdcard = Environment.getExternalStorageDirectory();
    		String dbfile = sdcard.getAbsolutePath() + File.separator + DATABASE_NAME;
    		Log.i("Utils", dbfile);
    		return dbfile; 
    	} else {
    		Log.i("Utils", "memory");
    		return DATABASE_NAME;
    	}
    }

}