package com.tkprof.hundredeightv

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.tkprof.hundredeightv.Util.Companion.initSound
import com.tkprof.hundredeightv.Util.Companion.loadFile2String
import com.tkprof.hundredeightv.Util.Companion.playSound
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

    private var mDetector: GestureDetector? = null

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        sharedPref = sharedPreferences
        f_LoadVariables()
    }

    override fun onResume() {
        super.onResume()
        getPreferences(MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        getPreferences(MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(this)
        f_SaveSharedpref()

        // Pause !
        tb1!!.setChecked(false)
        f_Pause()
        Log.d("Main:onPause", "Called")
    }

    private var mInterstitialAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_PORTRAIT
        ) {
            setContentView(R.layout.activity_main)
        } else {
            setContentView(R.layout.activity_main_land)
        }

        // In your MainActivity's onCreate method
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tv_Cnt = findViewById<TextView>(R.id.count)
        t_cnta = findViewById<TextView>(R.id.count_a)
        t_cntb = findViewById<TextView>(R.id.count_b)
        t_text = findViewById<TextView>(R.id.text)
        tb1 = findViewById<ToggleButton>(R.id.tgbBeginPause)

        cbx_tts_number = findViewById<CheckBox>(R.id.cbx_tts_number)
        cbx_tts_text = findViewById<CheckBox>(R.id.cbx_tts_text)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // retire Download Feature.
        //  checkInitialInstall();
        f_LoadVariables()
        val assetManager = getAssets()

        val audio_file = (getString(R.string.soundpath)
                + sharedPref!!.getString("bellsound", getString(R.string.pref_default_bellsound)))

        initSound(this, assetManager, audio_file)

        mDetector = GestureDetector(this, this)
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


        // 1. Initialize the Mobile Ads SDK
        // Your AdMob App ID (ca-app-pub-8979756439452342~1904313389) should be in AndroidManifest.xml
        MobileAds.initialize(this, object : OnInitializationCompleteListener {
            override fun onInitializationComplete(initializationStatus: InitializationStatus) {
                Log.d(TAG, "Mobile Ads SDK initialized.")
                // It's best practice to load ads after SDK initialization.
                loadInterstitialAd()
            }
        })
    }

    // 2. Method to load the Interstitial Ad
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this, AD_UNIT_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until an ad is loaded.
                    this@MainActivity.mInterstitialAd = interstitialAd
                    Log.i(TAG, "onAdLoaded")

                    // 3. Set FullScreenContentCallback (Highly Recommended)
                    mInterstitialAd!!.setFullScreenContentCallback(object :
                        FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            mInterstitialAd = null
                            // IMPORTANT: Load the next ad once the current one is dismissed
                            loadInterstitialAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when ad fails to show.
                            Log.e(
                                TAG,
                                "Ad failed to show fullscreen content: " + adError.getMessage()
                            )
                            mInterstitialAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.")
                        }
                    })
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage())
                    mInterstitialAd = null
                }
            })
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save the user's current game state 

        // Always call the superclass so it can save the view hierarchy state

        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState)

        // Restore state members from saved instance
        tb1!!.setChecked(false)
        saveCurrentCount = false
    }

    /* get saved values */
    private fun f_LoadVariables() {
        tv_Cnt!!.setText(sharedPref!!.getString("current_cnt", "0"))

        t_cnta!!.setText(sharedPref!!.getString("count_a", "0"))
        t_cntb!!.setText(sharedPref!!.getString("count_b", "0"))

        cbx_tts_number!!.setChecked(sharedPref!!.getBoolean("tts_number", true))
        cbx_tts_text!!.setChecked(sharedPref!!.getBoolean("tts_text", true))

        interval_sec = sharedPref!!.getString("interval", "9.4")!!.toDouble()
        Log.d("Main.f_LoadVariables", "interval_sec:" + interval_sec)

        //Default Korean
        val fileName: String = sharedPref!!.getString("file_name", "108vow.txt")!!
        file_line_cnt = sharedPref!!.getString("file_line_cnt", "108")!!.toInt()

        Log.d("MainActi.loadvariables", "file_name" + fileName)
        Log.d("Loadvariables", "" + file_line_cnt)


        //-- set BG COLOR test
        //Set an id to the layout
        val currentLayout =
            findViewById<View?>(R.id.mainLinearLayout1) as LinearLayout?


        //currentLayout.setBackgroundColor(getApplicationContext().getColor(R.color.grey1));

//		currentLayout.setBackgroundColor(getApplicationContext().getColor(
//		getResources().getIdentifier(sharedPref.getString("bgcolor", "white")
//                          , "color", getPackageName())));

        //-----
        val sound_filename: String =
            sharedPref!!.getString("bellsound", getString(R.string.pref_default_bellsound))!!
        val assetManager = getAssets()
        initSound(this, assetManager, getString(R.string.soundpath) + sound_filename)

        //Load file to JsonArray
        // only here ?? if setup changes 
        f_File2JsonArray(fileName)
    }

    public override fun onDestroy() {
        super.onDestroy() // Always call the superclass

        Log.d(
            "destroy.f_SaveSharedpref",
            ("f_SaveSharedpref toggle_on:" + (if (toggle_on) "on" else "Off")
                    + "  saveCurrentCount:" + (if (saveCurrentCount) " Save " else "No Save"))
        )

        if (ct != null) {
            ct!!.cancel()
        }


        if (ttobj != null) {
            ttobj!!.stop()
            ttobj!!.shutdown()
        }
    }


    private fun f_SaveSharedpref() {
        // save to profile

        val editor = sharedPref!!.edit()
        val currnet_cnt = if (toggle_on || saveCurrentCount) tv_Cnt!!.getText().toString() else "0"

        Log.d(
            "f_SaveSharedpref", ("f_SaveSharedpref toggle_on" + (if (toggle_on) "on" else "Off")
                    + "  saveCurrentCount:" + (if (saveCurrentCount) " Save " else "No Save"))
        )

        editor.putString("current_cnt", currnet_cnt)
        editor.putString("count_a", t_cnta!!.getText().toString())
        editor.putString("count_b", t_cntb!!.getText().toString())


        editor.putBoolean("tts_number", cbx_tts_number!!.isChecked())
        editor.putBoolean("tts_text", cbx_tts_text!!.isChecked())

        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.getItemId()

        if (id == R.id.menu_settingsn) {
            //	f_SaveSharedpref();

            saveCurrentCount = true
            f_Pause()

            // Inside your onOptionsItemSelected method or wherever you want to show the ad:
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(this) // 'this' refers to your Activity
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
                // Optional: You might want to try loading an ad here if it's null,
                // though it's generally better to have ads pre-loaded.
                // loadInterstitialAd(); // Make sure you have this method defined as shown previously
            }

            //Release Tobble btn
            tb1!!.setChecked(false)


            val intent = Intent(this, SettingsActivity::class.java)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, "News for you!")

            startActivityForResult(intent, SETTING_ACTIVITY)

            return true
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


