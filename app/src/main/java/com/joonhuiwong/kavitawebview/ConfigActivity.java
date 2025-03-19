package com.joonhuiwong.kavitawebview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.ComponentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Initialize UI elements
        Spinner[] spinners = new Spinner[]{
                findViewById(R.id.spinner_volume_up),
                findViewById(R.id.spinner_volume_down),
                findViewById(R.id.spinner_swipe_left),
                findViewById(R.id.spinner_swipe_right),
                findViewById(R.id.spinner_swipe_up),
                findViewById(R.id.spinner_swipe_down),
                findViewById(R.id.spinner_double_tap_top),
                findViewById(R.id.spinner_double_tap_bottom),
                findViewById(R.id.spinner_double_tap_left),
                findViewById(R.id.spinner_double_tap_right)
        };
        EditText editTextUrl = findViewById(R.id.edittext_url);
        Slider sliderGestureDistance = findViewById(R.id.slider_gesture_distance);
        Slider sliderGestureVelocity = findViewById(R.id.slider_gesture_velocity);
        SwitchMaterial switchFullscreen = findViewById(R.id.switch_fullscreen);
        MaterialButton buttonReset = findViewById(R.id.button_reset);
        MaterialButton buttonSave = findViewById(R.id.button_save);
        MaterialButton buttonCancel = findViewById(R.id.button_cancel);

        Intent intent = getIntent();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.key_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set up spinners with defaults
        ConfigHelper.setupSpinner(spinners[0], adapter, intent.getStringExtra(ConfigConstants.EXTRA_VOLUME_UP), "Page Up");
        ConfigHelper.setupSpinner(spinners[1], adapter, intent.getStringExtra(ConfigConstants.EXTRA_VOLUME_DOWN), "Page Down");
        ConfigHelper.setupSpinner(spinners[2], adapter, intent.getStringExtra(ConfigConstants.EXTRA_SWIPE_LEFT), "Right");
        ConfigHelper.setupSpinner(spinners[3], adapter, intent.getStringExtra(ConfigConstants.EXTRA_SWIPE_RIGHT), "Left");
        ConfigHelper.setupSpinner(spinners[4], adapter, intent.getStringExtra(ConfigConstants.EXTRA_SWIPE_UP), "None");
        ConfigHelper.setupSpinner(spinners[5], adapter, intent.getStringExtra(ConfigConstants.EXTRA_SWIPE_DOWN), "None");
        ConfigHelper.setupSpinner(spinners[6], adapter, intent.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_TOP), "None");
        ConfigHelper.setupSpinner(spinners[7], adapter, intent.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_BOTTOM), "None");
        ConfigHelper.setupSpinner(spinners[8], adapter, intent.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_LEFT), "None");
        ConfigHelper.setupSpinner(spinners[9], adapter, intent.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_RIGHT), "None");

        sliderGestureDistance.setValue(intent.getFloatExtra(ConfigConstants.EXTRA_GESTURE_DISTANCE, ConfigConstants.DEFAULT_GESTURE_DISTANCE));
        sliderGestureVelocity.setValue(intent.getFloatExtra(ConfigConstants.EXTRA_GESTURE_VELOCITY, ConfigConstants.DEFAULT_GESTURE_VELOCITY));
        String url = intent.getStringExtra(ConfigConstants.EXTRA_URL);
        if (url != null) editTextUrl.setText(url);
        switchFullscreen.setChecked(intent.getBooleanExtra(ConfigConstants.EXTRA_FULLSCREEN, ConfigConstants.DEFAULT_FULLSCREEN));

        buttonSave.setEnabled(ConfigHelper.isValidUrl(url));
        ConfigHelper.updateSaveButtonState(buttonSave);

        editTextUrl.addTextChangedListener(ConfigHelper.getUrlTextWatcher(editTextUrl, buttonSave));
        ConfigHelper.setupResetButton(this, buttonReset, adapter, spinners, sliderGestureDistance, sliderGestureVelocity, switchFullscreen);
        ConfigHelper.setupSaveButton(this, buttonSave, spinners, sliderGestureDistance, sliderGestureVelocity, editTextUrl, switchFullscreen);
        buttonCancel.setOnClickListener(v -> finish());
    }
}