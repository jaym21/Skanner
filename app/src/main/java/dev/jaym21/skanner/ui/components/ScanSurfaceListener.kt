package dev.jaym21.skanner.ui.components

import android.net.Uri

internal interface ScanSurfaceListener {
    fun scanSurfacePictureTaken(savedUri: Uri)
    fun scanSurfaceShowProgress()
    fun scanSurfaceHideProgress()
}