package com.example.photopickerkotlinsample

import java.util.*


internal enum class MediaType(val type: String) {
    IMAGE_GIF("image/gif"), IMAGE_PNG("image/png"), IMAGE_JPG("image/jpeg"), VIDEO_WEBM("video/webm"), VIDEO_MP4(
        "video/mp4"
    );

    companion object {
        @Throws(IllegalArgumentException::class)
        fun find(type: String): Optional<MediaType> {
            return Arrays.stream(values()).filter { mediaType: MediaType -> mediaType.type == type }
                .findFirst()
        }
    }
}