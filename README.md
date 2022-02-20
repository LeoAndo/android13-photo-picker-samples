# Overview

Sample for [Photo picker](https://developer.android.com/about/versions/13/features#photo-picker) (Android 13 +)<br>

# Dev Memo
- Photo pickerを使うと、Runtime PermissionなしでMediaにアクセスできる
- [複数ファイルを同時に選択することも可能](https://developer.android.com/about/versions/13/features/photopicker#define_sharing_limitations)
  - MediaStore.EXTRA_PICK_IMAGES_MAXに指定できる値は、2からMediaStore＃getPickImagesMaxLimit以下の正の整数まで。そうでない場合、Activity＃RESULT_CANCELEDが返される

# Test Data
- mp4
  - https://samplelib.com/sample-mp4.html
- png
  - https://assets.pokemon.com/assets/cms2/img/pokedex/full/001.png
  - https://assets.pokemon.com/assets/cms2/img/pokedex/full/500.png 

# For Compose

# For Kotlin

# [For Java](https://github.com/LeoAndo/android-photo-picker-samples/tree/main/PhotoPickerJavaSample)

# Capture

| Compose | View |
|:---|:---:|
|<img src="" width=320 /> |<img src="https://github.com/LeoAndo/android-photo-picker-samples/blob/main/PhotoPickerJavaSample/capture.gif" width=320 /> |

# refs
https://developer.android.com/about/versions/13/features#photo-picker<br>
https://developer.android.com/about/versions/13/features/photopicker<br>
