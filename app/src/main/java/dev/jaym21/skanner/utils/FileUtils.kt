package dev.jaym21.skanner.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import dev.jaym21.skanner.R
import java.io.File
import java.io.FileOutputStream

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


        //to get a list of all image files inside document directory
        fun getAllFiles(context: Context, dirPath: String): List<File> {
            val fileList = arrayListOf<File>()

            //getting external storage
            val externalStorageVolumes = ContextCompat.getExternalFilesDirs(context.applicationContext, null)
            val primaryExternalStorage = externalStorageVolumes[0]
            val targetDirectory = File(primaryExternalStorage, dirPath)

            if (targetDirectory.listFiles() != null) {
                targetDirectory.listFiles()?.forEach {
                    fileList.add(it)
                }
            }
            return fileList
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

        fun writeFile(activity: Activity, baseDirectory: String, fileName: String, callback: FileWritingCallback) {
            //creating destination file name
            val absFilename = baseDirectory + fileName
            //destination file
            val dest = File(getOutputDirectory(activity), absFilename)

            //writing file to destination
            val out = FileOutputStream(dest)
            callback.write(out)

            //closing FileOutputStream
            out.flush()
            out.close()
        }

        fun deleteFile(activity: Activity, filePath: String) {
            val targetFile = File(getOutputDirectory(activity), filePath)
            targetFile.delete()
        }
    }
}