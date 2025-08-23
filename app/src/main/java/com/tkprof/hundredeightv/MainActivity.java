package com.tkprof.hundredeightv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class MainActivity extends AppCompatActivity
implements SharedPreferences.OnSharedPreferenceChangeListener, GestureDetector.OnGestureListener,
GestureDetector.OnDoubleTapListener   {

    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
	private static final String TAG = "MainActivity";
	// IMPORTANT: Replace with your actual Ad Unit ID for interstitial ads
	// For testing, you can use Google's test ad unit ID: "ca-app-pub-3940256099942544/1033173712"
	private static final String AD_UNIT_ID = "ca-app-pub-8979756439452342/7964602504";


	CountDownTimer  ct ;
	CountDownTimer  ct_remain ;
	int file_line_cnt;
	
	// Default value for user to just f_Start without setup
    Double  interval_sec  ;


	boolean  saveCurrentCount = false;

	SharedPreferences sharedPref;
	
	TextView tv_Cnt ;
    TextView t_cnta ; 
    TextView t_cntb   ;
    
    TextView t_text ;

    CheckBox cbx_tts_number, cbx_tts_text;
    ToggleButton tb1;
    

    TextToSpeech ttobj;
	
	private GestureDetectorCompat  mDetector;


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

		sharedPref=  sharedPreferences;
		f_LoadVariables();
    }

    @Override
    protected void onResume() {
        super.onResume();
		getPreferences( Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
		getPreferences( Context.MODE_PRIVATE)
             .unregisterOnSharedPreferenceChangeListener(this);
		f_SaveSharedpref();

		// Pause !
		tb1.setChecked(false);  f_Pause();
		Log.d("Main:onPause","Called") ;
    }

	private InterstitialAd mInterstitialAd;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
    //	 Log.i("oncreate", "BNEgin0");
        super.onCreate(savedInstanceState);
    //    Log.i("oncreate", "BNEgin1");
        if (getResources().getConfiguration().orientation
        == Configuration.ORIENTATION_PORTRAIT ){
          setContentView(R.layout.activity_main);
        }else
        {setContentView(R.layout.activity_main_land);}
  //      Log.i("oncreate", "Begin2 ");

		// In your MainActivity's onCreate method
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		tv_Cnt =   findViewById(R.id.count) ;
        t_cnta =   findViewById(R.id.count_a) ;
        t_cntb =  findViewById(R.id.count_b) ;
        t_text =   findViewById(R.id.text);
		tb1=  findViewById(R.id.tgbBeginPause) ;
        
        cbx_tts_number =   findViewById(R.id.cbx_tts_number) ;
        cbx_tts_text =   findViewById(R.id.cbx_tts_text) ;
        
        sharedPref=  PreferenceManager.getDefaultSharedPreferences(this);

        // retire Download Feature.
        //  checkInitialInstall();

		f_LoadVariables();
        AssetManager assetManager = getAssets();
        
        String audio_file = getString(R.string.soundpath)
				+ sharedPref.getString( "bellsound",  getString(R.string.pref_default_bellsound));
        
        Util.initSound(this, assetManager, audio_file );
//        Util.initSound(this);
         
        mDetector = new GestureDetectorCompat(this,this); 
        mDetector.setOnDoubleTapListener(this);
        
        ttobj=new TextToSpeech(getApplicationContext(), 
      	      new TextToSpeech.OnInitListener() {
      	      @Override
      	      public void onInit(int status) {
      	         if(status != TextToSpeech.ERROR){
      	             ttobj.setLanguage(Locale.KOREA);
      	            }		
      	         }
      	      });


// following gemini ad , just so long
//		MobileAds.initialize(this,	"ca-app-pub-8979756439452342~1904313389");
//		mInterstitialAd = new InterstitialAd(this);
//		mInterstitialAd.setAdUnitId("ca-app-pub-8979756439452342/7964602504");
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());

		// 1. Initialize the Mobile Ads SDK
		// Your AdMob App ID (ca-app-pub-8979756439452342~1904313389) should be in AndroidManifest.xml
		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
				Log.d(TAG, "Mobile Ads SDK initialized.");
				// It's best practice to load ads after SDK initialization.
				loadInterstitialAd();
			}
		});

    }

	// 2. Method to load the Interstitial Ad
	private void loadInterstitialAd() {
		AdRequest adRequest = new AdRequest.Builder().build();

		InterstitialAd.load(this, AD_UNIT_ID, adRequest,
				new InterstitialAdLoadCallback() {
					@Override
					public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
						// The mInterstitialAd reference will be null until an ad is loaded.
						MainActivity.this.mInterstitialAd = interstitialAd;
						Log.i(TAG, "onAdLoaded");

						// 3. Set FullScreenContentCallback (Highly Recommended)
						mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
							@Override
							public void onAdClicked() {
								// Called when a click is recorded for an ad.
								Log.d(TAG, "Ad was clicked.");
							}

							@Override
							public void onAdDismissedFullScreenContent() {
								// Called when ad is dismissed.
								// Set the ad reference to null so you don't show the ad a second time.
								Log.d(TAG, "Ad dismissed fullscreen content.");
								mInterstitialAd = null;
								// IMPORTANT: Load the next ad once the current one is dismissed
								loadInterstitialAd();
							}

							@Override
							public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
								// Called when ad fails to show.
								Log.e(TAG, "Ad failed to show fullscreen content: " + adError.getMessage());
								mInterstitialAd = null;
							}

							@Override
							public void onAdImpression() {
								// Called when an impression is recorded for an ad.
								Log.d(TAG, "Ad recorded an impression.");
							}

							@Override
							public void onAdShowedFullScreenContent() {
								// Called when ad is shown.
								Log.d(TAG, "Ad showed fullscreen content.");
							}
						});
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						// Handle the error
						Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
						mInterstitialAd = null;
					}
				});
	}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state 
        
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
       
        // Restore state members from saved instance
        tb1.setChecked(false);
		saveCurrentCount = false;
    }
    


	/* get saved values */
	private void f_LoadVariables() {

		tv_Cnt.setText(sharedPref.getString("current_cnt","0"));

        t_cnta.setText(sharedPref.getString("count_a","0"));
		t_cntb.setText(sharedPref.getString("count_b","0"));

		cbx_tts_number.setChecked(sharedPref.getBoolean("tts_number", true) );
        cbx_tts_text.setChecked(sharedPref.getBoolean("tts_text", true) ) ;        
    	    		
    	interval_sec = Double.parseDouble(sharedPref.getString("interval", "9.4")) ;
		Log.d("Main.f_LoadVariables",  "interval_sec:" + interval_sec );

		//Default Korean
        String fileName = sharedPref.getString( "file_name", "108vow.txt");
    	file_line_cnt =  Integer.parseInt( sharedPref.getString("file_line_cnt", "108"));
    	
    	Log.d("MainActi.loadvariables", "file_name" + fileName);
    	Log.d("Loadvariables", "" + file_line_cnt);


		//-- set BG COLOR test
		//Set an id to the layout
		LinearLayout currentLayout =
				(LinearLayout) findViewById(R.id.mainLinearLayout1);


		//currentLayout.setBackgroundColor(getApplicationContext().getColor(R.color.grey1));

		currentLayout.setBackgroundColor(getApplicationContext().getColor(
		getResources().getIdentifier(sharedPref.getString("bgcolor", "white")
                          , "color", getPackageName())));

		//-----

        String sound_filename = sharedPref.getString("bellsound", getString(R.string.pref_default_bellsound));
		AssetManager assetManager = getAssets();
		Util.initSound(this, assetManager, getString(R.string.soundpath)+ sound_filename );

		//Load file to JsonArray
        // only here ?? if setup changes 
        f_File2JsonArray(  fileName) ;
	}
    
    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass

		Log.d("destroy.f_SaveSharedpref", "f_SaveSharedpref toggle_on:" + (toggle_on ? "on": "Off")
				+ "  saveCurrentCount:" + (saveCurrentCount ? " Save " : "No Save")) ;

        if ( ct != null ) {
        	ct.cancel(); 
        }


        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
	}

    }


	private void f_SaveSharedpref() {

		// save to profile
        SharedPreferences.Editor editor = sharedPref.edit();
        String currnet_cnt = toggle_on || saveCurrentCount  ? tv_Cnt.getText().toString() : "0" ;

        Log.d("f_SaveSharedpref", "f_SaveSharedpref toggle_on" + (toggle_on ? "on": "Off")
				 + "  saveCurrentCount:" + (saveCurrentCount ? " Save " : "No Save")) ;

        editor.putString("current_cnt", currnet_cnt);
        editor.putString("count_a", t_cnta.getText().toString());
        editor.putString("count_b", t_cntb.getText().toString());


		editor.putBoolean("tts_number", cbx_tts_number.isChecked());
        editor.putBoolean("tts_text", cbx_tts_text.isChecked());

		editor.apply();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private static final int SETTING_ACTIVITY = 10;
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

		if (id == R.id.menu_settingsn) {

		//	f_SaveSharedpref();
			saveCurrentCount = true;
			f_Pause(/*findViewById(R.id.Pause)*/);

			// Inside your onOptionsItemSelected method or wherever you want to show the ad:

			if (mInterstitialAd != null) {
				mInterstitialAd.show(this); // 'this' refers to your Activity
			} else {
				Log.d("TAG", "The interstitial ad wasn't ready yet.");
				// Optional: You might want to try loading an ad here if it's null,
				// though it's generally better to have ads pre-loaded.
				// loadInterstitialAd(); // Make sure you have this method defined as shown previously
			}

			//Release Tobble btn
            tb1.setChecked(false);


			Intent intent = new Intent(this, SettingsActivity.class);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, "News for you!");

			startActivityForResult(intent, SETTING_ACTIVITY);

			return true;
		}

         
        return super.onOptionsItemSelected(item);
    }
       
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode,   resultCode,   data);
    	
