package com.monitortracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
 
public class SendDataSocket extends Thread 
{

	private String address;
	private int port;
	private int function;
	private String timestamp;
	private int PicCount;
	private boolean IsOK;
	private MyGoogleMap GoogleMap;
	public int takePictureCode;
	public String error_string;

	String line;
	
	public SendDataSocket(MyGoogleMap map) 
  {
		//IsOK = false;
		GoogleMap = map;
  }
	
	public void SetAddressPort(String addr, int p)
	{		
		this.address = addr;
		this.port = p;
	}
	
	public String getTimeStamp()
	{
		return timestamp;		
	}
	
	public void SetFunction(int func)
	{
		function = func;		
	}

	public void SetCount(int count)
	{
		PicCount = count;
		
	}
	public boolean getIsOK()
	{
		return IsOK;
	}
	
	@Override
	public void run() 
	{
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);

        try {
            client.connect(isa, 10000);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
            {
            	out.writeUTF("setGPSRange");

            	// As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
                
              while (true)
              {
                line = is.readUTF();
                
                if (!line.equals("END"))
                	timestamp = line;
                
                if (line.equals("END")) {
                	break;
                  }
              }
              	
              is.close();
             }
            else if (function  == 2)
            {
           	out.writeUTF("Sync");
	        	// As long as we receive data, server will data back to the client.
	          DataInputStream is = new DataInputStream(client.getInputStream());
	              
	            while (true)
	              {
	              line = is.readUTF();
	              
	              if (line.equals("END")) {
	              	break;
	                }
	              }
	            
	              is.close();
            }
            else if (function  == 3)
            {
           	out.writeUTF("NeedSync");
           	// As long as we receive data, server will data back to the client.
           	DataInputStream is = new DataInputStream(client.getInputStream());
              
           	while (true)
           		{
	              line = is.readUTF();
	              
	              if (!line.equals("END"))
	              	timestamp = line;
	              
	              if (line.equals("END")) {
	              	break;
	                }
           		}

              is.close();
            }
            else if (function  == 4)
             {
              out.writeUTF("getTakePicture");
              out.writeUTF(String.valueOf(PicCount));  
              
             	// As long as we receive data, server will data back to the client.
             	DataInputStream is = new DataInputStream(client.getInputStream());
                
             	takePictureCode = 0;
             	while (true)
             	{
  	              line = is.readUTF();
  	              if (line.equals("OK")) 
                  {
  	                break;
  	              }
  	              else if (line.equals("Fail")) 
	                {
  	                break;
  	              }
             	}
             	
              is.close();
              
             }
            else if (function  == 5)
            {
            	out.writeUTF("reSetPicNumber");
            	out.writeUTF(String.valueOf(PicCount));            	
            }
            
        } catch (java.io.IOException e) {
        	System.out.println("IOException :" + e.toString());
           IsOK = false;
           error_string = e.toString();
           
           if (function == 1)
           {
           }
           if (function == 3)
           {
           }
           if (function == 4)
           {
           }
           //System.exit(1);
        }
	}
}