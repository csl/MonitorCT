package com.monitortracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.util.Log;
 
public class SendDataSocket extends Thread 
{

	private String address;
	private int port;
	private int function;
	private int IsOK;
	private MyGoogleMap MonitorMap;
  private addgpsrange agps;
  private mlist dmlist;
	public String error_string;
  public String send_Data;
	String line;
	
	public List<String> send_s; 
	
	public SendDataSocket(MyGoogleMap map) 
  {
		IsOK = 0;
		MonitorMap = map;
		agps=null;
		dmlist=null;
  }

	public SendDataSocket(addgpsrange cm) 
  {
    IsOK = 0;
    agps = cm;
    MonitorMap = null;
    dmlist = null;
    send_s = new ArrayList<String>();
  }

  public SendDataSocket(mlist cm) 
  {
    IsOK = 0;
    agps = null;
    MonitorMap = null;
    dmlist = cm;
    send_s = new ArrayList<String>();
  }
	
	
	public void addstring(String add)
	{
	  send_s.add(add);
	}
	
	//設定IPAddress和Port
	public void SetAddressPort(String addr, int p)
	{		
		this.address = addr;
		this.port = p;
	}
	
  public void SetSendData(String sdata)
  {   
    this.send_Data = sdata;
  }	
	
	public void SetFunction(int func)
	{
		function = func;		
	}

	public int getIsOK()
	{
		return IsOK;
	}
	
	@Override
	public void run() 
	{
	  int timeout=0;
	  
	  do {
        //傳送
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);

        try {
            client.connect(isa, 20021);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
             {
              //傳送字串座標
              out.writeUTF("SetGPSRange");
             	out.writeUTF(send_s.get(0));
              out.writeUTF(send_s.get(1));
              out.writeUTF(send_s.get(2));
              out.writeUTF(send_s.get(3));

            	// As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
               
              //是否傳送成功
              while (true)
              {
                line = is.readUTF();
                if (line.equals("OK")) 
                {
                  Log.v("vDEBUG: ", "SetGPSRange OK!!");
                  IsOK = 2;
                  agps.msg_ok();
                	break;
                }
              }
              is.close();
             }
            else if (function  == 2)
            {
              //傳送字串座標
              out.writeUTF("nowStatus");

              // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
               
              line = is.readUTF();
              Log.v("vDEBUG: ", "vClient " + line);
              if (!line.equals("NoStatus"))
                {
                StringTokenizer Tok = new StringTokenizer(line, ",");
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
              }
              is.close();
             }
            else if (function  == 3)
            {
              out.writeUTF("LGPS");

              // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();
              int dsize = Integer.valueOf(line);
              String cname, cgps, cstime, cdtime;
              for (int i = 0; i<dsize; i++)
              {
                  cname = is.readUTF();
                  cgps = is.readUTF();
                  cstime = is.readUTF();
                  cdtime = is.readUTF();
                  dmlist.recGPSRange(cname, cgps, cstime, cdtime);
              }
              is.close();
              
            }            
            
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        
        timeout++;
        if (timeout > 10)
        {
          agps.msg_fail();
          Log.i("...", "timeout");
          break;
        }
        
	  } while (IsOK != 2);
	}
}