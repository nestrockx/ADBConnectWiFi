package com.wegielek.adbconnectwifi

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

data class DeviceInfo(var deviceId: String = "", var deviceIp: String = "")

data class DeviceIpState(var devices: MutableList<DeviceInfo> = mutableListOf())

@Service
@State(name = "ADBConnectWiFiSettings", storages = [Storage("adbconnectwifi.xml")])
class DeviceIpService : PersistentStateComponent<DeviceIpState> {

    private var state = DeviceIpState()

    override fun getState(): DeviceIpState = state

    override fun loadState(state: DeviceIpState) {
        this.state = state
    }

    fun saveDevice(deviceId: String, ip: String) {
        // Replace if exists, else add
        val existing = state.devices.find { it.deviceId == deviceId }
        if (existing != null) {
            existing.deviceIp = ip
        } else {
            state.devices.add(DeviceInfo(deviceId, ip))
        }
    }

    fun clearDevices() {
        state.devices.clear()
    }

    fun getDeviceIp(deviceId: String): String? {
        return state.devices.find { it.deviceId == deviceId }?.deviceIp
    }

    fun getAllDevices(): List<DeviceInfo> = state.devices
}
