package com.alcubier.skoltialert;

import greendroid.app.GDApplication;
import android.content.Intent;
 
public class GDSkoltiAlertApplication extends GDApplication {
	@Override
	public Class<?> getHomeActivityClass() {
		return SkoltiAlert.class;
	}

	public static String getTrackerId() {
		return "UA-24173057-1";
	}

	@Override
	public Intent getMainApplicationIntent() {
		//    	 return new Intent(Intent.ACTION_MAIN);
		return new Intent(this, SkoltiAlert.class);
	}

}