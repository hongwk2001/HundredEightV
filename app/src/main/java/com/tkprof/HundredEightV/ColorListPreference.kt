package com.tkprof.hundredeightv

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class ColorListPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dialogPreferenceStyle,
    defStyleRes: Int = 0
) : ListPreference(context, attrs, defStyleAttr, defStyleRes)
