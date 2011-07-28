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
import java.util.Enumeration;
import java.util.List; 
import java.util.Locale; 
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

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
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent; 
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
import android.view.View; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.RatingBar;
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

public class MyGoogleMap extends MapActivity 
{ 
  private static final int MSG_DIALOG_SAFE = 1;  
  private static final int MSG_DIALOG_OVERRANGE = 2;  
  //private TextView mTextView01;
  static public MyGoogleMap my;
  private MyGoogleMap mMyGoogleMap = this;
  private Timer timer = new Timer();
  private SocketServer s_socket = null;
  
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MyOverLay overlay;
  private List<MapLocation> mapLocations;

  private Button mButton01,mButton02,mButton03,mButton04,mButton05;
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public GeoPoint nowGeoPoint;
  
  private String IPAddress;
  private SendDataSocket sData;
  private SendDataSocket rData;
  
  private int serve_port = 12341;
  
  public static  MapLocation mSelectedMapLocation;  
  
  public GeoPoint top_left;        
  public GeoPoint top_right;
  public GeoPoint bottom_left;
  public GeoPoint bottom_right;   
  public boolean mshow;
   
  public TextView label;
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 

    my = this;

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
     
    intZoomLevel = 15; 
    mMapController01.setZoom(intZoomLevel); 

    IPAddress ="192.168.173.101";
    mshow = false;
    
    //顯示輸入IP的windows
    final EditText input = new EditText(mMyGoogleMap);
    input.setText(IPAddress);
    AlertDialog.Builder alert = new AlertDialog.Builder(mMyGoogleMap);

    //openOptionsDialog(getLocalIpAddress());
    
    alert.setTitle("設定Child Phone IP");
    alert.setMessage("請輸入Child Phone IP位置");
    
    // Set an EditText view to get user input 
    alert.setView(input);
    
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int whichButton) 
    {
      try
      {
        IPAddress = input.getText().toString();  
        timer.schedule(new DateTask(), 0, 1000);     
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());
    }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        // Canceled.
      }
    });

    alert.show();      
    
    //mLocationManager01.requestLocationUpdates 
    //(strLocationProvider, 2000, 10, mLocationListener01); 
     
    //建構畫在GoogleMap的overlay
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());    

    //讀sdcard, 若之前有記下的gps range座標讀回來
    try
    {
      File vSDCard= Environment.getExternalStorageDirectory();
      File sFile = new File(vSDCard.getParent() + "/" + vSDCard.getName() + "/gps_handler");
      //exist file
      //Open file
      if(sFile.exists()) 
      {
        //getMapLocations(true);
        FileReader fileReader = new FileReader(sFile);
        BufferedReader bufReader = new BufferedReader(fileReader);
        String str="", 
        GPS_ORG_DATA = "";
        
        while((str = bufReader.readLine()) != null)
        {
          GPS_ORG_DATA = str;
        }        
        fileReader.close();        
        
        StringTokenizer Tok = new StringTokenizer(GPS_ORG_DATA, ",");
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

        //將座標設進overlay中，並顯示在畫面上
        overlay.SetPoint(top_left, bottom_right, top_right, bottom_left);
        Log.v("Loading file OK", vSDCard.getParent() + "/" + vSDCard.getName() + "/gps_handler");

	//傳出設定的GPS Range座標給Tracker
        SendGPSData(getLocalIpAddress() + "," + GPS_ORG_DATA);
        //sendtoChildTracker
      }    
    }
    catch (IOException  e)
    {
      e.printStackTrace();      
    }   
    catch (Exception e)
    {
      e.printStackTrace();      
    }  
    
    //按下清除座標
    mButton01 = (Button)findViewById(R.id.myButton1); 
    mButton01.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        overlay.clearRange();
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

    //重新設定IPAddress
    mButton05 = (Button)findViewById(R.id.myButton5); 
    mButton05.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      {
        String str = mButton05.getText().toString();
        
        if (str.equals("開啟軌跡"))
        {
          mButton05.setText("關軌跡");
          overlay.setTracker(true);
        }
        else
        {
          mButton05.setText("開啟軌跡");
          overlay.setTracker(false);
        }
      } 
    });

    Log.v("IPADDRESS", getLocalIpAddress());
    
    //Open Server Socket, for trakcer傳來的資料
    /*
    try {
        s_socket = new SocketServer(serve_port, this);
        Thread socket_thread = new Thread(s_socket);
        socket_thread.start();
    } 
    catch (IOException e) {
        e.printStackTrace();
    }
    catch (Exception e) {
        e.printStackTrace();
    }*/
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
  
  //傳送GPS Range座標出去給Tracker
  public void SendGPSData(String GPSData)
  {
    int port = 12341;

    sData = new SendDataSocket(this);
    sData.SetAddressPort(IPAddress , port);
    sData.SetSendData(GPSData);
    sData.SetFunction(1); 
    sData.start();
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
  
 /* private class MyItemOverlay extends ItemizedOverlay<OverlayItem>
  {
    private List<OverlayItem> items = new ArrayList<OverlayItem>();
    public MyItemOverlay(Drawable defaultMarker , GeoPoint gp)
    {
      super(defaultMarker);
      items.add(new OverlayItem(gp,"Title","Snippet"));
      populate();
    }
    
    @Override
    protected OverlayItem createItem(int i)
    {
      return items.get(i);
    }
    
    @Override
    public int size()
    {
      return items.size();
    }
    
    @Override
    protected boolean onTap(int pIndex)
    {
      Toast.makeText
      (
        Flora_Expo.this,items.get(pIndex).getSnippet(),
        Toast.LENGTH_LONG
      ).show();
      return true;
    }
  }*/
   
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
  
  //處理HANDER: refreshDouble2Geo會傳送Message出來，決定要顯示什麼
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_DIALOG_SAFE:
                label.setText("安全");
                break;
          case MSG_DIALOG_OVERRANGE:
                label.setText("超出");
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
      int port = 12341;
      rData = new SendDataSocket(my);
      rData.SetAddressPort(IPAddress , port);
      rData.SetFunction(2); 
      rData.start();
    }
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
