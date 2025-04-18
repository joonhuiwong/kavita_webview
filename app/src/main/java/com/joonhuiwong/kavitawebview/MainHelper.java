package com.joonhuiwong.kavitawebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MainHelper {

    private final WebView webView;
    private final Context context;
    private final SharedPreferences prefs;
    public String volumeUpBinding = "Page Up", volumeDownBinding = "Page Down", swipeLeftBinding = "Right",
            swipeRightBinding = "Left", swipeUpBinding = "None", swipeDownBinding = "None",
            doubleTapTopBinding = "None", doubleTapBottomBinding = "None", doubleTapLeftBinding = "None",
            doubleTapRightBinding = "None", currentUrl;
    public float gestureDistanceThreshold = 100f, gestureVelocityThreshold = 100f;
    public boolean hideStatusBar = ConfigConstants.DEFAULT_HIDE_STATUS_BAR,
            hideNavigationBar = ConfigConstants.DEFAULT_HIDE_NAVIGATION_BAR,
            disableTextSelection = ConfigConstants.DEFAULT_DISABLE_TEXT_SELECTION;
    private final GestureDetector gestureDetector;
    public boolean shouldClearHistory = false;
    private final Runnable showBackOptionsDialog; // Callback to trigger dialog

    public MainHelper(Context context, WebView webView, SharedPreferences prefs, Runnable showBackOptionsDialog) {
        this.context = context;
        this.webView = webView;
        this.prefs = prefs;
        this.showBackOptionsDialog = showBackOptionsDialog;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        loadPreferences();
    }

    public void loadPreferences() {
        volumeUpBinding = prefs.getString(MainConstants.PREF_KEYS[1], "Page Up");
        volumeDownBinding = prefs.getString(MainConstants.PREF_KEYS[2], "Page Down");
        swipeLeftBinding = prefs.getString(MainConstants.PREF_KEYS[3], "Right");
        swipeRightBinding = prefs.getString(MainConstants.PREF_KEYS[4], "Left");
        swipeUpBinding = prefs.getString(MainConstants.PREF_KEYS[5], "None");
        swipeDownBinding = prefs.getString(MainConstants.PREF_KEYS[6], "None");
        doubleTapTopBinding = prefs.getString(MainConstants.PREF_KEYS[7], "None");
        doubleTapBottomBinding = prefs.getString(MainConstants.PREF_KEYS[8], "None");
        doubleTapLeftBinding = prefs.getString(MainConstants.PREF_KEYS[9], "None");
        doubleTapRightBinding = prefs.getString(MainConstants.PREF_KEYS[10], "None");
        gestureDistanceThreshold = prefs.getFloat(MainConstants.PREF_KEYS[11], 100f);
        gestureVelocityThreshold = prefs.getFloat(MainConstants.PREF_KEYS[12], 100f);
        hideStatusBar = prefs.getBoolean(MainConstants.PREF_KEYS[13], ConfigConstants.DEFAULT_HIDE_STATUS_BAR);
        hideNavigationBar = prefs.getBoolean(MainConstants.PREF_KEYS[14], ConfigConstants.DEFAULT_HIDE_NAVIGATION_BAR);
        disableTextSelection = prefs.getBoolean(MainConstants.PREF_KEYS[15], ConfigConstants.DEFAULT_DISABLE_TEXT_SELECTION);
        currentUrl = prefs.getString(MainConstants.PREF_KEYS[0], null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupWebView() {
        webView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public void applyFullscreenMode(Window window) {
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        if (hideStatusBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (hideNavigationBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        if (!hideStatusBar && !hideNavigationBar) {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }

    public void handleConfigResult(Intent data) {
        volumeUpBinding = data.getStringExtra(ConfigConstants.EXTRA_VOLUME_UP);
        volumeDownBinding = data.getStringExtra(ConfigConstants.EXTRA_VOLUME_DOWN);
        swipeLeftBinding = data.getStringExtra(ConfigConstants.EXTRA_SWIPE_LEFT);
        swipeRightBinding = data.getStringExtra(ConfigConstants.EXTRA_SWIPE_RIGHT);
        swipeUpBinding = data.getStringExtra(ConfigConstants.EXTRA_SWIPE_UP);
        swipeDownBinding = data.getStringExtra(ConfigConstants.EXTRA_SWIPE_DOWN);
        doubleTapTopBinding = data.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_TOP);
        doubleTapBottomBinding = data.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_BOTTOM);
        doubleTapLeftBinding = data.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_LEFT);
        doubleTapRightBinding = data.getStringExtra(ConfigConstants.EXTRA_DOUBLE_TAP_RIGHT);
        gestureDistanceThreshold = data.getFloatExtra(ConfigConstants.EXTRA_GESTURE_DISTANCE, 100f);
        gestureVelocityThreshold = data.getFloatExtra(ConfigConstants.EXTRA_GESTURE_VELOCITY, 100f);
        String newUrl = data.getStringExtra(ConfigConstants.EXTRA_URL);
        hideStatusBar = data.getBooleanExtra(ConfigConstants.EXTRA_HIDE_STATUS_BAR, ConfigConstants.DEFAULT_HIDE_STATUS_BAR);
        hideNavigationBar = data.getBooleanExtra(ConfigConstants.EXTRA_HIDE_NAVIGATION_BAR, ConfigConstants.DEFAULT_HIDE_NAVIGATION_BAR);
        boolean newDisableTextSelection = data.getBooleanExtra(ConfigConstants.EXTRA_DISABLE_TEXT_SELECTION, ConfigConstants.DEFAULT_DISABLE_TEXT_SELECTION);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainConstants.PREF_KEYS[1], volumeUpBinding)
                .putString(MainConstants.PREF_KEYS[2], volumeDownBinding)
                .putString(MainConstants.PREF_KEYS[3], swipeLeftBinding)
                .putString(MainConstants.PREF_KEYS[4], swipeRightBinding)
                .putString(MainConstants.PREF_KEYS[5], swipeUpBinding)
                .putString(MainConstants.PREF_KEYS[6], swipeDownBinding)
                .putString(MainConstants.PREF_KEYS[7], doubleTapTopBinding)
                .putString(MainConstants.PREF_KEYS[8], doubleTapBottomBinding)
                .putString(MainConstants.PREF_KEYS[9], doubleTapLeftBinding)
                .putString(MainConstants.PREF_KEYS[10], doubleTapRightBinding)
                .putFloat(MainConstants.PREF_KEYS[11], gestureDistanceThreshold)
                .putFloat(MainConstants.PREF_KEYS[12], gestureVelocityThreshold)
                .putBoolean(MainConstants.PREF_KEYS[13], hideStatusBar)
                .putBoolean(MainConstants.PREF_KEYS[14], hideNavigationBar)
                .putBoolean(MainConstants.PREF_KEYS[15], disableTextSelection);
        if (newUrl != null && !newUrl.equals(currentUrl)) {
            currentUrl = newUrl;
            editor.putString(MainConstants.PREF_KEYS[0], currentUrl);
            shouldClearHistory = true;
            webView.loadUrl(currentUrl);
        }
        editor.apply();

        if (newDisableTextSelection != disableTextSelection) {
            disableTextSelection = newDisableTextSelection;
            if (disableTextSelection) {
                webView.loadUrl("javascript:(function() {" +
                        "var style = document.getElementById('disableTextSelectionStyle');" +
                        "if (!style) {" +
                        "  style = document.createElement('style');" +
                        "  style.id = 'disableTextSelectionStyle';" +
                        "  style.type = 'text/css';" +
                        "  style.innerHTML = '* { -webkit-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none; }';" +
                        "  document.head.appendChild(style);" +
                        "}" +
                        "})()");
            } else {
                webView.loadUrl("javascript:(function() {" +
                        "var style = document.getElementById('disableTextSelectionStyle');" +
                        "if (style) style.parentNode.removeChild(style);" +
                        "var enableStyle = document.createElement('style');" +
                        "enableStyle.id = 'enableTextSelectionStyle';" +
                        "enableStyle.type = 'text/css';" +
                        "enableStyle.innerHTML = '* { -webkit-user-select: auto; -moz-user-select: auto; -ms-user-select: auto; user-select: auto; }';" +
                        "document.head.appendChild(enableStyle);" +
                        "})()");
            }
        }
    }

    public Intent createConfigIntent() {
        Intent intent = new Intent(context, ConfigActivity.class);
        intent.putExtra(ConfigConstants.EXTRA_VOLUME_UP, volumeUpBinding)
                .putExtra(ConfigConstants.EXTRA_VOLUME_DOWN, volumeDownBinding)
                .putExtra(ConfigConstants.EXTRA_SWIPE_LEFT, swipeLeftBinding)
                .putExtra(ConfigConstants.EXTRA_SWIPE_RIGHT, swipeRightBinding)
                .putExtra(ConfigConstants.EXTRA_SWIPE_UP, swipeUpBinding)
                .putExtra(ConfigConstants.EXTRA_SWIPE_DOWN, swipeDownBinding)
                .putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_TOP, doubleTapTopBinding)
                .putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_BOTTOM, doubleTapBottomBinding)
                .putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_LEFT, doubleTapLeftBinding)
                .putExtra(ConfigConstants.EXTRA_DOUBLE_TAP_RIGHT, doubleTapRightBinding)
                .putExtra(ConfigConstants.EXTRA_GESTURE_DISTANCE, gestureDistanceThreshold)
                .putExtra(ConfigConstants.EXTRA_GESTURE_VELOCITY, gestureVelocityThreshold)
                .putExtra(ConfigConstants.EXTRA_URL, currentUrl)
                .putExtra(ConfigConstants.EXTRA_HIDE_STATUS_BAR, hideStatusBar)
                .putExtra(ConfigConstants.EXTRA_HIDE_NAVIGATION_BAR, hideNavigationBar)
                .putExtra(ConfigConstants.EXTRA_DISABLE_TEXT_SELECTION, disableTextSelection);
        return intent;
    }

    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            simulateKeyEvent(volumeUpBinding);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            simulateKeyEvent(volumeDownBinding);
            return true;
        }
        return false;
    }

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public boolean getShouldClearHistory() {
        return shouldClearHistory;
    }

    public void setShouldClearHistory(boolean shouldClearHistory) {
        this.shouldClearHistory = shouldClearHistory;
    }

    private void simulateKeyEvent(String binding) {
        int keyCode = getKeyCodeFromBinding(binding);
        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            webView.loadUrl("javascript:(function() { if (document.activeElement) document.activeElement.blur(); })()");
        }
    }

    private int getKeyCodeFromBinding(String binding) {
        switch (binding) {
            case "Page Up": return KeyEvent.KEYCODE_PAGE_UP;
            case "Page Down": return KeyEvent.KEYCODE_PAGE_DOWN;
            case "Left": return KeyEvent.KEYCODE_DPAD_LEFT;
            case "Right": return KeyEvent.KEYCODE_DPAD_RIGHT;
            case "Up": return KeyEvent.KEYCODE_DPAD_UP;
            case "Down": return KeyEvent.KEYCODE_DPAD_DOWN;
            default: return KeyEvent.KEYCODE_UNKNOWN;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - Objects.requireNonNull(e1).getX(), diffY = e2.getY() - e1.getY();
            float absDiffX = Math.abs(diffX), absDiffY = Math.abs(diffY);

            if (absDiffX > absDiffY && absDiffX > gestureDistanceThreshold && Math.abs(velocityX) > gestureVelocityThreshold) {
                String binding = diffX > 0 ? swipeRightBinding : swipeLeftBinding;
                int keyCode = getKeyCodeFromBinding(binding);
                if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                    simulateKeyEvent(binding);
                    return true;
                }
            } else if (absDiffY > absDiffX && absDiffY > gestureDistanceThreshold && Math.abs(velocityY) > gestureVelocityThreshold) {
                String binding = diffY > 0 ? swipeDownBinding : swipeUpBinding;
                int keyCode = getKeyCodeFromBinding(binding);
                if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                    simulateKeyEvent(binding);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            float x = e.getX(), y = e.getY();
            int viewWidth = webView.getWidth(), viewHeight = webView.getHeight();

            // Center region: 33% to 66% of width and height
            if (x >= viewWidth * 0.33f && x <= viewWidth * 0.66f &&
                    y >= viewHeight * 0.33f && y <= viewHeight * 0.66f) {
                showBackOptionsDialog.run();
                return true;
            }

            // Existing configurable double-tap regions
            if (x >= viewWidth * 0.33f && x <= viewWidth * 0.66f) {
                if (y <= viewHeight * 0.33f) simulateKeyEvent(doubleTapTopBinding);
                else if (y >= viewHeight * 0.66f) simulateKeyEvent(doubleTapBottomBinding);
            } else if (y > viewHeight * 0.33f && y < viewHeight * 0.66f) {
                if (x <= viewWidth * 0.33f) simulateKeyEvent(doubleTapLeftBinding);
                else if (x >= viewWidth * 0.66f) simulateKeyEvent(doubleTapRightBinding);
            }
            return true;
        }
    }
}