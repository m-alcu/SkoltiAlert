package com.alcubier.skoltialert.tracks;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.service.RefreshService;

public class TrackDialog {
	public static void ShowTrackAddedAlert(Context con)
	{
		CharSequence text;
		AlertDialog.Builder builder=new AlertDialog.Builder(con);
		text = con.getText(R.string.track_add_details);
		builder.setTitle(text);
		builder.setIcon(R.drawable.gd_action_bar_add);
		DialogListner listner=new DialogListner();
		text = con.getText(R.string.track_add_succesfully);
		builder.setMessage(text);
		builder.setPositiveButton("ok", listner);

		AlertDialog diag=builder.create();
		diag.show();
	}

	public static AlertDialog ShowEditDialog(final Context con,final Track track)
	{
		CharSequence text;
		AlertDialog.Builder b=new AlertDialog.Builder(con);
		text = con.getText(R.string.track_edit_details);
		b.setTitle(text);
		LayoutInflater li=LayoutInflater.from(con);
		View v=li.inflate(R.layout.editdialog, null);

		b.setIcon(R.drawable.gd_action_bar_edit);

		b.setView(v);
		final TextView trackSearch=(TextView)v.findViewById(R.id.editSearch);
		final TextView trackTop=(TextView)v.findViewById(R.id.editTop);

		trackSearch.setText(track.getSearchTrack());
		trackTop.setText(String.valueOf(track.getTopTrack()));

		text = con.getText(R.string.track_edit_modify);
		b.setPositiveButton(text, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				track.setSearchTrack(trackSearch.getText().toString());
				try
				{
					track.setTopTrack(Integer.valueOf(trackTop.getText().toString()));
				}
				catch(Exception ex)
				{
					track.setTopTrack(0);
				}
				DatabaseHelper db=new DatabaseHelper(con);
				db.UpdateTrackDialog(track);
				db.close();
			}
		});

		text = con.getText(R.string.track_edit_delete);
		b.setNeutralButton(text, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				DatabaseHelper db=new DatabaseHelper(con);
				db.DeleteTrack(track);
				db.close();
// No borramos los mentions y alerts (es mejor dejarlo así si no no permite borrar seguidos				
//            	Bundle bundle = new Bundle();
//            	bundle.putString("type", "delete");
//            	bundle.putInt("idtrack", track.getIdTrack());
//            	Intent refresh = new Intent(con, RefreshService.class);
//        		refresh.putExtras(bundle);
//              con.startService(refresh);   

			}
		});
		text = con.getText(R.string.track_edit_cancel);
		b.setNegativeButton(text, null);

		return b.create();
		//diag.show();

	}

	public static AlertDialog ShowAddDialog(final Context con)
	{
		CharSequence text;
		AlertDialog.Builder b=new AlertDialog.Builder(con);
		text = con.getText(R.string.track_add_details);
		b.setTitle(text);
		LayoutInflater li=LayoutInflater.from(con);
		View v=li.inflate(R.layout.editdialog, null);

		b.setIcon(R.drawable.gd_action_bar_add);

		b.setView(v);
		final TextView trackSearch=(TextView)v.findViewById(R.id.editSearch);
		final TextView trackTop=(TextView)v.findViewById(R.id.editTop);

//		trackSearch.setText(track.getSearchTrack());
//		trackTop.setText(String.valueOf(track.getTopTrack()));

 		text = con.getText(R.string.track_add_add);
		b.setPositiveButton(text, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Track track;
				track = new Track();

				Date data = new Date();
				String formatoFecha = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat formatter = new SimpleDateFormat(formatoFecha);
				String dateString = formatter.format(data);
				track.setDateActive(dateString);
					
				track.setSearchTrack(trackSearch.getText().toString());
				try
					{	
					track.setTopTrack(Integer.valueOf(trackTop.getText().toString()));
					}
				catch(Exception ex)
					{
					track.setTopTrack(0);
					}
				DatabaseHelper db=new DatabaseHelper(con);
				db.AddTrack(track);
				ShowTrackAddedAlert(con);
				db.close();

              	Bundle bundle = new Bundle();
              	bundle.putString("type", "add");
              	bundle.putString("track", trackSearch.getText().toString());
              	Intent refresh = new Intent(con, RefreshService.class);
          		refresh.putExtras(bundle);
                con.startService(refresh);   
			}
		});
		text = con.getText(R.string.track_edit_cancel);
		b.setNegativeButton(text, null);

		return b.create();
		//diag.show();

	}

	static public void CatchError(Context con, String Exception)
	{
		Dialog diag=new Dialog(con);
		diag.setTitle("Error");
		TextView txt=new TextView(con);
		txt.setText(Exception);
		diag.setContentView(txt);
		diag.show();
	}
	

}


