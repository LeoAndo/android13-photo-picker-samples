package com.example.photopickerkotlinsample


internal enum class MediaType(val type: String) {
    ALL("all"),
    IMAGE_ALL("image/*"),
    IMAGE_GIF("image/gif"),
    IMAGE_PNG("image/png"),
    IMAGE_JPG("image/jpeg"),
    VIDEO_ALL("video/*"),
    VIDEO_WEBM("video/webm"),
    VIDEO_MP4("video/mp4");

    companion object {
        fun firstOrNull(type: String): MediaType? {
            return values().firstOrNull { it.type == type }
        }
    }
}