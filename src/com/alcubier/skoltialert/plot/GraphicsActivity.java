package com.alcubier.skoltialert.plot;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alcubier.skoltialert.Preferences;
import com.alcubier.skoltialert.R;
import com.alcubier.skoltialert.alerts.Alert;
import com.alcubier.skoltialert.db.DatabaseHelper;
import com.alcubier.skoltialert.mentions.GDMentionsList;
import com.alcubier.skoltialert.mentions.Utils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.RectRegion;
import com.androidplot.xy.XLayoutStyle;
import com.androidplot.xy.XPositionMetric;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYRegionFormatter;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YValueMarker;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GraphicsActivity extends GDActivity implements OnTouchListener {
	private final int SETTINGS = 2;
	private float CHAR_HHMM = 3600000*24;	//1 day
	private float CHAR_DDHH = 3600000*24*7;	//1 week
	private XYPlot mySimpleXYPlot;
	private HorizontalScrollView myScrollView;
	private SimpleXYSeries mySeries;
	private PointF minXY;
	private PointF maxXY;
	private float absMinX;
	private float absMaxX;
	private float minNoError;
	private float maxNoError;
	private double minDif;
	private DatabaseHelper dbHelper;
	private LineAndPointFormatter lpFormatter1;
	private long firstTime;
	private Float floatTime;
	private ArrayList<Alert> alerts;
	static XmlResourceParser xpp;
	static Resources res;
	Drawable d;
	View view;
	LayoutInflater inflater;
	final String TAG = getClass().getName();
	static final float WIDTH_ALERT_DP = 80f;
	int mWidthAlert;
	Integer desplaza;
	
    static class DataItem
    {
    	Integer    	 _idTrack;
        String       _dateAlertFrom;
        String       _dateAlertTo;
    }    
      
	
	final private double difPadding = 0.1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.alertchart);
		addActionBarItem(Type.Settings, SETTINGS);
		
		final float scale = getResources().getDisplayMetrics().density;
		mWidthAlert = (int) (WIDTH_ALERT_DP * scale + 0.5f);
		
		Bundle bundle = this.getIntent().getExtras();
		Integer idTrack = bundle.getInt("idTrack");
		Integer top = bundle.getInt("top");
		String search = bundle.getString("search");
		
		myScrollView = (HorizontalScrollView) findViewById(R.id.myScrollView);
		
		mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		mySimpleXYPlot.setOnTouchListener(this);
		
		mySimpleXYPlot.setTitle(search);
		//Plot layout configurations
		mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(1);
		mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(1);
		mySimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("####.#"));
//		mySimpleXYPlot.getGraphWidget().setDomainValueFormat(
//				new DecimalFormat("####"));
		mySimpleXYPlot.getGraphWidget().setDomainValueFormat(
				new MyDateFormat());
		mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
		mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getLegendWidget());
		mySimpleXYPlot.setRangeLabel(getString(R.string.mentions_hour));
		mySimpleXYPlot.setDomainLabel("hours");
		
		//Estilo del marco exterior (puede ser redondeado)
		mySimpleXYPlot.setBorderStyle(XYPlot.BorderStyle.SQUARE, null, null);
		//Margenes
		mySimpleXYPlot.setPlotMargins(0, 0, 0, 0);
		//Margen interior (entre la grafica y leyendas y el marco)
		mySimpleXYPlot.setPlotPadding(10, 10, 10, 10);
		//Margen Superior interna grafica (no está la ordenada)
		mySimpleXYPlot.getGraphWidget().setMarginTop(10);
		//Margen Derecho interno grafica (no está la abcisa)
		mySimpleXYPlot.getGraphWidget().setMarginRight(15);
		//Para que no autoconfigure los parametros que le hemos marcado
		mySimpleXYPlot.disableAllMarkup();
		
		
		//Creation of the series
