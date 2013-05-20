package com.alcubier.skoltialert.mentions;

import greendroid.app.GDActivity;
import greendroid.image.ChainImageProcessor;
import greendroid.image.ImageProcessor;
import greendroid.image.MaskImageProcessor;
import greendroid.image.ScaleImageProcessor;
import greendroid.widget.AsyncImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.settings.TwitterUtils;
import com.alcubier.skoltialert.tracks.DialogListner;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Tweet extends GDActivity {

	final String TAG = getClass().getName();
	private ImageProcessor mImageProcessor;
	Mention mention;
	AsyncImageView imageView;
	TextView hFrom;
	TextView hDate;
	TextView hText;
	private SharedPreferences prefs;
	Date dataMention;
	Date dataNow;
	SimpleDateFormat dateFormat;
	String horaString;
	String diaNow;
	String diaString;
	Pattern pattern;
	String scheme;
	Context context;

	
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.tweet);
        
        context = this;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prepareImageProcessor(this);
		imageView = (AsyncImageView) findViewById(R.id.twitter_image);
		imageView.setImageProcessor(mImageProcessor);
        hFrom = (TextView) findViewById(R.id.viewWhoMention);
        hDate = (TextView) findViewById(R.id.viewDateMention);
        hText = (TextView) findViewById(R.id.viewTxtMention);
        dataNow = new Date();
        
		Bundle bundle = this.getIntent().getExtras();
		mention = new Mention();
		mention.setGifo(bundle.getString("Gifo"));
		mention.setFromUser(bundle.getString("From"));
		mention.setDateMentions(bundle.getString("Date"));
		mention.setText(bundle.getString("Text"));
		mention.setLastId(bundle.getLong("Status"));

		imageView.setUrl(mention.getGifo());
        hFrom.setText(mention.getFromUser());
//      hDate.setText(mention.getDateMentions());
        
        
        dataMention = Utils.String2Date(mention.getDateMentions(),"yyyy-MM-dd HH:mm:ss");
		dateFormat = new SimpleDateFormat("HH:mm");
		horaString = dateFormat.format(dataMention);

		dateFormat = new SimpleDateFormat("dd");
		diaNow = dateFormat.format(dataNow);
		diaString = dateFormat.format(dataMention);
		Integer difer = Integer.parseInt(diaNow) - Integer.parseInt(diaString);
		if (difer==0) {
			hDate.setText(getString(R.string.today_date) + " " + horaString);
		} else {
			if (difer==1) {
				hDate.setText(getString(R.string.yesterday_date) + " " + horaString);
			} else {
				dateFormat = new SimpleDateFormat("dd-MMM HH:mm");
				hDate.setText(dateFormat.format(dataMention));
			}
		}
        hText.setText(mention.getText().replaceAll("&quot;","\\\"").replaceAll("&amp;","&").replaceAll("&gt;",">").replaceAll("&lt;","<"));
        
        Linkify.addLinks(hText, Linkify.ALL);
        
        TransformFilter mentionFilter = new TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };
        
        pattern = Pattern.compile("@([A-Za-z0-9_-]+)");
        scheme = "http://twitter.com/";
        Linkify.addLinks(hText, pattern, scheme, null, mentionFilter);
        
        pattern = Pattern.compile("#([A-Za-z0-9_-]+)");
        scheme = "http://twitter.com/search?q=%23";
        Linkify.addLinks(hText, pattern, scheme, null, mentionFilter);
        
        GoogleAnalyticsTracker.getInstance().trackPageView("Tweet");

	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
    private void prepareImageProcessor(Context context) {
        
        final int thumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
        final int thumbnailRadius = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_radius);

        //REDONDEADO
        //@formatter:off
        mImageProcessor = new ChainImageProcessor(
                new ScaleImageProcessor(thumbnailSize, thumbnailSize, ScaleType.FIT_XY),
                new MaskImageProcessor(thumbnailRadius));
        //@formatter:on
    }
	
	public void btnSendRetweet_Click(View view)
	{
		try
		{
			if (!(prefs.getBoolean("Logged", false))) {
				ShowTweetError(this, getText(R.string.please_login_oauth).toString());
			} else {
				new asyncSendRetweet().execute();
				Toast.makeText(context, getString(R.string.sending_tweet), Toast.LENGTH_SHORT).show();
			}
			
		}
		catch(Exception ex)
		{
			ShowTweetError(this, ex.toString());
		}
	}
	
	public void btnSendCitar_Click(View view)
	{
		String msg;
       	Bundle bundle = new Bundle();
       	bundle.putString("type", "Citar");
       	msg = "RT @"+mention.getFromUser()+": "+mention.getText();
       	bundle.putString("text", msg);
		Intent i = new Intent(this, WriteTweet.class);
		i.putExtras(bundle);
		startActivity(i);
       	
	}

	public void btnSendReply_Click(View view)
	{
		String msg;
       	Bundle bundle = new Bundle();
       	bundle.putString("type", "Citar");
       	msg = "@"+mention.getFromUser()+" ";
       	bundle.putString("text", msg);
		Intent i = new Intent(this, WriteTweet.class);
		i.putExtras(bundle);
		startActivity(i);
       	
	}

	public static void ShowTweetError(Context con, String exception)
	{
		CharSequence text;
		AlertDialog.Builder builder=new AlertDialog.Builder(con);
		text = con.getText(R.string.tweet_error);
		builder.setTitle(text);
		builder.setIcon(R.drawable.gd_action_bar_info);
		DialogListner listner=new DialogListner();
		text = exception;
		builder.setMessage(text);
		builder.setPositiveButton("ok", listner);

		AlertDialog diag=builder.create();
		diag.show();
	}
	
	/** Background Task **/
	private class asyncSendRetweet extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... arg0) {
			//...
			try {
				TwitterUtils.sendRetweet(prefs,mention.getLastId());
				return "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return e.toString();
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
