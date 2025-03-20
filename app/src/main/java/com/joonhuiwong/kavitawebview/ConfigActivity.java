package com.joonhuiwong.kavitawebview;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigActivity extends ComponentActivity {

    private EditText editTextUrl;
    private Spinner[] spinners;
    private Slider sliderGestureDistance, sliderGestureVelocity;
    private SwitchMaterial switchHideStatusBar, switchHideNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupContentView(savedInstanceState);
    }

    private void setupContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_config);

        // Initialize UI elements
        spinners = new Spinner[]{
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
        editTextUrl = findViewById(R.id.edittext_url);
        sliderGestureDistance = findViewById(R.id.slider_gesture_distance);
        sliderGestureVelocity = findViewById(R.id.slider_gesture_velocity);
        switchHideStatusBar = findViewById(R.id.switch_hide_status_bar);
        switchHideNavigationBar = findViewById(R.id.switch_hide_navigation_bar);
        MaterialButton buttonReset = findViewById(R.id.button_reset);
        MaterialButton buttonSave = findViewById(R.id.button_save);
        MaterialButton buttonCancel = findViewById(R.id.button_cancel);

        Intent intent = getIntent();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.key_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set up spinners with defaults (first run) or restore state (rotation/process death)
        if (savedInstanceState == null) {
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
            switchHideStatusBar.setChecked(intent.getBooleanExtra(ConfigConstants.EXTRA_HIDE_STATUS_BAR, ConfigConstants.DEFAULT_HIDE_STATUS_BAR));
            switchHideNavigationBar.setChecked(intent.getBooleanExtra(ConfigConstants.EXTRA_HIDE_NAVIGATION_BAR, ConfigConstants.DEFAULT_HIDE_NAVIGATION_BAR));
        } else {
            restoreState(savedInstanceState);
        }

        buttonSave.setEnabled(ConfigHelper.isValidUrl(editTextUrl.getText().toString()));
        ConfigHelper.updateSaveButtonState(buttonSave);

        editTextUrl.addTextChangedListener(ConfigHelper.getUrlTextWatcher(editTextUrl, buttonSave));
        ConfigHelper.setupResetButton(this, buttonReset, adapter, spinners, sliderGestureDistance, sliderGestureVelocity, switchHideStatusBar, switchHideNavigationBar);
        ConfigHelper.setupSaveButton(this, buttonSave, spinners, sliderGestureDistance, sliderGestureVelocity, editTextUrl, switchHideStatusBar, switchHideNavigationBar);
        buttonCancel.setOnClickListener(v -> finish());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Bundle state = new Bundle();
        saveState(state);
        setupContentView(state);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
    }

    private void saveState(Bundle outState) {
        outState.putString("url", editTextUrl.getText().toString());
        for (int i = 0; i < spinners.length; i++) {
            outState.putInt("spinner_" + i, spinners[i].getSelectedItemPosition());
        }
        outState.putFloat("gestureDistance", sliderGestureDistance.getValue());
        outState.putFloat("gestureVelocity", sliderGestureVelocity.getValue());
        outState.putBoolean("hideStatusBar", switchHideStatusBar.isChecked());
        outState.putBoolean("hideNavigationBar", switchHideNavigationBar.isChecked());
    }

    private void restoreState(Bundle savedInstanceState) {
        editTextUrl.setText(savedInstanceState.getString("url", ""));
        for (int i = 0; i < spinners.length; i++) {
            spinners[i].setSelection(savedInstanceState.getInt("spinner_" + i, 0));
        }
        sliderGestureDistance.setValue(savedInstanceState.getFloat("gestureDistance", ConfigConstants.DEFAULT_GESTURE_DISTANCE));
        sliderGestureVelocity.setValue(savedInstanceState.getFloat("gestureVelocity", ConfigConstants.DEFAULT_GESTURE_VELOCITY));
        switchHideStatusBar.setChecked(savedInstanceState.getBoolean("hideStatusBar", ConfigConstants.DEFAULT_HIDE_STATUS_BAR));
        switchHideNavigationBar.setChecked(savedInstanceState.getBoolean("hideNavigationBar", ConfigConstants.DEFAULT_HIDE_NAVIGATION_BAR));
    }
}