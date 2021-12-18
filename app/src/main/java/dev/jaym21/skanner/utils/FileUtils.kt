package dev.jaym21.skanner.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import dev.jaym21.skanner.R
import java.io.File
import java.io.FileOutputStream

class FileUtils {
    companion object {

        // Using external media if it is available or else our app's file directory
        private fun getOutputDirectory(activity: Activity): File {
            val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
                File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir
            else
                activity.filesDir
        }

        // function to make directory in external storage
        fun mkdir(activity: Activity, dirPath: String): File {
            //creating a storage directory
            val storageDirectory = File(getOutputDirectory(activity), dirPath)
            if (!storageDirectory.exists()) {
                storageDirectory.mkdir()
            }
            return storageDirectory
        }

        fun writeBitmapToFile(fileName: File, bitmap: Bitmap) {
            if (fileName.exists())
                fileName.delete()

            //writing file to destination
            val out = FileOutputStream(fileName)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)

            //closing FileOutputStream
            out.flush()
            out.close()
        }
    }
}