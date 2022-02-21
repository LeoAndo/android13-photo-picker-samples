package com.example.photopickercomposesample


internal enum class MediaType(val type: String) {
    IMAGE_ALL("image/*"),
    IMAGE_GIF("image/gif"),
    IMAGE_PNG("image/png"),
    IMAGE_JPG("image/jpeg"),
    VIDEO_ALL("video/*"),
    VIDEO_WEBM("video/webm"),
    VIDEO_MP4("video/mp4");

    companion object {
        fun find(type: String): MediaType? {
            return values().firstOrNull { it.type == type }
        }

        fun findTypeNameByOrdinal(ordinal: Int): String {
            return values().firstOrNull { it.ordinal == ordinal }?.type ?: ""
        }
    }
}