package com.asyarifm.picturesgallery.activity

import android.app.Activity
import android.app.AlertDialog
import com.asyarifm.picturesgallery.R

class LoadingDialog(val mActivity: Activity) {
    private lateinit var isdialog:AlertDialog

    init {
        val infalter = mActivity.layoutInflater
        val dialogView = infalter.inflate(R.layout.layout_progress_dialog, null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog = builder.create()
    }

    fun startLoading(){
        if (!isdialog.isShowing) {
            isdialog.show()
        }
    }
    fun isDismiss(){
        if (isdialog.isShowing) {
            isdialog.dismiss()
        }
    }
}