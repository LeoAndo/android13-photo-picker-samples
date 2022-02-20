package com.example.photopickerjavasample;

import java.util.Arrays;
import java.util.Optional;

enum MediaType {
    IMAGE_GIF("image/gif"),
    IMAGE_PNG("image/png"),
    IMAGE_JPG("image/jpeg"),
    VIDEO_WEBM("video/webm"),
    VIDEO_MP4("video/mp4");

    private final String type;

    MediaType(String type) {
        this.type = type;
    }

    String getType() {
        return type;
    }


    static Optional<MediaType> find(String type) throws IllegalArgumentException {
        return Arrays.stream(values()).filter(mediaType -> mediaType.type.equals(type)).findFirst();
    }
}
