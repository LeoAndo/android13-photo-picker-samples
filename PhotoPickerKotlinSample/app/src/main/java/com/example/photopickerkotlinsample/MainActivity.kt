package com.example.photopickerkotlinsample

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.MimeTypeFilter
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val startForSingleModeResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            Log.d(TAG, " resultCode: $resultCode data: $data")
            if (result.resultCode == RESULT_OK) {
                if (data == null) return@registerForActivityResult
                // Get photo picker response for single select.
                val currentUri = data.data
                // Do stuff with the photo/video URI.
                handlePickerResponse(currentUri)
            }
        }
    private val startForMultipleModeResult = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data
        Log.d(TAG, " resultCode: $resultCode data: $data")
        if (result.resultCode == RESULT_OK) {
            if (data == null) return@registerForActivityResult
            val clipData = data.clipData ?: return@registerForActivityResult
            // output log.
            (0 until clipData.itemCount).forEach { i ->
                val currentUri2 = clipData.getItemAt(i).uri
                Log.d(TAG, "onActivityResult: currentUri$currentUri2")
                // Do stuff with each photo/video URI.
            }

            // Get photo picker response for multi select.
            val itemCnt = clipData.itemCount
            val bound = if (itemCnt == 1) itemCnt else itemCnt - 1
            val randomIndex = Random().nextInt(bound)
            Log.d(TAG, "onActivityResult: randomIndex: $randomIndex")
            val randomUri = clipData.getItemAt(randomIndex).uri
            handlePickerResponse(randomUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            when (viewId) {
                R.id.modeSingle -> layoutNum.visibility = View.GONE
                R.id.modeMultiple -> layoutNum.visibility = View.VISIBLE
                else -> showDialog("checkedViewId: $viewId")
            }
        }
        buttonLaunchPicker.setOnClickListener {
            imageView.setImageBitmap(null)
            when (val checkedViewId = groupSelectMode.checkedRadioButtonId) {
                R.id.modeSingle -> {
                    val selectedType = spinnerType.selectedItem as String
                    launchPickerSingleMode(selectedType)
                }
                R.id.modeMultiple -> {
                    try {
                        val edtNumText = edtNum.text.toString()
                        val maxNumPhotosAndVideos = edtNumText.toInt()
                        launchPickerMultipleMode(maxNumPhotosAndVideos)
                    } catch (ex: NumberFormatException) {
                        showDialog(ex.localizedMessage)
                    }
                }
                else -> showDialog("checkedViewId: $checkedViewId")
            }
        }
    }

    private fun handlePickerResponse(uri: Uri?) {
        Log.d(TAG, "handlePickerResponse: currentUri: $uri")
        val currentUri = uri ?: return
        val type = getMediaType(currentUri) ?: return // get MIME_TYPE
        when (type) {
            MediaType.IMAGE_GIF -> showDialog("TODO Process the gif file!")
            MediaType.IMAGE_PNG, MediaType.IMAGE_JPG -> {
                contentResolver.openInputStream(currentUri).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    imageView.setImageBitmap(bitmap)
                }
            }
            MediaType.VIDEO_MP4 -> startVideoActivity(this, currentUri)
            MediaType.VIDEO_WEBM -> showDialog("TODO Process the webm file!")
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
            result = MediaType.find(type)
        } catch (ex: IllegalArgumentException) {
            showDialog(ex.localizedMessage)
        }
        result?.let { ret -> Log.d(TAG, "getMimeType: " + ret.type) }// video/mp4
        return result
    }

    private fun launchPickerMultipleMode(maxNumPhotosAndVideos: Int) {
        if (maxNumPhotosAndVideos < 2 || maxNumPhotosAndVideos > MediaStore.getPickImagesMaxLimit()) {
            Log.d(TAG, "getPickImagesMaxLimit: " + MediaStore.getPickImagesMaxLimit())
            showDialog("The value of this intent-extra should be a positive integer greater than 1 and less than or equal to MediaStore#getPickImagesMaxLimit, otherwise Activity#RESULT_CANCELED is returned.")
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
            showDialog(ex.localizedMessage)
        }
    }

    private fun launchPickerSingleMode(type: String) {
        // Launches photo picker in single-select mode.
        // This means that the user can select one photo or video.
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        if ("all" != type) intent.type = type
        try {
            startForSingleModeResult.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            showDialog(ex.localizedMessage)
        }
    }

    private fun showDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton(
                android.R.string.ok
            ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            .create().show()
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