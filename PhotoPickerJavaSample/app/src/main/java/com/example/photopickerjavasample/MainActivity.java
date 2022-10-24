package com.example.photopickerjavasample;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.MimeTypeFilter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private final ActivityResultLauncher<Intent> startForSingleModeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                final int resultCode = result.getResultCode();
                final Intent data = result.getData();
                Log.d(TAG, " resultCode: " + resultCode + " data: " + data);
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (data == null) return;
                    // Get photo picker response for single select.
                    final Uri currentUri = data.getData();
                    // Do stuff with the photo/video URI.
                    handlePickerResponse(currentUri);
                }
            });
    private final ActivityResultLauncher<Intent> startForMultipleModeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                final int resultCode = result.getResultCode();
                final Intent data = result.getData();
                Log.d(TAG, " resultCode: " + resultCode + " data: " + data);
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (data == null) return;
                    // output log.
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        final Uri currentUri2 = data.getClipData().getItemAt(i).getUri();
                        Log.d(TAG, "onActivityResult: currentUri" + currentUri2.toString());
                        // Do stuff with each photo/video URI.
                    }

                    // Get photo picker response for multi select.
                    final int itemCnt = data.getClipData().getItemCount();
                    final int bound = (itemCnt == 1) ? itemCnt : itemCnt - 1;
                    final int randomIndex = new Random().nextInt(bound);
                    Log.d(TAG, "onActivityResult: randomIndex: " + randomIndex);
                    var randomUri = data.getClipData().getItemAt(randomIndex).getUri();
                    handlePickerResponse(randomUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner spinnerType = findViewById(R.id.spinnerType);
        final Button buttonLaunchPicker = findViewById(R.id.buttonLaunchPicker);
        final TextInputLayout layoutNum = findViewById(R.id.layoutNum);
        final TextInputEditText edtNum = findViewById(R.id.edtNum);
        imageView = findViewById(R.id.imageView);

        edtNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean errorEnabled = TextUtils.isEmpty(s) || !TextUtils.isDigitsOnly(s);
                layoutNum.setErrorEnabled(errorEnabled);
                if (errorEnabled) layoutNum.setError("Please Input Max Num (ex: 2)");
                buttonLaunchPicker.setEnabled(!errorEnabled);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        final RadioGroup groupSelectMode = findViewById(R.id.groupSelectMode);
        groupSelectMode.setOnCheckedChangeListener((radioGroup, viewId) -> {
            switch (viewId) {
                case R.id.modeSingle:
                    layoutNum.setVisibility(View.GONE);
                    break;
                case R.id.modeMultiple:
                    layoutNum.setVisibility(View.VISIBLE);
                    break;
                default:
                    showDialog("checkedViewId: " + viewId);
            }
        });

        buttonLaunchPicker.setOnClickListener(view -> {
            imageView.setImageBitmap(null);
            final Optional<Editable> edtNumEditable = Optional.ofNullable(edtNum.getText());
            final int checkedViewId = groupSelectMode.getCheckedRadioButtonId();
            switch (checkedViewId) {
                case R.id.modeSingle:
                    final String selectedType = (String) spinnerType.getSelectedItem();
                    launchPickerSingleMode(selectedType);
                    break;
                case R.id.modeMultiple:
                    edtNumEditable.ifPresentOrElse(editable -> {
                        try {
                            final String edtNumText = editable.toString();
                            final int maxNumPhotosAndVideos = Integer.parseInt(edtNumText);
                            launchPickerMultipleMode(maxNumPhotosAndVideos);
                        } catch (NumberFormatException ex) {
                            showDialog(ex.getLocalizedMessage());
                        }
                    }, () -> showDialog("editable is null..."));
                    break;
                default:
                    showDialog("checkedViewId: " + checkedViewId);
            }
        });
    }

    private void handlePickerResponse(Uri currentUri) {
        Log.d(TAG, "handlePickerResponse: currentUri: " + currentUri);
        final Optional<MediaType> clipMimeType = getMediaType(currentUri); // get MIME_TYPE
        clipMimeType.ifPresentOrElse(type -> {
            switch (type) {
                case IMAGE_GIF:
                    showDialog("TODO Process the gif file!");
                    break;
                case IMAGE_PNG:
                case IMAGE_JPG:
                    try (final InputStream stream = getContentResolver().openInputStream(currentUri)) {
                        final Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        showDialog(e.getLocalizedMessage());
                    }
                    break;
                case VIDEO_MP4:
                    startVideoActivity(this, currentUri);
                    break;
                case VIDEO_WEBM:
                    showDialog("TODO Process the webm file!");
                    break;
            }
        }, () -> Log.d(TAG, "handlePickerResponse: error!"));
    }

    public static void startVideoActivity(Context context, Uri uri) {
        Intent starter = new Intent(context, VideoActivity.class);
        starter.setData(uri);
        context.startActivity(starter);
    }

    private Optional<MediaType> getMediaType(Uri currentUri) {
        final String type = getContentResolver().getType(currentUri);
        final String mimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        Log.d(TAG, "type: " + type + " mimeType: " + mimeType); // type: video/mp4 mimeType: mp4
        Optional<MediaType> result = Optional.empty();
        try {
            final String[] MIME_TYPES = new String[]{"image/*", "video/*"};
            Log.d(TAG, "MimeTypeFilter.matches " + MimeTypeFilter.matches(type, MIME_TYPES));
            result = MediaType.find(type);
        } catch (IllegalArgumentException ex) {
            showDialog(ex.getLocalizedMessage());
        }
        result.ifPresent(ret -> Log.d(TAG, "getMimeType: " + ret.getType())); // video/mp4
        return result;
    }

    // TODO: 2022/10/24 jetpackライブラリ内部処理 (new PickVisualMediaRequest.Builder()) がJavaから呼び出す作りになっていない
    // https://developer.android.com/training/data-storage/shared/photopicker#select-multiple-items
    private void launchPickerMultipleMode(int maxNumPhotosAndVideos) {
        if (maxNumPhotosAndVideos < 2 || maxNumPhotosAndVideos > MediaStore.getPickImagesMaxLimit()) {
            Log.d(TAG, "getPickImagesMaxLimit: " + MediaStore.getPickImagesMaxLimit());
            showDialog("The value of this intent-extra should be a positive integer greater than 1 and less than or equal to MediaStore#getPickImagesMaxLimit, otherwise Activity#RESULT_CANCELED is returned.");
            return;
        }

        // Launches photo picker in multi-select mode.
        // This means that user can select multiple photos/videos, up to the limit
        // specified by the app in the extra (maxNumPhotosAndVideos in this example).
        final Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxNumPhotosAndVideos);
        try {
            startForMultipleModeResult.launch(intent);
        } catch (ActivityNotFoundException ex) {
            showDialog(ex.getLocalizedMessage());
        }
    }

    // TODO: 2022/10/24 jetpackライブラリ内部処理 (new PickVisualMediaRequest.Builder()) がJavaから呼び出す作りになっていない
    // https://developer.android.com/training/data-storage/shared/photopicker#select-single-item
    private void launchPickerSingleMode(String type) {
        // Launches photo picker in single-select mode.
        // This means that the user can select one photo or video.
        final Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        if (!"all".equals(type)) intent.setType(type);
        try {
            startForSingleModeResult.launch(intent);
        } catch (ActivityNotFoundException ex) {
            showDialog(ex.getLocalizedMessage());
        }
    }

    private void showDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                .create().show();
        Log.d(TAG, message);
    }
}