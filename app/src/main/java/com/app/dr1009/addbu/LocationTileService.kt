/*
The MIT License (MIT)

Copyright (c) 2018 Koji Wakamiya.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.app.dr1009.addbu

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.LocationServices

@RequiresApi(Build.VERSION_CODES.N)
class LocationTileService : TileService() {

    companion object {
        private const val NOTIFICATION_ID = 1111
    }

    override fun onClick() {
        super.onClick()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivityAndCollapse(intent)
            return
        }

        LocationServices.getFusedLocationProviderClient(applicationContext).run {
            lastLocation.addOnCompleteListener {
                showNotification(it.result)
            }
        }
    }

    private fun showNotification(location: Location) {
        val address = Geocoder(applicationContext).getFromLocation(location.latitude, location.longitude, 1).first()

        val text = address.getAddressLine(0)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        val chooserIntent = Intent.createChooser(intent, "Share")

        val stackBuilder = TaskStackBuilder.create(applicationContext).apply {
            addParentStack(MainActivity::class.java)
            addNextIntent(chooserIntent)
        }
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(applicationContext, "main")
                .setSmallIcon(R.drawable.ic_my_location_black_24dp)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }
}