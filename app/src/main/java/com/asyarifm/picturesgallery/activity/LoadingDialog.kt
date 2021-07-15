package com.asyarifm.picturesgallery.activity

import android.app.Activity
import android.app.AlertDialog
import com.asyarifm.picturesgallery.R

// class to create, show and dismis loading dialog.
// loading dialog is required by this app to ask user to wait for few seconds while the app downloading the image from internet
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

    // show dialog if dialog is not showing
    fun startLoading(){
        if (!isdialog.isShowing) {
            isdialog.show()
        }
    }

    // dismis dialog if dialog is showing
    fun isDismiss(){
        if (isdialog.isShowing) {
            isdialog.dismiss()
        }
    }
}