//		final Vector<Double> vector = new Vector<Double>();
//		for (double x = 0.0; x < Math.PI * 5; x += Math.PI / 20) {
//			vector.add(x);
//			vector.add(Math.sin(x));
//		}
		dbHelper=new DatabaseHelper(this);
		final Vector<Double> vector = dbHelper.VectorAllAlerts(idTrack);
		alerts = dbHelper.ListAllAlerts(idTrack, top);
		dbHelper.close();
		fillHorizontalScrollView(this);
		
		firstTime = vector.get(0).longValue();	//firstposition we get time initial 
		vector.setElementAt((double) 0, 0);		//after saved put 0 for the graph correct plotted
		
		mySeries = new SimpleXYSeries(vector);
		
		lpFormatter1 = new LineAndPointFormatter(Color.rgb(0, 0, 255), Color.rgb(127, 127, 255), Color.TRANSPARENT);
		
		//colors: (line, vertex, fill)
		mySimpleXYPlot.addSeries(mySeries, LineAndPointRenderer.class,lpFormatter1);
		
 		mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, 8);

		XYRegionFormatter regionFormatter = new XYRegionFormatter(Color.argb(100, 0, 0, 100));
		lpFormatter1.addRegion(new RectRegion(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, top.doubleValue(), "R1"), regionFormatter);
		XYRegionFormatter regionFormatter2 = new XYRegionFormatter(Color.argb(100, 204, 102, 51));
		lpFormatter1.addRegion(new RectRegion(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, top.doubleValue(), Double.POSITIVE_INFINITY, "R2"), regionFormatter2);
		
        // the comprehensive way:
		mySimpleXYPlot.addMarker(new YValueMarker(
                top.doubleValue(),                          // y-val to mark
                top.toString(),                             // marker label
                new XPositionMetric(                        // object instance to set text positioning on the marker
                        3,                                  // 3 pixel positioning offset
                        XLayoutStyle.ABSOLUTE_FROM_LEFT     // how/where the positioning offset is applied
                ),
                Color.rgb(153, 0, 102),                          // line paint color
                Color.rgb(153, 0, 102)                           // text paint color
        ));
		
		mySimpleXYPlot.calculateMinMaxVals();
		floatTime = mySimpleXYPlot.getCalculatedMaxX().floatValue() - mySimpleXYPlot.getCalculatedMinX().floatValue();
    	if (floatTime <= CHAR_HHMM) {
    		mySimpleXYPlot.setDomainLabel("hh:mm");
    	} else if (floatTime <= CHAR_DDHH) {
    		mySimpleXYPlot.setDomainLabel("dd.hh");
    	} else mySimpleXYPlot.setDomainLabel("dd MMM");

		//Enact all changes
    	getUltimaAlert();
		mySimpleXYPlot.redraw();
		
		
		//Set of internal variables for keeping track of the boundaries
		mySimpleXYPlot.calculateMinMaxVals();
		minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),
				mySimpleXYPlot.getCalculatedMinY().floatValue()); //initial minimum data point
		absMinX = minXY.x; //absolute minimum data point
		//absolute minimum value for the domain boundary maximum
		minNoError = Math.round(mySeries.getX(1).floatValue() + 2);
		maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),
				mySimpleXYPlot.getCalculatedMaxY().floatValue()); //initial maximum data point
		absMaxX = maxXY.x; //absolute maximum data point
		//absolute maximum value for the domain boundary minimum
		maxNoError = (float) Math.round(mySeries.getX(mySeries.size() - 1).floatValue()) - 2;
		
		//Check x data to find the minimum difference between two neighboring domain values
		//Will use to prevent zooming further in than this distance
		double temp1 = mySeries.getX(0).doubleValue();
		double temp2 = mySeries.getX(1).doubleValue();
		double temp3;
		double thisDif;
		minDif = 1000000;	//increase if necessary for domain values
		for (int i = 2; i < mySeries.size(); i++) {
			temp3 = mySeries.getX(i).doubleValue();
			thisDif = Math.abs(temp1 - temp3);
			if (thisDif < minDif)
				minDif = thisDif;
			temp1 = temp2;
			temp2 = temp3;
		}
		minDif = minDif + difPadding; //with padding, the minimum difference
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        GoogleAnalyticsTracker.getInstance().dispatch();
        GoogleAnalyticsTracker.getInstance().stop();
    }
    
	
	// Definition of the touch states
	static final private int NONE = 0;
	static final private int ONE_FINGER_DRAG = 1;
	static final private int TWO_FINGERS_DRAG = 2;
	private int mode = NONE;
	
	private PointF firstFinger;
	private float lastScrolling;
	private float distBetweenFingers;
	private float lastZooming;
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: // Start gesture
				firstFinger = new PointF(event.getX(), event.getY());
				mode = ONE_FINGER_DRAG;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
