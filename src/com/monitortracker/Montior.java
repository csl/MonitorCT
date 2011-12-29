package com.monitortracker;

//import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List; 
import java.util.Locale; 
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent; 
import android.graphics.Color;
//import android.graphics.drawable.Drawable;
import android.location.Address; 
import android.location.Criteria; 
import android.location.Geocoder; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.os.Bundle; 
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import com.google.android.maps.GeoPoint; 
//import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController; 
import com.google.android.maps.MapView; 
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;

public abstract class Montior extends MapActivity 
{
  private String TAG = "Monitor";
  
  private static final int MSG_DIALOG_SAFE = 1;  
  private static final int MSG_DIALOG_OVERRANGE = 2;
  private static final int MSG_DIALOG_SETS = 3;
  
  private static final int MENU_MANAGE = Menu.FIRST  ;
  private static final int MENU_EXIT = Menu.FIRST +1 ;

  //private TextView mTextView01;
  public Montior mMontior = this;
  private Timer timer;
  
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MyOverLay overlay;
  private List<MapLocation> mapLocations;

  private Button mButton01,mButton02,mButton03,mButton04;
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public static GeoPoint nowGeoPoint; 
  public String IPAddress;
  
  private TextView tlogin, tpwd;
  private EditText login, pwd;
  
  private String saccount, spwd;  
  
  public static  MapLocation mSelectedMapLocation;  
  
  public GeoPoint top_left;        
  public GeoPoint top_right;
  public GeoPoint bottom_left;
  public GeoPoint bottom_right;   
  public boolean mshow;
  public int port;
  public TextView label;
  public String oldGPSRangeData;

  private int mchildid=0;
  private medplayer mp;
  static private int display = 0;
  
  private ProgressDialog myDialog;
  private LoginXMLStruct data;
  private CSXMLStruct csdata;
  static public ArrayList<ChildStruct> childlist;
  
  private static volatile AtomicBoolean processing = new AtomicBoolean(false);
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 

    IPAddress = (String) this.getResources().getText(R.string.url);
    oldGPSRangeData = "";

    timer = new Timer();
    mp = null;
    childlist = null;
    mchildid = -1;
    
    mMontior = this;
    
    //googleMAP
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 

    //訊息顯示
    label = (TextView) findViewById(R.id.cstaus);
    
    //參數設定 
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 18; 
    mMapController01.setZoom(intZoomLevel); 

