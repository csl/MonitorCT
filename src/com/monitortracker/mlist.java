package com.monitortracker;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;

public class mlist extends Activity
{

  String newitem;
  private ArrayList<HashMap<String, String>> mrlist;
  private ArrayList<grstruct> grs;
  private ListView gpslist;
  private String IPAddress;
  private int port;
  
  private SendDataSocket sData;
  
  protected static final int CONTEXTMENU_EDIT = 0;
  
  protected static final int CONTEXTMENU_DELETE= 1;

/** Called when the activity is first created. */
protected void onCreate(Bundle icicle) 
{ 
    super.onCreate(icicle); 
    setContentView(R.layout.rlist);
    
    newitem = (String) this.getResources().getText(R.string.new_range);    
    gpslist = (ListView) findViewById(R.id.mlist);
    mrlist = new ArrayList<HashMap<String, String>>();
    grs = new ArrayList<grstruct>();
    
    sData = new SendDataSocket(this);

    IPAddress = MyGoogleMap.my.IPAddress;
    port = MyGoogleMap.my.port;
    updatedata();
    //sData.SetAddressPort(IPAddress , port);
    //sData.SetFunction(3); 
    //sData.start();
    

    gpslist.setOnItemClickListener(new OnItemClickListener() 
    {  
         public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                  long arg3) 
          {
            if (arg2 == 0)
              {
              Intent open = new Intent();
              
              open.setClass(mlist.this, addgpsrange.class);
              startActivity(open);                
              }
            
          }  
      });
      
     //long click
     gpslist.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
          
         public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
          {
             menu.setHeaderTitle("ContextMenu");
             menu.add(0, CONTEXTMENU_EDIT,0, "Edit"); 
             menu.add(0, CONTEXTMENU_DELETE,0, "Delete"); 
           
          }  
      });   
    
  }

@Override
public boolean onContextItemSelected(MenuItem aItem) 
{
          AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
          /* Switch on the ID of the item, to get what the user selected. */
          switch (aItem.getItemId()) {
               case CONTEXTMENU_EDIT:
                    return true; /* true means: "we handled the event". */
                    
               case CONTEXTMENU_DELETE:
                 return true; /* true means: "we handled the event". */
          }

          return false;
}

  public void recGPSRange(String name, String dgps, String stime, String dtime)
  {
    grstruct newitem = new grstruct(name, dgps, stime, dtime);
    
    grs.add(newitem);
    
  }

  public void updatedata() 
  {
    mrlist.clear();
    
    HashMap<String, String> map = new HashMap<String, String>();  
    map.put("ItemTitle", newitem);  
    map.put("ItemText", "new item"); 
    mrlist.add(map);
    
    for (int i=0; i<grs.size(); i++)
    {
      map = new HashMap<String, String>();  
      map.put("ItemTitle", grs.get(i).name);  
      map.put("ItemText", grs.get(i).stime + "," + grs.get(i).dtime);
      mrlist.add(map);      
    }
    
    //put in
    SimpleAdapter m = new SimpleAdapter(this,
              mrlist,
              R.layout.mlistview,
              new String[] {"ItemTitle", "ItemText"}, 
              new int[] {R.id.ItemTitle,R.id.ItemText});
      
    gpslist.setAdapter(m);
  }
}