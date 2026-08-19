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
import com.tkprof.HundredEightV.R
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
    var remainTimer: CountDownTimer? = null
    var fileLineCount: Int = 0
    private var previousCount = 0

    // Default value for user to just startVows without setup
    var intervalSec: Double? = null


    var saveCurrentCount: Boolean = false

    var sharedPref: SharedPreferences? = null

    var tvCount: TextView? = null
    var tvCountToday: TextView? = null
    var tvCountTotal: TextView? = null

    var tvVowText: TextView? = null

    var cbTtsNumber: CheckBox? = null
    var cbTtsText: CheckBox? = null
    var btnToggle: ToggleButton? = null
    var progressBar: ProgressBar? = null


    var ttobj: TextToSpeech? = null
    
    private var ttsReady = false
    private var soundReady = false
    private var isAutoStartTriggered = false

    private var mDetector: GestureDetector? = null
    private var lastFlingTime: Long = 0

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        loadVariables()
        saveCurrentCount = false
        toggle_on = false
    }


    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        sharedPref = sharedPreferences
        if (key == null) {
            loadVariables()
            return
        }

        when (key) {
            "interval" -> {
                intervalSec = sharedPreferences.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4
                findViewById<TextView>(R.id.remain_e)?.text = String.format(Locale.US, "%.2f", intervalSec)
                Log.d("Main.onPrefChanged", "intervalSec updated: $intervalSec")
            }
            "bgcolor" -> applyBackgroundColor()
            "bellsound" -> {
                val sound_filename = sharedPreferences.getString("bellsound", getString(R.string.pref_default_bellsound))
                initSound(this, assets, getString(R.string.soundpath) + sound_filename)
            }
            "file_name" -> {
                val fileName = sharedPreferences.getString("file_name", "불교방송_나를_깨우는_108배.json") ?: "불교방송_나를_깨우는_108배.json"
                file2JsonArray(fileName)
            }
            "tts_number" -> {
                val checked = sharedPreferences.getBoolean("tts_number", true)
                if (cbTtsNumber?.isChecked != checked) {
                    cbTtsNumber?.isChecked = checked
                }
            }
            "tts_text" -> {
                val checked = sharedPreferences.getBoolean("tts_text", true)
                if (cbTtsText?.isChecked != checked) {
                    cbTtsText?.isChecked = checked
                }
            }
            "tts_speed", "tts_pitch", "tts_voice" -> applyTtsSettings()
            "current_cnt", "count_a", "count_b" -> {
                // Optional: Update counts if changed elsewhere (e.g. SettingsActivity)
                tvCount?.text = sharedPreferences.getString("current_cnt", "0")
                tvCountToday?.text = sharedPreferences.getString("count_a", "0")
                tvCountTotal?.text = sharedPreferences.getString("count_b", "0")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPref?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPref?.unregisterOnSharedPreferenceChangeListener(this)
        saveSharedPrefs()

        // Automatically pause when the app loses focus (e.g., incoming phone call)
        if (toggle_on) {
            btnToggle?.isChecked = false
            toggle_on = false
            pauseVows()
        }
        Log.d("Main:onPause", "Called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        Util.migratePreferences(this)
        
        setupUI()

        findViewById<View>(R.id.rootLayout)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        checkDailyReset()
        loadVariables()
        
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
            btnToggle?.isChecked = true
            toggle_on = true
            startVows(isResume = false)
        }
    }

    private fun showPauseDialog() {
        val wasRunning = toggle_on
        pauseVows()
        
        if (wasRunning) {
            btnToggle?.isChecked = false
            toggle_on = false
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_pause_custom, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_continue).setOnClickListener {
            btnToggle?.isChecked = true
            toggle_on = true
            startVows(isResume = true)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_back_to_start).setOnClickListener {
            saveSharedPrefs()
            val intent = Intent(this@MainActivity, StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_go_to_end).setOnClickListener {
            saveSharedPrefs()
            val bowsDoneToday = tvCountToday?.text?.toString()?.toIntOrNull() ?: 0
            val intent = Intent(this@MainActivity, EndActivity::class.java)
            intent.putExtra("DONE_TODAY", bowsDoneToday)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_exit_app).setOnClickListener {
            saveCurrentCount = true
            saveSharedPrefs()
            finishAffinity()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkDailyReset() {
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
        val currentCnt = tvCount?.text?.toString()
        val currentCntA = tvCountToday?.text?.toString()
        val currentCntB = tvCountTotal?.text?.toString()
        val currentText = tvVowText?.text?.toString()
        val isToggled = btnToggle?.isChecked ?: false
        val isTtsNumberChecked = cbTtsNumber?.isChecked ?: sharedPref?.getBoolean("tts_number", true) ?: true
        val isTtsTextChecked = cbTtsText?.isChecked ?: sharedPref?.getBoolean("tts_text", true) ?: true

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

        tvCount = findViewById(R.id.count)
        tvCountToday = findViewById(R.id.count_a)
        tvCountTotal = findViewById(R.id.count_b)
        tvVowText = findViewById(R.id.text)
        btnToggle = findViewById(R.id.tgb_begin_pause)
        progressBar = findViewById(R.id.progress_remain)

        cbTtsNumber = findViewById(R.id.cbx_tts_number)
        cbTtsText = findViewById(R.id.cbx_tts_text)

        // Restore values
        if (currentCnt != null) tvCount?.text = currentCnt
        if (currentCntA != null) tvCountToday?.text = currentCntA
        if (currentCntB != null) tvCountTotal?.text = currentCntB
        if (currentText != null) tvVowText?.text = currentText
        btnToggle?.isChecked = isToggled
        
        cbTtsNumber?.isChecked = isTtsNumberChecked
        cbTtsText?.isChecked = isTtsTextChecked

        // Add listeners to save state immediately and prevent reverting when other preferences change
        cbTtsNumber?.setOnCheckedChangeListener { _, isChecked ->
            sharedPref?.edit { putBoolean("tts_number", isChecked) }
        }
        cbTtsText?.setOnCheckedChangeListener { _, isChecked ->
            sharedPref?.edit { putBoolean("tts_text", isChecked) }
        }

        // Re-apply background color
        applyBackgroundColor()
    }

    private fun changeInterval(delta: Double) {
        intervalSec = (intervalSec ?: 9.4) + delta
        if (intervalSec!! < 1.0) intervalSec = 1.0
        intervalSec = kotlin.math.round(intervalSec!! * 100.0) / 100.0
        saveInterval()
        findViewById<TextView>(R.id.remain_e)?.text = String.format(Locale.US, "%.2f", intervalSec)
        
        if (toggle_on) {
            restartTimerOnly()
        }
        
        Toast.makeText(this, "Interval: $intervalSec", Toast.LENGTH_SHORT).show()
    }

    private fun restartTimerOnly() {
        if (ct != null) {
            ct!!.cancel()
            ct = null
        }
        
        remainSecs()

        val current_cnt_val = tvCount?.text?.toString()?.toIntOrNull() ?: 0
        val remainingItemsAfterCurrent = fileLineCount - current_cnt_val
        val interval_ms = (intervalSec!! * 1000).toLong()

        if (remainingItemsAfterCurrent > 0) {
            val total_ms = remainingItemsAfterCurrent * interval_ms
            // Add a small 200ms buffer and logic check to prevent an immediate skip
            ct = object : CountDownTimer(total_ms + 200, interval_ms) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished > total_ms) return
                    nextWords(1, true)
                }

                override fun onFinish() {
                    goToEndActivity()
                }
            }
            ct!!.start()
        } else {
            goToEndActivity()
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

    private fun applyTtsSettings() {
        if (ttobj == null || !ttsReady) return

        val speedStr = sharedPref?.getString("tts_speed", "1.0") ?: "1.0"
        val speed = speedStr.toFloatOrNull() ?: 1.0f
        ttobj?.setSpeechRate(speed)

        val pitchStr = sharedPref?.getString("tts_pitch", "1.0") ?: "1.0"
        val pitch = pitchStr.toFloatOrNull() ?: 1.0f
        ttobj?.setPitch(pitch)

        val voiceId = sharedPref?.getString("tts_voice", null)
        if (voiceId != null) {
            val voice = ttobj?.voices?.find { it.name == voiceId }
            if (voice != null) {
                ttobj?.voice = voice
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("saved_cnt", tvCount?.text.toString())
        outState.putString("saved_cnta", tvCountToday?.text.toString())
        outState.putString("saved_cntb", tvCountTotal?.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tvCount?.text = savedInstanceState.getString("saved_cnt", "0")
        tvCountToday?.text = savedInstanceState.getString("saved_cnta", "0")
        tvCountTotal?.text = savedInstanceState.getString("saved_cntb", "0")
        btnToggle?.isChecked = false
        saveCurrentCount = false
    }


    /* get saved values */
    private fun loadVariables() {
        val current_val = sharedPref!!.getString("current_cnt", "0")
        if (tvCount?.text.toString() == "0" || tvCount?.text.toString() == "") {
             tvCount!!.setText(current_val)
        }

        if (tvCountToday?.text.toString() == "0" || tvCountToday?.text.toString() == "") {
            tvCountToday!!.setText(sharedPref!!.getString("count_a", "0"))
        }
        if (tvCountTotal?.text.toString() == "0" || tvCountTotal?.text.toString() == "") {
            tvCountTotal!!.setText(sharedPref!!.getString("count_b", "0"))
        }

        cbTtsNumber?.isChecked = sharedPref!!.getBoolean("tts_number", true)
        cbTtsText?.isChecked = sharedPref!!.getBoolean("tts_text", true)

        intervalSec = sharedPref!!.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4
        Log.d("Main.loadVariables", "intervalSec: $intervalSec")

        findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.2f", intervalSec))

        val fileName: String = sharedPref!!.getString("file_name", "불교방송_나를_깨우는_108배.json")!!
        
        applyBackgroundColor()
        applyTtsSettings()

        val sound_filename: String =
            sharedPref!!.getString("bellsound", getString(R.string.pref_default_bellsound))!!
        val assetManager = getAssets()
        initSound(this, assetManager, getString(R.string.soundpath) + sound_filename) {
            soundReady = true
            runOnUiThread { attemptAutoStart() }
        }

        file2JsonArray(fileName)
        
        // Update text if count > 0
        val count = tvCount?.text?.toString()?.toIntOrNull() ?: 0
        if (count > 0) {
            readJsonObject(count)
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


    private fun saveSharedPrefs() {
        val currnet_cnt = if (toggle_on || saveCurrentCount) tvCount?.text?.toString() ?: "0" else "0"
        sharedPref?.edit {
            putString("current_cnt", currnet_cnt)
            putString("count_a", tvCountToday?.text?.toString() ?: "0")
            putString("count_b", tvCountTotal?.text?.toString() ?: "0")
            putBoolean("tts_number", cbTtsNumber?.isChecked ?: true)
            putBoolean("tts_text", cbTtsText?.isChecked ?: true)
        }
    }




    private var endHandler: Handler? = null
    private var endRunnable: Runnable? = null
    var toggle_on: Boolean = false

    fun f_onToggleClicked(view: View) {
        val isChecked = (view as ToggleButton).isChecked
        if (isChecked) {
            toggle_on = true
            startVows(isResume = false)
        }
        else {
            showPauseDialog()
        }
    }

    fun startVows(isResume: Boolean) {
        pauseVows() 

        var current_cnt_val = tvCount?.text?.toString()?.toIntOrNull() ?: 0
        intervalSec = sharedPref!!.getString("interval", "9.4")?.toDoubleOrNull() ?: 9.4

        if (!isResume && current_cnt_val >= fileLineCount) {
            current_cnt_val = 0
            tvCount?.text = "0"
        }

        if (!isResume) {
            nextWords(1, true)
            current_cnt_val = tvCount?.text?.toString()?.toInt() ?: 0
        } else {
            remainSecs()
        }

        val remainingItemsAfterCurrent = fileLineCount - current_cnt_val
        val interval_ms = (intervalSec!! * 1000).toLong()

        if (remainingItemsAfterCurrent > 0) {
            val total_ms = remainingItemsAfterCurrent * interval_ms
            // Buffer to prevent immediate skip
            ct = object : CountDownTimer(total_ms + 200, interval_ms) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished > total_ms) return
                    nextWords(1, true)
                }

                override fun onFinish() {
                    goToEndActivity()
                }
            }
            ct!!.start()
        } else {
            goToEndActivity()
        }
    }

    fun pauseVows() {
        if (ct != null) {
            ct!!.cancel()
            ct = null
        }

        if (remainTimer != null) {
            remainTimer!!.cancel()
            remainTimer = null
        }

        endRunnable?.let { endHandler?.removeCallbacks(it) }

        ttobj?.stop()
    }

    private fun goToEndActivity() {
        if (endHandler == null) {
            endHandler = Handler(Looper.getMainLooper())
        }
        endRunnable?.let { endHandler?.removeCallbacks(it) }

        endRunnable = Runnable {
            if (isFinishing || isDestroyed) return@Runnable

            btnToggle?.isChecked = false
            toggle_on = false
            saveCurrentCount = false
            pauseVows()
            saveSharedPrefs()

            val bowsDoneToday = tvCountToday?.text?.toString()?.toIntOrNull() ?: 0
            val intent = Intent(this@MainActivity, EndActivity::class.java)
            intent.putExtra("DONE_TODAY", bowsDoneToday)
            startActivity(intent)
            finish()
        }

        // Wait for the final interval to finish + 3 seconds extra
        val finalDelay = ((intervalSec ?: 9.4) * 1000).toLong() + 3000L
        endHandler?.postDelayed(endRunnable!!, finalDelay)
    }


    private fun manualJump(i: Int) {
        val wasRunning = toggle_on
        pauseVows() 
        
        nextWords(i, true)

        if (wasRunning) {
            restartTimerOnly()
        }
    }

    private fun nextWords(i: Int, setRemainSeconds: Boolean) {
        var currentCnt = tvCount?.text?.toString()?.toIntOrNull() ?: 0

        val nextCnt = currentCnt + i
        if (nextCnt < 0 || nextCnt > fileLineCount) {
            return
        }
        
        currentCnt = nextCnt

        if (sharedPref!!.getBoolean("play_sound", true)) {
            playSound(this)
        }

        readJsonObject(currentCnt, shouldSpeak = true)

        if (setRemainSeconds) {
            remainSecs()
        }

        var currentCa = tvCountToday?.text?.toString()?.toIntOrNull() ?: 0
        var currentCb = tvCountTotal?.text?.toString()?.toIntOrNull() ?: 0

        currentCa += i
        currentCb += i

        tvCount?.text = String.format(Locale.US, "%d", currentCnt)
        tvCountToday?.text = String.format(Locale.US, "%d", currentCa)
        tvCountTotal?.text = String.format(Locale.US, "%d", currentCb)

        tvCount?.let { blinkView(it) }
    }

    private fun blinkView(view: View) {
        view.animate().alpha(0.2f).setDuration(150).withEndAction {
            view.animate().alpha(1.0f).setDuration(150).start()
        }.start()
    }

    fun readText() {
        var toSpeak = ""
        if (cbTtsNumber?.isChecked == true) {
            toSpeak = tvCount?.text?.toString() ?: ""
        }

        if (cbTtsText?.isChecked == true) {
            toSpeak = toSpeak + " " + (tvVowText?.text?.toString() ?: "")
        }

        if (toSpeak.isNotEmpty()) {
            ttobj?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }


    fun onClickSlower(view: View) {
        changeInterval(0.05)
    }

    fun onClickFaster(view: View) {
        changeInterval(-0.05)
    }

    private fun saveInterval() {
        sharedPref?.edit { putString("interval", intervalSec.toString()) }
    }

    private fun remainSecs() {
        remainTimer?.cancel()
        val totalMs = ((intervalSec ?: 9.4) * 1000).toLong()
        progressBar?.max = totalMs.toInt()
        progressBar?.progress = totalMs.toInt()
        findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.2f", intervalSec))

        remainTimer = object : CountDownTimer(totalMs, 40) {
            override fun onTick(millisUntilFinished: Long) {
                val remain = millisUntilFinished.toDouble() / 1000
                findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.2f", remain))
                progressBar?.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                findViewById<TextView>(R.id.remain_e)?.setText("0.00")
                progressBar?.progress = 0
            }
        }.start()
    }

    private var jsonArray: JSONArray? = null

    private fun file2JsonArray(fileName: String) {
        try {
            val jsonString = loadFile2String(this, fileName)
            jsonArray = JSONArray(jsonString)
            fileLineCount = jsonArray!!.length()
        } catch (e: JSONException) {
            e.printStackTrace()
            fileLineCount = 0
        }
    }

    private fun readJsonObject(count: Int, shouldSpeak: Boolean = false) {
        if (jsonArray == null || count <= 0 || count > jsonArray!!.length()) {
            tvVowText?.setText("")
            previousCount = count
            return
        }

        try {
            val jsonObject = jsonArray!!.getJSONObject(count - 1)
            val text = jsonObject.getString("text")
            
            if (tvVowText?.text?.toString() != text) {
                val goingForward = count >= previousCount
                val screenWidth = tvVowText?.width?.toFloat() ?: 1000f
                val outTranslation = if (goingForward) -screenWidth else screenWidth
                val inTranslation = if (goingForward) screenWidth else -screenWidth

                tvVowText?.animate()?.translationX(outTranslation)?.alpha(0f)?.setDuration(200)?.withEndAction {
                    tvVowText?.setText(text)
                    tvVowText?.translationX = inTranslation
                    tvVowText?.animate()?.translationX(0f)?.alpha(1f)?.setDuration(200)?.withEndAction {
                        if (shouldSpeak) readText()
                    }?.start()
                }?.start()
            } else {
                tvVowText?.setText(text)
                if (shouldSpeak) readText()
            }
            previousCount = count
        } catch (e: JSONException) {
            e.printStackTrace()
            tvVowText?.setText("")
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
                            manualJump(-1)
                        } else {
                            manualJump(1)
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
