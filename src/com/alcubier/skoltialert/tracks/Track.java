package com.alcubier.skoltialert.tracks;

//DTO (data transfer object) de la clase correspondiente 
// a la tabla de tracks (hay una idéntica en el servidor de skolti)

public class Track {
	
	int    _id_track;
	String _search_track;
	String _date_active;
	int    _top_track;
	long   _last_id;
	int    _count;
	
	public Track()
	{
		this._id_track=0;
		this._search_track="";
		this._date_active="";
		this._top_track=0;
		this._last_id=0;
		this._count=0;
	}
	
	public int getIdTrack()
	{
		return this._id_track;
	}
	public void setIdTrack(int id_track)
	{
		this._id_track=id_track;
	}
	
	public String getSearchTrack()
	{
		return this._search_track;
	}

	public void setSearchTrack(String search_track)
	{
		this._search_track=search_track;
	}

	public String getDateActive()
	{
		return this._date_active;
	}

	public void setDateActive(String date_active)
	{
		this._date_active=date_active;
	}

	public int getTopTrack()
	{
		return this._top_track;
	}
	public void setTopTrack(int top_track)
	{
		this._top_track=top_track;
	}
	
	public long getLastId()
	{
		return this._last_id;
	}

	public void setLastId(long last_id)
	{
		this._last_id=last_id;
	}

	public int getCount()
	{
		return this._count;
	}

	public void setCount(int count)
	{
		this._count=count;
	}

}
