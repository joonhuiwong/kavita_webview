package com.joonhuiwong.kavitawebview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigActivity extends ComponentActivity {

    private static final String TAG = "ConfigActivity";
    public static final String EXTRA_VOLUME_UP = "volume_up";
    public static final String EXTRA_VOLUME_DOWN = "volume_down";
    public static final String EXTRA_SWIPE_LEFT = "swipe_left";
    public static final String EXTRA_SWIPE_RIGHT = "swipe_right";
    public static final String EXTRA_GESTURE_DISTANCE = "gesture_distance";
    public static final String EXTRA_GESTURE_VELOCITY = "gesture_velocity";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_FULLSCREEN = "fullscreen";
    private static final float DEFAULT_GESTURE_DISTANCE = 100f;
    private static final float DEFAULT_GESTURE_VELOCITY = 100f;
    private static final boolean DEFAULT_FULLSCREEN = false;

    private Spinner spinnerVolumeUp;
    private Spinner spinnerVolumeDown;
    private Spinner spinnerSwipeLeft;
    private Spinner spinnerSwipeRight;
    private EditText editTextUrl;
    private Slider sliderGestureDistance;
    private Slider sliderGestureVelocity;
    private SwitchMaterial switchFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        spinnerVolumeUp = findViewById(R.id.spinner_volume_up);
        spinnerVolumeDown = findViewById(R.id.spinner_volume_down);
        spinnerSwipeLeft = findViewById(R.id.spinner_swipe_left);
        spinnerSwipeRight = findViewById(R.id.spinner_swipe_right);
        editTextUrl = findViewById(R.id.edittext_url);
        sliderGestureDistance = findViewById(R.id.slider_gesture_distance);
        sliderGestureVelocity = findViewById(R.id.slider_gesture_velocity);
        switchFullscreen = findViewById(R.id.switch_fullscreen);
        MaterialButton buttonReset = findViewById(R.id.button_reset);
        MaterialButton buttonSave = findViewById(R.id.button_save);
        MaterialButton buttonCancel = findViewById(R.id.button_cancel);

        Intent intent = getIntent();
        String volumeUp = intent.getStringExtra(EXTRA_VOLUME_UP);
        String volumeDown = intent.getStringExtra(EXTRA_VOLUME_DOWN);
        String swipeLeft = intent.getStringExtra(EXTRA_SWIPE_LEFT);
        String swipeRight = intent.getStringExtra(EXTRA_SWIPE_RIGHT);
        float gestureDistance = intent.getFloatExtra(EXTRA_GESTURE_DISTANCE, DEFAULT_GESTURE_DISTANCE);
        float gestureVelocity = intent.getFloatExtra(EXTRA_GESTURE_VELOCITY, DEFAULT_GESTURE_VELOCITY);
        String url = intent.getStringExtra(EXTRA_URL);
        boolean fullscreen = intent.getBooleanExtra(EXTRA_FULLSCREEN, DEFAULT_FULLSCREEN);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.key_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVolumeUp.setAdapter(adapter);
        spinnerVolumeDown.setAdapter(adapter);
        spinnerSwipeLeft.setAdapter(adapter);
        spinnerSwipeRight.setAdapter(adapter);

        if (volumeUp != null) {
            spinnerVolumeUp.setSelection(adapter.getPosition(volumeUp));
        } else {
            spinnerVolumeUp.setSelection(adapter.getPosition("Page Up"));
        }
        if (volumeDown != null) {
            spinnerVolumeDown.setSelection(adapter.getPosition(volumeDown));
        } else {
            spinnerVolumeDown.setSelection(adapter.getPosition("Page Down"));
        }
        if (swipeLeft != null) {
            spinnerSwipeLeft.setSelection(adapter.getPosition(swipeLeft));
        } else {
            spinnerSwipeLeft.setSelection(adapter.getPosition("Right"));
        }
        if (swipeRight != null) {
            spinnerSwipeRight.setSelection(adapter.getPosition(swipeRight));
        } else {
            spinnerSwipeRight.setSelection(adapter.getPosition("Left"));
        }
        sliderGestureDistance.setValue(gestureDistance);
        sliderGestureVelocity.setValue(gestureVelocity);
        if (url != null) {
            editTextUrl.setText(url);
        }
        switchFullscreen.setChecked(fullscreen);

        buttonReset.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to Reset to Defaults?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        spinnerVolumeUp.setSelection(adapter.getPosition("Page Up"));
                        spinnerVolumeDown.setSelection(adapter.getPosition("Page Down"));
                        spinnerSwipeLeft.setSelection(adapter.getPosition("Right"));
                        spinnerSwipeRight.setSelection(adapter.getPosition("Left"));
                        sliderGestureDistance.setValue(DEFAULT_GESTURE_DISTANCE);
                        sliderGestureVelocity.setValue(DEFAULT_GESTURE_VELOCITY);
                        switchFullscreen.setChecked(DEFAULT_FULLSCREEN);
                        Log.d(TAG, "Confirmed reset to defaults: Volume Up=Page Up, Volume Down=Page Down, " +
                                "Swipe Left=Right, Swipe Right=Left, Distance=" + DEFAULT_GESTURE_DISTANCE +
                                ", Velocity=" + DEFAULT_GESTURE_VELOCITY + ", Fullscreen=" + DEFAULT_FULLSCREEN +
                                ", URL unchanged=" + editTextUrl.getText().toString());
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        Log.d(TAG, "Reset to defaults cancelled");
                    })
                    .setCancelable(true)
                    .show();
        });

        buttonSave.setOnClickListener(v -> {
            String selectedVolumeUp = spinnerVolumeUp.getSelectedItem().toString();
            String selectedVolumeDown = spinnerVolumeDown.getSelectedItem().toString();
            String selectedSwipeLeft = spinnerSwipeLeft.getSelectedItem().toString();
            String selectedSwipeRight = spinnerSwipeRight.getSelectedItem().toString();
            float selectedGestureDistance = sliderGestureDistance.getValue();
            float selectedGestureVelocity = sliderGestureVelocity.getValue();
            String selectedUrl = editTextUrl.getText().toString().trim();
            boolean selectedFullscreen = switchFullscreen.isChecked();

            if (!isValidUrl(selectedUrl)) {
                Toast.makeText(this, "Invalid URL. Please enter a valid web address (e.g., http://192.168.1.100:5000 or https://example.com)", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Invalid URL attempted: " + selectedUrl);
                return;
            }

            Log.d(TAG, "Saving bindings: Volume Up=" + selectedVolumeUp + ", Volume Down=" + selectedVolumeDown +
                    ", Swipe Left=" + selectedSwipeLeft + ", Swipe Right=" + selectedSwipeRight +
                    ", Distance=" + selectedGestureDistance + ", Velocity=" + selectedGestureVelocity +
                    ", Fullscreen=" + selectedFullscreen + ", URL=" + selectedUrl);

            Intent result = new Intent();
            result.putExtra(EXTRA_VOLUME_UP, selectedVolumeUp);
            result.putExtra(EXTRA_VOLUME_DOWN, selectedVolumeDown);
            result.putExtra(EXTRA_SWIPE_LEFT, selectedSwipeLeft);
            result.putExtra(EXTRA_SWIPE_RIGHT, selectedSwipeRight);
            result.putExtra(EXTRA_GESTURE_DISTANCE, selectedGestureDistance);
            result.putExtra(EXTRA_GESTURE_VELOCITY, selectedGestureVelocity);
            result.putExtra(EXTRA_URL, selectedUrl);
            result.putExtra(EXTRA_FULLSCREEN, selectedFullscreen);
            setResult(RESULT_OK, result);
            finish();
        });

        buttonCancel.setOnClickListener(v -> {
            Log.d(TAG, "Cancelled config changes");
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return (url.startsWith("http://") || url.startsWith("https://")) && url.length() > "http://".length();
    }
}