package com.alcubier.skoltialert.mentions;

import greendroid.app.GDActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.settings.TwitterUtils;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class WriteTweet extends GDActivity {

	String msg;
	EditText myMsg;
	TextView countChars;
	Integer numChars;
	final Integer MAX_LENGH_TWEET = 140; 
	private SharedPreferences prefs;
	int red_color;
	int black_color;
	Context context;
	
	
	final String TAG = getClass().getName();
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.writetweet);
        
        context = this;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();
        red_color = res.getColor(R.color.crimson);
        black_color = res.getColor(R.color.black);
        
		Bundle bundle = this.getIntent().getExtras();
		String type = bundle.getString("type");
		if (type.equals("Citar")) {
			msg = bundle.getString("text");
			numChars = MAX_LENGH_TWEET - msg.length();  
		} else {
			msg = "";
			numChars = MAX_LENGH_TWEET;
		}
		
		
        
        myMsg = (EditText) findViewById(R.id.twitterMsg);
        myMsg.setText(msg);
        countChars = (TextView) findViewById(R.id.numberChars);
        countChars.setText(numChars.toString());

		if (numChars < 0) {
			countChars.setTextColor(red_color);
		} else {
			countChars.setTextColor(black_color);
		}

        
        myMsg.setOnKeyListener(new ContarPalabras());
        
        GoogleAnalyticsTracker.getInstance().trackPageView("WriteTweet");
	}
	
    public class ContarPalabras implements View.OnKeyListener
    {
    	@Override
    	public boolean onKey (View v, int keyCode, KeyEvent event){
    		msg = myMsg.getText().toString();
    		numChars = MAX_LENGH_TWEET - msg.length();
    		countChars.setText(numChars.toString());
    		if (numChars < 0) {
    			countChars.setTextColor(red_color);
    		} else {
    			countChars.setTextColor(black_color);
    		}
    		return false;
    	}	
    }

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void btnAddTweet_Click(View view)
	{
		if (!(prefs.getBoolean("Logged", false))) {
			Tweet.ShowTweetError(this, getText(R.string.please_login_oauth).toString());
		} else {
			msg = myMsg.getText().toString();
			if (msg.length() > MAX_LENGH_TWEET) {
				Tweet.ShowTweetError(this, getText(R.string.tweet_too_long).toString());
			} else {
				new asyncSendTweet().execute();
				Toast.makeText(context, getString(R.string.sending_tweet), Toast.LENGTH_SHORT).show();
				finish();					
			}
		}
	}
	
	public void btnCancelTweet_Click(View view)
	{
		finish();
	}
	
	/** Background Task **/
	private class asyncSendTweet extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... arg0) {
			//...
			try {
				TwitterUtils.sendTweet(prefs,msg);
				return "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return (e.toString());
			}
		}

		@Override /* Background Task is Done */
		protected void onPostExecute(String result) {
			if (result.equals("")) {
				Toast.makeText(context, getString(R.string.tweet_done), Toast.LENGTH_SHORT).show();
			} else	{
				Tweet.ShowTweetError(context, result);
			}
		}
	}

	
}
