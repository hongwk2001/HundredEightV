package com.tkprof.HundredEightV

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import android.widget.Toast
import java.io.IOException

object Util {
    private const val LOG_TAG = "Util"

    fun loadFile2String(context: Context, fileName: String?): String {
        return try {
            context.assets.open(fileName!!).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    private var soundPool: SoundPool? = null
    private var soundID = 0
    var loaded: Boolean = false

    fun initSound(activity: Activity, assetManager: AssetManager, fileName: String?, onLoaded: (() -> Unit)? = null) {
        // Set the hardware buttons to control the music
        activity.volumeControlStream = AudioManager.STREAM_MUSIC
        
        // Release previous soundPool to avoid memory leaks and track limits
        soundPool?.release()
        loaded = false

        soundPool = SoundPool.Builder().setMaxStreams(10).build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                loaded = true
                Log.d(LOG_TAG, "Sound loaded successfully: $fileName")
                onLoaded?.invoke()
            } else {
                loaded = false
                Log.e(LOG_TAG, "Sound load failed with status $status: $fileName")
            }
        }

        try {
            soundID = soundPool?.load(assetManager.openFd(fileName!!), 1) ?: 0
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error opening sound file: $fileName", e)
            Toast.makeText(activity, "$fileName Load Fail ", Toast.LENGTH_SHORT).show()
        }
    }

    fun playSound(activity: Activity) {
        val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val actualVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
        val volume = if (maxVolume > 0) actualVolume / maxVolume else 0.5f
        
        if (loaded) {
            soundPool?.play(soundID, volume, volume, 1, 0, 1f)
            Log.d(LOG_TAG, "Played sound ID: $soundID")
        } else {
            Log.d(LOG_TAG, "Sound not loaded yet")
        }
    }

    fun initNPlaySound(activity: Activity, assetManager: AssetManager, fileName: String?) {
        Log.d(LOG_TAG, "initNPlaySound: $fileName")
        initSound(activity, assetManager, fileName) {
            playSound(activity)
        }
    }
}
