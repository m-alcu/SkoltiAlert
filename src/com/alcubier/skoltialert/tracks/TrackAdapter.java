package com.alcubier.skoltialert.tracks;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.alerts.AlertsList;
import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.mentions.GDMentionsList;
import com.alcubier.skoltialert.plot.GraphicsActivity;

public class TrackAdapter extends SimpleCursorAdapter {
	
	final String TAG = getClass().getName();
    private int mLayout;
    private Context context;
    private Cursor crTrack;
    private QuickActionWidget mBar;
    private Track selectedTrack;

    static class ViewHolderAdapter
    {
        TextView     hSearch;
        TextView     hTop;
        TextView     hCount;
        ImageView    hGraph;
        LinearLayout hLayout;
    }    
    
	public TrackAdapter(Context context, int layout, Cursor c, String[] from, int[] to) { 
        super(context, layout, c, from, to);
        this.mLayout = layout;
        this.context = context;
        this.crTrack = c;
        prepareQuickActionBar();
	}

    private void prepareQuickActionBar() {
        mBar = new QuickActionBar(context);
        mBar.addQuickAction(new MyQuickAction(context, R.drawable.gd_action_bar_slideshow, R.string.ver_charts));
        mBar.addQuickAction(new MyQuickAction(context, R.drawable.gd_action_bar_info, R.string.ver_alerts));
        mBar.addQuickAction(new MyQuickAction(context, R.drawable.gd_action_bar_edit, R.string.edit_track));
        mBar.addQuickAction(new MyQuickAction(context, R.drawable.ic_tab_delete, R.string.delete_track));
        mBar.setOnQuickActionClickListener(mActionListener);
    }
    
    private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
            if (position == 0) {
            	DatabaseHelper dbHelper=new DatabaseHelper(context);
            	int numAlerts = dbHelper.getAlertCount(selectedTrack.getIdTrack());
            	dbHelper.close();
            	if (numAlerts < 3) {
            		Toast.makeText(context, context.getString(R.string.needed_tree_samples), Toast.LENGTH_SHORT).show();
            	} else {
            		Bundle bundle = new Bundle();
        			bundle.putInt("idTrack", selectedTrack.getIdTrack());
        			bundle.putInt("top", selectedTrack.getTopTrack());
        			bundle.putString("search", selectedTrack.getSearchTrack());
        			Intent i = new Intent(context, GraphicsActivity.class);
        			i.putExtras(bundle);
        			context.startActivity(i);           		
            	}
            }
            if (position == 1) {
        		Bundle bundle = new Bundle();
    			bundle.putInt("idTrack", selectedTrack.getIdTrack());
    			Intent i = new Intent(context, AlertsList.class);
    			i.putExtras(bundle);
    			context.startActivity(i);
            }
            if (position == 2) {
            	ModificarClickhandler ();
            }
            if (position == 3) {
				DatabaseHelper db=new DatabaseHelper(context);
				db.DeleteTrack(selectedTrack);
				db.close();
				crTrack.requery();
				notifyDataSetChanged();
            }
        }
    };
	
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        ViewHolderAdapter holder = null;
        holder = new ViewHolderAdapter();
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(mLayout, parent, false);     

        holder.hSearch = (TextView) v.findViewById(R.id.colSearch);
        holder.hTop = (TextView) v.findViewById(R.id.colTop);
        holder.hCount = (TextView) v.findViewById(R.id.colCount);
        holder.hGraph = (ImageView) v.findViewById(R.id.image_graph);
        holder.hLayout = (LinearLayout) v.findViewById(R.id.colRight);
        
        v.setTag(holder);
        
        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

        ViewHolderAdapter holder = null;
        holder = new ViewHolderAdapter();
        Track trackTag = new Track();
        trackTag.setIdTrack(c.getInt(c.getColumnIndex("_id")));
        trackTag.setSearchTrack(c.getString(c.getColumnIndex(DatabaseHelper.col_SearchTrack)));
        trackTag.setCount(c.getInt(c.getColumnIndex(DatabaseHelper.col_Count)));
        trackTag.setTopTrack(c.getInt(c.getColumnIndex(DatabaseHelper.col_TopTrack)));
        trackTag.setLastId(c.getLong(c.getColumnIndex(DatabaseHelper.col_LastId)));
        holder = (ViewHolderAdapter) v.getTag();
        
        if (holder.hSearch != null) {
            holder.hSearch.setText(trackTag.getSearchTrack());
        }

        if (holder.hTop != null) {
        	Integer top = trackTag.getTopTrack();
        	holder.hTop.setText(top.toString());
        }
        
        if (holder.hCount != null) {
        	Integer top = trackTag.getTopTrack();
        	Integer count = trackTag.getCount();
        	holder.hCount.setText(count.toString());
        	if (count > top) {
        		holder.hCount.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundred));
        	} else {
        		holder.hCount.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundgreen));
        	}
        		
        }
        
        if (holder.hGraph != null) {
            holder.hGraph.setTag(trackTag);
        }

        if (holder.hLayout != null) {
            holder.hLayout.setTag(trackTag);
        }
        
        v.setTag(holder);
  
        holder.hLayout.setOnClickListener(new MentionClickhandler());
//      holder.hSearch.setOnClickListener(new MentionClickhandler());
//      holder.hTop.setOnClickListener(new MentionClickhandler());
        holder.hGraph.setOnClickListener(new ModificarBar());
    }

    public class ModificarBar implements View.OnClickListener
    {
    	public void onClick( View view ){
    		onShowBar(view);
    	}	
    }
    
    public void onShowBar(View v) {
        mBar.show(v);
        selectedTrack = (Track) v.getTag();
    }
    
    
    public void ModificarClickhandler (){
    	Log.i(TAG, "item: "+selectedTrack.getSearchTrack());
    	try
    	{

    		AlertDialog diag= TrackDialog.ShowEditDialog(context,selectedTrack);
    		diag.setOnDismissListener(new OnDismissListener() {

    			@Override
    			public void onDismiss(DialogInterface dialog) {
    				// TODO Auto-generated method stub
    				crTrack.requery();
    				notifyDataSetChanged();
    			}
    		});
    		diag.show();
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG, "Excepcion3");
    		TrackDialog.CatchError(context, ex.toString());
    	}
    }

    /**********BOTONES*****************/
    public class MentionClickhandler implements View.OnClickListener 
    {
    	public void onClick( View view ){
    		
            Track trackTag = new Track();
            trackTag = (Track) view.getTag();
            Log.i(TAG, "item: "+trackTag.getSearchTrack());
            
    		Bundle bundle = new Bundle();
			bundle.putInt("idTrack", trackTag.getIdTrack());
			bundle.putString("searchTrack", trackTag.getSearchTrack());
			bundle.putInt("topTrack", trackTag.getTopTrack());
			bundle.putLong("lastId", trackTag.getLastId());
			bundle.putInt("count", trackTag.getCount());
			bundle.putString("type", "LastMentions");
			
			Intent i = new Intent(context, GDMentionsList.class);
			i.putExtras(bundle);
			context.startActivity(i);

        }
    }

    private static class MyQuickAction extends QuickAction {
        
        private static final ColorFilter BLACK_CF = new LightingColorFilter(Color.BLACK, Color.BLACK);

        public MyQuickAction(Context ctx, int drawableId, int titleId) {
            super(ctx, buildDrawable(ctx, drawableId), titleId);
        }
        
        private static Drawable buildDrawable(Context ctx, int drawableId) {
            Drawable d = ctx.getResources().getDrawable(drawableId);
            d.setColorFilter(BLACK_CF);
            return d;
        }
        
    }

}