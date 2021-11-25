package dev.jaym21.skanner.utils

import java.io.FileOutputStream

interface FileWritingCallback {

    fun write(out: FileOutputStream)
}
