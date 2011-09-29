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
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;

public class mlist extends Activity
{

  String newitem;
  private ArrayList<HashMap<String, String>> mrlist;
  private ListView gpslist;

/** Called when the activity is first created. */
protected void onCreate(Bundle icicle) 
{ 
    super.onCreate(icicle); 
    setContentView(R.layout.rlist);
    
    newitem = (String) this.getResources().getText(R.string.new_range);    
    gpslist = (ListView) findViewById(R.id.mlist);
    mrlist = new ArrayList<HashMap<String, String>>();

    updatedata();
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
           
           
          }  
      });   
    
  }

  private void updatedata() 
  {
    mrlist.clear();
    
    HashMap<String, String> map = new HashMap<String, String>();  
    map.put("ItemTitle", newitem);  
    map.put("ItemText", "new item");  
    
    mrlist.add(map);
    
    //put in
    SimpleAdapter m = new SimpleAdapter(this,
              mrlist,
              R.layout.mlistview,
              new String[] {"ItemTitle", "ItemText"}, 
              new int[] {R.id.ItemTitle,R.id.ItemText});
      
    gpslist.setAdapter(m);
  }
}