package com.example.photopickerkotlinsample

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val startForSingleModeResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            Log.d(
                TAG,
                " resultCode: $resultCode data: $data"
            )
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
        Log.d(
            TAG,
            " resultCode: $resultCode data: $data"
        )
        if (result.resultCode == RESULT_OK) {
            if (data == null) return@registerForActivityResult
            // output log.
            for (i in 0 until data.clipData!!.itemCount) {
                val currentUri2 = data.clipData!!.getItemAt(i).uri
                Log.d(
                    TAG,
                    "onActivityResult: currentUri$currentUri2"
                )
                // Do stuff with each photo/video URI.
            }

            // Get photo picker response for multi select.
            val itemCnt = data.clipData!!.itemCount
            val bound = if (itemCnt == 1) itemCnt else itemCnt - 1
            val randomIndex = Random().nextInt(bound)
            Log.d(
                TAG,
                "onActivityResult: randomIndex: $randomIndex"
            )
            val randomUri =
                data.clipData!!.getItemAt(randomIndex).uri
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
        edtNum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val errorEnabled = TextUtils.isEmpty(s) || !TextUtils.isDigitsOnly(s)
                layoutNum.isErrorEnabled = errorEnabled
                if (errorEnabled) layoutNum.error = "Please Input Max Num (ex: 2)"
                buttonLaunchPicker.isEnabled = !errorEnabled
            }

            override fun afterTextChanged(s: Editable) {}
        })
        val groupSelectMode = findViewById<RadioGroup>(R.id.groupSelectMode)
        groupSelectMode.setOnCheckedChangeListener { radioGroup: RadioGroup?, viewId: Int ->
            when (viewId) {
                R.id.modeSingle -> layoutNum.visibility = View.GONE
                R.id.modeMultiple -> layoutNum.visibility = View.VISIBLE
                else -> showDialog("checkedViewId: $viewId")
            }
        }
        buttonLaunchPicker.setOnClickListener { view: View? ->
            imageView.setImageBitmap(null)
            val edtNumEditable =
                Optional.ofNullable(edtNum.text)
            val checkedViewId = groupSelectMode.checkedRadioButtonId
            when (checkedViewId) {
                R.id.modeSingle -> {
                    val selectedType = spinnerType.selectedItem as String
                    launchPickerSingleMode(selectedType)
                }
                R.id.modeMultiple -> edtNumEditable.ifPresentOrElse({ editable: Editable ->
                    try {
                        val edtNumText = editable.toString()
                        val maxNumPhotosAndVideos = edtNumText.toInt()
                        launchPickerMultipleMode(maxNumPhotosAndVideos)
                    } catch (ex: NumberFormatException) {
                        showDialog(ex.localizedMessage)
                    }
                }) { showDialog("editable is null...") }
                else -> showDialog("checkedViewId: $checkedViewId")
            }
        }
    }

    private fun handlePickerResponse(currentUri: Uri?) {
        Log.d(
            TAG,
            "handlePickerResponse: currentUri: $currentUri"
        )
        val clipMimeType = getMediaType(currentUri) // get MIME_TYPE
        clipMimeType.ifPresentOrElse(
            { type: MediaType? ->
                when (type) {
                    MediaType.IMAGE_GIF -> showDialog("TODO Process the gif file!")
                    MediaType.IMAGE_PNG, MediaType.IMAGE_JPG -> try {
                        contentResolver.openInputStream(currentUri!!).use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            imageView!!.setImageBitmap(bitmap)
                        }
                    } catch (e: IOException) {
                        showDialog(e.localizedMessage)
                    }
                    MediaType.VIDEO_MP4 -> startVideoActivity(this, currentUri)
                    MediaType.VIDEO_WEBM -> showDialog("TODO Process the webm file!")
                }
            }
        ) {
            Log.d(
                TAG,
                "handlePickerResponse: error!"
            )
        }
    }

    private fun getMediaType(currentUri: Uri?): Optional<MediaType> {
        val type = contentResolver.getType(currentUri!!)
        val mimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
        Log.d(TAG, "type: $type mimeType: $mimeType") // type: video/mp4 mimeType: mp4
        var result = Optional.empty<MediaType>()
        try {
            val MIME_TYPES = arrayOf("image/*", "video/*")
            Log.d(TAG, "MimeTypeFilter.matches " + MimeTypeFilter.matches(type, MIME_TYPES))
            result = MediaType.find(type!!)
        } catch (ex: IllegalArgumentException) {
            showDialog(ex.localizedMessage)
        }
        result.ifPresent { ret: MediaType -> Log.d(TAG, "getMimeType: " + ret.type) } // video/mp4
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
            ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
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