package com.tkprof.hundredeightv

import android.app.Activity
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.nio.file.NoSuchFileException

class Util : Activity() {
    val isExternalStorageWritable: Boolean
        /* Checks if external storage is available for read and write */
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    val dir: File
        get() {
            // Get the directory for the user's public pictures directory. 
            val file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ), getString(R.string.app_name)
            )

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e(
                        LOG_TAG,
                        "Directory not created"
                    )
                }
            }
            return file
        } /*	    public void download1( ) {
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


    companion object {
        private const val LOG_TAG = "Util"

        val isExternalStorageReadable: Boolean
            /* Checks if external storage is available to at least read */
            get() {
                val state = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state ||
                        Environment.MEDIA_MOUNTED_READ_ONLY == state
            }


        // 
		@JvmStatic
		fun loadFile2String(act: Activity, fileName: String?): String {
            val builder = StringBuilder()

            //File myFile =   Util.openFile(fileName); 
            val assetManager = act.getAssets()

            try {
                val reader =
                    //new BufferedReader(new InputStreamReader(new FileInputStream(myFile), "UTF-8"));
                    BufferedReader(
                        InputStreamReader(
                            assetManager.open(fileName!!),
                            StandardCharsets.UTF_8
                        )
                    )
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    builder.append(line)
                }
                reader.close()
            } catch (e: IOException) {
                Log.e(
                    "MyActivityTag",
                    "Error during I/O operation: " + e.message,
                    e
                ) // Include the exception for the full stack trace in logs
            }
            return builder.toString()
        }

        private var soundPool: SoundPool? = null
        private var soundID = 0
        var loaded: Boolean = false

        @JvmStatic
		fun initSound(act: Activity, asst: AssetManager, fileName: String?) {
            // Set the hardware buttons to control the music
            act.setVolumeControlStream(AudioManager.STREAM_MUSIC)
            // Load the sound
            //      soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            soundPool = SoundPool.Builder().setMaxStreams(10).build()

            soundPool!!.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener {
                override fun onLoadComplete(
                    soundPool: SoundPool?, sampleId: Int,
                    status: Int
                ) {
                    loaded = true
                }
            })
            try {
                soundID = soundPool!!.load(asst.openFd(fileName!!), 1)
                //	Toast.makeText(act, fileName + "loaded", Toast.LENGTH_SHORT).show();
            } catch (e: IOException) {
                Toast.makeText(act, fileName + " Fail ", Toast.LENGTH_SHORT).show()
                Log.e(
                    "MyActivityTag",
                    "Error initSound: " + e.message,
                    e
                ) // Include the exception for the full stack trace in logs
            }
        }

        @JvmStatic
		fun playSound(act: Activity) {
            // Getting the user sound settings
            val audioManager = act.getSystemService(AUDIO_SERVICE) as AudioManager?
            var actualVolume = 0f
            var maxVolume = 0f
            if (audioManager != null) {
                actualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            }
            val volume = actualVolume / maxVolume
            // Is the sound loaded already?
            if (loaded) {
                soundPool!!.play(soundID, volume, volume, 1, 0, 1f)
                Log.d("Test", "Played sound")
            } else {
                Log.d("Util.playSound", "sound not loaded")
            }

            // return false;
        }


        fun initNPlaySound(act: Activity, asst: AssetManager, fileName: String?) {
            Log.d("Util.initNPlaySound", fileName!!)
            // Set the hardware buttons to control the music
            act.setVolumeControlStream(AudioManager.STREAM_MUSIC)
            // Load the sound
            //     soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            soundPool = SoundPool.Builder().setMaxStreams(10).build()

            soundPool!!.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener {
                override fun onLoadComplete(
                    soundPool: SoundPool?, sampleId: Int,
                    status: Int
                ) {
                    loaded = true
                    playSound(act) // for debug
                }
            })

            // soundID = soundPool.load(act,  R.raw.templebell_soundbiiblecom_756181215, 1); 
            try {
                soundID = soundPool!!.load(asst.openFd(fileName), 1)
                //	Toast.makeText(act, fileName + "loaded", Toast.LENGTH_SHORT).show();
            } catch (e: IOException) {
                // Log the detailed exception for developers
                Log.e(
                    "FileOperations",
                    "Error accessing file: " + fileName,
                    e
                ) // Added a tag and more context

                // Provide more specific user feedback
                var errorMessage = "Failed to access " + fileName + ". "
                if (e is NoSuchFileException) { // Example of checking specific exception type
                    errorMessage += "The file was not found."
                } else if (e is FileNotFoundException) {
                    errorMessage += "The file could not be found or opened."
                } else if (e is SocketTimeoutException) { // If this could be network related
                    errorMessage += "The connection timed out. Please check your network."
                } else {
                    errorMessage += "Please try again later." // Generic fallback
                }

                // It's often better to show a Snackbar for errors that don't require immediate dismissal
                // or for longer messages.
                Snackbar.make(
                    act.findViewById<View?>(android.R.id.content),
                    errorMessage,
                    Snackbar.LENGTH_LONG
                ).show()
                // Or, if a Toast is preferred for its brevity:
                // Toast.makeText(act, errorMessage, Toast.LENGTH_LONG).show(); // Use LENGTH_LONG for errors
            }
        }
    }
}