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

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.location.Address
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class MainListAdapter : ListAdapter<MainListAdapter.Item, MainListAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    fun setAddress(context: Context, address: Address?) {
        val list = mutableListOf<Item>()
        address?.run {
            getAddressLine(0)?.let {
                if (it.isNotEmpty()) {
                    list.add(Item(context.getString(R.string.full_address), it))
                }
            }
            countryCode?.let {
                list.add(Item(context.getString(R.string.country_code), it))
            }
            countryName?.let {
                list.add(Item(context.getString(R.string.country_name), it))
            }
            featureName?.let {
                list.add(Item(context.getString(R.string.feature_name), it))
            }
            adminArea?.let {
                list.add(Item(context.getString(R.string.admin_area), it))
            }
            subAdminArea?.let {
                list.add(Item(context.getString(R.string.sub_admin_area), it))
            }
            locality?.let {
                list.add(Item(context.getString(R.string.locality), it))
            }
            subLocality?.let {
                list.add(Item(context.getString(R.string.sub_locality), it))
            }
            thoroughfare?.let {
                list.add(Item(context.getString(R.string.thoroughfare), it))
            }
            subThoroughfare?.let {
                list.add(Item(context.getString(R.string.sub_thoroughfare), it))
            }
            premises?.let {
                list.add(Item(context.getString(R.string.premises), it))
            }
            postalCode?.let {
                list.add(Item(context.getString(R.string.postal_code), "〒$it"))
            }
        }

        submitList(list)
    }

    inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private val binding: ViewDataBinding? = DataBindingUtil.bind(view)
        fun bindTo(item: Item) {
            binding?.run {
                setVariable(BR.item, item)
                executePendingBindings()
            }
        }
    }

    inner class Item(val title: String, val text: String)
}