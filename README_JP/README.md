# Overview

Sample for [Photo picker](https://developer.android.com/about/versions/13/features#photo-picker) (Android 13 +)<br>

# Dev Memo
- Photo pickerを使うと、Runtime PermissionなしでMediaにアクセスできる
- [複数ファイルを同時に選択することも可能](https://developer.android.com/about/versions/13/features/photopicker#define_sharing_limitations)
  - [MediaStore.EXTRA_PICK_IMAGES_MAX](https://developer.android.com/reference/android/provider/MediaStore#EXTRA_PICK_IMAGES_MAX)に指定できる値は、2から[MediaStore#getPickImagesMaxLimit](https://developer.android.com/reference/android/provider/MediaStore#getPickImagesMaxLimit())以下の正の整数まで。そうでない場合、Activity＃RESULT_CANCELEDが返される
- 公式ドキュメントでは、非推奨のonActivityResultを使用しているが、推奨APIのActivityResultLauncherを使用できる。
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
- VideoViewのみ AndroidViewで実装
- Jetpackの互換性APIを使用

# [For Kotlin](https://github.com/LeoAndo/android-photo-picker-samples/tree/main/PhotoPickerKotlinSample)
- material3
- Jetpackの互換性APIを使用

# [For Java](https://github.com/LeoAndo/android-photo-picker-samples/tree/main/PhotoPickerJavaSample)
- use Java 11 API
- Material2
- Jetpackの互換性API 未使用

# Capture

| Compose | View |
|:---|:---:|
|<img src="https://github.com/LeoAndo/android-photo-picker-samples/blob/main/PhotoPickerComposeSample/capture.gif" width=320 /> |<img src="https://github.com/LeoAndo/android-photo-picker-samples/blob/main/PhotoPickerJavaSample/capture.gif" width=320 /> |

# refs
https://developer.android.com/about/versions/13/features#photo-picker<br>
https://developer.android.com/about/versions/13/features/photopicker<br>
