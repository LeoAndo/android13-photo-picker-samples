package com.example.photopickerkotlinsample

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.MimeTypeFilter
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val startForSingleModeResult =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { currentUri ->
            if (currentUri != null) {
                showToast("Selected URI: $currentUri")
                handlePickerResponse(currentUri)
            } else {
                showToast("No media selected")
            }
        }
    private val startForMultipleModeResult =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)
        ) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                // output log.
                uris.forEach { currentUri -> Log.d(TAG, "currentUri $currentUri") }
                // Get photo picker response for multi select.
                val randomIndex = Random().nextInt(uris.size)
                showToast("randomIndex: $randomIndex")
                val randomUri = uris[randomIndex]
                handlePickerResponse(randomUri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showToast("isPhotoPickerAvailable: ${isPhotoPickerAvailable()}")
        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val buttonLaunchPicker = findViewById<Button>(R.id.buttonLaunchPicker)
        val layoutNum = findViewById<TextInputLayout>(R.id.layoutNum)
        val edtNum = findViewById<TextInputEditText>(R.id.edtNum)
        imageView = findViewById(R.id.imageView)
        edtNum.doOnTextChanged { s, start, before, count ->
            val errorEnabled = TextUtils.isEmpty(s) || !TextUtils.isDigitsOnly(s)
            layoutNum.isErrorEnabled = errorEnabled
            if (errorEnabled) layoutNum.error = "Please Input Max Num (ex: 2)"
            buttonLaunchPicker.isEnabled = !errorEnabled
        }
        val groupSelectMode = findViewById<RadioGroup>(R.id.groupSelectMode)
        groupSelectMode.setOnCheckedChangeListener { _: RadioGroup?, viewId: Int ->
//            when (viewId) {
//                R.id.modeSingle -> layoutNum.visibility = View.GONE
//                R.id.modeMultiple -> layoutNum.visibility = View.VISIBLE
//                else -> showToast("checkedViewId: $viewId")
//            }
        }
        buttonLaunchPicker.setOnClickListener {
            imageView.setImageBitmap(null)
            when (val checkedViewId = groupSelectMode.checkedRadioButtonId) {
                R.id.modeSingle -> {
                    launchPickerSingleMode(MediaType.firstOrNull(spinnerType.selectedItem as String))
                }
                R.id.modeMultiple -> {
                    try {
                        val edtNumText = edtNum.text.toString()
                        val maxNumPhotosAndVideos = edtNumText.toInt()
                        // launchPickerMultipleMode(maxNumPhotosAndVideos)
                        launchPickerMultipleMode()
                    } catch (ex: NumberFormatException) {
                        showToast(ex.localizedMessage ?: "error")
                    }
                }
                else -> showToast("checkedViewId: $checkedViewId")
            }
        }
    }

    private fun handlePickerResponse(uri: Uri?) {
        Log.d(TAG, "handlePickerResponse: currentUri: $uri")
        val currentUri = uri ?: return
        val type = getMediaType(currentUri) ?: return // get MIME_TYPE
        when (type) {
            MediaType.IMAGE_GIF -> showToast("TODO Process the gif file!")
            MediaType.IMAGE_PNG, MediaType.IMAGE_JPG -> {
                contentResolver.openInputStream(currentUri).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    imageView.setImageBitmap(bitmap)
                }
            }
            MediaType.VIDEO_MP4 -> startVideoActivity(this, currentUri)
            MediaType.VIDEO_WEBM -> showToast("TODO Process the webm file!")
            else -> showToast("TODO Process the ${type.type} file!")
        }
    }

    private fun getMediaType(currentUri: Uri): MediaType? {
        val type = contentResolver.getType(currentUri) ?: return null
        val mimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
        Log.d(TAG, "type: $type mimeType: $mimeType") // type: video/mp4 mimeType: mp4
        var result: MediaType? = null
        try {
            val filters = arrayOf("image/*", "video/*")
            Log.d(TAG, "MimeTypeFilter.matches " + MimeTypeFilter.matches(type, filters))
            result = MediaType.firstOrNull(type)
        } catch (ex: IllegalArgumentException) {
            showToast(ex.localizedMessage ?: "error")
        }
        result?.let { ret -> Log.d(TAG, "getMimeType: " + ret.type) }// video/mp4
        return result
    }

    /*
    private fun launchPickerMultipleMode(maxNumPhotosAndVideos: Int) {
        if (maxNumPhotosAndVideos < 2 || maxNumPhotosAndVideos > MediaStore.getPickImagesMaxLimit()) {
            Log.d(TAG, "getPickImagesMaxLimit: " + MediaStore.getPickImagesMaxLimit())
            showToast("The value of this intent-extra should be a positive integer greater than 1 and less than or equal to MediaStore#getPickImagesMaxLimit, otherwise Activity#RESULT_CANCELED is returned.")
            return
        }

        // Launches photo picker in multi-select mode.
        // This means that user can select multiple photos/videos, up to the limit
        // specified by the app in the extra (maxNumPhotosAndVideos in this example).
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxNumPhotosAndVideos)
        try {
            startForMultipleModeResult.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.localizedMessage)
        }
    }
     */
    private fun launchPickerMultipleMode() {
        try {
            startForMultipleModeResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.localizedMessage ?: "error")
        }
    }

    private fun launchPickerSingleMode(type: MediaType?) {
        type ?: return
        val m = when (type) {
            MediaType.ALL -> {
                ActivityResultContracts.PickVisualMedia.ImageAndVideo
            }
            MediaType.IMAGE_ALL -> {
                ActivityResultContracts.PickVisualMedia.ImageOnly
            }
            MediaType.VIDEO_ALL -> {
                ActivityResultContracts.PickVisualMedia.VideoOnly
            }
            MediaType.IMAGE_GIF, MediaType.IMAGE_PNG, MediaType.IMAGE_JPG, MediaType.VIDEO_WEBM, MediaType.VIDEO_MP4 -> {
                ActivityResultContracts.PickVisualMedia.SingleMimeType(type.type)
            }
        }
        try {
            startForSingleModeResult.launch(
                PickVisualMediaRequest.Builder().setMediaType(m).build()
            )
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.localizedMessage ?: "error")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, message)
    }

    companion object {
        private const val TAG = "MainActivity"
        fun startVideoActivity(context: Context, uri: Uri?) {
            val starter = Intent(context, VideoActivity::class.java)
            starter.data = uri
            context.startActivity(starter)
        }
    }
}