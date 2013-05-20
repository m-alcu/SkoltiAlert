package com.alcubier.skoltialert.mentions;

import greendroid.app.GDActivity;
import greendroid.app.GDListActivity;
import greendroid.image.ChainImageProcessor;
import greendroid.image.ImageProcessor;
import greendroid.image.MaskImageProcessor;
import greendroid.image.ScaleImageProcessor;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.AsyncImageView;
import greendroid.widget.LoaderActionBarItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.service.RefreshService;
import com.alcubier.skoltialert.tracks.Track;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GDMentionsList extends GDListActivity implements OnScrollListener, OnClickListener {

	Track track;
	final String TAG = getClass().getName();
	private final int REFRESH = 0;
	private final int COMPOSE = 1;
	final int MAX_MENTIONS = 100;
	boolean haymasmentions;
	String type;
	String dateAlertFrom;
	String dateAlertTo;
	DatabaseHelper dbHelper;
	ArrayList<Mention> mentions;
	protected MyAdapter m_adapter;
	static long lastIdMention;
	//the ViewSwitcher
	private ViewSwitcher switcher;
	
	final GDActivity thisActivity = this;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mentions= new ArrayList<Mention>();
        track= new Track();
		Bundle bundle = this.getIntent().getExtras();
		track.setIdTrack(bundle.getInt("idTrack"));
		track.setSearchTrack(bundle.getString("searchTrack"));
		track.setTopTrack(bundle.getInt("topTrack"));
		track.setLastId(bundle.getLong("lastId"));
		type = bundle.getString("type");
		if (!(type.equals("LastMentions"))) {
    		dateAlertFrom = bundle.getString("dateAlertFrom");
    		dateAlertTo = bundle.getString("dateAlertTo");
		}

        
        dbHelper=new DatabaseHelper(this);
        
        lastIdMention = Long.MAX_VALUE;
        mentions = LoadMentions(lastIdMention);
        initListView();
        
  	  	getListView().setOnScrollListener(this);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
  	  	addActionBarItem(Type.Refresh, REFRESH);
  	  	addActionBarItem(Type.Compose, COMPOSE);
  	  	
  	  	setTitle(track.getSearchTrack());
  	  	
  	  	GoogleAnalyticsTracker.getInstance().trackPageView("GDMentionsList");
    }
    
    public void initListView() {

        m_adapter = new MyAdapter(this, R.layout.listrowmention, mentions);
        
        if (haymasmentions) {
        	getListView().removeFooterView(switcher);
        }
        haymasmentions = false;
        if (mentions.size()==MAX_MENTIONS) {
        	//create the ViewSwitcher in the current context
  	  		switcher = new ViewSwitcher(this);
  	  
  	  		//footer Button: see XML1
  	  		Button footer = (Button)View.inflate(this, R.layout.loadmore, null);
  	  
  	  		//progress View: see XML2
  	  		View progress = View.inflate(this, R.layout.progreso, null);
  	  
  	  		//add the views (first added will show first)
  	  		switcher.addView(footer);
  	  		switcher.addView(progress);
  	  
  	  		//add the ViewSwitcher to the footer
  	  		getListView().addFooterView(switcher);
  	  		haymasmentions = true;
        }
        
  	  	setListAdapter(m_adapter);

    }
    
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	dbHelper.close();
    }
    
	@Override /* Load More Button Was Clicked */
	public void onClick(View arg0) {
		
		//Ojo que cuando se ponga alguna otra view que soporte Onclick habrá que discernir entre uno y otro
		
		//first view is showing, show the second progress view
		switcher.showNext();
		//and start background work
		new getMoreItems().execute();
	}
	
    private static class MyAdapter extends ArrayAdapter<Mention> {

    	private ArrayList<Mention> mentions;
        Date dataNow;
        Context context;



        static class ViewHolder {
            public AsyncImageView imageView;
            public TextView hFrom;
            public TextView hText;
            public TextView hDate;
            public LinearLayout hMention;
        }

        private LayoutInflater mInflater;
        private ImageProcessor mImageProcessor;

        public MyAdapter(Context context, int textViewResourceId, ArrayList<Mention> mentions) {
        	super(context, textViewResourceId, mentions);
        	this.mentions = mentions;
        	this.context = context;
        	dataNow = new Date();
        	
            mInflater = LayoutInflater.from(context);
//          mImageForPosition = context.getString(R.string.image_for_position);

            prepareImageProcessor(context);
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

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
    		Date dataMention;
    		SimpleDateFormat dateFormat;
    		String horaString;
    		String diaNow;
    		String diaString;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listrowmention, parent, false);
                holder = new ViewHolder();
                holder.imageView = (AsyncImageView) convertView.findViewById(R.id.async_image);
                holder.imageView.setImageProcessor(mImageProcessor);
                holder.hFrom = (TextView) convertView.findViewById(R.id.colWhoMention);
                holder.hDate = (TextView) convertView.findViewById(R.id.colDateMention);
                holder.hText = (TextView) convertView.findViewById(R.id.colTxtMention);
                holder.hMention = (LinearLayout) convertView.findViewById(R.id.one_mention);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Mention mention = mentions.get(position);
            
            if (lastIdMention > mention.getLastId()) {
            	lastIdMention = mention.getLastId();
            }

            holder.imageView.setUrl(mention.getGifo());
            holder.hFrom.setText(mention.getFromUser());
//          holder.hDate.setText(mention.getDateMentions());
            
            dataMention = Utils.String2Date(mention.getDateMentions(),"yyyy-MM-dd HH:mm:ss");
    		dateFormat = new SimpleDateFormat("HH:mm");
    		horaString = dateFormat.format(dataMention);

    		dateFormat = new SimpleDateFormat("dd");
    		diaNow = dateFormat.format(dataNow);
    		diaString = dateFormat.format(dataMention);
    		Integer difer = Integer.parseInt(diaNow) - Integer.parseInt(diaString);
    		if (difer==0) {
    			holder.hDate.setText(context.getString(R.string.today_date) + " " + horaString);
    		} else {
    			if (difer==1) {
    				holder.hDate.setText(context.getString(R.string.yesterday_date) + " " + horaString);
    			} else {
    				dateFormat = new SimpleDateFormat("dd-MMM HH:mm");
    				holder.hDate.setText(dateFormat.format(dataMention));
    			}
    		}
            
            holder.hText.setText(mention.getText().replaceAll("&quot;","\\\"").replaceAll("&amp;","&").replaceAll("&gt;",">").replaceAll("&lt;","<"));
            holder.hMention.setOnClickListener(new VerTweet(mention));

            return convertView;
        }
        
        public class VerTweet implements View.OnClickListener 
        {
        	
        	Mention mention;
        	
        	public VerTweet ( Mention mention) {
        		this.mention = mention;
        	}
        	
        	public void onClick( View view ){
        		
        		Bundle bundle = new Bundle();
    			//Le ponemos la variable parametro con el contenido test
    			bundle.putInt("idTrack", mention.getIdTrack());
    			bundle.putString("Gifo", mention.getGifo());
    			bundle.putString("From", mention.getFromUser());
    			bundle.putString("Date", mention.getDateMentions());
    			bundle.putString("Text", mention.getText());
    			bundle.putLong("Status", mention.getLastId());
    			Intent i = new Intent(getContext(), Tweet.class);
    			i.putExtras(bundle);
    			getContext().startActivity(i);

            }
        }

    }

    public void onScroll(AbsListView arg0, int firstVisible, int visibleCount, int totalCount) {
    	
//    	boolean loadMore = /* maybe add a padding */
//          firstVisible + visibleCount >= totalCount;

//      if(loadMore) {
//        	Toast.makeText(this, "Hemos llegado al final1",Toast.LENGTH_SHORT).show();
//      }
    }

    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (getListView() == listView) {
            searchAsyncImageViews(listView, scrollState == OnScrollListener.SCROLL_STATE_FLING);
        }
    }

    private void searchAsyncImageViews(ViewGroup viewGroup, boolean pause) {
        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AsyncImageView image = (AsyncImageView) viewGroup.getChildAt(i).findViewById(R.id.async_image);
            if (image != null) {
                image.setPaused(pause);
            }
        }
    }
    
    public ArrayList<Mention> LoadMentions(long NextMention)
    {
    	
    	try
    	{
    		if (type.equals("LastMentions")) {
    			mentions = dbHelper.ListAllMentions(track.getIdTrack(), NextMention, MAX_MENTIONS);
    		} else {
    			mentions = dbHelper.ListAlertMentions(track.getIdTrack(), dateAlertFrom, dateAlertTo, NextMention, MAX_MENTIONS);
    		}
    		return mentions;
    	}
    	catch(Exception ex)
    	{
    		Log.i(TAG,"excepcion loadMentions: "+ex);
    		return null;
    	}
    }

	/** Background Task To Get More Items**/
	private class getMoreItems extends AsyncTask<Void, Void, Object> {
		@Override
		protected Object doInBackground(Void... arg0) {
			//code to add more items
			//...
			if (haymasmentions) {
		        mentions = LoadMentions(lastIdMention);
		    }
			else {
				mentions = null;
			}
			return null;
		}

		@Override /* Background Task is Done */
		protected void onPostExecute(Object result) {
			//go back to the first view
			if (haymasmentions) {
				if(mentions != null && mentions.size() > 0) {
					for(int i=0;i<mentions.size();i++) m_adapter.add(mentions.get(i));
					m_adapter.notifyDataSetChanged();
				}
				if ((mentions != null && mentions.size() < MAX_MENTIONS) || mentions == null) {
					haymasmentions = false;
					getListView().removeFooterView(switcher);
				} else {
					switcher.showPrevious();
				}
			}
                        //update the ListView
		}
	}
	
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    	Bundle bundle; 
    	
    	switch (item.getItemId()) {
                case REFRESH:
//                  new getMoreMentions().execute();
                 	bundle = new Bundle();
                 	bundle.putString("type", "refreshone");
                 	bundle.putString("track", track.getSearchTrack());
                 	Intent refresh = new Intent(this, RefreshService.class);
             		refresh.putExtras(bundle);
             		refresh.putExtra(RefreshService.EXTRA_MESSENGER, new Messenger(handler));
                    startService(refresh);   
                    break;
                case COMPOSE:
                	bundle = new Bundle();
                	bundle.putString("type", "newTweet");
            		Intent i = new Intent(this, WriteTweet.class);
            		i.putExtras(bundle);
            		startActivity(i);
                    break;
              default:
                  return super.onHandleActionBarItemClick(item, position);
         }
     
         return true;
    }
    
	private Handler handler=new Handler() {
		@Override
		public void handleMessage(Message msg) {
			new getMoreMentions().execute();
		}
	};

	/** Background Task To Get More Items**/
	private class getMoreMentions extends AsyncTask<Void, Void, Object> {
		
		@Override
		protected Object doInBackground(Void... arg0) {
			//code to add more items
			//...
			lastIdMention = Long.MAX_VALUE;
			mentions = LoadMentions(lastIdMention);
			return null;
		}

		@Override /* Background Task is Done */
		protected void onPostExecute(Object result) {
			//go back to the first view
			initListView();
			m_adapter.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), getString(R.string.download_done), Toast.LENGTH_SHORT).show();
			((LoaderActionBarItem) getActionBar().getItem(REFRESH)).setLoading(false);
            //update the ListView
		}
	}

	
}

