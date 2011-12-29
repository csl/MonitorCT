package com.monitortracker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Adapter;
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
  private static final int MENU_EXIT = Menu.FIRST;

  String newitem;
  private ArrayList<HashMap<String, String>> mrlist;
  static public ArrayList<grstruct> grs;
  private ListView gpslist;
  private String IPAddress;
  
  protected static final int CONTEXTMENU_EDIT = 0;
  protected static final int CONTEXTMENU_DELETE= 1;

  private static final int MSG_DIALOG_SUCCESS = 1;  
  private static final int MSG_DIALOG_FAIL = 2;
  private static final int MSG_DIALOG_UGUI = 3;
  
  public int cindex;
  
  public String TAG = "mlist";

/** Called when the activity is first created. */
protected void onCreate(Bundle icicle) 
{ 
    super.onCreate(icicle); 
    setContentView(R.layout.rlist);
    
    newitem = (String) this.getResources().getText(R.string.new_range);    
    gpslist = (ListView) findViewById(R.id.mlist);
    mrlist = new ArrayList<HashMap<String, String>>();
    cindex = 0;
    
    //read all data
    IPAddress = (String) this.getResources().getText(R.string.url);
    String uriAPI = IPAddress + "getallrange.php";
    
    URL url = null;
    try{
      url = new URL(uriAPI);
      
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();
      XMLReader xr = sp.getXMLReader();
      RangeListHandler myHandler = new RangeListHandler();
      xr.setContentHandler(myHandler);
      //open connection
      xr.parse(new InputSource(url.openStream()));
      grs = myHandler.getContainer().getListItems();
    }
    catch(Exception e){
      e.printStackTrace();
      return;
    }
    
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
              mlist.this.finish();
            }
            else
            {
              cindex = arg2 - 1;
              
              Intent open = new Intent();
              Bundle bundle = new Bundle();
              
              bundle.putInt("cindex", cindex);
              open.setClass(mlist.this, addgpsrange.class);
              open.putExtras(bundle);
              
              mlist.this.finish();
              startActivity(open);                
            }
          }  
      });
      
     //long click
     gpslist.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
          
         public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
          {
             menu.setHeaderTitle("操作");
             menu.add(0, CONTEXTMENU_EDIT, 0 , "Edit"); 
             menu.add(0, CONTEXTMENU_DELETE, 1 , "Delete"); 
          }  
      });   
    
  }

@Override
public boolean onContextItemSelected(MenuItem aItem) 
{
          AdapterContextMenuInfo menuInfo;
          menuInfo = (AdapterContextMenuInfo)aItem.getMenuInfo();
          int index = menuInfo.position;
  
          if (index == 0) return false;
          
          cindex = index - 1;
          Log.i(TAG, Integer.toString(index));

          /* Switch on the ID of the item, to get what the user selected. */
          switch (aItem.getItemId()) 
          {
               case CONTEXTMENU_EDIT:
                   Intent open = new Intent();
                   Bundle bundle = new Bundle();
                   
                   bundle.putInt("cindex", cindex);
                   open.setClass(mlist.this, addgpsrange.class);
                   open.putExtras(bundle);
                   
                   mlist.this.finish();
                   startActivity(open);                

                   return true; /* true means: "we handled the event". */
                    
               case CONTEXTMENU_DELETE:
                 deleteList();
                 return true; /* true means: "we handled the event". */
          }

          return false;
  }

  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_EXIT, 1 ,R.string.menu_exit)
    .setAlphabeticShortcut('E');
  
     return true;  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
      { 
          case MENU_EXIT:
            Intent open = new Intent();
            
            open.setClass(mlist.this, Montior.class);
            mlist.this.finish();
            startActivity(open);
            break ;
      }
    
  return true ;
  }
  
  public void deleteList()
  {
    String url_list = IPAddress + "deleterange.php?id=" + grs.get(cindex).id;
    toweb(url_list);
  }


  public void recGPSRange(String id, String name, String dgps, String stime, String dtime)
  {
    grstruct newitem = new grstruct(id, name, dgps, stime, dtime);
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
    m.notifyDataSetChanged();
  }
  
  void msg_fail()
  {
    //Over range
    Message msg = new Message();
    msg.what = MSG_DIALOG_FAIL;
    myHandler.sendMessage(msg);       
  }
  
  void updategui()
  {
    Intent open = new Intent();
    open.setClass(mlist.this, mlist.class);
    startActivity(open);                
    mlist.this.finish();
    
  }
 
  void update()
  {
    //Over range
    Message msg = new Message();
    msg.what = MSG_DIALOG_UGUI;
    myHandler.sendMessage(msg);       
  }
 
  
  //處理HANDER: refreshDouble2Geo會傳送Message出來，決定要顯示什麼
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_DIALOG_SUCCESS:
                openOptionsDialog("成功");
                break;
          case MSG_DIALOG_FAIL:
                openOptionsDialog("失敗");
                break;
          case MSG_DIALOG_UGUI:
                updatedata();
                break;
          default:
                //openOptionsDialog(Integer.toString(msg.what));
        }
        super.handleMessage(msg);
    }
  };
  
  public int toweb(String uriAPI)
  {
    int error = 0;
       HttpGet httpRequest = new HttpGet(uriAPI);
         
       try
        {
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
              String strResult = EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
              //mTextView1.setText("Error Response: "+httpResponse.getStatusLine().toString());
            }
          }
          catch (ClientProtocolException e)
          {
            //mTextView1.setText(e.getMessage().toString());
            e.printStackTrace();
            error = 1;
          }
          catch (IOException e)
          {
            //mTextView1.setText(e.getMessage().toString());
            e.printStackTrace();
            error = 1;
          }
          catch (Exception e)
          {
            //mTextView1.setText(e.getMessage().toString());
            e.printStackTrace();
            error = 1;
          }

         return error;
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
         }
         }
        )
    .show();
  }


}