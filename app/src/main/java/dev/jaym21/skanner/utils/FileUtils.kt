package dev.jaym21.skanner.utils

import android.content.Context
import dev.jaym21.skanner.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileUtils {
    companion object {
        // Using external media if it is available or else our app's file directory
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir
            else
                appContext.filesDir
        }

        // function to create a timestamped file
        fun createFile(baseFolder: File, format: String, extension: String): File {
            return File(baseFolder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension)
        }
    }
}