//    	Toast.makeText(this, "Main.onActivityResult Called",
//                Toast.LENGTH_SHORT).show();

     	sharedPref =
 	     	PreferenceManager.getDefaultSharedPreferences(this);

//       Log.d( "sharePrefAll:" , sharedPref.getAll().toString()) ;
         // These day, expected setup updated when back from setting screen,
         // no confirm, no save button.
         f_LoadVariables();
         // save curretcount before going into setting. when back set No so other back buttons to not save count
         saveCurrentCount = false; toggle_on =false;

    }

	boolean toggle_on =false;

    // Begin Pause button
    public void f_onToggleClicked(View view) {
        // Is the toggle on?
		toggle_on = ((ToggleButton) view).isChecked();
        
        if (toggle_on) {
        	f_Start();
        } else {
            f_Pause( );
        }
    }
    
    // click f_Start button
    public void f_Start( ){

        //current_cnt
    	int Current_cnt  = Integer.parseInt(tv_Cnt.getText().toString());
    	file_line_cnt = Integer.parseInt( sharedPref.getString("file_line_cnt","108")) ;
    	interval_sec = Double.parseDouble(sharedPref.getString("interval", "9.4")) ;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d("start0", "currCnt:" + Current_cnt + " tCnt:" + file_line_cnt + " Interval:" + interval_sec);

    	// Restart to Continue
    	if ( Current_cnt == file_line_cnt) {
    		tv_Cnt.setText("0");
    		Current_cnt = 0 ;
		}

    	f_CountDownTimer(( file_line_cnt - Current_cnt )  , interval_sec );
    }

    public void f_Pause( ){
    	
    	if (ct != null ) {
	    	ct.cancel(); 
	    	ct = null; 
    	}
    	
    	if (ct_remain != null ) {
	    	ct_remain.cancel(); 
	    	ct_remain = null;
    	}

    	// let it close when not run
        getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    
    private void f_CountDownTimer(int target_count  , Double countDownInterval_sec ) {
    	Log.d("countDownTimer1", "target_cnt:"+ target_count + " intervalSec:" + countDownInterval_sec);

    	final int countDownInterval_milisec = (int)(countDownInterval_sec * 1000) ;
     	int tc =  (target_count  )  * countDownInterval_milisec;

     	ct = new CountDownTimer(tc, countDownInterval_milisec ) {
				public void onTick(long millisUntilFinished) {
	        	// not really..
	        	if (ct_remain != null ) { ct_remain.onFinish();  }
	        	//plusOne();
	        	f_NextWords( /*plusMinus*/ 1 , /*setRemainSec*/ true);
	        }

	        public void onFinish() { 
	        	//re.setText("done!");
				tb1.setChecked(false);  toggle_on = false; saveCurrentCount = false;
				f_Pause();
	        	Log.d("countdownTimer", "onfinish");
				Toast.makeText(getApplicationContext(),getString(R.string.thankyou),Toast.LENGTH_SHORT).show();
			}
        };

    	ct.start();
    }

 // plus one on tv_odometer
 	private void f_NextWords(int i, boolean setRemainSeconds){
         int Current_cnt =  Integer.parseInt(tv_Cnt.getText().toString()); 
         
		// Back button, don't go minus, just ignore
		if( i== -1  &&  Current_cnt == 0 ) {
			return;  
		}

		Current_cnt = Current_cnt + i ;
		    	
		Log.d("f_NextWords", "CCnt:" + Current_cnt);

         if ( sharedPref.getBoolean("play_sound", true)){
 	    	// Bell -> Read & Vow 
 	    	Util.playSound(this);
         } 
         
     	f_ReadJsonObject(Current_cnt);
     	
     	/* show remaining Pint seconds CountdownTimer
     	  need to change progressivebar */
     	if (setRemainSeconds ) remainSecs();
     	 
     	int Current_ca =  Integer.parseInt(t_cnta.getText().toString()); 
     	int Current_cb =  Integer.parseInt(t_cntb.getText().toString()); 
  
     	Current_ca = Current_ca+ i;
     	Current_cb = Current_cb+ i;
     	
     	// now change Number on screen 
     	tv_Cnt.setText( String.format(Locale.US,"%d",Current_cnt));
     	t_cnta.setText( String.format(Locale.US,"%d",Current_ca));
     	t_cntb.setText( String.format(Locale.US,"%d",Current_cb));
     	
     	f_ReadText();

     }

 	public void f_ReadText(){
 		
        String toSpeak ="";
     	if( cbx_tts_number.isChecked() ) {
     		toSpeak = tv_Cnt.getText().toString();
     	} 
 
     	if( cbx_tts_text.isChecked() ) {
     		toSpeak = toSpeak +" "+ t_text.getText().toString();
     	} 
     	 
     	// only when it has length
     	if (toSpeak.length() > 0) {
         	ttobj.speak(toSpeak,TextToSpeech.QUEUE_FLUSH,null,null);
        }
    	
 	}


   public void f_onClickSlower(View view){

	   interval_sec  +=  0.2D;
	   interval_sec = (double) Math.round(interval_sec * 100.0D) /100.0D;

	   saveInterval();
	   Toast.makeText(this, ""+interval_sec,
	            Toast.LENGTH_SHORT).show();
   }

   public void f_onClickFaster(View view){

	   interval_sec  -=  0.2D;
	   interval_sec = (double) Math.round(interval_sec * 100.0D) /100.0D;

	   saveInterval();
	   Toast.makeText(this, ""+interval_sec,
	            Toast.LENGTH_SHORT).show();
   }

	private void saveInterval() {

		// save to profile
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("interval", ""+ interval_sec );

		editor.apply();
	}

    private void remainSecs(){

    	//  remain 
    	final TextView  t_remain = findViewById(R.id.remain_e);

      //  Log.i("remainSecs", "" + interval_sec);
    	ct_remain = new CountDownTimer((long) (interval_sec *1000), 200) {

	        public void onTick(long millisUntilFinished) {
	        	t_remain.setText( String.format(Locale.US,"%.1f",(float) ( Math.round( millisUntilFinished / 100)) /10  ));
	        }

	        public void onFinish() {
	        //	Log.i("remainder", "remainder Finished!!");
	        	t_remain.setText("0" );

	        }
        };

    	ct_remain.start();
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
    
    JSONArray jsonArray ;
    public void f_File2JsonArray(String fileName)  {
    	String fileReadStringFeed = Util.loadFile2String(this, fileName);

	      // this is expensive. load file once use repeatedly. 
	     try {
			jsonArray = new JSONArray(fileReadStringFeed);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
     
    // read Json file and set the value
    public void f_ReadJsonObject(Integer new_cnt){
    	int ii ;
    	
  	    try {  
	  	    if (new_cnt > file_line_cnt  ){
	  	       ii = new_cnt % file_line_cnt;
	  	 	}else { 
	  	 	   ii = new_cnt; 
	  	 	} 
  	    	 Log.d("jsonObjectRead", "file_line_cnt:" + file_line_cnt + " new cnt:" + new_cnt + " ii:"+  ii );

  	    	 
  	      /* integer .. Java starts with 0 */
  	      JSONObject jsonObject = jsonArray.getJSONObject(ii -1 ); 

          t_text.setText(jsonObject.getString("text"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }
 
 
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	  static final int SWIPE_MIN_DISTANCE = 120;
	  static final int SWIPE_MAX_OFF_PATH = 250;
	  static final int SWIPE_THRESHOLD_VELOCITY = 200;
	  
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
        	f_NextWords( /*plusMinus*/ 1 , /*setRemainSec*/ false);
		 } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
			 f_NextWords( /*plusMinus*/ -1 , /*setRemainSec*/ false);
		 }
 
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		f_NextWords( /*plusMinus*/ 1 , /*setRemainSec*/ false);
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

}