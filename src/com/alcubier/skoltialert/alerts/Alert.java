package com.alcubier.skoltialert.alerts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.mentions.Utils;
import com.alcubier.skoltialert.tracks.Track;


public class Alert {

	final String TAG = getClass().getName();
	public static final int EACH_HOUR = 0;
	public static final int EACH_DAY = 1;
	
	int    _id_alert;
	int    _id_track;
	int    _id_client;
	String _search_track;
	String _date_alert_from;
	String _date_alert_to;
	int    _ind_alert;
	int    _count;	
	int    _top_track;
	int    _bottom_track;
	int    _period;

	
	public Alert()
	{
		this._id_alert=0;
		this._id_track=0;
		this._id_client=0;
		this._search_track="";
		this._date_alert_from="";
		this._date_alert_to="";	
		this._ind_alert=0;
		this._count=0;		
		this._top_track=0;
		this._bottom_track=0;
		this._period=0;

	}

	public int getIdAlert()
	{
		return this._id_alert;
	}
	public void setIdAlert(int id_alert)
	{
		this._id_alert=id_alert;
	}

	public int getIdTrack()
	{
		return this._id_track;
	}
	public void setIdTrack(int id_track)
	{
		this._id_track=id_track;
	}

	public int getIdClient()
	{
		return this._id_client;
	}
	public void setIdClient(int id_client)
	{
		this._id_client=id_client;
	}
	
	public String getSearchTrack()
	{
		return this._search_track;
	}

	public void setSearchTrack(String search_track)
	{
		this._search_track=search_track;
	}

	public String getDateAlertFrom()
	{
		return this._date_alert_from;
	}

	public void setDateAlertFrom(String date_alert_from)
	{
		this._date_alert_from=date_alert_from;
	}

	public String getDateAlertTo()
	{
		return this._date_alert_to;
	}

	public void setDateAlertTo(String date_alert_to)
	{
		this._date_alert_to=date_alert_to;
	}
	
	public int getIndAlert()
	{
		return this._ind_alert;
	}

	public void setIndAlert(int ind_alert)
	{
		this._ind_alert=ind_alert;
	}

	public int getCount()
	{
		return this._count;
	}

	public void setCount(int count)
	{
		this._count=count;
	}

	public int getTopTrack()
	{
		return this._top_track;
	}
	public void setTopTrack(int top_track)
	{
		this._top_track=top_track;
	}
	
	public int getBottomTrack()
	{
		return this._bottom_track;
	}
	public void setBottomTrack(int bottom_track)
	{
		this._bottom_track=bottom_track;
	}

	public int getPeriod()
	{
		return this._period;
	}
	public void setPeriod(int period)
	{
		this._period=period;
	}

	public boolean CheckAlert(DatabaseHelper dbHelper, Track track) {

		Date dataLastAlert;
		Date dataNow;
		Date dataNextAlert;
		Date dataLastMentions;
		Date dataFirstMention;
		Calendar calendar;
//		int count;
		boolean hayAlarma = false;
		boolean hayAlgunaAlarma = false;
		
		dataNow = new Date(); 									//Today
		dataLastMentions = Utils.String2Date(track.getDateActive(),"yyyy-MM-dd HH:mm:ss"); //LastMention
		calendar = Calendar.getInstance();
		dataLastAlert = dbHelper.getLastAlert(track.getIdTrack());
		//Desde la ultima alerta (con o sin alarma)
		if (dataLastAlert != null) {
			calendar.setTime(dataLastAlert);
			calendar.add(Calendar.HOUR_OF_DAY, +1);
			dataNextAlert = calendar.getTime();
			Log.i(TAG, "hay alertas anteriores (alerta anterior+1hora): "+dataNextAlert);

		} else {
			dataFirstMention = dbHelper.getFirstMention(track.getIdTrack());
			if (dataFirstMention != null) {
				calendar.setTime(dataFirstMention);
				calendar.add(Calendar.HOUR_OF_DAY, +1);
				dataNextAlert = calendar.getTime();
				Log.i(TAG, "hay algun mention (primer mention+1periodo): "+dataNextAlert);
			} else {
				dataNextAlert = dataNow;
				Log.i(TAG, "desde ahora: "+dataNextAlert);
			}
		}
		Log.i(TAG, "Fecha tope ultima actualizacion mentions: "+dataLastMentions);
		while (dataLastMentions.after(dataNextAlert)) {
//			Log.i(TAG, "Empiezo muestreo: "+dataNextAlert);
			hayAlarma = CheckOneAlert(dbHelper, track, dataNextAlert);
			if (hayAlarma) {
				hayAlgunaAlarma = true;
			}
			calendar.setTime(dataNextAlert);
			calendar.add(Calendar.HOUR_OF_DAY, +1);
			dataNextAlert = calendar.getTime();
//			Log.i(TAG, "Siguiente muestreo: "+dataNextAlert);
		}
		return hayAlgunaAlarma;
				
	}

	public boolean CheckOneAlert(DatabaseHelper dbHelper, Track track, Date data) {

		Calendar calendar;
		String formatoFecha;
		SimpleDateFormat formatter;
		String dateFrom;
		String dateTo;
		int count;
		
		formatoFecha = "yyyy-MM-dd HH:mm:ss";
		formatter = new SimpleDateFormat(formatoFecha);
		dateTo = formatter.format(data);
//		Log.i("TAG", "dateTo: "+dateTo);

		calendar = Calendar.getInstance();
		calendar.setTime(data);
		calendar.add(Calendar.HOUR_OF_DAY, -1);
		data = calendar.getTime();
		formatoFecha = "yyyy-MM-dd HH:mm:ss";
		formatter = new SimpleDateFormat(formatoFecha);
		dateFrom = formatter.format(data);
//		Log.i("TAG", "dateFrom: "+dateFrom);

		count = dbHelper.getMentionsCount(track.getIdTrack(), dateFrom, dateTo);
//		Log.i("TAG", "count: "+count);

		setIdTrack(track.getIdTrack());
		setSearchTrack(track.getSearchTrack());
		setDateAlertFrom(dateFrom);
		setDateAlertTo(dateTo);
		setCount(count);
		setTopTrack(track.getTopTrack());
		if (count > track.getTopTrack()) {
			setIndAlert(1);
		}
		else {
			setIndAlert(0);
		}
		dbHelper.AddAlert(this);
		if (count > track.getTopTrack()) {
			return true;
		}
		else {
			return false;
		}
	}
}
