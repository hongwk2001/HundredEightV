package com.tkprof.HundredEightV

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.tkprof.HundredEightV.Util.initSound
import com.tkprof.HundredEightV.Util.loadFile2String
import com.tkprof.HundredEightV.Util.playSound
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener,
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    var ct: CountDownTimer? = null
    var ct_remain: CountDownTimer? = null
    var file_line_cnt: Int = 0
    private var previousCount = 0

    // Default value for user to just f_Start without setup
    var interval_sec: Double? = null


    var saveCurrentCount: Boolean = false

    var sharedPref: SharedPreferences? = null

    var tv_Cnt: TextView? = null
    var t_cnta: TextView? = null
    var t_cntb: TextView? = null

    var t_text: TextView? = null

    var cbx_tts_number: CheckBox? = null
    var cbx_tts_text: CheckBox? = null
    var tb1: ToggleButton? = null
    var progressBar: ProgressBar? = null


    var ttobj: TextToSpeech? = null
    
    private var ttsReady = false
    private var soundReady = false
    private var isAutoStartTriggered = false

    private var mDetector: GestureDetector? = null
    private var lastFlingTime: Long = 0

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        f_LoadVariables()
        saveCurrentCount = false
        toggle_on = false
    }


    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        sharedPref = sharedPreferences
        if (key == null) {
            f_LoadVariables()
            return
        }

        when (key) {
            "interval" -> {
                interval_sec = sharedPreferences.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4
                findViewById<TextView>(R.id.remain_e)?.text = String.format(Locale.US, "%.1f", interval_sec)
                Log.d("Main.onPrefChanged", "interval_sec updated: $interval_sec")
            }
            "bgcolor" -> applyBackgroundColor()
            "bellsound" -> {
                val sound_filename = sharedPreferences.getString("bellsound", getString(R.string.pref_default_bellsound))
                initSound(this, assets, getString(R.string.soundpath) + sound_filename)
            }
            "file_name" -> {
                val fileName = sharedPreferences.getString("file_name", "불교방송_나를_깨우는_108배.json") ?: "불교방송_나를_깨우는_108배.json"
                f_File2JsonArray(fileName)
            }
            "tts_number" -> {
                val checked = sharedPreferences.getBoolean("tts_number", true)
                if (cbx_tts_number?.isChecked != checked) {
                    cbx_tts_number?.isChecked = checked
                }
            }
            "tts_text" -> {
                val checked = sharedPreferences.getBoolean("tts_text", true)
                if (cbx_tts_text?.isChecked != checked) {
                    cbx_tts_text?.isChecked = checked
                }
            }
            "current_cnt", "count_a", "count_b" -> {
                // Optional: Update counts if changed elsewhere (e.g. SettingsActivity)
                tv_Cnt?.text = sharedPreferences.getString("current_cnt", "0")
                t_cnta?.text = sharedPreferences.getString("count_a", "0")
                t_cntb?.text = sharedPreferences.getString("count_b", "0")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        f_SaveSharedpref()

        // Automatically pause when the app loses focus (e.g., incoming phone call)
        if (toggle_on) {
            tb1?.isChecked = false
            toggle_on = false
            f_Pause()
        }
        Log.d("Main:onPause", "Called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        
        setupUI()

        f_CheckDailyReset()
        f_LoadVariables()
        
        mDetector = GestureDetector(this, this)
        mDetector!!.setOnDoubleTapListener(this)

        ttobj = TextToSpeech(
            getApplicationContext(),
            object : OnInitListener {
                override fun onInit(status: Int) {
                    if (status != TextToSpeech.ERROR) {
                        ttobj!!.setLanguage(Locale.KOREA)
                        ttsReady = true
                        attemptAutoStart()
                    }
                }
            })

        // Handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showPauseDialog()
            }
        })

        attemptAutoStart()
    }

    private fun attemptAutoStart() {
        if (intent.getBooleanExtra("AUTO_START", false) && ttsReady && soundReady && !isAutoStartTriggered) {
            isAutoStartTriggered = true
            tb1?.isChecked = true
            toggle_on = true
            f_Start(isResume = false)
        }
    }

    private fun showPauseDialog() {
        val wasRunning = toggle_on
        f_Pause()
        
        if (wasRunning) {
            tb1?.isChecked = false
            toggle_on = false
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_pause_custom, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_continue).setOnClickListener {
            tb1?.isChecked = true
            toggle_on = true
            f_Start(isResume = true)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_back_to_start).setOnClickListener {
            f_SaveSharedpref()
            val intent = Intent(this@MainActivity, StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_go_to_end).setOnClickListener {
            f_SaveSharedpref()
            val bowsDoneToday = t_cnta?.text?.toString()?.toIntOrNull() ?: 0
            val intent = Intent(this@MainActivity, EndActivity::class.java)
            intent.putExtra("DONE_TODAY", bowsDoneToday)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_exit_app).setOnClickListener {
            saveCurrentCount = true
            f_SaveSharedpref()
            finishAffinity()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun f_CheckDailyReset() {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = sdf.format(Date())
        val lastDate = sharedPref?.getString("last_use_date", "")
        
        if (lastDate != today) {
            sharedPref?.edit {
                putString("count_a", "0")
                putString("last_use_date", today)
            }
        }
    }

    private fun setupUI() {
        // Save current values if they exist
        val currentCnt = tv_Cnt?.text?.toString()
        val currentCntA = t_cnta?.text?.toString()
        val currentCntB = t_cntb?.text?.toString()
        val currentText = t_text?.text?.toString()
        val isToggled = tb1?.isChecked ?: false
        val isTtsNumberChecked = cbx_tts_number?.isChecked ?: sharedPref?.getBoolean("tts_number", true) ?: true
        val isTtsTextChecked = cbx_tts_text?.isChecked ?: sharedPref?.getBoolean("tts_text", true) ?: true

        setContentView(R.layout.activity_main)

        val mainView = findViewById<View>(R.id.rootLayout)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tv_Cnt = findViewById(R.id.count)
        t_cnta = findViewById(R.id.count_a)
        t_cntb = findViewById(R.id.count_b)
        t_text = findViewById(R.id.text)
        tb1 = findViewById(R.id.tgb_begin_pause)
        progressBar = findViewById(R.id.progress_remain)

        cbx_tts_number = findViewById(R.id.cbx_tts_number)
        cbx_tts_text = findViewById(R.id.cbx_tts_text)

        // Restore values
        if (currentCnt != null) tv_Cnt?.text = currentCnt
        if (currentCntA != null) t_cnta?.text = currentCntA
        if (currentCntB != null) t_cntb?.text = currentCntB
        if (currentText != null) t_text?.text = currentText
        tb1?.isChecked = isToggled
        
        cbx_tts_number?.isChecked = isTtsNumberChecked
        cbx_tts_text?.isChecked = isTtsTextChecked

        // Add listeners to save state immediately and prevent reverting when other preferences change
        cbx_tts_number?.setOnCheckedChangeListener { _, isChecked ->
            sharedPref?.edit()?.putBoolean("tts_number", isChecked)?.apply()
        }
        cbx_tts_text?.setOnCheckedChangeListener { _, isChecked ->
            sharedPref?.edit()?.putBoolean("tts_text", isChecked)?.apply()
        }

        // Re-apply background color
        applyBackgroundColor()
    }

    private fun changeInterval(delta: Double) {
        interval_sec = (interval_sec ?: 9.4) + delta
        if (interval_sec!! < 1.0) interval_sec = 1.0
        interval_sec = Math.round(interval_sec!! * 10.0) / 10.0
        saveInterval()
        findViewById<TextView>(R.id.remain_e)?.text = String.format(Locale.US, "%.1f", interval_sec)
        
        if (toggle_on) {
            f_RestartTimerOnly()
        }
        
        Toast.makeText(this, "Interval: $interval_sec", Toast.LENGTH_SHORT).show()
    }

    private fun f_RestartTimerOnly() {
        if (ct != null) {
            ct!!.cancel()
            ct = null
        }
        
        remainSecs()

        val current_cnt_val = tv_Cnt!!.getText().toString().toIntOrNull() ?: 0
        val remainingItemsAfterCurrent = file_line_cnt - current_cnt_val
        val interval_ms = (interval_sec!! * 1000).toLong()

        if (remainingItemsAfterCurrent > 0) {
            val total_ms = remainingItemsAfterCurrent * interval_ms
            // Add a small 200ms buffer and logic check to prevent an immediate skip
            ct = object : CountDownTimer(total_ms + 200, interval_ms) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished > total_ms) return
                    f_NextWords(1, true)
                }

                override fun onFinish() {
                    f_GoToEndActivity()
                }
            }
            ct!!.start()
        } else {
            f_GoToEndActivity()
        }
    }



    private fun applyBackgroundColor() {
        val currentLayout = findViewById<View?>(R.id.rootLayout)
        val colorName = sharedPref?.getString("bgcolor", "white") ?: "white"
        val colorResId = resources.getIdentifier(colorName, "color", packageName)

        if (colorResId != 0 && currentLayout != null) {
            currentLayout.setBackgroundColor(ContextCompat.getColor(this, colorResId))
        } else if (currentLayout != null) {
            currentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("saved_cnt", tv_Cnt?.text.toString())
        outState.putString("saved_cnta", t_cnta?.text.toString())
        outState.putString("saved_cntb", t_cntb?.text.toString())
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tv_Cnt?.text = savedInstanceState.getString("saved_cnt", "0")
        t_cnta?.text = savedInstanceState.getString("saved_cnta", "0")
        t_cntb?.text = savedInstanceState.getString("saved_cntb", "0")
        tb1?.isChecked = false
        saveCurrentCount = false
    }


    /* get saved values */
    private fun f_LoadVariables() {
        val current_val = sharedPref!!.getString("current_cnt", "0")
        if (tv_Cnt?.text.toString() == "0" || tv_Cnt?.text.toString() == "") {
             tv_Cnt!!.setText(current_val)
        }

        if (t_cnta?.text.toString() == "0" || t_cnta?.text.toString() == "") {
            t_cnta!!.setText(sharedPref!!.getString("count_a", "0"))
        }
        if (t_cntb?.text.toString() == "0" || t_cntb?.text.toString() == "") {
            t_cntb!!.setText(sharedPref!!.getString("count_b", "0"))
        }

        cbx_tts_number?.isChecked = sharedPref!!.getBoolean("tts_number", true)
        cbx_tts_text?.isChecked = sharedPref!!.getBoolean("tts_text", true)

        interval_sec = sharedPref!!.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4
        Log.d("Main.f_LoadVariables", "interval_sec: $interval_sec")

        findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.1f", interval_sec))

        val fileName: String = sharedPref!!.getString("file_name", "불교방송_나를_깨우는_108배.json")!!
        
        applyBackgroundColor()

        val sound_filename: String =
            sharedPref!!.getString("bellsound", getString(R.string.pref_default_bellsound))!!
        val assetManager = getAssets()
        initSound(this, assetManager, getString(R.string.soundpath) + sound_filename) {
            soundReady = true
            runOnUiThread { attemptAutoStart() }
        }

        f_File2JsonArray(fileName)
        
        // Update text if count > 0
        val count = tv_Cnt?.text?.toString()?.toIntOrNull() ?: 0
        if (count > 0) {
            f_ReadJsonObject(count)
        }
    }

    public override fun onDestroy() {
        super.onDestroy() 

        if (ct != null) {
            ct!!.cancel()
        }

        if (ttobj != null) {
            ttobj!!.stop()
            ttobj!!.shutdown()
        }
    }


    private fun f_SaveSharedpref() {
        val currnet_cnt = if (toggle_on || saveCurrentCount) tv_Cnt?.text?.toString() ?: "0" else "0"
        sharedPref?.edit {
            putString("current_cnt", currnet_cnt)
            putString("count_a", t_cnta?.text?.toString() ?: "0")
            putString("count_b", t_cntb?.text?.toString() ?: "0")
            putBoolean("tts_number", cbx_tts_number?.isChecked ?: true)
            putBoolean("tts_text", cbx_tts_text?.isChecked ?: true)
        }
    }




    private var endHandler: Handler? = null
    private var endRunnable: Runnable? = null
    var toggle_on: Boolean = false

    fun f_onToggleClicked(view: View) {
        val isChecked = (view as ToggleButton).isChecked()
        if (isChecked) {
            toggle_on = true
            f_Start(isResume = false)
        }
        else {
            showPauseDialog()
        }
    }

    fun f_Start(isResume: Boolean) {
        f_Pause() 

        var current_cnt_val = tv_Cnt!!.getText().toString().toIntOrNull() ?: 0
        interval_sec = sharedPref!!.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4

        if (!isResume && current_cnt_val >= file_line_cnt) {
            current_cnt_val = 0
            tv_Cnt!!.setText("0")
        }

        if (!isResume) {
            f_NextWords(1, true)
            current_cnt_val = tv_Cnt!!.getText().toString().toInt()
        } else {
            remainSecs()
        }

        val remainingItemsAfterCurrent = file_line_cnt - current_cnt_val
        val interval_ms = (interval_sec!! * 1000).toLong()

        if (remainingItemsAfterCurrent > 0) {
            val total_ms = remainingItemsAfterCurrent * interval_ms
            // Buffer to prevent immediate skip
            ct = object : CountDownTimer(total_ms + 200, interval_ms) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished > total_ms) return
                    f_NextWords(1, true)
                }

                override fun onFinish() {
                    f_GoToEndActivity()
                }
            }
            ct!!.start()
        } else {
            f_GoToEndActivity()
        }
    }

    fun f_Pause() {
        if (ct != null) {
            ct!!.cancel()
            ct = null
        }

        if (ct_remain != null) {
            ct_remain!!.cancel()
            ct_remain = null
        }

        endRunnable?.let { endHandler?.removeCallbacks(it) }

        ttobj?.stop()
    }

    private fun f_GoToEndActivity() {
        if (endHandler == null) {
            endHandler = Handler(Looper.getMainLooper())
        }
        endRunnable?.let { endHandler?.removeCallbacks(it) }

        endRunnable = Runnable {
            if (isFinishing || isDestroyed) return@Runnable

            tb1?.isChecked = false
            toggle_on = false
            saveCurrentCount = false
            f_Pause()
            f_SaveSharedpref()

            val bowsDoneToday = t_cnta?.text?.toString()?.toIntOrNull() ?: 0
            val intent = Intent(this@MainActivity, EndActivity::class.java)
            intent.putExtra("DONE_TODAY", bowsDoneToday)
            startActivity(intent)
            finish()
        }

        // Wait for the final interval to finish + 3 seconds extra
        val finalDelay = ((interval_sec ?: 9.4) * 1000).toLong() + 3000L
        endHandler?.postDelayed(endRunnable!!, finalDelay)
    }


    private fun f_ManualJump(i: Int) {
        val wasRunning = toggle_on
        f_Pause() 
        
        f_NextWords(i, true)

        if (wasRunning) {
            f_RestartTimerOnly()
        }
    }

    private fun f_NextWords(i: Int, setRemainSeconds: Boolean) {
        var Current_cnt = tv_Cnt!!.getText().toString().toIntOrNull() ?: 0

        val nextCnt = Current_cnt + i
        if (nextCnt < 0 || nextCnt > file_line_cnt) {
            return
        }
        
        Current_cnt = nextCnt

        if (sharedPref!!.getBoolean("play_sound", true)) {
            playSound(this)
        }

        f_ReadJsonObject(Current_cnt, shouldSpeak = true)

        if (setRemainSeconds) {
            remainSecs()
        }

        var Current_ca = t_cnta!!.getText().toString().toIntOrNull() ?: 0
        var Current_cb = t_cntb!!.getText().toString().toIntOrNull() ?: 0

        Current_ca = Current_ca + i
        Current_cb = Current_cb + i

        tv_Cnt!!.setText(String.format(Locale.US, "%d", Current_cnt))
        t_cnta!!.setText(String.format(Locale.US, "%d", Current_ca))
        t_cntb!!.setText(String.format(Locale.US, "%d", Current_cb))

        blinkView(tv_Cnt!!)
    }

    private fun blinkView(view: View) {
        view.animate().alpha(0.2f).setDuration(150).withEndAction {
            view.animate().alpha(1.0f).setDuration(150).start()
        }.start()
    }

    fun f_ReadText() {
        var toSpeak = ""
        if (cbx_tts_number?.isChecked == true) {
            toSpeak = tv_Cnt!!.getText().toString()
        }

        if (cbx_tts_text?.isChecked == true) {
            toSpeak = toSpeak + " " + t_text!!.getText().toString()
        }

        if (toSpeak.length > 0) {
            ttobj!!.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }


    fun f_onClickSlower(view: View) {
        changeInterval(0.2)
    }

    fun f_onClickFaster(view: View) {
        changeInterval(-0.2)
    }

    private fun saveInterval() {
        sharedPref?.edit { putString("interval", interval_sec.toString()) }
    }

    private fun remainSecs() {
        ct_remain?.cancel()
        val totalMs = ((interval_sec ?: 9.4) * 1000).toLong()
        progressBar?.max = totalMs.toInt()
        progressBar?.progress = totalMs.toInt()
        findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.1f", interval_sec))

        ct_remain = object : CountDownTimer(totalMs, 40) {
            override fun onTick(millisUntilFinished: Long) {
                val remain = millisUntilFinished.toDouble() / 1000
                findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.1f", remain))
                progressBar?.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                findViewById<TextView>(R.id.remain_e)?.setText("0.0")
                progressBar?.progress = 0
            }
        }.start()
    }

    private var jsonArray: JSONArray? = null

    private fun f_File2JsonArray(fileName: String) {
        try {
            val jsonString = loadFile2String(this, fileName)
            jsonArray = JSONArray(jsonString)
            file_line_cnt = jsonArray!!.length()
        } catch (e: JSONException) {
            e.printStackTrace()
            file_line_cnt = 0
        }
    }

    private fun f_ReadJsonObject(count: Int, shouldSpeak: Boolean = false) {
        if (jsonArray == null || count <= 0 || count > jsonArray!!.length()) {
            t_text?.setText("")
            previousCount = count
            return
        }

        try {
            val jsonObject = jsonArray!!.getJSONObject(count - 1)
            val text = jsonObject.getString("text")
            
            if (t_text?.text?.toString() != text) {
                val goingForward = count >= previousCount
                val screenWidth = t_text?.width?.toFloat() ?: 1000f
                val outTranslation = if (goingForward) -screenWidth else screenWidth
                val inTranslation = if (goingForward) screenWidth else -screenWidth

                t_text?.animate()?.translationX(outTranslation)?.alpha(0f)?.setDuration(200)?.withEndAction {
                    t_text?.setText(text)
                    t_text?.translationX = inTranslation
                    t_text?.animate()?.translationX(0f)?.alpha(1f)?.setDuration(200)?.withEndAction {
                        if (shouldSpeak) f_ReadText()
                    }?.start()
                }?.start()
            } else {
                t_text?.setText(text)
                if (shouldSpeak) f_ReadText()
            }
            previousCount = count
        } catch (e: JSONException) {
            e.printStackTrace()
            t_text?.setText("")
        }
    }

    companion object {
        const val SETTING_ACTIVITY = 1
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mDetector!!.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        event1: MotionEvent?,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            if (event1 != null) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFlingTime < 300) {
                    return false 
                }
                
                val diffY = event2.y - event1.y
                val diffX = event2.x - event1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        lastFlingTime = currentTime
                        if (diffX > 0) {
                            f_ManualJump(-1)
                        } else {
                            f_ManualJump(1)
                        }
                        return true
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }

    override fun onLongPress(event: MotionEvent) {}

    override fun onScroll(
        event1: MotionEvent?,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onShowPress(event: MotionEvent) {}

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        return false
    }
}
