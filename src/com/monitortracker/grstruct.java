package com.monitortracker;

public class grstruct 
{
  public String id;
  public String name;
  public String gpsdata;
  public String stime;
  public String dtime;
  public String child;

  public grstruct()
  {

  }
  
  public grstruct(String cid, String cname, String cgps, String sdtime, String ddtime)
  {
    id = cid;
    name = cname;
    gpsdata = cgps;
    stime = sdtime;
    dtime = ddtime;
  }
}