package dev.jaym21.skanner.ui.components

internal interface ScanSurfaceListener {
    fun scanSurfacePictureTaken()
    fun scanSurfaceShowProgress()
    fun scanSurfaceHideProgress()
}