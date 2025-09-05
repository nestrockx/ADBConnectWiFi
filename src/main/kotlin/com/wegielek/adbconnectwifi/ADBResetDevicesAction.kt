package com.wegielek.adbconnectwifi

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages

class ADBResetDevicesAction: AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        try {
            val service = service<DeviceIpService>()
            val devices = service.getAllDevices()
            for (device in devices) {
                AdbUtils.disconnectOverWifi(device.deviceIp)
            }
            service.clearDevices()
        } catch (e: Exception) {
            Messages.showErrorDialog(e.message, "ADB Wi-Fi Error")
        }
        Messages.showInfoMessage("Removed all save devices.", "Reset Devices")
    }
}