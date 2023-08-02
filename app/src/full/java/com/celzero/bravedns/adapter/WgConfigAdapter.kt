/*
 * Copyright 2023 RethinkDNS and its authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.celzero.bravedns.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.celzero.bravedns.R
import com.celzero.bravedns.database.WgConfigFiles
import com.celzero.bravedns.databinding.ListItemWgInterfaceBinding
import com.celzero.bravedns.service.WireguardManager
import com.celzero.bravedns.ui.WgConfigDetailActivity
import com.celzero.bravedns.ui.WgConfigEditorActivity.Companion.INTENT_EXTRA_WG_ID
import com.celzero.bravedns.util.LoggerConstants

class WgConfigAdapter(private val context: Context) :
    PagingDataAdapter<WgConfigFiles, WgConfigAdapter.WgInterfaceViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<WgConfigFiles>() {

                override fun areItemsTheSame(
                    oldConnection: WgConfigFiles,
                    newConnection: WgConfigFiles
                ): Boolean {
                    return (oldConnection.id == newConnection.id)
                }

                override fun areContentsTheSame(
                    oldConnection: WgConfigFiles,
                    newConnection: WgConfigFiles
                ): Boolean {
                    return (oldConnection.id == newConnection.id)
                }
            }
    }

    override fun onBindViewHolder(holder: WgInterfaceViewHolder, position: Int) {
        val item = getItem(position)
        Log.d(
            LoggerConstants.LOG_TAG_PROXY,
            "onBindViewHolder: position - $position, ${item?.name}, ${item?.id}"
        )
        val wgConfigFiles: WgConfigFiles = item ?: return
        holder.update(wgConfigFiles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WgInterfaceViewHolder {
        val itemBinding =
            ListItemWgInterfaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WgInterfaceViewHolder(itemBinding)
    }

    inner class WgInterfaceViewHolder(private val b: ListItemWgInterfaceBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun update(wgConfigFiles: WgConfigFiles) {
            b.interfaceNameText.text = wgConfigFiles.name
            b.interfaceSwitch.isChecked = wgConfigFiles.isActive
            setupClickListeners(wgConfigFiles)
        }

        fun setupClickListeners(wgConfigFiles: WgConfigFiles) {
            b.interfaceNameText.setOnClickListener { launchConfigDetail(wgConfigFiles.id) }

            b.interfaceSwitch.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    if (WireguardManager.canEnableConfig(wgConfigFiles)) {
                        WireguardManager.enableConfig(wgConfigFiles)
                    } else {
                        b.interfaceSwitch.isChecked = false
                        Toast.makeText(
                                context,
                                context.getString(R.string.wireguard_enabled_failure),
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }
                } else {
                    WireguardManager.disableConfig(wgConfigFiles)
                }
            }
        }

        private fun launchConfigDetail(id: Int) {
            val intent = Intent(context, WgConfigDetailActivity::class.java)
            intent.putExtra(INTENT_EXTRA_WG_ID, id)
            context.startActivity(intent)
        }
    }
}
