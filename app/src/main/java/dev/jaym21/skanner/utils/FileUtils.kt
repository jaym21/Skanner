package dev.jaym21.skanner.utils

import android.app.Activity
import android.content.Context
import android.os.Environment
import dev.jaym21.skanner.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileUtils {
    companion object {
        // Using external media if it is available or else our app's file directory
        fun getOutputDirectory(activity: Activity): File {
            val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
                File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir
            else
                activity.filesDir
        }

        // function to create a timestamped file
        fun createFile(baseFolder: File, format: String, extension: String): File {
            return File(baseFolder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension)
        }

        fun writeFile(context: Context, baseDirectory: String, fileName: String, callback: FileWritingCallback) {
            val sd = context.getExternalFilesDir(null).ab
        }
    }
}