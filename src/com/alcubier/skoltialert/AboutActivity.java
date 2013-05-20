package com.alcubier.skoltialert;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.webkit.WebView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * The Activity that shows 'About' information.
 */
public class AboutActivity extends Activity {

        /*
         * (non-Javadoc)
         * 
         * @see android.app.Activity#onCreate(android.os.Bundle)
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                GoogleAnalyticsTracker.getInstance().trackPageView("About");
                setContentView(R.layout.about);
                String version = "";
                try {
                        version = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
                } catch (NameNotFoundException e) {
                        // Do nothing
                }
                setTitle(String.format(getString(R.string.activity_about), version));
                ((WebView) findViewById(R.id.about)).loadUrl("file:///android_asset/about.html");
        }
}
