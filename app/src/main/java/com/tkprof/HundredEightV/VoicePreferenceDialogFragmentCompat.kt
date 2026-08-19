package com.tkprof.HundredEightV

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreferenceDialogFragmentCompat

class VoicePreferenceDialogFragmentCompat : ListPreferenceDialogFragmentCompat() {

    private var mClickedDialogEntryIndex = 0

    companion object {
        fun newInstance(key: String): VoicePreferenceDialogFragmentCompat {
            val fragment = VoicePreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listPreference = preference as androidx.preference.ListPreference
        mClickedDialogEntryIndex = listPreference.findIndexOfValue(listPreference.value)
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        // We don't call super.onPrepareDialogBuilder because it sets a dismissing listener
        val listPreference = preference as androidx.preference.ListPreference
        
        builder.setSingleChoiceItems(listPreference.entries, mClickedDialogEntryIndex) { _, which ->
            mClickedDialogEntryIndex = which
            // Play sample when an item is clicked
            val voiceName = listPreference.entryValues[which].toString()
            (targetFragment as? VoiceSamplePlayer)?.playVoiceSample(voiceName)
        }

        builder.setPositiveButton(R.string.close) { dialog, which ->
            onClick(dialog, DialogInterface.BUTTON_POSITIVE)
        }
        
        builder.setNegativeButton(R.string.cancel) { dialog, which ->
            onClick(dialog, DialogInterface.BUTTON_NEGATIVE)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && mClickedDialogEntryIndex >= 0) {
            val listPreference = preference as androidx.preference.ListPreference
            val value = listPreference.entryValues[mClickedDialogEntryIndex].toString()
            if (listPreference.callChangeListener(value)) {
                listPreference.value = value
            }
        }
    }

    interface VoiceSamplePlayer {
        fun playVoiceSample(voiceName: String)
    }
}
