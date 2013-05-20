package com.alcubier.skoltialert.alerts;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.mentions.Utils;

public class AlertsAdapter extends SimpleCursorAdapter {

    private int mLayout;
    Context context;
    Cursor crAlert;
    final String TAG = getClass().getName();

    static class ViewHolderAdapter
    {
        TextView     hSearch;
        TextView     hTop;
        TextView     hCount;
        TextView	 hDate;
        TextView	 hHour;
    }    
    
	public AlertsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) { 
        super(context, layout, c, from, to);
        this.mLayout = layout;
        this.context = context;
        this.crAlert = c;
	}
	
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        ViewHolderAdapter holder = null;
        holder = new ViewHolderAdapter();
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(mLayout, parent, false);     

        holder.hSearch = (TextView) v.findViewById(R.id.colSearchAlert);
        holder.hTop = (TextView) v.findViewById(R.id.colTopAlert);
        holder.hCount = (TextView) v.findViewById(R.id.colCountAlert);
        holder.hDate = (TextView) v.findViewById(R.id.colDateAlert);
        holder.hHour = (TextView) v.findViewById(R.id.colHourAlert);
        
        v.setTag(holder);
        
        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

		Date dataTo;
		SimpleDateFormat dateFormat;
		String dateString;
		
        ViewHolderAdapter holder = null;
        holder = new ViewHolderAdapter();
        Alert alertTag = new Alert();
//      alertTag.setIdAlert(c.getInt(c.getColumnIndex("_id")));
//      alertTag.setIdTrack(c.getInt(c.getColumnIndex(DatabaseHelper.col_IdTrack)));
        alertTag.setSearchTrack(c.getString(c.getColumnIndex(DatabaseHelper.col_SearchTrack)));
        alertTag.setCount(c.getInt(c.getColumnIndex(DatabaseHelper.col_Count)));
        alertTag.setTopTrack(c.getInt(c.getColumnIndex(DatabaseHelper.col_TopTrack)));
        alertTag.setDateAlertTo(c.getString(c.getColumnIndex(DatabaseHelper.col_DateAlertTo)));
        
        holder = (ViewHolderAdapter) v.getTag();
        
        if (holder.hSearch != null) {
            holder.hSearch.setText(alertTag.getSearchTrack());
        }

        if (holder.hCount != null) {
        	Integer count = alertTag.getCount();
        	Integer top = alertTag.getTopTrack();
        	holder.hCount.setText(count.toString());
        	if (count > top) {
        		holder.hCount.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundred));
        	} else {
        		holder.hCount.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundgreen));
        	}
        }
        
        if (holder.hTop != null) {
        	Integer top = alertTag.getTopTrack();
        	holder.hTop.setText(top.toString());
        	holder.hTop.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.roundgray));
        }
        
        if (holder.hDate != null) {
            dataTo = Utils.String2Date(alertTag.getDateAlertTo(),"yyyy-MM-dd HH:mm:ss");
    		dateFormat = new SimpleDateFormat("dd-MMM");
    		dateString = dateFormat.format(dataTo);
        	holder.hDate.setText(dateString);
        }

        if (holder.hHour != null) {
            dataTo = Utils.String2Date(alertTag.getDateAlertTo(),"yyyy-MM-dd HH:mm:ss");
    		dateFormat = new SimpleDateFormat("HH:mm");
    		dateString = dateFormat.format(dataTo);
        	holder.hHour.setText(dateString);
        }

        v.setTag(holder);
    }
	
}
