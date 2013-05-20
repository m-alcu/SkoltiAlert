package com.alcubier.skoltialert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.alcubier.skoltialert.service.ProgramRefresh;
import com.alcubier.skoltialert.settings.OAuthPrepReqTokenActivity;
 
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	
	final String TAG = getClass().getName();
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setPreferenceScreen(createPreferenceHierarchy());

		
        // Create an IntentSender that will launch our service, to be scheduled
        // with the alarm manager.
//1. lanzar servicio directamente		
//      mAlarmSender = PendingIntent.getService(this,
//              0, new Intent(this, RefreshService.class), 0);
//2. lanzar receiver que lanza el servicio		
		
	}		
	
    @Override
    protected void onResume() {
        super.onResume();

        PreferenceScreen accountPref = (PreferenceScreen) findPreference( "user_autenticated" );
        if (prefs.getBoolean("Logged", false)) {
        	String userAccount = prefs.getString("twittername", "");
        	accountPref.setSummary(userAccount);
        } else
        {
        	accountPref.setSummary(R.string.summary_oauth_twitter);
        }

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }	
	
    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        
        
        //**********************************************
        // Categoria de Account
        PreferenceCategory accountCat = new PreferenceCategory(this);
        accountCat.setTitle(R.string.category_account);
        root.addPreference(accountCat);

       
        // Intent preference
        PreferenceScreen accountPref = getPreferenceManager().createPreferenceScreen(this);
        accountPref.setIntent(new Intent(this, OAuthPrepReqTokenActivity.class));
        accountPref.setKey("user_autenticated");
        accountPref.setTitle(R.string.title_oauth_twitter);
        if (prefs.getBoolean("Logged", false)) {
        	String userAccount = prefs.getString("twittername", "");
        	accountPref.setSummary(userAccount);
        } else
        {
        	accountPref.setSummary(R.string.summary_oauth_twitter);
        }
        accountCat.addPreference(accountPref);
        
        //**********************************************
        // Categoria de Refresh
        PreferenceCategory refreshPrefCat = new PreferenceCategory(this);
        refreshPrefCat.setTitle(R.string.category_refresh);
        root.addPreference(refreshPrefCat);

        // Toggle Refresh
        CheckBoxPreference trefreshPref = new CheckBoxPreference(this);
        trefreshPref.setKey("toggle_refresh");
        trefreshPref.setTitle(R.string.title_toggle_refresh);
        trefreshPref.setSummary(R.string.summary_toggle_refresh);
        refreshPrefCat.addPreference(trefreshPref);

        // Notification Refresh
        CheckBoxPreference nrefreshPref = new CheckBoxPreference(this);
        nrefreshPref.setKey("notification_refresh");
        nrefreshPref.setTitle(R.string.title_notification_refresh);
        nrefreshPref.setSummary(R.string.summary_notification_refresh);
        refreshPrefCat.addPreference(nrefreshPref);
        
        // List preference
        ListPreference lrefreshPref = new ListPreference(this);
        lrefreshPref.setEntries(R.array.a_refresh);
        lrefreshPref.setEntryValues(R.array.v_refresh);
        lrefreshPref.setDialogTitle(R.string.title_list_refresh);
        lrefreshPref.setKey("list_refresh");
        lrefreshPref.setTitle(R.string.title_list_refresh);
        lrefreshPref.setSummary(R.string.summary_list_refresh);
        refreshPrefCat.addPreference(lrefreshPref);

        //**********************************************
        // Categoria de Mentions
        PreferenceCategory mentionsPrefCat = new PreferenceCategory(this);
        mentionsPrefCat.setTitle(R.string.category_mentions);
        root.addPreference(mentionsPrefCat);

        // List preference
        ListPreference lmentionsPref = new ListPreference(this);
        lmentionsPref.setEntries(R.array.a_last_mentions);
        lmentionsPref.setEntryValues(R.array.v_last_mentions);
        lmentionsPref.setDialogTitle(R.string.title_last_mentions);
        lmentionsPref.setKey("list_last_mentions");
        lmentionsPref.setTitle(R.string.title_last_mentions);
        lmentionsPref.setSummary(R.string.summary_last_mentions);
        mentionsPrefCat.addPreference(lmentionsPref);
        
        
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.GINGERBREAD){
            // Toggle Refresh
            CheckBoxPreference tsdcardPref = new CheckBoxPreference(this);
            tsdcardPref.setKey("sd_card");
            tsdcardPref.setTitle(R.string.title_toggle_sdcard);
            tsdcardPref.setSummary(R.string.summary_toggle_sdcard);
            mentionsPrefCat.addPreference(tsdcardPref);
        } 

        //**********************************************
        // Categoría de información adicional
        PreferenceCategory moreinfoPrefCat = new PreferenceCategory(this);
        moreinfoPrefCat.setTitle(R.string.category_more_info);
        root.addPreference(moreinfoPrefCat);

        // Intent preference
        PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
        intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse("http://www.skolti.com/laboratorio/")));
        intentPref.setTitle(R.string.title_visit_skolti);
        intentPref.setSummary(R.string.summary_visit_skolti);
        moreinfoPrefCat.addPreference(intentPref);

        return root;
    }
	
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when my counter preference value changes
        if (key.equals("toggle_refresh")) {
//          Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            ProgramRefresh.Schedule(this, sharedPreferences, true);
        }

        Preference pref = findPreference(key);
        if (key.equals("user_autenticated")) {
            if (prefs.getBoolean("Logged", false)) {
            	String userAccount = prefs.getString("twittername", "");
            	pref.setSummary(userAccount);
            } else
            {
            	pref.setSummary(R.string.summary_oauth_twitter);
            }
        }        
        
//        Preference pref = findPreference(key);
//        if (pref instanceof EditTextPreference) {
//            EditTextPreference etp = (EditTextPreference) pref;
//            pref.setSummary(etp.getText());
//        }
    }
    
}