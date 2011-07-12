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
	private boolean IsOK;
	private MyGoogleMap GoogleMap;
	public String error_string;
  public String send_Data;
	String line;
	
	public SendDataSocket(MyGoogleMap map) 
  {
		IsOK = false;
		GoogleMap = map;
  }
	
	//�]�wIPAddress�MPort
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

	public boolean getIsOK()
	{
		return IsOK;
	}
	
	@Override
	public void run() 
	{
        //�ǰe
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);

        try {
            client.connect(isa, 10001);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
            {
              //�ǰe�r��y��
            	out.writeUTF(send_Data);

            	// As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
               
              //�O�_�ǰe���\
              while (true)
              {
                line = is.readUTF();
                if (line.equals("OK")) 
                {
                  IsOK = true;
                	break;
                }
              }
              is.close();
             }
            
            
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
	}
}