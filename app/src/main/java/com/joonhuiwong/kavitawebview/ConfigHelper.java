package com.joonhuiwong.kavitawebview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigHelper {

    public static void setupSpinner(Spinner spinner, ArrayAdapter<CharSequence> adapter, String value, String defaultValue) {
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(value != null ? value : defaultValue));
    }

    public static String normalizeUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        String trimmedInput = input.trim();
        if (!trimmedInput.startsWith("http://") && !trimmedInput.startsWith("https://")) {
            return "https://" + trimmedInput;
        }
        return trimmedInput;
    }

    public static boolean isValidUrl(String url) {
        String normalizedUrl = normalizeUrl(url);
        return !normalizedUrl.isEmpty() && (normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://"));
    }

    public static void updateSaveButtonState(MaterialButton buttonSave) {
        buttonSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                buttonSave.isEnabled() ? 0xFF4AC694 : 0xFFB0B0B0));
    }

    public static TextWatcher getUrlTextWatcher(EditText editTextUrl, MaterialButton buttonSave) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                String normalizedInput = normalizeUrl(input);
                buttonSave.setEnabled(isValidUrl(input));
                updateSaveButtonState(buttonSave);
                editTextUrl.setHint(input.isEmpty() ? "Enter URL *" : "Enter URL");
                editTextUrl.setHintTextColor(input.isEmpty() ? 0xFFFF0000 : 0xFF757575);
                Log.d(ConfigConstants.TAG, "URL input changed: " + input + ", Normalized: " + normalizedInput + ", Save enabled: " + buttonSave.isEnabled());
            }
        };
    }

    public static void setupResetButton(ConfigActivity activity, MaterialButton buttonReset,
                                        ArrayAdapter<CharSequence> adapter, Spinner[] spinners,
                                        Slider sliderGestureDistance, Slider sliderGestureVelocity,
                                        SwitchMaterial switchHideStatusBar, SwitchMaterial switchHideNavigationBar,
                                        SwitchMaterial switchDisableTextSelection) {
        buttonReset.setOnClickListener(v -> new AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to Reset to Defaults?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    spinners[0].setSelection(adapter.getPosition("Page Up"));      // Volume Up
                    spinners[1].setSelection(adapter.getPosition("Page Down"));   // Volume Down
                    spinners[2].setSelection(adapter.getPosition("Right"));       // Swipe Left
                    spinners[3].setSelection(adapter.getPosition("Left"));        // Swipe Right
                    spinners[4].setSelection(adapter.getPosition("None"));        // Swipe Up
                    spinners[5].setSelection(adapter.getPosition("None"));        // Swipe Down
                    spinners[6].setSelection(adapter.getPosition("None"));        // Double Tap Top
                    spinners[7].setSelection(adapter.getPosition("None"));        // Double Tap Bottom
                    spinners[8].setSelection(adapter.getPosition("None"));        // Double Tap Left
                    spinners[9].setSelection(adapter.getPosition("None"));        // Double Tap Right
                    sliderGestureDistance.setValue(ConfigConstants.DEFAULT_GESTURE_DISTANCE);
                    sliderGestureVelocity.setValue(ConfigConstants.DEFAULT_GESTURE_VELOCITY);
                    switchHideStatusBar.setChecked(ConfigConstants.DEFAULT_HIDE_STATUS_BAR);
                    switchHideNavigationBar.setChecked(ConfigConstants.DEFAULT_HIDE_NAVIGATION_BAR);
                    switchDisableTextSelection.setChecked(ConfigConstants.DEFAULT_DISABLE_TEXT_SELECTION);
                    Log.d(ConfigConstants.TAG, "Reset to defaults");
                })
                .setNegativeButton("No", null)
                .show());
    }

    public static void setupSaveButton(ConfigActivity activity, MaterialButton buttonSave,
                                       Spinner[] spinners, Slider sliderGestureDistance,
                                       Slider sliderGestureVelocity, EditText editTextUrl,
                                       SwitchMaterial switchHideStatusBar, SwitchMaterial switchHideNavigationBar,
                                       SwitchMaterial switchDisableTextSelection) {
        buttonSave.setOnClickListener(v -> {
            if (!buttonSave.isEnabled()) return;
            Intent result = new Intent();
            result.putExtra(ConfigConstants.EXTRA_VOLUME_UP, spinners[0].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_VOLUME_DOWN, spinners[1].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_SWIPE_LEFT, spinners[2].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_SWIPE_RIGHT, spinners[3].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_SWIPE_UP, spinners[4].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_SWIPE_DOWN, spinners[5].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_TOP, spinners[6].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_BOTTOM, spinners[7].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_LEFT, spinners[8].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_RIGHT, spinners[9].getSelectedItem().toString());
            result.putExtra(ConfigConstants.EXTRA_GESTURE_DISTANCE, sliderGestureDistance.getValue());
            result.putExtra(ConfigConstants.EXTRA_GESTURE_VELOCITY, sliderGestureVelocity.getValue());
            String inputUrl = editTextUrl.getText().toString().trim();
            String normalizedUrl = normalizeUrl(inputUrl);
            result.putExtra(ConfigConstants.EXTRA_URL, normalizedUrl); // Save the normalized URL
            result.putExtra(ConfigConstants.EXTRA_HIDE_STATUS_BAR, switchHideStatusBar.isChecked());
            result.putExtra(ConfigConstants.EXTRA_HIDE_NAVIGATION_BAR, switchHideNavigationBar.isChecked());
            result.putExtra(ConfigConstants.EXTRA_DISABLE_TEXT_SELECTION, switchDisableTextSelection.isChecked());
            Log.d(ConfigConstants.TAG, "Saving config with URL: " + normalizedUrl);
            activity.setResult(Activity.RESULT_OK, result);
            activity.finish();
        });
    }
}