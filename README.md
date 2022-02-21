# Overview

Sample for [Photo picker](https://developer.android.com/about/versions/13/features#photo-picker) (Android 13 +)<br>

# Dev Memo
- Photo picker allows you to access Media without Runtime Permission
- [Define sharing limitations](https://developer.android.com/about/versions/13/features/photopicker#define_sharing_limitations)
  - The value of this intent-extra should be a positive integer greater than 1 and less than or equal to [MediaStore#getPickImagesMaxLimit](https://developer.android.com/reference/android/provider/MediaStore#getPickImagesMaxLimit()), otherwise Activity#RESULT_CANCELED is returned.
- The official documentation uses the deprecated onActivityResult, but you can use the recommended API ActivityResultLauncher.
  - https://github.com/LeoAndo/ActivityResultContractsKotlinSample
  - https://github.com/LeoAndo/ActivityResultContractsJavaSample 

# Test Data
- mp4
  - https://samplelib.com/sample-mp4.html
- png
  - https://assets.pokemon.com/assets/cms2/img/pokedex/full/001.png
  - https://assets.pokemon.com/assets/cms2/img/pokedex/full/500.png 

# [For Compose](https://github.com/LeoAndo/android-photo-picker-samples/tree/main/PhotoPickerComposeSample)
- material3
- VideoView (AndroidView)

# [For Kotlin](https://github.com/LeoAndo/android-photo-picker-samples/tree/main/PhotoPickerKotlinSample)
- material3

# [For Java](https://github.com/LeoAndo/android-photo-picker-samples/tree/main/PhotoPickerJavaSample)
- use Java 11 API
- Material2

# Capture

| Compose | View |
|:---|:---:|
|<img src="https://github.com/LeoAndo/android-photo-picker-samples/blob/main/PhotoPickerComposeSample/capture.gif" width=320 /> |<img src="https://github.com/LeoAndo/android-photo-picker-samples/blob/main/PhotoPickerJavaSample/capture.gif" width=320 /> |

# refs
https://developer.android.com/about/versions/13/features#photo-picker<br>
https://developer.android.com/about/versions/13/features/photopicker<br>
