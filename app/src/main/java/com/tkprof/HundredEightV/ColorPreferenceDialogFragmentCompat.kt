package com.tkprof.hundredeightv

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.ListPreferenceDialogFragmentCompat

class ColorPreferenceDialogFragmentCompat : ListPreferenceDialogFragmentCompat() {

    private var mClickedIndex = -1

    private fun getColorListPreference(): ListPreference {
        return preference as ListPreference
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        val preference = getColorListPreference()
        val entries = preference.entries
        val entryValues = preference.entryValues
        val selectedValue = preference.value
        mClickedIndex = preference.findIndexOfValue(selectedValue)

        val adapter = ColorAdapter(requireContext(), R.layout.item_color_choice, entries, entryValues)
        
        builder.setAdapter(adapter) { dialog, which ->
            mClickedIndex = which
            this.onClick(dialog, AlertDialog.BUTTON_POSITIVE)
            dialog.dismiss()
        }
        
        builder.setPositiveButton(null, null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        val preference = getColorListPreference()
        if (positiveResult && mClickedIndex >= 0 && mClickedIndex < preference.entryValues.size) {
            val value = preference.entryValues[mClickedIndex].toString()
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
        }
    }

    private inner class ColorAdapter(
        context: Context,
        private val resource: Int,
        private val entries: Array<CharSequence>,
        private val entryValues: Array<CharSequence>
    ) : ArrayAdapter<CharSequence>(context, resource, entries) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
            
            val colorIndicator = view.findViewById<View>(R.id.color_indicator)
            val colorName = view.findViewById<TextView>(android.R.id.text1)
            val radioButton = view.findViewById<RadioButton>(R.id.radio_button)

            val name = entries[position]
            val value = entryValues[position].toString()
            
            colorName.text = name
            radioButton.isChecked = (position == mClickedIndex)

            val colorResId = context.resources.getIdentifier(value, "color", context.packageName)
            if (colorResId != 0) {
                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, colorResId))
            } else {
                colorIndicator.setBackgroundColor(Color.TRANSPARENT)
            }

            return view
        }
    }

    companion object {
        fun newInstance(key: String): ColorPreferenceDialogFragmentCompat {
            val fragment = ColorPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}
