package com.alcubier.skoltialert.mentions;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.tracks.Track;
import com.google.gson.Gson;

public class Mention {
	
	final String TAG = getClass().getName();
	int    _id_mentions;
	int    _id_track;
	String _date_mentions;
	String _gifo;
	long   _last_id;
	String _text;
	String _from_user;
	String _iso;

	
	public Mention()
	{
		this._id_mentions=0;
		this._id_track=0;
		this._date_mentions="";
		this._gifo="";
		this._last_id=0;
		this._text="";
		this._from_user="";
		this._iso="";

	}
	
	public int getIdMentions()
	{
		return this._id_mentions;
	}
	public void setIdMentions(int id_mentions)
	{
		this._id_mentions=id_mentions;
	}
	public int getIdTrack()
	{
		return this._id_track;
	}
	public void setIdTrack(int id_track)
	{
		this._id_track=id_track;
	}
	
	public String getDateMentions()
	{
		return this._date_mentions;
	}

	public void setDateMentions(String date_mentions)
	{
		this._date_mentions=date_mentions;
	}

	public String getGifo()
	{
		return this._gifo;
	}
	public void setGifo(String gifo)
	{
		this._gifo=gifo;
	}
	
	public long getLastId()
	{
		return this._last_id;
	}

	public void setLastId(long last_id)
	{
		this._last_id=last_id;
	}

	public String getText()
	{
		return this._text;
	}

	public void setText(String text)
	{
		this._text=text;
	}

	public String getFromUser()
	{
		return this._from_user;
	}

	public void setFromUser(String from_user)
	{
		this._from_user=from_user;
	}
	
	public String getIso()
	{
		return this._iso;
	}

	public void setIso(String iso)
	{
		this._iso=iso;
	}
	
	 public void GetMentions(DatabaseHelper dbHelper, String busqueda, Track track){

		 long next_id = 0;
      	 DateFormat formatter_long_US = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
      	 SimpleDateFormat formatter;
      	 String dateString;

		 try{
			 Gson gson = new Gson();
			 String urltwitter = "http://search.twitter.com/search.json";
			 busqueda = busqueda.replaceAll("#", "").replaceAll(" ", "");
			 
			 next_id = dbHelper.getMaxLastId(track.getIdTrack());
			 String urlbusqueda = urltwitter + "?q="+ busqueda + "&since_id=" + next_id + "&rpp=100";
			 Log.i(TAG,"Urlbusqueda: "+urlbusqueda);
			 InputStream iStream = getJSONData(urlbusqueda);
			 Reader r = new InputStreamReader(iStream);
			 TwitterSearch objs = gson.fromJson(r, TwitterSearch.class);
			 int countresults = objs.getResults().size();
			 
			 for(TwitterMention tr : objs.getResults()){
				 setIdTrack(track.getIdTrack());
				 Date data = tr.getFechaTweet(formatter_long_US);
				 String formatoFecha = "yyyy-MM-dd HH:mm:ss";
				 formatter = new SimpleDateFormat(formatoFecha);
				 dateString = formatter.format(data);
				 setDateMentions(dateString);
				 setGifo(tr.getProfileImageUrl());
				 setLastId(tr.getId());
				 if (next_id < tr.getId()) {
					 next_id = tr.getId(); 
				 }
				 setText(tr.getText());
				 setFromUser(tr.getFromUser());
				 setIso(tr.getIsoLanguageCode());
				 dbHelper.AddMentions(this);
			 }
			 track.setLastId(next_id);
			 formatter = new SimpleDateFormat("yyyy-MM-dd");
			 dateString = formatter.format(new Date());
			 track.setDateActive(dateString);
			 dbHelper.UpdateTrackLastId(track);

			 int count = 0;
			 while ((objs.getNext_page() != null) & (count < 9)) { //Ya no hay más menciones
 				 urlbusqueda = urltwitter + objs.getNext_page();
				 iStream = getJSONData(urlbusqueda);
				 r = new InputStreamReader(iStream);
				 objs = gson.fromJson(r, TwitterSearch.class);
				 count++;
				 countresults = countresults + objs.getResults().size();
				 for(TwitterMention tr : objs.getResults()){
					 setIdTrack(track.getIdTrack());
					 Date data = tr.getFechaTweet(formatter_long_US);
					 String formatoFecha = "yyyy-MM-dd HH:mm:ss";
					 formatter = new SimpleDateFormat(formatoFecha);
					 dateString = formatter.format(data);
					 setDateMentions(dateString);
					 setGifo(tr.getProfileImageUrl());
					 setLastId(tr.getId());
					 if (next_id < tr.getId()) {
						 next_id = tr.getId(); 
					 }
					 setText(tr.getText());
					 setFromUser(tr.getFromUser());
					 setIso(tr.getIsoLanguageCode());
					 dbHelper.AddMentions(this);
				 }
				 track.setLastId(next_id);
				 formatter = new SimpleDateFormat("yyyy-MM-dd");
				 dateString = formatter.format(new Date());
				 track.setDateActive(dateString);
				 dbHelper.UpdateTrackLastId(track);
			 }
			 //asignamos para actualización del track

		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
	 }
	
	 public InputStream getJSONData(String url){
		 DefaultHttpClient httpClient = new DefaultHttpClient();
		 URI uri;
		 InputStream data = null;
		 try {
			 uri = new URI(url);
			 HttpGet method = new HttpGet(uri);
			 HttpResponse response = httpClient.execute(method);
			 data = response.getEntity().getContent();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }

		 return data;
	 }
	 
}
