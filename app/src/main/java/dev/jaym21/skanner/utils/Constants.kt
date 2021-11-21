package dev.jaym21.skanner.utils

class Constants {
    companion object {
        const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val PHOTO_EXTENSION = ".jpg"
        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0
        const val CAMERA_REQUEST_CODE = 1001
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1002
        const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1003
        const val IMAGE_ANALYSIS_SCALE_WIDTH = 400
        const val BLURRING_KERNEL_SIZE = 5.0
        const val NORMALIZATION_MIN_VALUE = 0.0
        const val NORMALIZATION_MAX_VALUE = 255.0
        const val ANGLES_NUMBER = 4
        const val EPSILON_CONSTANT = 0.02
        const val CLOSE_KERNEL_SIZE = 10.0
        const val CANNY_THRESHOLD_LOW = 75.0
        const val CANNY_THRESHOLD_HIGH = 200.0
        const val CUTOFF_THRESHOLD = 155.0
        const val TRUNCATE_THRESHOLD = 150.0
    }
}