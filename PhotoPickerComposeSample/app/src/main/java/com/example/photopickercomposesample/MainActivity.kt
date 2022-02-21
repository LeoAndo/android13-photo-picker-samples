package com.example.photopickercomposesample

import android.content.ClipData
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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        private const val TAG = "MainActivity"
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
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = LocalContentColor.current.copy(
                        LocalContentAlpha.current
                    )
                ),
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
        OutlinedTextField(
            value = maxNum,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { maxNum = it },
            modifier = Modifier.fillMaxWidth(),
            isError = TextUtils.isEmpty(maxNum) || !TextUtils.isDigitsOnly(maxNum),
            label = {
                Text(
                    text = "max Num Photos And Videos(Multiple Mode Only)", fontSize = 12.sp
                )
            }
        )

        var imageUri by remember { mutableStateOf<Pair<Uri?, MediaType?>>(Pair(null, null)) }
        val startForSingleModeResult =
            rememberLauncherForActivityResult(GetPhotoPickerSingleContent()) { uri: Uri? ->
                // Get photo picker response for single select.
                Log.d("", "GetPhotoPickerSingleContent: uri: $uri")
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
            rememberLauncherForActivityResult(GetPhotoPickerMultipleContent()) { data: ClipData? ->
                Log.d("", "GetPhotoPickerMultipleContent: clipData: $data")
                val clipData = data ?: return@rememberLauncherForActivityResult
                // output log.
                (0 until clipData.itemCount).forEach { i ->
                    val currentUri2 = clipData.getItemAt(i).uri
                    Log.d("", "onActivityResult: currentUri$currentUri2")
                    // Do stuff with each photo/video URI.
                }
                // Get photo picker response for multi select.
                val itemCnt = clipData.itemCount
                val bound = if (itemCnt == 1) itemCnt else itemCnt - 1
                val randomIndex = Random().nextInt(bound)
                Log.d("", "onActivityResult: itemCnt: $itemCnt")
                Log.d("", "onActivityResult: randomIndex: $randomIndex")
                val randomUri = clipData.getItemAt(randomIndex).uri
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
            }
        OutlinedButton(
            onClick = {
                Log.d("HomeScreen", "Button Clicked")
                if (selectedOption == radioOptions[0]) {
                    // single mode
                    startForSingleModeResult.launch(
                        SingleInputData(
                            MediaType.findTypeNameByOrdinal(
                                typeOrdinal
                            )
                        )
                    )
                } else if (selectedOption == radioOptions[1]) {
                    startForMultipleModeResult.launch(MultipleInputData(maxNum.toInt()))
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
    Log.d("", "handlePickerResponse: currentUri: $uri")
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