package com.orbitalsonic.locationfinder.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orbitalsonic.locationfinder.R

object DialogUtils {

    fun showPermissionRationaleDialog(
        context: Context,
        onPositiveClick: () -> Unit
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.permission_rationale_title)
            .setMessage(R.string.permission_rationale_message)
            .setPositiveButton(R.string.action_enable) { _, _ -> onPositiveClick() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    fun showPermissionDeniedDialog(
        context: Context,
        onSettingsClick: () -> Unit
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.permission_denied_title)
            .setMessage(R.string.permission_denied_message)
            .setPositiveButton(R.string.action_open_settings) { _, _ -> onSettingsClick() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    fun showGpsDisabledDialog(
        context: Context,
        onEnableClick: () -> Unit
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.gps_required_title)
            .setMessage(R.string.gps_required_message)
            .setPositiveButton(R.string.action_enable) { _, _ -> onEnableClick() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