    //顯示輸入IP的windows
    if (display != 1)
    {
      AlertDialog.Builder alert = new AlertDialog.Builder(mMontior);

      alert.setTitle("登入login");
      alert.setMessage("請輸入帳號 和 密碼");
      ScrollView sv = new ScrollView(this);
      LinearLayout ll = new LinearLayout(this);
      ll.setOrientation(LinearLayout.VERTICAL);
      sv.addView(ll);

      tlogin = new TextView(this);
      tlogin.setText("帳號: ");
      login = new EditText(this);      
      login.setText("test");
      ll.addView(tlogin);
      ll.addView(login);

      tpwd = new TextView(this);
      tlogin.setText("密碼: ");
      pwd = new EditText(this);
      pwd.setText("test");
      ll.addView(tpwd);
      ll.addView(pwd);
      
      // Set an EditText view to get user input 
      alert.setView(sv);
      
      alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) 
      {
        saccount = login.getText().toString();
        spwd = pwd.getText().toString();
        
        if (saccount.equals("") || spwd.equals(""))
        {
            openOptionsDialog("null");
            return;
        }

        //Progress
        myDialog = ProgressDialog.show
        (
            Montior.this,
            "login",
            "...",
            true
        );
        
        new Thread()
        {
          public void run()
          {
            
            String uriAPI = IPAddress + "login.php?username=" + saccount + "&pwd=" + spwd;
            
            URL url = null;
            try{
              url = new URL(uriAPI);
              
              SAXParserFactory spf = SAXParserFactory.newInstance();
              SAXParser sp = spf.newSAXParser();
              XMLReader xr = sp.getXMLReader();
              //Using login handler for xml
              LoginXMLHandler myHandler = new LoginXMLHandler();
              xr.setContentHandler(myHandler);
              //open connection
              xr.parse(new InputSource(url.openStream()));
              //verify OK
              data = myHandler.getParsedData();
            }
            catch(Exception e){
              e.printStackTrace();
              return;
            }
            finally
            {
              myDialog.dismiss();
              
              try {
              
                   if (data.h_chilid.equals(""))
                   {
                     Log.i("ERROR", "LOGINFAIL");
                     finish();
                   }
                   else
                   {
                     if (getChildList() != null)
                     {
                      //error
                      if (childlist == null)
                      {
                        return;
                      }          
                      
                      ArrayList<Integer> ChildData = new  ArrayList<Integer>();
                      StringTokenizer Tok = new StringTokenizer(data.h_chilid, ",");
                      while (Tok.hasMoreElements())
                      {
                        ChildData.add( Integer.valueOf((String) Tok.nextElement()) );
                      }
                      
                      final CharSequence[] child_id = new String[ChildData.size()];
                      int checked = 0;
                      
                      for(int i = 0 ;i<ChildData.size(); i++)
                      {
                        child_id[i] = childlist.get(ChildData.get(i)).name; 
                      }
                       
                       AlertDialog.Builder builder = new AlertDialog.Builder(mMontior);
                       builder.setTitle("選擇監控小孩");  
                        //builder.setCancelable(false);
                       
                       builder.setSingleChoiceItems(child_id, checked, new DialogInterface.OnClickListener() { 
                         public void onClick(DialogInterface dialog, int which) 
                         {
                           mchildid = which;
                         } 
                      }); 
                       
                       builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
                         public void onClick(DialogInterface dialog, int which) 
                         {
                           timer.schedule(new DateTask(), 0, 5000);                        
                         } 
                      }); 
                        
                      AlertDialog alert = builder.create();  
                      alert.show();
                     }
                   }
               }
              catch (Exception err)
              {
                     err.printStackTrace();
              }
             }                 
           }
         }.start();                               
      }
      });
    
      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) 
          {
            finish();
          }
        });
    
        alert.show();      
    }
        
    //建構畫在GoogleMap的overlay
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    
    nowGeoPoint = new GeoPoint((int) (24.070801 * 1000000),(int) (120.715486 * 1000000));

    refreshMapViewByGeoPoint(nowGeoPoint, 
        mMapView, intZoomLevel);

    //按下軌跡
    mButton01 = (Button)findViewById(R.id.myButton1); 
    mButton01.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        //overlay.clearRange();
        String str = mButton01.getText().toString();
        
        if (str.equals("開啟軌跡"))
        {
          mButton01.setText("關軌跡");
          overlay.setTracker(true);
        }
        else
        {
          mButton01.setText("開啟軌跡");
          overlay.setTracker(false);
        }
      } 
    }); 
     
    //放大地圖
    mButton02 = (Button)findViewById(R.id.myButton2); 
    mButton02.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub 
        intZoomLevel++; 
        if(intZoomLevel>mMapView.getMaxZoomLevel()) 
        { 
          intZoomLevel = mMapView.getMaxZoomLevel(); 
        } 
        mMapController01.setZoom(intZoomLevel); 
      } 
    }); 
     
    //縮小地圖
    mButton03 = (Button)findViewById(R.id.myButton3); 
    mButton03.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub 
        intZoomLevel--; 
        if(intZoomLevel<1) 
        { 
          intZoomLevel = 1; 
        } 
        mMapController01.setZoom(intZoomLevel); 
      } 
    });

    //Satellite或街道
    mButton04 = (Button)findViewById(R.id.myButton4); 
    mButton04.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub
       String str = mButton04.getText().toString();
        
       if (str.equals("衛星"))
       {
        mButton04.setText("街道");
        mMapView.setStreetView(false);
        mMapView.setSatellite(true);
        mMapView.setTraffic(false);
       }
       else
       {
         mButton04.setText("衛星");
         mMapView.setStreetView(true);
         mMapView.setSatellite(false);
         mMapView.setTraffic(false);
       }
      } 
    }); 
  }
  
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_MANAGE, 0 ,R.string.menu_manager)
    .setAlphabeticShortcut('S');
    menu.add(0 , MENU_EXIT, 1 ,R.string.menu_exit)
    .setAlphabeticShortcut('E');
  
     return true;  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Intent intent = new Intent() ;
    
    switch (item.getItemId())
      { 
          case MENU_MANAGE: 
            Intent open = new Intent();
             
            open.setClass(Montior.this, mlist.class);
            timer.cancel();
            finish();
            startActivity(open);
            return true;
      
          case MENU_EXIT:
            timer.cancel();
            android.os.Process.killProcess(android.os.Process.myPid());           
            finish();
            break ;
      }
    
  return true ;
  }

  public List<MapLocation> getMapLocations(boolean doit) 
  {
    if (mapLocations == null || doit == true) 
    {
      mapLocations = new ArrayList<MapLocation>();
    }
    return mapLocations;
  }
 
  //由Tracker送來的座標來更新現在位置
  public static void refreshMapViewByGeoPoint 
  (GeoPoint gp, MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(gp); 
      myMC.setZoom(zoomLevel); 
      //mapview.setSatellite(false);
      
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  }
  
  //將Tracker傳來的座標更新&showrange要不要顯示超出或安全
  public int refreshDouble2Geo(double lat, double longa, int showrange)
  {
    GeoPoint gp = new GeoPoint((int)(lat * 1e6),
        (int)(longa * 1e6));
    
    nowGeoPoint = gp;
    
    //add to tracker
    overlay.addGeoPoint(gp);
    
    refreshMapViewByGeoPoint(nowGeoPoint, 
        mMapView, intZoomLevel);
    
    if (showrange == 1)
    {
      //Over range
      Message msg = new Message();
      msg.what = MSG_DIALOG_OVERRANGE;
      myHandler.sendMessage(msg);       
    }
    else
    {
      //Safe
      mshow = true;
      Message msg = new Message();
      msg.what = MSG_DIALOG_SAFE;
      myHandler.sendMessage(msg);       
    }
    
    return 1;
  }
   
  public void getLocationProvider() 
  { 
    try 
    { 
      Criteria mCriteria01 = new Criteria(); 
      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
      mCriteria01.setAltitudeRequired(false); 
      mCriteria01.setBearingRequired(false); 
      mCriteria01.setCostAllowed(true); 
      mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
      //strLocationProvider = mLocationManager01.getBestProvider(mCriteria01, true); 
       
      //mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
  @Override 
  protected boolean isRouteDisplayed() 
  { 
    // TODO Auto-generated method stub 
    return false; 
  } 
  
  //抓取手機的IP
  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
      {
          NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
            {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress().toString();
                }
            }
      }
    }
    catch (SocketException ex) {
        Log.e("", ex.toString());
    }

    return null;
  }
  
  void GPSRhander(String gpsdata)
  {
    if ( gpsdata != null )
    {
      StringTokenizer Tok = new StringTokenizer(gpsdata, ",");
      double GPSData[] = new double[8];
      int i=0;
      while (Tok.hasMoreElements())
      {
        GPSData[i] = Double.valueOf((String) Tok.nextElement());
        i++;
      }
      
      top_left = new GeoPoint((int)(GPSData[0] * 1e6),
          (int)(GPSData[1] * 1e6));
      top_right = new GeoPoint((int)(GPSData[2] * 1e6),
          (int)(GPSData[3] * 1e6));
      bottom_left = new GeoPoint((int)(GPSData[4] * 1e6),
          (int)(GPSData[5] * 1e6));
      bottom_right = new GeoPoint((int)(GPSData[6] * 1e6),
          (int)(GPSData[7] * 1e6));
      
      overlay.SetPoint(top_left, bottom_right, top_right, bottom_left);
      Log.i(TAG, "GPSData set OK");
    }
    else
    {
      overlay.clearRange();
    }
  }
  
  void setStatus()
  {
    Message msg = new Message();
    msg.what = MSG_DIALOG_SETS;
    myHandler.sendMessage(msg);      
  }
  
  //處理HANDER: refreshDouble2Geo會傳送Message出來，決定要顯示什麼
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_DIALOG_SAFE:
                label.setTextColor(Color.BLACK);
                if (overlay.getGPSRangeSize() != 0)
                  label.setText("安全/設置範圍");
                else
                  label.setText("安全/未設置範圍");
                  
                if (mp != null)
                {
                  mp.stop_voice();
                  mp = null;
                }
                break;
          case MSG_DIALOG_OVERRANGE:
                label.setTextColor(Color.RED);
                label.setText("超出設置範圍");
                
                if (mp == null)
                {
                  //voice
                  mp = new medplayer();
                  mp.play_voice("warn.mp3");
                }
                break;
          case MSG_DIALOG_SETS:
            if (overlay.getGPSRangeSize() != 0)
              label.setText("安全/設置範圍");
            else
              label.setText("安全/未設置範圍");
            break;
            
          default:
                label.setText(Integer.toString(msg.what));
        }
        super.handleMessage(msg);
    }
};  

  public class DateTask extends TimerTask {
    public void run() 
    {
      if (!processing.compareAndSet(false, true)) return;
      
      int shour, sminute;
      final Calendar c = Calendar.getInstance();
      shour = c.get(Calendar.HOUR_OF_DAY);
      sminute = c.get(Calendar.MINUTE);      
      
      String uriAPI = IPAddress + "getchildstatus.php?nowtime=" + shour + ":" + sminute + "&childid=" + mchildid;
      
      URL url = null;
      try{
        url = new URL(uriAPI);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        //Using login handler for xml
        CSXMLHandler myHandler = new CSXMLHandler();
        xr.setContentHandler(myHandler);
        //open connection
        xr.parse(new InputSource(url.openStream()));
        //verify OK
        csdata = myHandler.getParsedData();
      }
      catch(Exception e){
        e.printStackTrace();
        return;
      }
      
      //handle gps message
      if (!csdata.h_nowgps.equals("-1"))
      {
        StringTokenizer Tok = new StringTokenizer(csdata.h_nowgps, ",");
        double GPSData[] = new double[3];
        int i=0;
        while (Tok.hasMoreElements())
        {
          GPSData[i] = Double.valueOf((String) Tok.nextElement());
          i++;
        }      
         
        //若有3個參數, 代表超過range
        if (i == 3)
        {
          //送出更新座標, 要求顯示超過req給MyGoogle.java
          refreshDouble2Geo(GPSData[0], GPSData[1], 1);
        } 
        else
        {
          //送出更新座標給MyGoogle.java
          refreshDouble2Geo(GPSData[0], GPSData[1], 0);
        }
      }
      
      if (!csdata.h_name.equals("nodata"))
      {
        String cname, cgps, cstime, cdtime;
        
        cname = csdata.h_name;
        cgps = csdata.h_rangegps;
        cstime = csdata.h_stime;
        cdtime = csdata.h_dtime;

        if (oldGPSRangeData.equals(""))
        {
          Log.i(TAG, "get: " +cgps);
          oldGPSRangeData = cgps;
          GPSRhander(cgps);
          setStatus();
        }
        else if (!oldGPSRangeData.equals(cgps))
        {
          Log.i(TAG, "get: " + cgps);
          oldGPSRangeData = "";
          GPSRhander(cgps);
          setStatus();
        }
        else
          setStatus();
      }

      processing.set(false);      
    }
  }
  
  public ArrayList<ChildStruct> getChildList()
  {
    String uriAPI = IPAddress + "getchildlist.php";
    
    URL url = null;
    try{
      url = new URL(uriAPI);
      
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();
      XMLReader xr = sp.getXMLReader();
      //Using login handler for xml
      ChildListHandler myHandler = new ChildListHandler();
      xr.setContentHandler(myHandler);
      //open connection
      xr.parse(new InputSource(url.openStream()));
      //verify OK
      childlist = myHandler.getContainer().getListItems();
    }
    catch(Exception e){
      e.printStackTrace();
      return null;
    }
    
    return childlist;
  }
  
  //show message
  public void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
           mshow = false;
         }
         }
        )
    .show();
  }
}
