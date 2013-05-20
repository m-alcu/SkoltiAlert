package com.alcubier.skoltialert.mentions;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.util.Log;

public class TwitterMention {

	final String TAG = getClass().getName();
	private String profile_image_url;
	private String created_at;
	private String from_user;
	private String text;
	private long id;
	private String iso_language_code;

    public String getProfileImageUrl() {
        return profile_image_url;
    }
    public void setProfileImageUrl(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }
	
    public String getCreatedAt() {
        return created_at;
    }
    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }
    
    public String getFromUser() {
        return from_user;
    }
    public void setFromUser(String from_user) {
        this.from_user = from_user;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getIsoLanguageCode() {
        return iso_language_code;
    }
    public void setIsoLanguageCode(String iso_language_code) {
        this.iso_language_code = iso_language_code;
    }

    public Date getFechaTweet( DateFormat formatter) {
    	
    	Date dataParsed;
		try {
			dataParsed = (Date)formatter.parse(created_at);
		} catch (ParseException e) {
			 Date data = new Date(); 
			 Log.i(TAG, " Error parse:" + created_at);
			 return data;
			 
			// TODO Auto-generated catch block
		}
		return dataParsed;
    }
    
}
