package com.alcubier.skoltialert.plot;

import java.util.Vector;

import com.androidplot.series.XYSeries;

public class SimpleXYSeries implements XYSeries { 
	private Vector<Double> data;

	public SimpleXYSeries(Vector<Double> data){
		update(data);
	}
	public void update(Vector<Double> data){
		this.data=data;
	}
	@Override
    public Number getX(int index) {
        return data.get(index*2);
    }
   
    @Override
    public String getTitle() {
        return "";
    }
 
    @Override
    public int size() {
        return data.size()/2;
    }
 
    @Override
    public Number getY(int index) {        
        return data.get(index*2+1);
    }
  }