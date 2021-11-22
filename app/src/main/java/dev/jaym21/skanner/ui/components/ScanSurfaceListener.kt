package dev.jaym21.skanner.ui.components

import android.net.Uri
import java.io.File

internal interface ScanSurfaceListener {
    fun scanSurfacePictureTaken(savedUri: Uri, originalImageFile: File)
    fun scanSurfaceShowProgress()
    fun scanSurfaceHideProgress()
}