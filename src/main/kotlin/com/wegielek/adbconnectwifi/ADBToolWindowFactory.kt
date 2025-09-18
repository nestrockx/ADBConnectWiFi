package com.wegielek.adbconnectwifi

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.content.ContentFactory
import javax.swing.JButton
import javax.swing.SwingUtilities

class ADBToolWindowFactory : ToolWindowFactory, DumbAware {



    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        fun refresh() {
            SwingUtilities.invokeLater {
                toolWindow.contentManager.removeAllContents(true)
                createToolWindowContent(project, toolWindow)
            }
        }

        val service = service<DeviceIpService>()

        fun refreshUi(): javax.swing.JComponent {
            val savedDevices = service.getAllDevices()
            val usbDevices = AdbUtils.getConnectedDevices()
                .map { id -> AdbUtils.getDeviceModel(id).trim() + " ($id)" }

            return panel {
                group("Saved Devices") {
                    if (savedDevices.isEmpty()) {
                        row { label("No saved devices") }
                    } else {
                        for (device in savedDevices) {
                            val isConnected = AdbUtils.isDeviceConnected(device.deviceIp)
                            row {
                                label(device.deviceId.split(" ")[0])
                                if (!isConnected) {
                                    button("Connect") {
                                        val result = AdbUtils.connectOverWifi(device.deviceIp)
                                        com.intellij.openapi.ui.Messages.showInfoMessage(
                                            "Connected to ${device.deviceId} at ${device.deviceIp}\n$result",
                                            "ADB Wi-Fi"
                                        )
                                        refresh()
                                    }
                                } else {
                                    button("Disconnect") {
                                        val result = AdbUtils.disconnectOverWifi(device.deviceIp)
                                        com.intellij.openapi.ui.Messages.showInfoMessage(
                                            "Disconnected from ${device.deviceId} at ${device.deviceIp}\n$result",
                                            "ADB Wi-Fi"
                                        )
                                        refresh()
                                    }
                                }
                            }
                        }
                    }
                }

                group("USB Devices") {
                    if (usbDevices.isEmpty()) {
                        row { label("No devices connected via USB") }
                    } else {
                        for (device in usbDevices) {
                            val regex = Regex("\\(\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+\\)")
                            if (regex.find(device) != null) {
                                continue
                            }
                            val id = device.substringAfter("(").substringBefore(")")
                            row {
                                label(device)
                                button("Connect Wi-Fi") {
                                    AdbUtils.enableTcpIp(id)
                                    Thread.sleep(500)
                                    val ip = AdbUtils.getDeviceIp(id)
                                    if (ip != null) {
                                        val result = AdbUtils.connectOverWifi(ip)
                                        service.saveDevice("$device [saved]", ip)
                                        com.intellij.openapi.ui.Messages.showInfoMessage(
                                            "Connected to $device at $ip\n$result",
                                            "ADB Wi-Fi"
                                        )
                                    } else {
                                        com.intellij.openapi.ui.Messages.showErrorDialog(
                                            "Could not get IP for $device",
                                            "ADB Wi-Fi Error"
                                        )
                                    }
                                    // refresh UI after action
                                    SwingUtilities.invokeLater {
                                        toolWindow.contentManager.removeAllContents(true)
                                        createToolWindowContent(project, toolWindow)
                                    }
                                }
                            }
                        }
                    }
                }

                group("Actions") {
                    row {
                        button("Forget All Devices") {
                            for (device in savedDevices) {
                                AdbUtils.disconnectOverWifi(device.deviceIp)
                            }
                            service.clearDevices()
                            com.intellij.openapi.ui.Messages.showInfoMessage(
                                "All saved devices removed.",
                                "ADB Wi-Fi"
                            )
                            SwingUtilities.invokeLater {
                                toolWindow.contentManager.removeAllContents(true)
                                createToolWindowContent(project, toolWindow)
                            }
                        }
                        button("Refresh") {
                            SwingUtilities.invokeLater {
                                toolWindow.contentManager.removeAllContents(true)
                                createToolWindowContent(project, toolWindow)
                            }
                        }
                    }
                }
            }
        }

        val content = ContentFactory.getInstance().createContent(refreshUi(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}
