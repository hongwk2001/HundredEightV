package com.tkprof.hundredeightv;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util extends Activity{
	  private static final String LOG_TAG = "Util";

	  /* Checks if external storage is available for read and write */
	  public boolean isExternalStorageWritable() {
	      String state = Environment.getExternalStorageState();
          return Environment.MEDIA_MOUNTED.equals(state);
      }

	  /* Checks if external storage is available to at least read */
	  public static boolean isExternalStorageReadable() {
	      String state = Environment.getExternalStorageState();
          return Environment.MEDIA_MOUNTED.equals(state) ||
                  Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
      }
	  
	  // 


	    public static String loadFile2String(Activity act, String fileName) {
	    	  StringBuilder builder = new StringBuilder();

	    		//File myFile =   Util.openFile(fileName); 
	    		
	    		AssetManager assetManager = act.getAssets();
	    		
	    		try {
	    	        BufferedReader reader = 
	    	        		//new BufferedReader(new InputStreamReader(new FileInputStream(myFile), "UTF-8"));
	    	        		new BufferedReader(new InputStreamReader(assetManager.open(fileName), "UTF-8"));
	    	        String line;
	    	        while ((line = reader.readLine()) != null) {
	    	          builder.append(line);
	    	        }
	    	        reader.close();

	    	    } catch (IOException e) {
	    	      e.printStackTrace();
	    	    }
	    	    return builder.toString();
	    	  }

	  private static SoundPool soundPool;
	  private static int soundID;
	  static boolean  loaded = false;

	  public static void initSound(final Activity act, AssetManager asst, String fileName){
	      // Set the hardware buttons to control the music
	      act.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	      // Load the sound
	//      soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
          soundPool = new SoundPool.Builder() .setMaxStreams(10)  .build();

          soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
	        @Override
	        public void onLoadComplete(SoundPool soundPool, int sampleId,
	            int status) {
	          loaded = true;
	         // playSound(act);  // for debug
	        }
	      }); 
	     // soundID = soundPool.load(act,  R.raw.templebell_soundbiiblecom_756181215, 1); 
	      try {
			soundID = soundPool.load(asst.openFd( fileName), 1);
		//	Toast.makeText(act, fileName + "loaded", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(act, fileName + " Fail ", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	  }
      
	    public static  void playSound(Activity act ) {
	        // Getting the user sound settings
	        AudioManager audioManager = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
            float actualVolume  = 0f, maxVolume = 0f;
	        if ( audioManager != null ) {
                 actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                 maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            }
	        float volume       = actualVolume / maxVolume;
	        // Is the sound loaded already?
	        if (loaded) {
	          soundPool.play(soundID, volume, volume, 1, 0, 1f);
	          Log.d("Test", "Played sound");
	        } else {
	        	Log.d("Util.playSound", "sound not loaded");
	        }

	      // return false;
	    }


		  public static void initNPlaySound(final Activity act, AssetManager asst, String fileName){
			  
			  Log.d("Util.initNPlaySound", fileName );
		    // Set the hardware buttons to control the music
			      act.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			      // Load the sound
			 //     soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			  soundPool = new SoundPool.Builder() .setMaxStreams(10)  .build();

			      soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			        @Override
			        public void onLoadComplete(SoundPool soundPool, int sampleId,
			            int status) {
			          loaded = true;
			          playSound(act);  // for debug
			        }
			      }); 
			     // soundID = soundPool.load(act,  R.raw.templebell_soundbiiblecom_756181215, 1); 
			      try {
					soundID = soundPool.load(asst.openFd(fileName), 1);
				//	Toast.makeText(act, fileName + "loaded", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(act, fileName + " Fail ", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			  }
		       
	
	    public File getDir() {
	        // Get the directory for the user's public pictures directory. 
	        File file = new File(Environment.getExternalStoragePublicDirectory(
	                Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name));
	        
	        if(!file.exists()) {
		        if (!file.mkdirs()) {
		            Log.e(LOG_TAG, "Directory not created");
		        } 
	        }
	        return file;
	    }
  
		 
/*	    public void download1( ) {
		      DownloadWebPageTask task = new DownloadWebPageTask();
		      // Hardcode v3 .
		      task.execute(new String[] {  "http://www.tkprof.com/bookmark.htm", 
		    		  "http://www.tkprof.com/bookmark.htm",
		    		  "http://www.tkprof.com/bookmark.htm"}); 
		}
	    */
	   /* public void initialsetup(SharedPreferences sp) {
	    	if(sp.getString( "initsetup_done", "No").equals("Yes")){
	    		download();
	    		sp.edit().putString("initsetup_done", "Yes");
	    	}
	    }*/
	} 