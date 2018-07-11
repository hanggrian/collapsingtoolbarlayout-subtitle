package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ColorPickerFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo." +
            "ColorPickerFragment"

        private val COLOR_MAP = mapOf(
            "Black" to Color.BLACK,
            "Blue" to Color.BLUE,
            "Cyan" to Color.CYAN,
            "Dark gray" to Color.DKGRAY,
            "Gray" to Color.GRAY,
            "Green" to Color.GREEN,
            "Light gray" to Color.LTGRAY,
            "Magenta" to Color.MAGENTA,
            "Red" to Color.RED,
            "Transparent" to Color.TRANSPARENT,
            "White" to Color.WHITE,
            "Yellow" to Color.YELLOW
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ListView(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view as ListView
        view.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1,
            COLOR_MAP.keys.toList())
        view.setOnItemClickListener { _, _, i, _ ->
            arguments = Bundle().apply { putInt(TAG, COLOR_MAP.values.toList()[i]) }
            dismiss()
        }
    }
}