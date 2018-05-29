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
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.dr1009.addbu.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.reactivex.Single
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
            it.setLifecycleOwner(this@MainActivity)
            it.viewModel = viewModel
        }
    }

    private val adapter = MainListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)

        binding.mainContent.let {
            it.recycler.adapter = adapter
            val latAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.lat_array, android.R.layout.simple_spinner_item)
            latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            it.spinnerLat.adapter = latAdapter
            it.spinnerLat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.isSouth = position == 1
                }
            }

            val lonAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.lon_array, android.R.layout.simple_spinner_item)
            lonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            it.spinnerLon.adapter = lonAdapter
            it.spinnerLon.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.isWest = position == 1
                }
            }
        }

        viewModel.address.observe(this, Observer {
            adapter.setAddress(applicationContext, it)
        })
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
        LocationServices.getFusedLocationProviderClient(this@MainActivity).run {
            Single
                    .create<Location> { lastLocation.addOnSuccessListener(it::onSuccess).addOnFailureListener(it::onError) }
                    .subscribeOn(Schedulers.io())
                    .subscribe(viewModel::updateLocation, { Log.e("MainActivity", "fetchLocation: ", it) })
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
