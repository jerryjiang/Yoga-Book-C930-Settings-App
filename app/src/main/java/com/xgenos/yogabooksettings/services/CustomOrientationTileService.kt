package com.xgenos.yogabooksettings.services

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.view.View
import android.view.WindowManager
import android.widget.Toast

import com.xgenos.yogabooksettings.R

class CustomOrientationTileService : TileService() {
    companion object {
        var mView: View? = null
    }

    override fun onClick() {
        val granted = ensurePermission()

        if (granted) {
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = CustomOrientationLayoutParams(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

            if (mView == null) {
                mView = View(this)
                manager.addView(mView, params)
            } else {
                manager.removeView(mView)
                mView = null
            }

            updateTile()
        } else {
            Toast.makeText(applicationContext, getString(R.string.system_overlay_permission_hint), Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onStartListening() {
        updateTile()
    }

    private fun getLockStatus(): Boolean {
        return null != mView
    }

    private fun updateTile() {
        val tile = qsTile
        val locked = getLockStatus()

        tile.state = if (locked) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

        val label =
            if (locked) R.string.custom_orientation_laptop_mode
            else R.string.custom_orientation_tile_name
        tile.label = getString(label)

        tile.updateTile()
    }

    private fun ensurePermission(): Boolean {
        var granted = true
        if (!Settings.canDrawOverlays(this)) {
            granted = false

            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)

            Toast.makeText(applicationContext, getString(R.string.system_overlay_permission_hint), Toast.LENGTH_LONG)
                .show()
        }

        return granted
    }

    class CustomOrientationLayoutParams(orientation: Int) :
        WindowManager.LayoutParams(0, 0, TYPE_APPLICATION_OVERLAY,  FLAG_NOT_FOCUSABLE or FLAG_FULLSCREEN, PixelFormat.RGBX_8888) {
        init {
            screenOrientation = orientation
        }
    }
}

