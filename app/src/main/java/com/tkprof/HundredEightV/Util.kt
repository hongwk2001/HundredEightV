package com.tkprof.HundredEightV

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.preference.PreferenceManager
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

    fun migratePreferences(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val keysToMigrate = listOf("tts_speed", "tts_pitch", "interval", "count_a", "count_b", "current_cnt")
        
        sharedPref.edit {
            // Migrate renamed asset files
            val currentFileName = sharedPref.getString("file_name", null)
            when (currentFileName) {
                "법구경_p1_106.json" -> putString("file_name", "법구경_p1_108.json")
                "법구경_p2_105.json" -> putString("file_name", "법구경_p2_105.json")
                "법구경_p2_106.json" -> putString("file_name", "법구경_p2_105.json")
                "법구경_p3_106.json" -> putString("file_name", "법구경_p3_109.json")
                "법구경_p4_103.json" -> putString("file_name", "법구경_p4_106.json")
            }

            keysToMigrate.forEach { key ->
                try {
                    // Try reading as String, which is what the current app expects
                    sharedPref.getString(key, null)
                } catch (e: ClassCastException) {
                    // If it fails, it's likely an Int or Float from an older version
                    Log.d(LOG_TAG, "Migrating key: $key from Int/Float to String")
                    try {
                        val intValue = sharedPref.getInt(key, -1)
                        if (intValue != -1) {
                            val stringValue = if (key == "tts_speed" || key == "tts_pitch") {
                                (intValue / 10.0).toString()
                            } else {
                                intValue.toString()
                            }
                            putString(key, stringValue)
                        }
                    } catch (e2: ClassCastException) {
                        try {
                            val floatValue = sharedPref.getFloat(key, -1f)
                            if (floatValue != -1f) {
                                putString(key, floatValue.toString())
                            }
                        } catch (e3: Exception) {
                            Log.e(LOG_TAG, "Failed to migrate key: $key", e3)
                        }
                    }
                }
            }
        }
    }
}
