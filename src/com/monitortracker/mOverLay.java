package com.monitortracker;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Environment;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class mOverLay  extends Overlay {

	/**
	 * Stored as global instances as one time initialization is enough
	 */
    private Bitmap mBubbleIcon, mShadowIcon;
    
    private Bitmap mNowIcon;
    
    private addgpsrange mLocationViewers;
    
    private Paint	mInnerPaint, mBorderPaint, mTextPaint;
    
    private List<GeoPoint> gp;
	
    private int infoWindowOffsetX;
    private int infoWindowOffsetY;
    
    private boolean showWinInfo;
    
    private boolean ReadyShowRange;

    private boolean ShowTracker;    
        
    private ArrayList<GeoPoint> tracker; 

    private int mRadius=6;
    
	/**
	 * It is used to track the visibility of information window and clicked location is known location or not 
	 * of the currently selected Map Location
	 */
    
  //建構子, 初始化
	public mOverLay(addgpsrange mLocationViewers) {
		
		this.mLocationViewers = mLocationViewers;
		
		mBubbleIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.bubble);
		mShadowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.shadow);
		mNowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.mappin_blue);
		showWinInfo = false;
		
		gp = new ArrayList<GeoPoint>();
		tracker = new ArrayList<GeoPoint>();
		ReadyShowRange = false;
		ShowTracker = false;
	}
	
	public void setTracker(boolean st)
	{
	  ShowTracker = st;
	}

  public boolean addGeoPoint(GeoPoint p)
  {
      if (p != null)
      {
        tracker.add(p);
        return true;
      }     
      
      return false;
  }

  public GeoPoint getGeoPoint(int index)
  {
      return tracker.get(index);
  }

	@Override
  //處理draw map上的圖案
	public boolean onTap(GeoPoint p, MapView mapView)  {
		

		Log.i("TAG", "onTap");
		
		//看現在記了幾點
		int newPointSize = gp.size();

		//若小於兩點
		if (newPointSize < 2)
		{
		 
		  //加入
		  gp.add(p);

		  //若為2代表輸入完了, 顯示座標
		  if (gp.size() == 2)
	    {
		    //TopLeft
        double Tlplat = gp.get(0).getLatitudeE6()/ 1E6;
        double Tlplon = gp.get(0).getLongitudeE6()/ 1E6;
		    
		    //BottomRight
        double Brplat = gp.get(1).getLatitudeE6()/ 1E6;
        double Brplon = gp.get(1).getLongitudeE6()/ 1E6;
		    
        double Trplat = Brplat;
        double Trplon = Tlplon;
        double Blplat = Tlplat;
        double Blplon = Brplon;

        gp.add(new GeoPoint((int)(Trplat * 1e6),
            (int)(Trplon * 1e6)));
        gp.add(new GeoPoint((int)(Blplat * 1e6),
            (int)(Blplon * 1e6)));
		    
        String str = Tlplat + "," + Tlplon + "," + Trplat + "," + Trplon + "," + Blplat + "," + Blplon + "," + Brplat + "," + Brplon;
		    Log.v("LAT/LONG", str);
		    ReadyShowRange=true;
		    //傳送給Tracker
		    mLocationViewers.gpsrange = str;
	    }
		  
		}

		/**
		 *   Return true if we handled this onTap()
		 */
		return true;
	}
	
  @Override
  //draw method  
	public void draw(Canvas canvas, MapView	mapView, boolean shadow) 
  {
      //畫現在位置
      drawNowGeoMap(canvas, mapView, shadow);
      //畫地圖座標
   		drawMapLocations(canvas, mapView, shadow);
      //畫地圖GPS Range座標
   		drawPointRange(canvas, mapView, shadow);
   		//畫tracker
   		drawTracker(canvas, mapView, shadow);
   		//drawInfoWindow(canvas, mapView, shadow);
  }
    
  //清除GPS Range座標
  public void clearRange()
  {
    ReadyShowRange = false;
    gp.clear();
  }


  //draw range
  private void drawPointRange(Canvas canvas, MapView mapView, boolean shadow) 
  {
      //若要求顯示才顯示
      if (ReadyShowRange == true)
      {
        Paint paint = new Paint();
        Point myScreenCoords1 = new Point();
        Point myScreenCoords2 = new Point();
        Point myScreenCoords3 = new Point();
        Point myScreenCoords4 = new Point();
        
        //calculate
        GeoPoint top_left = gp.get(0);        
        GeoPoint top_right =  gp.get(2);
        GeoPoint bottom_left =  gp.get(3);
        GeoPoint buttom_right = gp.get(1);        
        
        //將座標顯示上去
        mapView.getProjection().toPixels(top_left, myScreenCoords1);
        mapView.getProjection().toPixels(top_right, myScreenCoords2);
        mapView.getProjection().toPixels(bottom_left, myScreenCoords3);
        mapView.getProjection().toPixels(buttom_right, myScreenCoords4);
        paint.setStrokeWidth(2);
        paint.setARGB(255, 255, 0, 0);

        canvas.drawLine((float) myScreenCoords1.x, (float) myScreenCoords1.y, (float) myScreenCoords2.x,
            (float) myScreenCoords2.y, paint);
        canvas.drawLine((float) myScreenCoords1.x, (float) myScreenCoords1.y, (float) myScreenCoords3.x,
            (float) myScreenCoords3.y, paint);
        canvas.drawLine((float) myScreenCoords2.x, (float) myScreenCoords2.y, (float) myScreenCoords4.x,
            (float) myScreenCoords4.y, paint);
        canvas.drawLine((float) myScreenCoords3.x, (float) myScreenCoords3.y, (float) myScreenCoords4.x,
            (float) myScreenCoords4.y, paint);
  
      }
    }
    
    
    private void drawNowGeoMap(Canvas canvas, MapView mapView, boolean shadow) 
    {
      //顯示現在位置
      if (mLocationViewers.nowGeoPoint != null)
      {
        Paint paint = new Paint();
        Point myScreenCoords = new Point();
        
        //顯示現在位置  
        mapView.getProjection().toPixels(mLocationViewers.nowGeoPoint, myScreenCoords);
        paint.setStrokeWidth(1);
        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
  
        canvas.drawBitmap(mNowIcon, myScreenCoords.x, myScreenCoords.y, paint);
        canvas.drawText("現在位置", myScreenCoords.x, myScreenCoords.y, paint);
      }
    }
    
    private void drawMapLocations(Canvas canvas, MapView	mapView, boolean shadow) {
    	
		Iterator<GeoPoint> iterator = gp.iterator();
		Point screenCoords = new Point();
    	while(iterator.hasNext()) {	   
    	  GeoPoint location = iterator.next();
    		mapView.getProjection().toPixels(location, screenCoords);
			
	    	if (shadow) {
	    		// Offset the shadow in the y axis as it is angled, so the base is at x=0
	    		canvas.drawBitmap(mShadowIcon, screenCoords.x, screenCoords.y - mShadowIcon.getHeight(),null);
	    	} else {
    			canvas.drawBitmap(mBubbleIcon, screenCoords.x - mBubbleIcon.getWidth()/2, screenCoords.y - mBubbleIcon.getHeight(),null);
	    	}
    	}
    }
    
    private void drawTracker(Canvas canvas, MapView mapView, boolean shadow)
    {
      if (tracker.size() == 0 ) return;
      else if (ShowTracker == false) return;
      
      if (shadow == false) 
      {      
        Projection projection = mapView.getProjection();
        
        Paint paint = new Paint(); 
        paint.setAntiAlias(true); 
        paint.setColor(Color.BLACK);
        
        //Start
        Point spoint = new Point(); 
        projection.toPixels(tracker.get(0), spoint);
        RectF startoval = new RectF(spoint.x - mRadius, spoint.y - mRadius,  
                               spoint.x + mRadius, spoint.y + mRadius); 
        canvas.drawOval(startoval, paint);
        
        //middle ~ end
          for (int i=1; i<tracker.size()-1; i++)
          {
            Point srcpoint = new Point(); 
            projection.toPixels(tracker.get(i), srcpoint);
            Point dstpoint = new Point(); 
            projection.toPixels(tracker.get(i+1), dstpoint);
  
            paint.setStrokeWidth(5);
            paint.setAlpha(120);
            canvas.drawLine(srcpoint.x, srcpoint.y, dstpoint.x, dstpoint.y, paint);
          }
        
          //EndPoint
          Point endpoint = new Point(); 
          projection.toPixels(tracker.get(tracker.size()-1), endpoint);
          RectF endoval = new RectF(endpoint.x - mRadius, endpoint.y - mRadius,  
                            endpoint.x + mRadius, endpoint.y + mRadius); 
          paint.setAlpha(255);
          canvas.drawOval(endoval, paint);
        }      
    }

	public Paint getmInnerPaint() {
		if ( mInnerPaint == null) {
			mInnerPaint = new Paint();
			mInnerPaint.setARGB(225, 50, 50, 50); //inner color
			mInnerPaint.setAntiAlias(true);
		}
		return mInnerPaint;
	}

	public Paint getmBorderPaint() {
		if ( mBorderPaint == null) {
			mBorderPaint = new Paint();
			mBorderPaint.setARGB(255, 255, 255, 255);
			mBorderPaint.setAntiAlias(true);
			mBorderPaint.setStyle(Style.STROKE);
			mBorderPaint.setStrokeWidth(2);
		}
		return mBorderPaint;
	}

	public Paint getmTextPaint() {
		if ( mTextPaint == null) {
			mTextPaint = new Paint();
			mTextPaint.setARGB(255, 255, 255, 255);
			mTextPaint.setAntiAlias(true);
		}
		return mTextPaint;
	}

	//設定GPS Range座標
	public void SetPoint(GeoPoint G1, GeoPoint G2, GeoPoint G3, GeoPoint G4)
	{
	  gp.clear();
	  
    gp.add(G1);
    gp.add(G2);
    gp.add(G3);
    gp.add(G4);
    
    ReadyShowRange = true;
	}
}