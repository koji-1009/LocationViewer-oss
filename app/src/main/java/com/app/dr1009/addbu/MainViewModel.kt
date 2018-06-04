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

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var latitude = ObservableField("0.0")
        set (value) {
            var tmp = value.get()?.toDoubleOrNull() ?: 0.0
            if (tmp > 90.0) {
                tmp = 90.0
            } else if (tmp < -90.0) {
                tmp = -90.0
            }

            field.set(tmp.toString())
        }
    var longitude = ObservableField("0.0")
        set(value) {
            var tmp = value.get()?.toDoubleOrNull() ?: 0.0
            if (tmp > 180.0) {
                tmp = 180.0
            } else if (tmp < -180.0) {
                tmp = -180.0
            }

            field.set(tmp.toString())
        }

    var address = MutableLiveData<Address>()

    fun updateLocation(location: Location) {
        latitude.set(location.latitude.toString())
        longitude.set(location.longitude.toString())
    }

    fun updateAddress() {
        Completable
                .create {
                    try {
                        val list = Geocoder(getApplication()).getFromLocation(latitude.get()!!.toDouble(), longitude.get()!!.toDouble(), 1)
                        address.postValue(if (list.isEmpty()) {
                            null
                        } else {
                            list.first()
                        })
                    } catch (e: IOException) {
                        Log.e("MainViewModel", "updateAddress: ", e)
                    }
                }
                .observeOn(Schedulers.computation())
                .subscribe()

    }
}