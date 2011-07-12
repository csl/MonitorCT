package com.monitortracker;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import com.google.android.maps.GeoPoint;

import android.util.Log;
import android.widget.Toast;

//等待Tracker傳送GPS和是否超出之判斷結果
public class SocketServer implements Runnable
{
	private int port;
	private ServerSocket sc;
	private MyGoogleMap MonitorMap;
	
	public boolean ChildPhoneReady;

	public SocketServer(int port, MyGoogleMap mmap)throws IOException
	{
		this.port = port;
		this.sc = new ServerSocket(port);
		MonitorMap = mmap;
		ChildPhoneReady = false;
	}
	
	public void run()
	{
		Socket con = null;
		while(true)
		{
			try
			{
			  //Listen Port
				con = this.sc.accept();
				DataInputStream in = new DataInputStream(con.getInputStream());
				//將收到的資料
				String str = in.readUTF();
				Log.v("vDEBUG: ", "vClient " + str);
				//用,切開來
		    StringTokenizer Tok = new StringTokenizer(str, ",");
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
          MonitorMap.refreshDouble2Geo(GPSData[0], GPSData[1], 1);
		    }
		    else
		    {
          //送出更新座標給MyGoogle.java
	        MonitorMap.refreshDouble2Geo(GPSData[0], GPSData[1], 0);
		    }
		    
		    //回傳給Tracker, OK
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        
        out.writeUTF(Long.toString(System.currentTimeMillis()/1000));
        out.writeUTF("OK");
        out.flush();
        
				in.close();
				con.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