//				myScrollView.smoothScrollTo(5, 0);
				//When the gesture ends, a thread is created to give inertia to the scrolling and zoom 
				final Timer t = new Timer();
				t.schedule(new TimerTask() {
					@Override
					public void run() {
						while(Math.abs(lastScrolling) > 1f || Math.abs(lastZooming - 1) < 1.01) {
							lastScrolling *= .8;	//speed of scrolling damping
							scroll(lastScrolling);
							lastZooming += (1 - lastZooming) * .2;	//speed of zooming damping
							zoom(lastZooming);
							checkBoundaries();
							try {
								floatTime = maxXY.x - minXY.x;
						    	if (floatTime <= CHAR_HHMM) {
						    		mySimpleXYPlot.setDomainLabel("hh:mm");
						    	} else if (floatTime <= CHAR_DDHH) {
						    		mySimpleXYPlot.setDomainLabel("dd.hh");
						    	} else mySimpleXYPlot.setDomainLabel("dd MMM");
								mySimpleXYPlot.postRedraw();
								
								
							} catch (final InterruptedException e) {
								e.printStackTrace();
							}
							// the thread lives until the scrolling and zooming are imperceptible
						}
					}
				}, 0);
				
			case MotionEvent.ACTION_POINTER_DOWN: // second finger
				distBetweenFingers = spacing(event);
				// the distance check is done to avoid false alarms
				if (distBetweenFingers > 5f)
					mode = TWO_FINGERS_DRAG;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == ONE_FINGER_DRAG) {
					final PointF oldFirstFinger = firstFinger;
					firstFinger = new PointF(event.getX(), event.getY());
					lastScrolling = oldFirstFinger.x - firstFinger.x;
					scroll(lastScrolling);
					lastZooming = (firstFinger.y - oldFirstFinger.y) / mySimpleXYPlot.getHeight();
					if (lastZooming < 0)
						lastZooming = 1 / (1 - lastZooming);
					else
						lastZooming += 1;
					zoom(lastZooming);
					checkBoundaries();
					floatTime = maxXY.x - minXY.x;
			    	if (floatTime <= CHAR_HHMM) {
			    		mySimpleXYPlot.setDomainLabel("hh:mm");
			    	} else if (floatTime <= CHAR_DDHH) {
			    		mySimpleXYPlot.setDomainLabel("dd.hh");
			    	} else mySimpleXYPlot.setDomainLabel("dd MMM");
			    	getUltimaAlert();
					mySimpleXYPlot.redraw();
					
					
				} else if (mode == TWO_FINGERS_DRAG) {
					final float oldDist = distBetweenFingers;
					distBetweenFingers = spacing(event);
					lastZooming = oldDist / distBetweenFingers;
					zoom(lastZooming);
					checkBoundaries();
					floatTime = maxXY.x - minXY.x;
			    	if (floatTime <= CHAR_HHMM) {
			    		mySimpleXYPlot.setDomainLabel("hh:mm");
			    	} else if (floatTime <= CHAR_DDHH) {
			    		mySimpleXYPlot.setDomainLabel("dd.hh");
			    	} else mySimpleXYPlot.setDomainLabel("dd MMM");
			    	getUltimaAlert();
					mySimpleXYPlot.redraw();
					
				}
				break;
		}
		return true;
	}
	
	private void zoom(float scale) {
		final float domainSpan = maxXY.x - minXY.x;
		final float domainMidPoint = maxXY.x - domainSpan / 2.0f;
		final float offset = domainSpan * scale / 2.0f;
		minXY.x = domainMidPoint - offset;
		maxXY.x = domainMidPoint + offset;
	}
	
	private void scroll(float pan) {
		final float domainSpan = maxXY.x - minXY.x;
		final float step = domainSpan / mySimpleXYPlot.getWidth();
		final float offset = pan * step;
		minXY.x += offset;
		maxXY.x += offset;
	}
	
	private float spacing(MotionEvent event) {
		final float x = event.getX(0) - event.getX(1);
		final float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	private void checkBoundaries() {
		//Make sure the proposed domain boundaries will not cause plotting issues
		if (minXY.x < absMinX)
			minXY.x = absMinX;
		else if (minXY.x > maxNoError)
			minXY.x = maxNoError;
		if (maxXY.x > absMaxX)
			maxXY.x = absMaxX;
		else if (maxXY.x < minNoError)
			maxXY.x = minNoError;
		if (maxXY.x - minXY.x < minDif)
			maxXY.x = maxXY.x + (float) (minDif - (maxXY.x - minXY.x));
		mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.AUTO);
	}
	
    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
         switch (item.getItemId()) {
              case SETTINGS:
            	  startActivity(new Intent(this, Preferences.class));
                  break;
     
              default:
                  return super.onHandleActionBarItemClick(item, position);
         }
     
         return true;
    }
	
	private void getUltimaAlert() {
		
		Date data = new Date(mySimpleXYPlot.getCalculatedMaxX().longValue()+firstTime);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(data);
		Integer pos = 0;
		for (Alert currentAlert : alerts) {
			if (dateString.compareTo(currentAlert.getDateAlertTo()) > 0) {
				pos = pos + 1;
			} else break;
		}
//		Log.i("alcu3", "posicion1: "+pos.toString());
		Integer ofset = mWidthAlert*(pos-4);
		if (ofset < 0) ofset = 0;
//		Log.i("alcu3", "pixels1: "+ofset.toString());
		myScrollView.smoothScrollTo(ofset, 0);
	}
        
	private void fillHorizontalScrollView(Context context) {

		LinearLayout layout = (LinearLayout)findViewById(R.id.id_linearlayout);
		for (Alert currentAlert : alerts) {
			addScrollView(context, currentAlert, layout);
		}
	}

	private void addScrollView(Context context, Alert currentAlert, LinearLayout layout) {

		Date dataTo;
		SimpleDateFormat dateFormat;
		String dateString;
		TextView txt_top;
		TextView txt_mid;
		TextView txt_bottom;
		Integer count;
		DataItem data2;
		
		data2 = new DataItem();
        data2._idTrack = currentAlert.getIdTrack();
        data2._dateAlertFrom = currentAlert.getDateAlertFrom();
        data2._dateAlertTo = currentAlert.getDateAlertTo();
        Resources res = getResources();
        int ink_color = res.getColor(R.color.ivory_2);

    	dataTo = Utils.String2Date(currentAlert.getDateAlertTo(),"yyyy-MM-dd HH:mm:ss");
        
   		dateFormat = new SimpleDateFormat("dd-MMM");
   		dateString = dateFormat.format(dataTo);
        
        txt_top = new TextView(this);
   		txt_top.setText(dateString);

   		txt_top.setTextSize(12);
		txt_top.setGravity(0x01);
		txt_top.setTextColor(ink_color);

		txt_mid = new TextView(this);
		count = currentAlert.getCount();
		txt_mid.setText(count.toString());
		txt_mid.setTextSize(16);
		txt_mid.setGravity(0x01);
		txt_mid.setTextColor(ink_color);

		dateFormat = new SimpleDateFormat("HH:mm");
		dateString = dateFormat.format(dataTo);
		
		txt_bottom = new TextView(this);
		txt_bottom.setText(dateString);
		
		txt_bottom.setTextSize(12);
		txt_bottom.setGravity(0x01);
		txt_bottom.setTextColor(ink_color);
		 
		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		view = inflater.inflate(R.layout.onealert, null);
		view.setTag(data2);
		view.setBackgroundDrawable(res.getDrawable(R.drawable.text_selector));			
		view.setOnClickListener (new Alerts2Clickhandler());
		
		((ViewGroup) view).addView(txt_top);
		((ViewGroup) view).addView(txt_mid);
		((ViewGroup) view).addView(txt_bottom);
	    layout.addView(view);
	}
	
	private class MyDateFormat extends Format {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		// create a simple date format that draws on the year portion of our timestamp.
        // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
        // for a full description of SimpleDateFormat.
        private SimpleDateFormat dateFormat;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

        	if (floatTime <= CHAR_HHMM) {
        		dateFormat = new SimpleDateFormat("HH:mm");
        	} else if (floatTime <= CHAR_DDHH) {
        		dateFormat = new SimpleDateFormat("dd.HH");
        	} else dateFormat = new SimpleDateFormat("dd MMM");
        	
            long timestamp = ((Number) obj).longValue() + firstTime;
            Date date = new Date(timestamp);
            StringBuffer hora = dateFormat.format(date, toAppendTo, pos);
//          hora.append("h");
            return hora; 
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;

        }
    }

    public class Alerts2Clickhandler implements View.OnClickListener 
    {
    	public void onClick( View view ){

            Log.i(TAG, "...clicked view is...."+view);
            DataItem data = new DataItem();
            data = (DataItem) view.getTag();

    		Bundle bundle = new Bundle();
			//Le ponemos la variable parametro con el contenido test
    		Log.i("TAG", "Track: "+data._idTrack);
			bundle.putInt("idTrack", (int)data._idTrack);
			bundle.putString("dateAlertFrom", data._dateAlertFrom);
			bundle.putString("dateAlertTo", data._dateAlertTo);
			bundle.putString("type", "AlertMentions");
			Intent i = new Intent(GraphicsActivity.this, GDMentionsList.class);
			i.putExtras(bundle);
			startActivity(i);
    		
    	}	
    }
	
}//plotActivity