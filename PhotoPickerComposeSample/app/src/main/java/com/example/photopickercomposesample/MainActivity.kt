package com.example.photopickercomposesample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.photopickercomposesample.ui.theme.PhotoPickerComposeSampleTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(
            this,
            "isPhotoPickerAvailable: ${isPhotoPickerAvailable()}",
            Toast.LENGTH_LONG
        ).show()
        setContent {
            PhotoPickerComposeSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }

    companion object {
        fun startVideoActivity(context: Context, uri: Uri?) {
            val starter = Intent(context, VideoActivity::class.java)
            starter.data = uri
            context.startActivity(starter)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun HomeScreen(
    scrollState: ScrollState = rememberScrollState(),
) {
    var expanded by remember { mutableStateOf(false) }
    var typeOrdinal by remember { mutableStateOf(MediaType.IMAGE_ALL.ordinal) }
    val radioOptions = listOf("single", "multiple")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    var maxNum by remember { mutableStateOf("10") }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                PaddingValues(20.dp)
            )
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Type(Single Mode Only)")
        Box {
            OutlinedTextField(
                value = MediaType.findTypeNameByOrdinal(typeOrdinal),
                onValueChange = { },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                placeholder = { Text(text = "select Media Type") }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                val menuItems = MediaType.values()
                menuItems.forEach {
                    DropdownMenuItem(
                        text = { Text(MediaType.findTypeNameByOrdinal(it.ordinal)) },
                        onClick = {
                            typeOrdinal = it.ordinal
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "Media type: ${it.name}"
                            )
                        })
                }
            }
        }

        Text(text = "Select Mode")

        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
        Column(Modifier.selectableGroup()) {
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = { onOptionSelected(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        // TODO: jetpackライブラリを使う場合、最大数の指定は動的に変更できない作りなので、蓋閉じする.
        OutlinedTextField(
            value = maxNum,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { maxNum = it },
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0f),
            isError = TextUtils.isEmpty(maxNum) || !TextUtils.isDigitsOnly(maxNum),
            label = {
                Text(
                    text = "max Num Photos And Videos(Multiple Mode Only)", fontSize = 12.sp
                )
            }
        )

        var imageUri by remember { mutableStateOf<Pair<Uri?, MediaType?>>(Pair(null, null)) }
        val startForSingleModeResult =
            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                Log.d("MainActivity", "Get photo picker response for single select: uri: $uri")
                // Do stuff with the photo/video URI.
                val (resUri, resType) = handlePickerResponse(context, uri)
                when (resType) {
                    MediaType.IMAGE_ALL, MediaType.IMAGE_GIF -> {
                        Toast.makeText(context, "TODO impl...", Toast.LENGTH_SHORT).show()
                    }
                    MediaType.IMAGE_PNG, MediaType.IMAGE_JPG -> {
                        imageUri = Pair(resUri, resType)
                    }
                    MediaType.VIDEO_ALL, MediaType.VIDEO_WEBM -> {
                        Toast.makeText(context, "TODO impl...", Toast.LENGTH_SHORT).show()
                    }
                    MediaType.VIDEO_MP4 -> {
                        MainActivity.startVideoActivity(context, resUri)
                    }
                    null -> {
                        Toast.makeText(context, "handle error...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        val startForMultipleModeResult =
            rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                Log.d("MainActivity", "GetPhotoPickerMultipleContent: uris: $uris")
                if (uris.isNotEmpty()) {
                    Log.d("MainActivity", "Number of items selected: ${uris.size}")
                    // output log.
                    uris.forEach { currentUri -> Log.d("MainActivity", "currentUri $currentUri") }

                    // Get photo picker response for multi select.
                    val randomIndex = Random().nextInt(uris.size)
                    Log.d("MainActivity", "size: ${uris.size} , randomIndex: $randomIndex")
                    val randomUri = uris[randomIndex]
                    val (resUri, resType) = handlePickerResponse(context, randomUri)
                    when (resType) {
                        MediaType.IMAGE_ALL, MediaType.IMAGE_GIF -> {
                            Toast.makeText(context, "TODO impl...", Toast.LENGTH_SHORT).show()
                        }
                        MediaType.IMAGE_PNG, MediaType.IMAGE_JPG -> {
                            imageUri = Pair(resUri, resType)
                        }
                        MediaType.VIDEO_ALL, MediaType.VIDEO_WEBM -> {
                            Toast.makeText(context, "TODO impl...", Toast.LENGTH_SHORT).show()
                        }
                        MediaType.VIDEO_MP4 -> {
                            MainActivity.startVideoActivity(context, resUri)
                        }
                        null -> {
                            Toast.makeText(context, "handle error...", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        OutlinedButton(
            onClick = {
                Log.d("HomeScreen", "Button Clicked")
                if (selectedOption == radioOptions[0]) {
                    // single mode
                    startForSingleModeResult.launch(
                        PickVisualMediaRequest.Builder()
                            .setMediaType(
                                ActivityResultContracts.PickVisualMedia.SingleMimeType(
                                    MediaType.findTypeNameByOrdinal(typeOrdinal)
                                )
                            ).build()
                    )
                } else if (selectedOption == radioOptions[1]) {
                    startForMultipleModeResult.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            },
            enabled = !TextUtils.isEmpty(maxNum) && TextUtils.isDigitsOnly(maxNum),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "launch photo Picker")
        }
        Image(
            painter = rememberImagePainter(imageUri.first),
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally),
            contentDescription = "selected Photo"
        )
    }
}

internal fun handlePickerResponse(context: Context, uri: Uri?): Pair<Uri?, MediaType?> {
    Log.d("MainActivity", "handlePickerResponse: currentUri: $uri")
    val currentUri = uri ?: return Pair(null, null)
    val type = getMediaType(context, currentUri) ?: return Pair(null, null)// get media type
    return Pair(currentUri, type)
}

private fun getMediaType(context: Context, currentUri: Uri): MediaType? {
    val type = context.contentResolver.getType(currentUri) ?: return null
    val mimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
    Log.d("", "type: $type mimeType: $mimeType") // type: video/mp4 mimeType: mp4
    var result: MediaType? = null
    try {
        result = MediaType.find(type)
    } catch (ex: IllegalArgumentException) {
        Toast.makeText(context, ex.localizedMessage, Toast.LENGTH_SHORT).show()
    }
    result?.let { ret -> Log.d("", "getMimeType: " + ret.type) }// video/mp4
    return result
}

@Preview
@Composable
fun Prev_HomeScreen() {
    PhotoPickerComposeSampleTheme {
        HomeScreen()
    }
}