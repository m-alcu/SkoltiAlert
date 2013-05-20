package com.alcubier.skoltialert.alerts;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.alcubier.skoltialert.Preferences;
import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.mentions.GDMentionsList;
import com.alcubier.skoltialert.tracks.TrackDialog;

public class AlertsList extends GDActivity {
    private final int SETTINGS = 0;
	DatabaseHelper dbHelper;
	static public ListView listAlert;
	final String TAG = getClass().getName();
	AlertsAdapter sca;
	SQLiteCursor cr;
	Cursor c;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.listviewalert);
        addActionBarItem(Type.Settings, SETTINGS);
        listAlert=(ListView)findViewById(R.id.listalert);
        Log.i(TAG, "onCreate AlertsList");
//      listAlert.setDivider(null);
//      listAlert.setDividerHeight(0);
        
        LoadGrid();
       
        try
        {
        listAlert.setOnItemClickListener(new OnItemClickListener()
        {

        	@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				try
				{
			
				cr=(SQLiteCursor)parent.getItemAtPosition(position);
				String dateAlertFrom=cr.getString(cr.getColumnIndex(DatabaseHelper.col_DateAlertFrom));
				String dateAlertTo=cr.getString(cr.getColumnIndex(DatabaseHelper.col_DateAlertTo));
				int idTrack=cr.getInt(cr.getColumnIndex(DatabaseHelper.col_IdTrack));
				//Creamos el bundle
				Bundle bundle = new Bundle();
				bundle.putInt("idTrack", idTrack);
				bundle.putString("dateAlertFrom", dateAlertFrom);
				bundle.putString("dateAlertTo", dateAlertTo);
				bundle.putString("type", "AlertMentions");
				Intent i = new Intent(AlertsList.this, GDMentionsList.class);
				i.putExtras(bundle);
				startActivity(i);

				}
				catch(Exception ex)
				{
					Log.i(TAG, "Excepcion1");
					TrackDialog.CatchError(AlertsList.this, ex.toString());
				}
			}

			
        }
        );
        }
        catch(Exception ex)
        {
        	Log.i(TAG, "Excepcion2");
        }

    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }

    @Override
    public void onResume()
    {
    	super.onResume();
  		c.requery();
  		sca.notifyDataSetChanged();
		
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	dbHelper.close();
    }

    
    public void LoadGrid()
    {
    	dbHelper=new DatabaseHelper(this);
    	try
    	{
    		Bundle bundle = this.getIntent().getExtras();
    		Integer idTrack = bundle.getInt("idTrack");
    		c=dbHelper.getAllAlerts(idTrack);
    		startManagingCursor(c);
    		
    		String [] from=new String []{DatabaseHelper.col_SearchTrack,DatabaseHelper.col_DateAlertTo,DatabaseHelper.col_Count, DatabaseHelper.col_TopTrack};
    		int [] to=new int [] {R.id.colSearchAlert,R.id.colDateAlert,R.id.colCountAlert,R.id.colTopAlert};
    		sca=new AlertsAdapter(this,R.layout.listrowalert,c,from,to);
    		listAlert.setAdapter(sca);
    		
    	}
    	catch(Exception ex)
    	{
    		AlertDialog.Builder b=new AlertDialog.Builder(this);
    		b.setMessage(ex.toString());
    		b.show();
    	}
    }
    
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
         switch (item.getItemId()) {
              case SETTINGS:
            	  startActivity(new Intent(this, Preferences.class));
                  break;
     
              default:
                  return super.onHandleActionBarItemClick(item, position);
         }
     
         return true;
    }
    
}
