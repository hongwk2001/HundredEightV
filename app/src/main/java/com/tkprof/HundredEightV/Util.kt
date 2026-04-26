package com.tkprof.HundredEightV

import android.app.Activity
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

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
        } 

    companion object {
        private const val LOG_TAG = "Util"

        val isExternalStorageReadable: Boolean
            /* Checks if external storage is available to at least read */
            get() {
                val state = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state ||
                        Environment.MEDIA_MOUNTED_READ_ONLY == state
            }


        @JvmStatic
        fun loadFile2String(act: Activity, fileName: String?): String {
            val builder = StringBuilder()
            val assetManager = act.getAssets()

            try {
                val reader =
                    BufferedReader(InputStreamReader(assetManager.open(fileName!!), "UTF-8"))
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    builder.append(line)
                }
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
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
            
            // Release previous soundPool to avoid memory leaks and track limits
            soundPool?.release()
            loaded = false

            soundPool = SoundPool.Builder().setMaxStreams(10).build()

            soundPool!!.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener {
                override fun onLoadComplete(
                    soundPool: SoundPool?, sampleId: Int,
                    status: Int
                ) {
                    if (status == 0) {
                        loaded = true
                        Log.d(LOG_TAG, "Sound loaded successfully: $fileName")
                    } else {
                        loaded = false
                        Log.e(LOG_TAG, "Sound load failed with status $status: $fileName")
                    }
                }
            })

            try {
                soundID = soundPool!!.load(asst.openFd(fileName!!), 1)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error opening sound file: $fileName", e)
                Toast.makeText(act, "$fileName Load Fail ", Toast.LENGTH_SHORT).show()
            }
        }

        @JvmStatic
        fun playSound(act: Activity) {
            val audioManager = act.getSystemService(AUDIO_SERVICE) as AudioManager?
            var actualVolume = 0f
            var maxVolume = 0f
            if (audioManager != null) {
                actualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
            }
            val volume = if (maxVolume > 0) actualVolume / maxVolume else 0.5f
            
            if (loaded) {
                soundPool!!.play(soundID, volume, volume, 1, 0, 1f)
                Log.d(LOG_TAG, "Played sound ID: $soundID")
            } else {
                Log.d(LOG_TAG, "Sound not loaded yet")
            }
        }


        fun initNPlaySound(act: Activity, asst: AssetManager, fileName: String?) {
            Log.d(LOG_TAG, "initNPlaySound: $fileName")
            
            act.setVolumeControlStream(AudioManager.STREAM_MUSIC)
            
            soundPool?.release()
            loaded = false

            soundPool = SoundPool.Builder().setMaxStreams(10).build()

            soundPool!!.setOnLoadCompleteListener(object : SoundPool.OnLoadCompleteListener {
                override fun onLoadComplete(
                    soundPool: SoundPool?, sampleId: Int,
                    status: Int
                ) {
                    if (status == 0) {
                        loaded = true
                        playSound(act)
                    } else {
                        Log.e(LOG_TAG, "initNPlaySound load failed: $status")
                    }
                }
            })

            try {
                soundID = soundPool!!.load(asst.openFd(fileName!!), 1)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "initNPlaySound error: $fileName", e)
                Toast.makeText(act, "$fileName Load Fail ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
