package dev.jaym21.skanner.utils

class Constants {
    companion object {
        const val FILENAME = "dd_MM_yyyy_HH_mm_ss"
        const val PHOTO_EXTENSION = ".jpg"
        const val PDF_EXTENSION = ".pdf"
        const val CAMERA_REQUEST_CODE = 1001
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1002
        const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1003
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
        const val DOWNSCALE_IMAGE_SIZE = 600.0
        const val FIRST_MAX_CONTOURS = 10
        const val SMALLEST_ANGLE_COS = 0.172
        const val ANGLE_OF_ROTATION = 90
    }
}