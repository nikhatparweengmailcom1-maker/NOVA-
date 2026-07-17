package com.nova.assistant.features.flashlight

import android.content.Context
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controls the device flashlight via Camera2 API.
 */
@Singleton
class FlashlightManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var isOn = false
    private var cameraId: String? = null

    private fun getCameraId(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get camera ID for flashlight")
            null
        }
    }

    fun turnOn() {
        try {
            val id = cameraId ?: getCameraId() ?: return
            cameraId = id
            cameraManager.setTorchMode(id, true)
            isOn = true
            Timber.d("Flashlight: ON")
        } catch (e: Exception) {
            Timber.e(e, "Failed to turn on flashlight")
        }
    }

    fun turnOff() {
        try {
            val id = cameraId ?: getCameraId() ?: return
            cameraManager.setTorchMode(id, false)
            isOn = false
            Timber.d("Flashlight: OFF")
        } catch (e: Exception) {
            Timber.e(e, "Failed to turn off flashlight")
        }
    }

    fun toggle() {
        if (isOn) turnOff() else turnOn()
    }

    fun isFlashlightOn() = isOn

    fun isAvailable(): Boolean {
        return try {
            context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_FLASH)
        } catch (e: Exception) {
            false
        }
    }
}
