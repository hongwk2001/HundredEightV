package com.tkprof.HundredEightV

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.tkprof.HundredEightV.Util.Companion.initSound
import com.tkprof.HundredEightV.Util.Companion.loadFile2String
import com.tkprof.HundredEightV.Util.Companion.playSound
import org.json.JSONArray
import org.json.JSONException
import java.util.Locale

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener,
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    var ct: CountDownTimer? = null
    var ct_remain: CountDownTimer? = null
    var file_line_cnt: Int = 0

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


    var ttobj: TextToSpeech? = null

    private var mDetector: GestureDetectorCompat? = null


    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        sharedPref = sharedPreferences
        f_LoadVariables()
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

        f_LoadVariables()
        val assetManager = getAssets()

        val audio_file = (getString(R.string.soundpath)
                + sharedPref!!.getString("bellsound", getString(R.string.pref_default_bellsound)))

        initSound(this, assetManager, audio_file)

        mDetector = GestureDetectorCompat(this, this)
        mDetector!!.setOnDoubleTapListener(this)

        ttobj = TextToSpeech(
            getApplicationContext(),
            object : OnInitListener {
                override fun onInit(status: Int) {
                    if (status != TextToSpeech.ERROR) {
                        ttobj!!.setLanguage(Locale.KOREA)
                    }
                }
            })
    }

    private fun setupUI() {
        // Save current values if they exist
        val currentCnt = tv_Cnt?.text?.toString()
        val currentCntA = t_cnta?.text?.toString()
        val currentCntB = t_cntb?.text?.toString()
        val currentText = t_text?.text?.toString()
        val isToggled = tb1?.isChecked ?: false

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main)
        } else {
            setContentView(R.layout.activity_main_land)
        }

        val mainView = findViewById<View>(R.id.mainLinearLayout1)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply the insets as padding to the view. This ensures that the 
                // content doesn't overlap with system UI elements like the status bar or navigation bar.
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
        tb1 = findViewById(R.id.tgbBeginPause)

        cbx_tts_number = findViewById(R.id.cbx_tts_number)
        cbx_tts_text = findViewById(R.id.cbx_tts_text)

        // Restore values
        if (currentCnt != null) tv_Cnt?.text = currentCnt
        if (currentCntA != null) t_cnta?.text = currentCntA
        if (currentCntB != null) t_cntb?.text = currentCntB
        if (currentText != null) t_text?.text = currentText
        tb1?.isChecked = isToggled

        // Re-apply background color
        applyBackgroundColor()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupUI()
    }

    private fun applyBackgroundColor() {
        val currentLayout = findViewById<View?>(R.id.mainLinearLayout1) as LinearLayout?
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

        cbx_tts_number!!.setChecked(sharedPref!!.getBoolean("tts_number", true))
        cbx_tts_text!!.setChecked(sharedPref!!.getBoolean("tts_text", true))

        interval_sec = sharedPref!!.getString("interval", "9.4")!!.toDouble()
        Log.d("Main.f_LoadVariables", "interval_sec:" + interval_sec)

        findViewById<TextView>(R.id.remain_e)?.setText(String.format(Locale.US, "%.1f", interval_sec))

        val fileName: String = sharedPref!!.getString("file_name", "108vow.txt")!!
        file_line_cnt = sharedPref!!.getString("file_line_cnt", "108")!!.toInt()

        applyBackgroundColor()

        val sound_filename: String =
            sharedPref!!.getString("bellsound", getString(R.string.pref_default_bellsound))!!
        val assetManager = getAssets()
        initSound(this, assetManager, getString(R.string.soundpath) + sound_filename)

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
        val editor = sharedPref!!.edit()
        val currnet_cnt = if (toggle_on || saveCurrentCount) tv_Cnt!!.getText().toString() else "0"

        editor.putString("current_cnt", currnet_cnt)
        editor.putString("count_a", t_cnta!!.getText().toString())
        editor.putString("count_b", t_cntb!!.getText().toString())

        editor.putBoolean("tts_number", cbx_tts_number!!.isChecked())
        editor.putBoolean("tts_text", cbx_tts_text!!.isChecked())

        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == R.id.menu_settingsn) {
            saveCurrentCount = true
            f_Pause()
            tb1!!.setChecked(false)

            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, SETTING_ACTIVITY)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        f_LoadVariables()
        saveCurrentCount = false
        toggle_on = false
    }

    var toggle_on: Boolean = false

    fun f_onToggleClicked(view: View) {
        toggle_on = (view as ToggleButton).isChecked()
        if (toggle_on) {
            f_Start()
        }
        else {
            f_Pause()
        }
    }

    fun f_Start() {
        var Current_cnt = tv_Cnt!!.getText().toString().toInt()
        file_line_cnt = sharedPref!!.getString("file_line_cnt", "108")!!.toInt()
        interval_sec = sharedPref!!.getString("interval", "9.4")!!.toDouble()

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Current_cnt >= file_line_cnt) {
            tv_Cnt!!.setText("0")
            Current_cnt = 0
        }

        f_CountDownTimer((file_line_cnt - Current_cnt), interval_sec!!)
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

        ttobj?.stop()

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    private fun f_CountDownTimer(target_count: Int, countDownInterval_sec: Double) {
        val countDownInterval_milisec = (countDownInterval_sec * 1000).toInt()
        val tc = (target_count) * countDownInterval_milisec

        ct = object : CountDownTimer(tc.toLong(), countDownInterval_milisec.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                f_NextWords(1, true)
            }

            override fun onFinish() {
                tb1!!.setChecked(false)
                toggle_on = false
                saveCurrentCount = false
                f_Pause()
                Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.thankyou),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        ct!!.start()
    }

    private fun f_NextWords(i: Int, setRemainSeconds: Boolean) {
        var Current_cnt = tv_Cnt!!.getText().toString().toInt()

        if (i == -1 && Current_cnt == 0) {
            return
        }

        Current_cnt = Current_cnt + i

        if (sharedPref!!.getBoolean("play_sound", true)) {
            playSound(this)
        }

        f_ReadJsonObject(Current_cnt)

        if (setRemainSeconds) remainSecs()

        var Current_ca = t_cnta!!.getText().toString().toInt()
        var Current_cb = t_cntb!!.getText().toString().toInt()

        Current_ca = Current_ca + i
        Current_cb = Current_cb + i

        tv_Cnt!!.setText(String.format(Locale.US, "%d", Current_cnt))
        t_cnta!!.setText(String.format(Locale.US, "%d", Current_ca))
        t_cntb!!.setText(String.format(Locale.US, "%d", Current_cb))

        f_ReadText()
    }

    fun f_ReadText() {
        var toSpeak = ""
        if (cbx_tts_number!!.isChecked()) {
            toSpeak = tv_Cnt!!.getText().toString()
        }

        if (cbx_tts_text!!.isChecked()) {
            toSpeak = toSpeak + " " + t_text!!.getText().toString()
        }

        if (toSpeak.length > 0) {
            ttobj!!.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }


    fun f_onClickSlower(view: View) {
        this.interval_sec = this.interval_sec?.plus(0.2)
        interval_sec = Math.round(interval_sec!! * 100.0).toDouble() / 100.0
        saveInterval()
        Toast.makeText(this, "" + interval_sec, Toast.LENGTH_SHORT).show()
    }

    fun f_onClickFaster(view: View) {
        this.interval_sec = this.interval_sec?.minus(0.2)
        interval_sec = Math.round(interval_sec!! * 100.0).toDouble() / 100.0
        saveInterval()
        Toast.makeText(this, "" + interval_sec, Toast.LENGTH_SHORT).show()
    }

    private fun saveInterval() {
        val editor = sharedPref!!.edit()
        editor.putString("interval", "" + interval_sec)
        editor.apply()
    }

    private fun remainSecs() {
        ct_remain?.cancel()
        ct_remain = object : CountDownTimer((interval_sec!! * 1000).toLong(), 200) {
            override fun onTick(millisUntilFinished: Long) {
                // Re-finding the view each time because orientation might change
                findViewById<TextView>(R.id.remain_e)?.setText(
                    String.format(
                        Locale.US,
                        "%.1f",
                        (Math.round((millisUntilFinished / 100).toFloat())).toFloat() / 10
                    )
                )
            }

            override fun onFinish() {
                findViewById<TextView>(R.id.remain_e)?.setText("0")
            }
        }
        ct_remain!!.start()
    }

    var jsonArray: JSONArray? = null
    fun f_File2JsonArray(fileName: String?) {
        val fileReadStringFeed = loadFile2String(this, fileName)
        try {
            jsonArray = JSONArray(fileReadStringFeed)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun f_ReadJsonObject(new_cnt: Int) {
        if (jsonArray == null || new_cnt <= 0) return
        val ii: Int
        try {
            if (new_cnt > file_line_cnt) {
                ii = new_cnt % file_line_cnt
            }
            else {
                ii = new_cnt
            }
            val jsonObject = jsonArray!!.getJSONObject(if (ii == 0) file_line_cnt - 1 else ii - 1)
            t_text!!.setText(jsonObject.getString("text"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.mDetector!!.onTouchEvent(event)
        return super.onTouchEvent(event)
    }


    override fun onDown(e: MotionEvent): Boolean = false
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(e2: MotionEvent?, p1: MotionEvent, distanceY: Float, p3: Float): Boolean = false
    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityY: Float, p3: Float): Boolean {
        e1?.getX()?.minus(e2.getX())?.let {
            if (it > SWIPE_MIN_DISTANCE) {
                f_NextWords(1, false)
            } else if ( it < - SWIPE_MIN_DISTANCE) {
                f_NextWords(-1, false)
            }
        }
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = false
    override fun onDoubleTap(e: MotionEvent): Boolean {
        f_NextWords(1, false)
        return false
    }
    override fun onDoubleTapEvent(e: MotionEvent): Boolean = false

    companion object {
        const val KEY_PREF_SYNC_CONN: String = "pref_syncConnectionType"
        private const val TAG = "MainActivity"
        private const val SETTING_ACTIVITY = 10
        const val SWIPE_MIN_DISTANCE: Int = 120
    }
}
