package com.alcubier.skoltialert.settings;

import greendroid.app.GDActivity;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.User;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alcubier.skoltialert.R;

/**
 * Prepares a OAuthConsumer and OAuthProvider 
 * 
 * OAuthConsumer is configured with the consumer key & consumer secret.
 * OAuthProvider is configured with the 3 OAuth endpoints.
 * 
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * 
 * After the request is authorized, a callback is made here.
 * 
 */
public class OAuthPrepReqTokenActivity extends GDActivity {

	final String TAG = getClass().getName();
	private SharedPreferences prefs;
	private boolean logged;
	private boolean logging = false;
	private TextView nameTwitter;
	private Button signTwitter;
	private Button contTwitter;
    private OAuthConsumer consumer; 
    private OAuthProvider provider;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.msettings);
		nameTwitter = (TextView) this.findViewById(R.id.name_twitter);
		signTwitter = (Button) this.findViewById(R.id.login_status);
		contTwitter = (Button) this.findViewById(R.id.skip_status);
		signTwitter.setText(R.string.signin_twitter);
		contTwitter.setText(R.string.cont_twitter);
	}

	@Override
	protected void onResume() {
		super.onResume();
        if (logging) {
        	nameTwitter.setText(getString(R.string.still_loading));
        } else {
        	this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        	logged = prefs.getBoolean("Logged", false);
        	nameTwitter.setText(prefs.getString("twittername", ""));
        }
        
        Log.i("TAG", "On Resume-activity"+prefs.getString("twittername", ""));
//		updateLoginStatus();
	}
	
	public void btnLogUser(View v) {
		if(!logged) { 
			if (!logging) {
				Toast.makeText(this,getString(R.string.not_logged_logging),Toast.LENGTH_SHORT).show();
				try {
					this.consumer = new CommonsHttpOAuthConsumer(OAuthConstants.CONSUMER_KEY, OAuthConstants.CONSUMER_SECRET);
					this.provider = new CommonsHttpOAuthProvider(OAuthConstants.REQUEST_URL,OAuthConstants.ACCESS_URL,OAuthConstants.AUTHORIZE_URL);
				} catch (Exception e) {
					Log.e(TAG, "Error creating consumer / provider",e);
				}

				Log.i(TAG, "Starting task to retrieve request token.");
				new OAuthRequestTokenTask(this,consumer,provider).execute();
				nameTwitter.setText(R.string.now_loading);
				logging = true;
			} else {
				Toast.makeText(this, getString(R.string.logging_please_wait),Toast.LENGTH_LONG).show();
			}
				
		}
		else {
//			Toast.makeText(this, "Clearing Credentials",Toast.LENGTH_SHORT).show();
			clearCredentials();
			logged = false;
			logging = false;
			nameTwitter.setText(R.string.userhint);
        	signTwitter.setText(R.string.signin_twitter);
		}
	}
	
	public void btnSkip(View v) {
		finish();
	}
	
	/**
	 * Called when the OAuthRequestTokenTask finishes (user has authorized the request token).
	 * The callback URL will be intercepted here.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent); 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Uri uri = intent.getData();
		nameTwitter.setText(R.string.still_loading);
		if (uri != null && uri.getScheme().equals(OAuthConstants.OAUTH_CALLBACK_SCHEME)) {
			Log.i(TAG, "Callback received : " + uri);
			Log.i(TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(this,consumer,provider,prefs).execute(uri);
			
		}
	}
		
	private void clearCredentials() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.remove("Logged");
		edit.remove("twittername");
		edit.remove("screeenname");
		edit.commit();
	}
	
	public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, User> {

		final String TAG = getClass().getName();
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		SharedPreferences prefs;
		User user;
		
		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer,OAuthProvider provider, SharedPreferences prefs) {
			
			this.consumer = consumer;
			this.provider = provider;
			this.prefs=prefs;
		}

		/**
		 * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret 
		 * for future API calls.
		 */
		@Override
		protected User doInBackground(Uri...params) {
			final Uri uri = params[0];
			final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			User twitteruser = null;

			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);

				Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.putBoolean("Logged", true);
				edit.commit();
				
				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
				
				consumer.setTokenWithSecret(token, secret);

				twitteruser = TwitterUtils.getCredentials(prefs);
			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
			}
			return twitteruser;
		}
		
		protected void onPostExecute(User twitteruser) {
			Editor edit = prefs.edit();
			Log.i(TAG, "NOMBRE: "+twitteruser.getName());
			Log.i(TAG, "SNOMBRE: "+twitteruser.getScreenName());
			edit.putString("twittername", twitteruser.getName());
			edit.putString("screeenname", twitteruser.getScreenName());
			edit.putString("tprofile", twitteruser.getProfileBackgroundImageUrl());
			edit.commit();
			nameTwitter.setText(twitteruser.getName());
	        logged = true;
	        logging = false;
	        signTwitter.setText(R.string.signoff_twitter);
		}

	}	
	
}
