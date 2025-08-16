//package com.tkprof.hundredeightv
//
//import android.content.res.Configuration
//import android.os.Bundle
//import android.preference.PreferenceActivity // Note: This class is deprecated
//import android.view.MenuInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.annotation.LayoutRes
//import androidx.appcompat.app.ActionBar
//import androidx.appcompat.app.AppCompatDelegate
//
///**
// * A [PreferenceActivity] which implements and proxies the necessary calls
// * to be used with AppCompat.
// */
//abstract class AppCompatPreferenceActivity : PreferenceActivity() {
//
//    private lateinit var appCompatDelegate: AppCompatDelegate
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        appCompatDelegate = AppCompatDelegate.create(this, null) // Or your AppCompatCallback
//        appCompatDelegate.installViewFactory()
//        appCompatDelegate.onCreate(savedInstanceState) // This internally calls Activity.onCreate()
//        super.onCreate(savedInstanceState)
//    }
//
//    private var mDelegate: AppCompatDelegate? = null
//
//    override fun onPostCreate(savedInstanceState: Bundle?) {
//        super.onPostCreate(savedInstanceState)
//        appCompatDelegate.onPostCreate(savedInstanceState)
//    }
//
//    val supportActionBar: ActionBar?
//        get() = appCompatDelegate.supportActionBar
//
//    override fun getMenuInflater(): MenuInflater {
//        return appCompatDelegate.menuInflater
//    }
//
//    override fun setContentView(@LayoutRes layoutResID: Int) {
//        appCompatDelegate.setContentView(layoutResID)
//    }
//
//    override fun setContentView(view: View) {
//        appCompatDelegate.setContentView(view)
//    }
//
//    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
//        appCompatDelegate.setContentView(view, params)
//    }
//
//    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
//        appCompatDelegate.addContentView(view, params)
//    }
//
//    override fun onPostResume() {
//        super.onPostResume()
//        appCompatDelegate.onPostResume()
//    }
//
//    override fun onTitleChanged(title: CharSequence, color: Int) {
//        super.onTitleChanged(title, color)
//        appCompatDelegate.setTitle(title)
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        appCompatDelegate.onConfigurationChanged(newConfig)
//    }
//
//    override fun onStop() {
//        super.onStop()
//        appCompatDelegate.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        appCompatDelegate.onDestroy()
//    }
//
//}