//    	Toast.makeText(this, "Main.onActivityResult Called",
//                Toast.LENGTH_SHORT).show();
        sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        //       Log.d( "sharePrefAll:" , sharedPref.getAll().toString()) ;
        // These day, expected setup updated when back from setting screen,
        // no confirm, no save button.
        f_LoadVariables()
        // save curretcount before going into setting. when back set No so other back buttons to not save count
        saveCurrentCount = false
        toggle_on = false
    }

    var toggle_on: Boolean = false

    // Begin Pause button
    fun f_onToggleClicked(view: View) {
        // Is the toggle on?
        toggle_on = (view as ToggleButton).isChecked()

        if (toggle_on) {
            f_Start()
        } else {
            f_Pause()
        }
    }

    // click f_Start button
    fun f_Start() {
        //current_cnt

        var Current_cnt = tv_Cnt!!.getText().toString().toInt()
        file_line_cnt = sharedPref!!.getString("file_line_cnt", "108")!!.toInt()
        interval_sec = sharedPref!!.getString("interval", "9.4")!!.toDouble()

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Log.d(
            "start0",
            "currCnt:" + Current_cnt + " tCnt:" + file_line_cnt + " Interval:" + interval_sec
        )

        // Restart to Continue
        if (Current_cnt == file_line_cnt) {
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

        // let it close when not run
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    private fun f_CountDownTimer(target_count: Int, countDownInterval_sec: Double) {
        Log.d(
            "countDownTimer1",
            "target_cnt:" + target_count + " intervalSec:" + countDownInterval_sec
        )

        val countDownInterval_milisec = (countDownInterval_sec * 1000).toInt()
        val tc = (target_count) * countDownInterval_milisec

        ct = object : CountDownTimer(tc.toLong(), countDownInterval_milisec.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                // not really..
                if (ct_remain != null) {
                    ct_remain!!.onFinish()
                }
                //plusOne();
                f_NextWords( /*plusMinus*/1,  /*setRemainSec*/true)
            }

            override fun onFinish() {
                //re.setText("done!");
                tb1!!.setChecked(false)
                toggle_on = false
                saveCurrentCount = false
                f_Pause()
                Log.d("countdownTimer", "onfinish")
                Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.thankyou),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        ct!!.start()
    }

    // plus one on tv_odometer
    private fun f_NextWords(i: Int, setRemainSeconds: Boolean) {
        var Current_cnt = tv_Cnt!!.getText().toString().toInt()


        // Back button, don't go minus, just ignore
        if (i == -1 && Current_cnt == 0) {
            return
        }

        Current_cnt = Current_cnt + i

        Log.d("f_NextWords", "CCnt:" + Current_cnt)

        if (sharedPref!!.getBoolean("play_sound", true)) {
            // Bell -> Read & Vow 
            playSound(this)
        }

        f_ReadJsonObject(Current_cnt)


        /* show remaining Pint seconds CountdownTimer
     	  need to change progressivebar */
        if (setRemainSeconds) remainSecs()

        var Current_ca = t_cnta!!.getText().toString().toInt()
        var Current_cb = t_cntb!!.getText().toString().toInt()

        Current_ca = Current_ca + i
        Current_cb = Current_cb + i


        // now change Number on screen 
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


        // only when it has length
        if (toSpeak.length > 0) {
            ttobj!!.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }


    fun f_onClickSlower(view: View?) {
        this.interval_sec = this.interval_sec?.plus(0.2)
        interval_sec = Math.round(interval_sec!! * 100.0).toDouble() / 100.0

        saveInterval()
        Toast.makeText(
            this, "" + interval_sec,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun f_onClickFaster(view: View?) {
        this.interval_sec = this.interval_sec?.minus(0.2)
        interval_sec = Math.round(interval_sec!! * 100.0).toDouble() / 100.0

        saveInterval()
        Toast.makeText(
            this, "" + interval_sec,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun saveInterval() {
        // save to profile

        val editor = sharedPref!!.edit()
        editor.putString("interval", "" + interval_sec)

        editor.apply()
    }

    private fun remainSecs() {
        //  remain 

        val t_remain = findViewById<TextView>(R.id.remain_e)

        //  Log.i("remainSecs", "" + interval_sec);
        ct_remain = object : CountDownTimer((interval_sec!! * 1000).toLong(), 200) {
            override fun onTick(millisUntilFinished: Long) {
                t_remain.setText(
                    String.format(
                        Locale.US,
                        "%.1f",
                        (Math.round((millisUntilFinished / 100).toFloat())).toFloat() / 10
                    )
                )
            }

            override fun onFinish() {
                //	Log.i("remainder", "remainder Finished!!");
                t_remain.setText("0")
            }
        }

        ct_remain!!.start()
    }

    //
    //    public void onClickCheckbox(View view){
    //
    //    	Log.d("checkBox","Clicked");
    //
    //    	final CheckBox checkBox = findViewById(R.id.keep_screen_on);
    //        if (checkBox.isChecked()) {
    //        	Log.d("checkBox","ischecked");
    //            checkBox.setChecked(true);
    //
    //        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    //        } else {
    //        	Log.d("checkBox","Un checked");
    //        	checkBox.setChecked(false);
    //        	getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    //        }
    //    }
    var jsonArray: JSONArray? = null
    fun f_File2JsonArray(fileName: String?) {
        val fileReadStringFeed = loadFile2String(this, fileName)

        // this is expensive. load file once use repeatedly. 
        try {
            jsonArray = JSONArray(fileReadStringFeed)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    // read Json file and set the value
    fun f_ReadJsonObject(new_cnt: Int) {
        val ii: Int

        try {
            if (new_cnt > file_line_cnt) {
                ii = new_cnt % file_line_cnt
            } else {
                ii = new_cnt
            }
            Log.d(
                "jsonObjectRead",
                "file_line_cnt:" + file_line_cnt + " new cnt:" + new_cnt + " ii:" + ii
            )


            /* integer .. Java starts with 0 */
            val jsonObject = jsonArray!!.getJSONObject(ii - 1)

            t_text!!.setText(jsonObject.getString("text"))
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mDetector != null && mDetector!!.onTouchEvent(event!!)) {
            return true
        }
        return super.onTouchEvent(event)
    }


    override fun onDown(p0: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {
        e1?.getX()?.minus(e2.getX())?.let {
            if (it > SWIPE_MIN_DISTANCE) {
                f_NextWords( /*plusMinus*/1,  /*setRemainSec*/false)
            } else if (e2.getX() - (e1?.getX() ?: 0f) > SWIPE_MIN_DISTANCE) {
                f_NextWords( /*plusMinus*/-1,  /*setRemainSec*/false)
            }
        }

        return false
    }


    override fun onLongPress(p0: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onScroll(
        p0: MotionEvent?,
        p1: MotionEvent,
        p2: Float,
        p3: Float
    ): Boolean {
        TODO("Not yet implemented")
    }


    override fun onShowPress(p0: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDoubleTap(p0: MotionEvent): Boolean {
        f_NextWords( /*plusMinus*/1,  /*setRemainSec*/false)
        return false
    }

    override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    companion object {
        const val KEY_PREF_SYNC_CONN: String = "pref_syncConnectionType"
        private const val TAG = "MainActivity"

        // IMPORTANT: Replace with your actual Ad Unit ID for interstitial ads
        // For testing, you can use Google's test ad unit ID: "ca-app-pub-3940256099942544/1033173712"
        private const val AD_UNIT_ID = "ca-app-pub-8979756439452342/7964602504"


        private const val SETTING_ACTIVITY = 10

        // In your Activity or a constants file
        private const val PREFS_NAME =
            "com.tkprof.hundredeightv.PREFERENCES" // Choose a unique name

        const val SWIPE_MIN_DISTANCE: Int = 120
        const val SWIPE_MAX_OFF_PATH: Int = 250
        const val SWIPE_THRESHOLD_VELOCITY: Int = 200
    }
}