package com.tkprof.HundredEightV

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import java.util.Locale

class IntervalPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_interval
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        
        val valueText = holder.findViewById(R.id.interval_value) as TextView
        updateValueDisplay(valueText)

        holder.findViewById(R.id.btn_minus_1).setOnClickListener { changeValue(-1.0, valueText) }
        holder.findViewById(R.id.btn_minus_02).setOnClickListener { changeValue(-0.2, valueText) }
        holder.findViewById(R.id.btn_plus_02).setOnClickListener { changeValue(0.2, valueText) }
        holder.findViewById(R.id.btn_plus_1).setOnClickListener { changeValue(1.0, valueText) }
    }

    private fun changeValue(delta: Double, view: TextView) {
        var value = sharedPreferences?.getString(key, "9.4")?.toDoubleOrNull() ?: 9.4
        value += delta
        if (value < 1.0) value = 1.0
        value = Math.round(value * 10.0) / 10.0
        
        sharedPreferences?.edit()?.putString(key, value.toString())?.apply()
        updateValueDisplay(view)
        
        // Notify change
        callChangeListener(value.toString())
    }

    private fun updateValueDisplay(view: TextView) {
        val value = sharedPreferences?.getString(key, "9.4")?.toDoubleOrNull() ?: 9.4
        view.text = String.format(Locale.US, "%.1f s", value)
    }
}
