package com.esaurio.codingchallenge.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.esaurio.codingchallenge.R

class AlertFactory {
    companion object {
        fun showMessage(ctx : Context, message : Int) : AlertDialog{
            val dialog = AlertDialog.Builder(ctx)
                    .setMessage(message)
                    .setPositiveButton(R.string.aceptar,null).create()
            try {
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dialog
        }

        fun showMessage(ctx : Context, message : String) : AlertDialog{
            val dialog = AlertDialog.Builder(ctx)
                    .setMessage(message)
                    .setPositiveButton(R.string.aceptar,null).create()
            try {
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dialog
        }

        fun showQuestion(
            ctx: Context,
            message: String,
            okListener: DialogInterface.OnClickListener
        ) : AlertDialog{
            val dialog = AlertDialog.Builder(ctx)
                .setMessage(message)
                .setPositiveButton(R.string.si,okListener)
                .setNegativeButton(R.string.no, null)
                .create()
            try {
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dialog
        }
    }
}