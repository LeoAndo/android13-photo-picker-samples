package com.example.photopickerkotlinsample


internal enum class MediaType(val type: String) {
    IMAGE_GIF("image/gif"), IMAGE_PNG("image/png"), IMAGE_JPG("image/jpeg"), VIDEO_WEBM("video/webm"), VIDEO_MP4(
        "video/mp4"
    );

    companion object {
        fun find(type: String): MediaType? {
            return values().firstOrNull { it.type == type }
        }
    }
}