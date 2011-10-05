package com.monitortracker;

import java.io.IOException; 
import android.app.Activity; 
import android.media.MediaPlayer; 
import android.media.MediaPlayer.OnCompletionListener; 
import android.os.Bundle; 

public class medplayer
{ 
  /*設定bIsReleased一開始為false */
  private boolean bIsReleased = false;
  /*設定bIsPaused一開始為false */
  private boolean bIsPaused = false; 
  public MediaPlayer mp;

  medplayer()
  {
    mp = new MediaPlayer();    
  }
  
  void play_voice(String filename)
  {
    try 
    { 
      if(mp.isPlaying()==true) 
      {
        mp.reset();            
      }
      mp.setDataSource( "/sdcard/" + filename);
      mp.setLooping(true);
      mp.prepare();
      mp.start(); 
    } 
    catch (IllegalStateException e) 
    { 
      // TODO Auto-generated catch block 
      e.printStackTrace(); 
    } 
    catch (IOException e) 
    { 
      // TODO Auto-generated catch block 
      e.printStackTrace(); 
    } 
     
    mp.setOnCompletionListener(new OnCompletionListener() 
    { 
      // @Override 
      public void onCompletion(MediaPlayer arg0) 
      {  
      } 
    });
    
  }
  
  void pause_voice()
  {
    if (mp != null) 
    { 
      if(bIsReleased == false) 
      { 
        if(bIsPaused==false) 
        { 
          /*設定 MediaPlayer暫停播放*/
          mp.pause(); 
          bIsPaused = true; 
        } 
        else if(bIsPaused==true) 
        { 
          /*設定 MediaPlayer播放*/
          mp.start(); 
          bIsPaused = false; 
        } 
      } 
    }
  }
  
  void stop_voice()
  {
    if(mp.isPlaying()==true) 
    { 
      /*將 MediaPlayer重設*/
      mp.reset(); 
    } 
  }
}
