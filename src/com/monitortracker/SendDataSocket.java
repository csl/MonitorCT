package com.monitortracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
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

  private String TAG = "SendDataSocket";
	private String address;
	private int port;
	private int function;
	private int IsOK;
	private Montior MonitorMap;
  private addgpsrange agps;
  private menu MenuMain;
  private mlist dmlist;
	public String error_string;
  public String send_Data;
  public String line;
  public String user;
  public String pwd;
	
	public List<String> send_s; 
	
	public SendDataSocket(Montior map) 
  {
		IsOK = 0;
		MonitorMap = map;
		agps=null;
		dmlist=null;
  }

  public SendDataSocket(menu map) 
  {
    IsOK = 0;
    MenuMain = map;
    MonitorMap = null;
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

  
  public void SetLogin(String muser, String mpwd)
  {   
    this.user = muser;
    this.pwd = muser;
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
              int shour, sminute;
              final Calendar c = Calendar.getInstance();
              shour = c.get(Calendar.HOUR_OF_DAY);
              sminute = c.get(Calendar.MINUTE);
              
              //傳送字串座標
              out.writeUTF("nowStatus");
              out.writeUTF(shour + ":" + sminute);

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
              line = is.readUTF();
              
              if (line.equals("nowGPSRange"))
              {
                String cname, cgps, cstime, cdtime;
                
                cname = is.readUTF();
                cgps = is.readUTF();
                cstime = is.readUTF();
                cdtime = is.readUTF();

                if (MonitorMap.oldGPSRangeData.equals(""))
                {
                  
                  Log.i(TAG, "get: " +cgps);
                  MonitorMap.oldGPSRangeData = cgps;
                  MonitorMap.GPSRhander(cgps);
                  MonitorMap.setStatus();
                  
                }
                else if (!MonitorMap.oldGPSRangeData.equals(cgps))
                {
                  Log.i(TAG, "get: " + cgps);
                  MonitorMap.oldGPSRangeData = "";
                  MonitorMap.GPSRhander(cgps);
                  MonitorMap.setStatus();
                }
                else
                  MonitorMap.setStatus();
                
              }
              else if (line.equals("NoGPSRange"))
              {
                Log.i(TAG, "nogpsrange");
                MonitorMap.GPSRhander(null);
              }            
              
              is.close();
            }
            else if (function  == 3)
            {
              String cid, cname, cgps, cstime, cdtime;
              out.writeUTF("LGPS");

              // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();
              int dsize = Integer.valueOf(line);
              IsOK = 2;

              for (int i = 0; i<dsize; i++)
              {
                  cid = is.readUTF();
                  cname = is.readUTF();
                  cgps = is.readUTF();
                  cstime = is.readUTF();
                  cdtime = is.readUTF();
                  dmlist.recGPSRange(cid, cname, cgps, cstime, cdtime);
              }
              is.close();
              
              dmlist.update();
            }            
            if (function == 4)
            {
             //傳送字串座標
             out.writeUTF("DGPS");
             out.writeUTF(dmlist.grs.get(dmlist.cindex).name);
             
             // As long as we receive data, server will data back to the client.
             DataInputStream is = new DataInputStream(client.getInputStream());
              
             //是否傳送成功
             while (true)
             {
               line = is.readUTF();
               if (line.equals("OK")) 
               {
                 Log.v("vDEBUG: ", "DGPS OK!!");
                 IsOK = 2;
                 dmlist.updategui();
                 break;
               }
             }
             is.close();
             
            }
            if (function  == 5)
            {
              //傳送字串座標
              out.writeUTF("UGPS");
              out.writeUTF(send_s.get(0));
              out.writeUTF(send_s.get(1));
              out.writeUTF(send_s.get(2));
              out.writeUTF(send_s.get(3));
              out.writeUTF(send_s.get(4));

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
            if (function  == 6)
            {
              //傳送字串座標
              out.writeUTF("Login");
              out.writeUTF(user);
              out.writeUTF(pwd);

              // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
               
              //是否傳送成功
              while (true)
              {
                line = is.readUTF();
                if (line.equals("OK") || line.equals("FAIL")) 
                {
                  Log.v("vDEBUG: ", "SetGPSRange OK!!");
                  IsOK = 2;
                  break;
                }
              }
              is.close();
             }
            
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        
        timeout++;
        if (timeout > 10)
        {
          if (agps != null)
            agps.msg_fail();
          //else if (MonitorMap != null)
            //MonitorMap.msg_fail();
          else if (dmlist != null)
            dmlist.msg_fail();       
          
          Log.i("...", "timeout");
          break;
        }
        
	  } while (IsOK != 2);
	  
	}
}