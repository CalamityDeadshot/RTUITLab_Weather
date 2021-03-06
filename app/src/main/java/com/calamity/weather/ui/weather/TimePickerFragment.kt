package com.calamity.weather.ui.weather

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*


class TimePickerFragment(private val listener: TimePickerDialog.OnTimeSetListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(
            activity,
            listener,
            hour,
            minute,
            DateFormat.is24HourFormat(activity)
        )
    }
}