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
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.dr1009.addbu.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy { ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java) }
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).also {
            it.lifecycleOwner = this@MainActivity
            it.viewModel = viewModel
        }
    }

    private var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)

        val adapter = MainListAdapter()
        binding.mainContent.recycler.adapter = adapter

        viewModel.address.observe(this, Observer {
            adapter.setAddress(applicationContext, it)
        })
    }

    override fun onPause() {
        super.onPause()
        if (mDisposable?.isDisposed == false) {
            mDisposable?.dispose()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.menu_get_location -> {
            fetchLocationWithPermissionCheck()
            true
        }
        R.id.menu_oss_license -> {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            true
        }
        else -> false
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun fetchLocation() {
        if (mDisposable?.isDisposed == false) {
            mDisposable?.dispose()
        }

        mDisposable = Single
                .create<Location> { emitter ->
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                            .run {
                                lastLocation.addOnCompleteListener {
                                    val result = it.result
                                    if (result != null) {
                                        emitter.onSuccess(result)
                                    } else {
                                        emitter.onError(IllegalStateException())
                                    }
                                }
                            }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { location, _ ->
                    if (location != null) {
                        viewModel.updateLocation(location)
                    } else {
                        Toast.makeText(applicationContext, "Problems occurred", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForLocation(request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok) { _, _ -> request.proceed() }
                .setCancelable(false)
                .setMessage(R.string.require_location_permission)
                .show()
    }
}
