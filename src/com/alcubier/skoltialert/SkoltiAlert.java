package com.alcubier.skoltialert;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.settings.OAuthPrepReqTokenActivity;
import com.alcubier.skoltialert.tracks.TrackAdapter;
import com.alcubier.skoltialert.tracks.TrackDialog;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SkoltiAlert extends GDActivity {
    private final int ADD = 0;
    private final String HAS_ENTERED = "has_entered";
    private final String NEVER = "never";
    private final String ANY = "any";
    private final int SETTINGS = 1;
	private DatabaseHelper dbHelper;
	static public ListView listTrack;
	final String TAG = getClass().getName();
	private SimpleCursorAdapter scaTrack;
	private Cursor cursorTrack;
	private SharedPreferences prefs;
	
	final GDActivity thisActivity = this;
	/** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsTracker.getInstance().start(GDSkoltiAlertApplication.getTrackerId(), 60, this);
        GoogleAnalyticsTracker.getInstance().trackPageView("SkoltiAlert");
        setActionBarContentView(R.layout.listviewtrack);
        addActionBarItem(Type.Add, ADD);
        addActionBarItem(Type.Settings, SETTINGS);
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        listTrack=(ListView)findViewById(R.id.listtrack);
        listTrack.setEmptyView(findViewById(R.id.notracks));
        listTrack.setDivider(null);
        listTrack.setDividerHeight(0);
        
        if (prefs.getString(HAS_ENTERED, NEVER).equals(NEVER)) {
			Editor edit = prefs.edit();
			edit.putString(HAS_ENTERED, ANY);
			edit.putBoolean("toggle_refresh", true);
			edit.putBoolean("notification_refresh", true);
			edit.commit();
        	startActivity(new Intent(this, OAuthPrepReqTokenActivity.class));
        }
    	LoadGrid();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();

    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }

    /** 
	 * onDestory escribe en el Log el paso por este método.
	 * Android llama al método onDestory antes de que se destruya la actividad. Esto puede ocurrir tanto por que se llama al 
	 * método finish de la actividad o porque el sistema destruye la actividad para liberar espacio.
	 */
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        GoogleAnalyticsTracker.getInstance().dispatch();
        GoogleAnalyticsTracker.getInstance().stop();
    	dbHelper.close();
    }
    
    
    public void LoadGrid()
    {
    	dbHelper=new DatabaseHelper(this);
    	try
    	{
    		cursorTrack=dbHelper.getAllTracks();
    		startManagingCursor(cursorTrack);
    		
    		String [] from=new String []{DatabaseHelper.col_SearchTrack,DatabaseHelper.col_TopTrack};
    		int [] to=new int [] {R.id.colSearch,R.id.colTop};
    		scaTrack=new TrackAdapter(this,R.layout.listrowtrack,cursorTrack,from,to);
    		listTrack.setAdapter(scaTrack);
    		
    	}
    	catch(Exception ex)
    	{
    		AlertDialog.Builder b=new AlertDialog.Builder(this);
    		b.setMessage(ex.toString());
    		b.show();
    	}
    }

	//Se crea el menu cuando se crea la actividad
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   super.onCreateOptionsMenu(menu);
	   MenuInflater inflater = getMenuInflater();
	   inflater.inflate(R.menu.menu, menu);
	   return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   switch (item.getItemId()) {
	   case R.id.about:
		  startActivity(new Intent(this, AboutActivity.class));
		  return true;
	   // More items go here (if any) ...
	   }
	   return false;
	}
    
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
         switch (item.getItemId()) {
              case ADD:
            	  AddClickhandler();
                  break;
              case SETTINGS:
            	  startActivity(new Intent(this, Preferences.class));
                  break;
     
              default:
                  return super.onHandleActionBarItemClick(item, position);
         }
     
         return true;
    }
    
    public void AddClickhandler (){
    	try
    	{

    		AlertDialog diag= TrackDialog.ShowAddDialog(this);
    		diag.setOnDismissListener(new OnDismissListener() {

    			@Override
    			public void onDismiss(DialogInterface dialog) {
    				// TODO Auto-generated method stub
    				Log.i(TAG, "onDismiss");
    				cursorTrack.requery();
    				scaTrack.notifyDataSetChanged();
    			}
    		});
    		diag.show();
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "Excepcion3");
    		TrackDialog.CatchError(this, ex.toString());
    	}
    }

}
