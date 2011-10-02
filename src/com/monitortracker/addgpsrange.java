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
import android.view.Menu;
import android.view.MenuItem;
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

public class addgpsrange extends MapActivity 
{ 
  private static final int MSG_DIALOG_SUCCESS = 1;  
  private static final int MSG_DIALOG_FAIL = 2;
  
  private static final int MENU_MANAGE = Menu.FIRST  ;
  private static final int MENU_EXIT = Menu.FIRST +1 ;

  public String gpsrange;
  private TextView name;
  private TextView stime;
  private TextView dtime;  
  static public addgpsrange my;
  private Timer timer = new Timer();
  //private SocketServer s_socket = null;
  
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private mOverLay overlay;
  private List<MapLocation> mapLocations;

  private Button mButton01,mButton02,mButton03,mButton04,mButton05;
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public GeoPoint nowGeoPoint;
  
  private String IPAddress;
  private SendDataSocket sData;
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
    setContentView(R.layout.addgpsrange); 

    IPAddress = MyGoogleMap.my.IPAddress;
    
    //googleMAP
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 

    //訊息顯示
    name = (TextView) findViewById(R.id.name);
    stime = (TextView) findViewById(R.id.stime_text);
    dtime = (TextView) findViewById(R.id.dtime_text);
    gpsrange="";
    
    //參數設定 
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 15; 
    mMapController01.setZoom(intZoomLevel); 

    mshow = false;
    
    //mLocationManager01.requestLocationUpdates 
    //(strLocationProvider, 2000, 10, mLocationListener01); 
     
    //建構畫在GoogleMap的overlay
    overlay = new mOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());    

    mButton01 = (Button)findViewById(R.id.clear_button); 
    mButton01.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        overlay.clearRange();
        gpsrange="";
        
      } 
    }); 

    mButton02 = (Button)findViewById(R.id.add_button); 
    mButton02.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      {
        String cname = name.getText().toString();
        String sctime = stime.getText().toString();
        String dctime = dtime.getText().toString();
        if (!gpsrange.equals("") && !cname.equals("") 
                        && !sctime.equals("") &&  !dctime.equals(""))
        {
          //sending
          SendGPSData(cname, sctime, dctime, gpsrange);
        }
        
      } 
    }); 

    mButton03 = (Button)findViewById(R.id.cancel_button); 
    mButton03.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      {
        
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
  
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_MANAGE, 0 ,R.string.menu_start).setIcon(R.drawable.start)
    .setAlphabeticShortcut('S');
    menu.add(0 , MENU_EXIT, 1 ,R.string.menu_exit).setIcon(R.drawable.exit)
    .setAlphabeticShortcut('E');
  
     return true;  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {    
    switch (item.getItemId())
      { 
          case MENU_MANAGE: 
            Intent open = new Intent();
             
            open.setClass(addgpsrange.this, mlist.class);
            startActivity(open);

            return true;
      
          case MENU_EXIT:
    
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
  
  //傳送GPS Range座標出去給Tracker
  public void SendGPSData(String name, String gpsdata, String st, String dt)
  {
    int port = 12341;

    sData = new SendDataSocket(this);
    //handler: data
    sData.addstring(name);
    sData.addstring(gpsdata);
    sData.addstring(st);
    sData.addstring(dt);

    sData.SetAddressPort(IPAddress , port);
    sData.SetFunction(1); 
    sData.start();
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
  
  void msg_ok()
  {
    //Over range
    Message msg = new Message();
    msg.what = MSG_DIALOG_SUCCESS;
    myHandler.sendMessage(msg);       
  }

  void msg_fail()
  {
    //Over range
    Message msg = new Message();
    msg.what = MSG_DIALOG_FAIL;
    myHandler.sendMessage(msg);       
  }
  
  //處理HANDER: refreshDouble2Geo會傳送Message出來，決定要顯示什麼
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_DIALOG_SUCCESS:
                openOptionsDialog("新增成功");
                break;
          case MSG_DIALOG_FAIL:
                openOptionsDialog("新增失敗");
                break;
          default:
                openOptionsDialog(Integer.toString(msg.what));
        }
        super.handleMessage(msg);
    }
};  
  
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
           //mshow = false;
         }
         }
        )
    .show();
  }
}
