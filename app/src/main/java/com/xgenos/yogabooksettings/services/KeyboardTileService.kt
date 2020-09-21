package com.xgenos.yogabooksettings.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.xgenos.yogabooksettings.R
import java.io.*

class KeyboardTileService : TileService() {
    override fun onClick() {
        val bound = getBindStatus()
        val action = if (bound) "unbind" else "bind"
        val cmd = arrayOf("su", "-c", "echo -n \"1-7:1.2\" > /sys/bus/usb/drivers/usbhid/$action")

        exec(cmd)

        val message =
            if (bound) getString(R.string.keyboard_unbound_message)
            else getString(R.string.keyboard_bound_message)

        Toast.makeText(applicationContext, "$message", Toast.LENGTH_LONG)
            .show()

        updateTile()
    }

    override fun onStartListening() {
        updateTile()
    }

    private fun getBindStatus(): Boolean {
        val device = File("/sys/bus/usb/drivers/usbhid/1-7:1.2")

        return device.exists() && device.isDirectory
    }

    private fun updateTile() {
        val tile = qsTile
        val bound = getBindStatus()

        tile.state = if (bound) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

//        val label =
//            if (bound) R.string.keyboard_tile_on
//            else R.string.keyboard_tile_off
//        tile.label = getString(label)

//        Toast.makeText(this, "List result: $result", Toast.LENGTH_SHORT).show()

        tile.updateTile()
    }

    @Throws(IOException::class)
    fun exec(command: Array<String>): String? {
        var line: String? = ""
        val sb = StringBuilder(line!!)

        try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(command)

            val inputStream: InputStream = process.inputStream
            val streamReader = InputStreamReader(inputStream)
            val reader = BufferedReader(streamReader)

            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }

            if (process.waitFor() != 0) {
                Toast.makeText(
                    applicationContext,
                    "Command exit with code: ${process.exitValue()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: InterruptedException) {
            Toast.makeText(
                applicationContext,
                "Run command error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        return sb.toString()
    }
}