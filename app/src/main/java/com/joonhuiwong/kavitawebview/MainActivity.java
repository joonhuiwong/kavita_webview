package com.joonhuiwong.kavitawebview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class MainActivity extends ComponentActivity {

    private WebView webView;
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "KavitaWebViewPrefs";
    private static final String KEY_URL = "currentUrl";
    private static final String KEY_VOLUME_UP = "volumeUpBinding";
    private static final String KEY_VOLUME_DOWN = "volumeDownBinding";
    private static final String KEY_SWIPE_LEFT = "swipeLeftBinding";
    private static final String KEY_SWIPE_RIGHT = "swipeRightBinding";
    private static final String KEY_SWIPE_UP = "swipeUpBinding"; // New
    private static final String KEY_SWIPE_DOWN = "swipeDownBinding"; // New
    private static final String KEY_DOUBLE_TAP_TOP = "doubleTapTopBinding";
    private static final String KEY_DOUBLE_TAP_BOTTOM = "doubleTapBottomBinding";
    private static final String KEY_DOUBLE_TAP_LEFT = "doubleTapLeftBinding"; // New
    private static final String KEY_DOUBLE_TAP_RIGHT = "doubleTapRightBinding"; // New
    private static final String KEY_GESTURE_DISTANCE = "gestureDistanceThreshold";
    private static final String KEY_GESTURE_VELOCITY = "gestureVelocityThreshold";
    private static final String KEY_FULLSCREEN = "fullscreenEnabled";
    private String volumeUpBinding;
    private String volumeDownBinding;
    private String swipeLeftBinding;
    private String swipeRightBinding;
    private String swipeUpBinding; // New
    private String swipeDownBinding; // New
    private String doubleTapTopBinding;
    private String doubleTapBottomBinding;
    private String doubleTapLeftBinding; // New
    private String doubleTapRightBinding; // New
    private float gestureDistanceThreshold;
    private float gestureVelocityThreshold;
    private String currentUrl;
    private boolean shouldClearHistory = false;
    private boolean fullscreenEnabled;
    private GestureDetector gestureDetector;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<Intent> configLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String newVolumeUp = result.getData().getStringExtra(ConfigActivity.EXTRA_VOLUME_UP);
                    String newVolumeDown = result.getData().getStringExtra(ConfigActivity.EXTRA_VOLUME_DOWN);
                    String newSwipeLeft = result.getData().getStringExtra(ConfigActivity.EXTRA_SWIPE_LEFT);
                    String newSwipeRight = result.getData().getStringExtra(ConfigActivity.EXTRA_SWIPE_RIGHT);
                    String newSwipeUp = result.getData().getStringExtra(ConfigActivity.EXTRA_SWIPE_UP); // New
                    String newSwipeDown = result.getData().getStringExtra(ConfigActivity.EXTRA_SWIPE_DOWN); // New
                    String newDoubleTapTop = result.getData().getStringExtra(ConfigActivity.EXTRA_DOUBLE_TAP_TOP);
                    String newDoubleTapBottom = result.getData().getStringExtra(ConfigActivity.EXTRA_DOUBLE_TAP_BOTTOM);
                    String newDoubleTapLeft = result.getData().getStringExtra(ConfigActivity.EXTRA_DOUBLE_TAP_LEFT); // New
                    String newDoubleTapRight = result.getData().getStringExtra(ConfigActivity.EXTRA_DOUBLE_TAP_RIGHT); // New
                    float newGestureDistance = result.getData().getFloatExtra(ConfigActivity.EXTRA_GESTURE_DISTANCE, 100f);
                    float newGestureVelocity = result.getData().getFloatExtra(ConfigActivity.EXTRA_GESTURE_VELOCITY, 100f);
                    String newUrl = result.getData().getStringExtra(ConfigActivity.EXTRA_URL);
                    boolean newFullscreen = result.getData().getBooleanExtra(ConfigActivity.EXTRA_FULLSCREEN, true);

                    volumeUpBinding = newVolumeUp;
                    volumeDownBinding = newVolumeDown;
                    swipeLeftBinding = newSwipeLeft;
                    swipeRightBinding = newSwipeRight;
                    swipeUpBinding = newSwipeUp; // New
                    swipeDownBinding = newSwipeDown; // New
                    doubleTapTopBinding = newDoubleTapTop;
                    doubleTapBottomBinding = newDoubleTapBottom;
                    doubleTapLeftBinding = newDoubleTapLeft; // New
                    doubleTapRightBinding = newDoubleTapRight; // New
                    gestureDistanceThreshold = newGestureDistance;
                    gestureVelocityThreshold = newGestureVelocity;
                    fullscreenEnabled = newFullscreen;

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_VOLUME_UP, volumeUpBinding);
                    editor.putString(KEY_VOLUME_DOWN, volumeDownBinding);
                    editor.putString(KEY_SWIPE_LEFT, swipeLeftBinding);
                    editor.putString(KEY_SWIPE_RIGHT, swipeRightBinding);
                    editor.putString(KEY_SWIPE_UP, swipeUpBinding); // New
                    editor.putString(KEY_SWIPE_DOWN, swipeDownBinding); // New
                    editor.putString(KEY_DOUBLE_TAP_TOP, doubleTapTopBinding);
                    editor.putString(KEY_DOUBLE_TAP_BOTTOM, doubleTapBottomBinding);
                    editor.putString(KEY_DOUBLE_TAP_LEFT, doubleTapLeftBinding); // New
                    editor.putString(KEY_DOUBLE_TAP_RIGHT, doubleTapRightBinding); // New
                    editor.putFloat(KEY_GESTURE_DISTANCE, gestureDistanceThreshold);
                    editor.putFloat(KEY_GESTURE_VELOCITY, gestureVelocityThreshold);
                    editor.putBoolean(KEY_FULLSCREEN, fullscreenEnabled);
                    if (newUrl != null && !newUrl.equals(currentUrl)) {
                        currentUrl = newUrl;
                        editor.putString(KEY_URL, currentUrl);
                    }
                    editor.apply();

                    Log.d(TAG, "Updated and persisted bindings: Volume Up=" + volumeUpBinding +
                            ", Volume Down=" + volumeDownBinding + ", Swipe Left=" + swipeLeftBinding +
                            ", Swipe Right=" + swipeRightBinding + ", Swipe Up=" + swipeUpBinding +
                            ", Swipe Down=" + swipeDownBinding + ", Double Tap Top=" + doubleTapTopBinding +
                            ", Double Tap Bottom=" + doubleTapBottomBinding + ", Double Tap Left=" + doubleTapLeftBinding +
                            ", Double Tap Right=" + doubleTapRightBinding + ", Distance=" + gestureDistanceThreshold +
                            ", Velocity=" + gestureVelocityThreshold + ", Fullscreen=" + fullscreenEnabled +
                            ", URL=" + currentUrl);

                    applyFullscreenMode();

                    if (newUrl != null && !newUrl.equals(currentUrl)) {
                        shouldClearHistory = true;
                        webView.loadUrl(currentUrl);
                        Log.d(TAG, "URL changed, will clear history after load: " + currentUrl);
                    }
                }
                if (currentUrl == null || currentUrl.isEmpty()) {
                    showConfigPrompt();
                }
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        volumeUpBinding = prefs.getString(KEY_VOLUME_UP, "Page Up");
        volumeDownBinding = prefs.getString(KEY_VOLUME_DOWN, "Page Down");
        swipeLeftBinding = prefs.getString(KEY_SWIPE_LEFT, "Right");
        swipeRightBinding = prefs.getString(KEY_SWIPE_RIGHT, "Left");
        swipeUpBinding = prefs.getString(KEY_SWIPE_UP, "None"); // New, default None
        swipeDownBinding = prefs.getString(KEY_SWIPE_DOWN, "None"); // New, default None
        doubleTapTopBinding = prefs.getString(KEY_DOUBLE_TAP_TOP, "None");
        doubleTapBottomBinding = prefs.getString(KEY_DOUBLE_TAP_BOTTOM, "None");
        doubleTapLeftBinding = prefs.getString(KEY_DOUBLE_TAP_LEFT, "None"); // New, default None
        doubleTapRightBinding = prefs.getString(KEY_DOUBLE_TAP_RIGHT, "None"); // New, default None
        gestureDistanceThreshold = prefs.getFloat(KEY_GESTURE_DISTANCE, 100f);
        gestureVelocityThreshold = prefs.getFloat(KEY_GESTURE_VELOCITY, 100f);
        fullscreenEnabled = prefs.getBoolean(KEY_FULLSCREEN, true);
        currentUrl = prefs.getString(KEY_URL, null);

        webView = findViewById(R.id.webView);

        applyFullscreenMode();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                float absDiffX = Math.abs(diffX);
                float absDiffY = Math.abs(diffY);

                // Horizontal swipe detection
                if (absDiffX > absDiffY && absDiffX > gestureDistanceThreshold && Math.abs(velocityX) > gestureVelocityThreshold) {
                    if (diffX > 0) {
                        int keyCode = getKeyCodeFromBinding(swipeRightBinding);
                        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                            Log.d(TAG, "Swipe right detected: Simulated " + swipeRightBinding);
                        }
                    } else {
                        int keyCode = getKeyCodeFromBinding(swipeLeftBinding);
                        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                            Log.d(TAG, "Swipe left detected: Simulated " + swipeLeftBinding);
                        }
                    }
                    return true;
                }
                // Vertical swipe detection
                else if (absDiffY > absDiffX && absDiffY > gestureDistanceThreshold && Math.abs(velocityY) > gestureVelocityThreshold) {
                    if (diffY > 0) {
                        int keyCode = getKeyCodeFromBinding(swipeDownBinding);
                        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                            Log.d(TAG, "Swipe down detected: Simulated " + swipeDownBinding);
                        }
                    } else {
                        int keyCode = getKeyCodeFromBinding(swipeUpBinding);
                        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                            Log.d(TAG, "Swipe up detected: Simulated " + swipeUpBinding);
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                webView.performClick();
                Log.d(TAG, "Single tap detected, calling performClick for accessibility");
                return false;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                int viewWidth = webView.getWidth();
                int viewHeight = webView.getHeight();

                // Define regions: top/bottom 1/3, left/right 1/3
                boolean isTopY = y <= viewHeight * 0.33f;
                boolean isBottomY = y >= viewHeight * 0.66f;
                boolean isLeftX = x <= viewWidth * 0.33f;
                boolean isRightX = x >= viewWidth * 0.66f;
                boolean isMiddleX = x >= viewWidth * 0.33f && x <= viewWidth * 0.66f;

                if (isMiddleX && isTopY) {
                    int keyCode = getKeyCodeFromBinding(doubleTapTopBinding);
                    if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                        Log.d(TAG, "Double tap at top-middle (12 o'clock): Simulated " + doubleTapTopBinding);
                    } else {
                        Log.d(TAG, "Double tap at top-middle: No action (bound to None)");
                    }
                    return true;
                } else if (isMiddleX && isBottomY) {
                    int keyCode = getKeyCodeFromBinding(doubleTapBottomBinding);
                    if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                        Log.d(TAG, "Double tap at bottom-middle (6 o'clock): Simulated " + doubleTapBottomBinding);
                    } else {
                        Log.d(TAG, "Double tap at bottom-middle: No action (bound to None)");
                    }
                    return true;
                } else if (isLeftX && !isTopY && !isBottomY) {
                    int keyCode = getKeyCodeFromBinding(doubleTapLeftBinding);
                    if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                        Log.d(TAG, "Double tap at left-middle (9 o'clock): Simulated " + doubleTapLeftBinding);
                    } else {
                        Log.d(TAG, "Double tap at left-middle: No action (bound to None)");
                    }
                    return true;
                } else if (isRightX && !isTopY && !isBottomY) {
                    int keyCode = getKeyCodeFromBinding(doubleTapRightBinding);
                    if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                        Log.d(TAG, "Double tap at right-middle (3 o'clock): Simulated " + doubleTapRightBinding);
                    } else {
                        Log.d(TAG, "Double tap at right-middle: No action (bound to None)");
                    }
                    return true;
                }
                Log.d(TAG, "Double tap detected outside target regions (x=" + x + ", y=" + y + ")");
                return false; // Pass to WebView if not in target regions
            }
        });

        WebView.setWebContentsDebuggingEnabled(true);

        WebSettings webSettings = getWebSettings();
        Log.d(TAG, "WebView settings: JS=" + webSettings.getJavaScriptEnabled() +
                ", DOM=" + webSettings.getDomStorageEnabled() + ", UA=" + webSettings.getUserAgentString());

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        Log.d(TAG, "CookieManager configured");

        webView.clearCache(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                Log.d(TAG, "Page started: " + url);
                view.evaluateJavascript(
                        "if (!('fetch' in window)) { " +
                                "  var script = document.createElement('script'); " +
                                "  script.src = 'https://polyfill.io/v3/polyfill.min.js?features=es6'; " +
                                "  document.head.appendChild(script); " +
                                "}", null);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page finished: " + url);
                if (shouldClearHistory) {
                    view.clearHistory();
                    shouldClearHistory = false;
                    Log.d(TAG, "History cleared after loading new URL: " + url);
                }
                view.evaluateJavascript(
                        "console.log('Angular app loaded: ' + (document.querySelector('app-root') ? document.querySelector('app-root').innerHTML.length > 0 : 'N/A'));" +
                                "console.log('Body content: ' + document.body.innerHTML.substring(0, 200));",
                        null
                );
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e(TAG, "Error: " + error.getDescription() + " (Code: " + error.getErrorCode() + ") URL: " + request.getUrl());
                view.loadData("<html><body><h1>Error: " + error.getDescription() + "</h1></body></html>",
                        "text/html", "UTF-8");
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                Log.e(TAG, "SSL Error: " + error.toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("SSL Security Warning")
                        .setMessage("The website's security certificate is not trusted.\n\n" +
                                "Error: " + getSslErrorMessage(error) + "\n\n" +
                                "Do you want to proceed anyway? This may pose a security risk.")
                        .setPositiveButton("Proceed", (dialog, which) -> {
                            Log.d(TAG, "User chose to proceed with SSL error");
                            handler.proceed();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Log.d(TAG, "User cancelled SSL error, blocking connection");
                            handler.cancel();
                            view.loadData("<html><body><h1>Connection Blocked: SSL Error</h1></body></html>",
                                    "text/html", "UTF-8");
                        })
                        .setCancelable(false)
                        .show();
            }

            private String getSslErrorMessage(android.net.http.SslError error) {
                switch (error.getPrimaryError()) {
                    case android.net.http.SslError.SSL_UNTRUSTED:
                        return "The certificate authority is not trusted.";
                    case android.net.http.SslError.SSL_EXPIRED:
                        return "The certificate has expired.";
                    case android.net.http.SslError.SSL_IDMISMATCH:
                        return "The certificate hostname mismatch.";
                    case android.net.http.SslError.SSL_NOTYETVALID:
                        return "The certificate is not yet valid.";
                    case android.net.http.SslError.SSL_DATE_INVALID:
                        return "The certificate date is invalid.";
                    case android.net.http.SslError.SSL_INVALID:
                        return "The certificate is invalid.";
                    default:
                        return "An unknown SSL error occurred.";
                }
            }
        });

        webView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });

        if (currentUrl == null || currentUrl.isEmpty()) {
            showConfigPrompt();
        } else {
            webView.loadUrl(currentUrl);
            Log.d(TAG, "Loading persisted URL: " + currentUrl);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    0,
                    () -> {
                        if (webView.canGoBack()) {
                            webView.goBack();
                            Log.d(TAG, "Navigated back (OnBackInvoked)");
                        } else {
                            showBackOptionsDialog();
                        }
                    }
            );
        } else {
            OnBackPressedCallback callback = new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        Log.d(TAG, "Navigated back (OnBackPressed)");
                    } else {
                        showBackOptionsDialog();
                    }
                }
            };
            getOnBackPressedDispatcher().addCallback(this, callback);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @NonNull
    private WebSettings getWebSettings() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 13; SM-A536B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        return webSettings;
    }

    private void applyFullscreenMode() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        if (fullscreenEnabled) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Log.d(TAG, "Fullscreen mode enabled");
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Log.d(TAG, "Fullscreen mode disabled");
        }
        decorView.requestLayout();
    }

    private void showConfigPrompt() {
        new AlertDialog.Builder(this)
                .setTitle("Configuration Required")
                .setMessage("Please enter your Kavita URL (required)")
                .setPositiveButton("Configure", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                    intent.putExtra(ConfigActivity.EXTRA_VOLUME_UP, volumeUpBinding);
                    intent.putExtra(ConfigActivity.EXTRA_VOLUME_DOWN, volumeDownBinding);
                    intent.putExtra(ConfigActivity.EXTRA_SWIPE_LEFT, swipeLeftBinding);
                    intent.putExtra(ConfigActivity.EXTRA_SWIPE_RIGHT, swipeRightBinding);
                    intent.putExtra(ConfigActivity.EXTRA_SWIPE_UP, swipeUpBinding); // New
                    intent.putExtra(ConfigActivity.EXTRA_SWIPE_DOWN, swipeDownBinding); // New
                    intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_TOP, doubleTapTopBinding);
                    intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_BOTTOM, doubleTapBottomBinding);
                    intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_LEFT, doubleTapLeftBinding); // New
                    intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_RIGHT, doubleTapRightBinding); // New
                    intent.putExtra(ConfigActivity.EXTRA_GESTURE_DISTANCE, gestureDistanceThreshold);
                    intent.putExtra(ConfigActivity.EXTRA_GESTURE_VELOCITY, gestureVelocityThreshold);
                    intent.putExtra(ConfigActivity.EXTRA_URL, currentUrl);
                    intent.putExtra(ConfigActivity.EXTRA_FULLSCREEN, fullscreenEnabled);
                    configLauncher.launch(intent);
                    Log.d(TAG, "Prompted user to configure URL");
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "User cancelled config prompt, exiting app");
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showBackOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_back_options, null);
        builder.setView(dialogView);

        MaterialButton buttonConfig = dialogView.findViewById(R.id.button_config);
        MaterialButton buttonExit = dialogView.findViewById(R.id.button_exit);
        MaterialButton buttonClose = dialogView.findViewById(R.id.button_close);

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        buttonConfig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
            intent.putExtra(ConfigActivity.EXTRA_VOLUME_UP, volumeUpBinding);
            intent.putExtra(ConfigActivity.EXTRA_VOLUME_DOWN, volumeDownBinding);
            intent.putExtra(ConfigActivity.EXTRA_SWIPE_LEFT, swipeLeftBinding);
            intent.putExtra(ConfigActivity.EXTRA_SWIPE_RIGHT, swipeRightBinding);
            intent.putExtra(ConfigActivity.EXTRA_SWIPE_UP, swipeUpBinding); // New
            intent.putExtra(ConfigActivity.EXTRA_SWIPE_DOWN, swipeDownBinding); // New
            intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_TOP, doubleTapTopBinding);
            intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_BOTTOM, doubleTapBottomBinding);
            intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_LEFT, doubleTapLeftBinding); // New
            intent.putExtra(ConfigActivity.EXTRA_DOUBLE_TAP_RIGHT, doubleTapRightBinding); // New
            intent.putExtra(ConfigActivity.EXTRA_GESTURE_DISTANCE, gestureDistanceThreshold);
            intent.putExtra(ConfigActivity.EXTRA_GESTURE_VELOCITY, gestureVelocityThreshold);
            intent.putExtra(ConfigActivity.EXTRA_URL, currentUrl);
            intent.putExtra(ConfigActivity.EXTRA_FULLSCREEN, fullscreenEnabled);
            configLauncher.launch(intent);
            dialog.dismiss();
            Log.d(TAG, "Opening ConfigActivity");
        });

        buttonExit.setOnClickListener(v -> {
            finish();
            Log.d(TAG, "Exiting app from dialog");
        });

        buttonClose.setOnClickListener(v -> {
            dialog.dismiss();
            Log.d(TAG, "Dialog closed");
        });

        dialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                int upKeyCode = getKeyCodeFromBinding(volumeUpBinding);
                if (upKeyCode != KeyEvent.KEYCODE_UNKNOWN) {
                    webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, upKeyCode));
                    webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, upKeyCode));
                    Log.d(TAG, "Volume Up pressed: Simulated " + volumeUpBinding);
                } else {
                    Log.d(TAG, "Volume Up pressed: No action (bound to None)");
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                int downKeyCode = getKeyCodeFromBinding(volumeDownBinding);
                if (downKeyCode != KeyEvent.KEYCODE_UNKNOWN) {
                    webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, downKeyCode));
                    webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, downKeyCode));
                    Log.d(TAG, "Volume Down pressed: Simulated " + volumeDownBinding);
                } else {
                    Log.d(TAG, "Volume Down pressed: No action (bound to None)");
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getKeyCodeFromBinding(String binding) {
        switch (binding) {
            case "Page Up":
                return KeyEvent.KEYCODE_PAGE_UP;
            case "Page Down":
                return KeyEvent.KEYCODE_PAGE_DOWN;
            case "Left":
                return KeyEvent.KEYCODE_DPAD_LEFT;
            case "Right":
                return KeyEvent.KEYCODE_DPAD_RIGHT;
            case "Up":
                return KeyEvent.KEYCODE_DPAD_UP;
            case "Down":
                return KeyEvent.KEYCODE_DPAD_DOWN;
            case "None":
                return KeyEvent.KEYCODE_UNKNOWN;
            default:
                Log.w(TAG, "Unknown binding: " + binding + ", defaulting to KEYCODE_UNKNOWN");
                return KeyEvent.KEYCODE_UNKNOWN;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        outState.putString("volumeUpBinding", volumeUpBinding);
        outState.putString("volumeDownBinding", volumeDownBinding);
        outState.putString("swipeLeftBinding", swipeLeftBinding);
        outState.putString("swipeRightBinding", swipeRightBinding);
        outState.putString("swipeUpBinding", swipeUpBinding); // New
        outState.putString("swipeDownBinding", swipeDownBinding); // New
        outState.putString("doubleTapTopBinding", doubleTapTopBinding);
        outState.putString("doubleTapBottomBinding", doubleTapBottomBinding);
        outState.putString("doubleTapLeftBinding", doubleTapLeftBinding); // New
        outState.putString("doubleTapRightBinding", doubleTapRightBinding); // New
        outState.putFloat("gestureDistanceThreshold", gestureDistanceThreshold);
        outState.putFloat("gestureVelocityThreshold", gestureVelocityThreshold);
        outState.putString("currentUrl", currentUrl);
        outState.putBoolean("shouldClearHistory", shouldClearHistory);
        outState.putBoolean("fullscreenEnabled", fullscreenEnabled);
        Log.d(TAG, "State saved for rotation");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
        volumeUpBinding = savedInstanceState.getString("volumeUpBinding", prefs.getString(KEY_VOLUME_UP, "Page Up"));
        volumeDownBinding = savedInstanceState.getString("volumeDownBinding", prefs.getString(KEY_VOLUME_DOWN, "Page Down"));
        swipeLeftBinding = savedInstanceState.getString("swipeLeftBinding", prefs.getString(KEY_SWIPE_LEFT, "Right"));
        swipeRightBinding = savedInstanceState.getString("swipeRightBinding", prefs.getString(KEY_SWIPE_RIGHT, "Left"));
        swipeUpBinding = savedInstanceState.getString("swipeUpBinding", prefs.getString(KEY_SWIPE_UP, "None")); // New
        swipeDownBinding = savedInstanceState.getString("swipeDownBinding", prefs.getString(KEY_SWIPE_DOWN, "None")); // New
        doubleTapTopBinding = savedInstanceState.getString("doubleTapTopBinding", prefs.getString(KEY_DOUBLE_TAP_TOP, "None"));
        doubleTapBottomBinding = savedInstanceState.getString("doubleTapBottomBinding", prefs.getString(KEY_DOUBLE_TAP_BOTTOM, "None"));
        doubleTapLeftBinding = savedInstanceState.getString("doubleTapLeftBinding", prefs.getString(KEY_DOUBLE_TAP_LEFT, "None")); // New
        doubleTapRightBinding = savedInstanceState.getString("doubleTapRightBinding", prefs.getString(KEY_DOUBLE_TAP_RIGHT, "None")); // New
        gestureDistanceThreshold = savedInstanceState.getFloat("gestureDistanceThreshold", prefs.getFloat(KEY_GESTURE_DISTANCE, 100f));
        gestureVelocityThreshold = savedInstanceState.getFloat("gestureVelocityThreshold", prefs.getFloat(KEY_GESTURE_VELOCITY, 100f));
        currentUrl = savedInstanceState.getString("currentUrl", prefs.getString(KEY_URL, null));
        shouldClearHistory = savedInstanceState.getBoolean("shouldClearHistory", false);
        fullscreenEnabled = savedInstanceState.getBoolean("fullscreenEnabled", prefs.getBoolean(KEY_FULLSCREEN, true));
        applyFullscreenMode();
        Log.d(TAG, "State restored for rotation: Volume Up=" + volumeUpBinding + ", Volume Down=" + volumeDownBinding +
                ", Swipe Left=" + swipeLeftBinding + ", Swipe Right=" + swipeRightBinding +
                ", Swipe Up=" + swipeUpBinding + ", Swipe Down=" + swipeDownBinding +
                ", Double Tap Top=" + doubleTapTopBinding + ", Double Tap Bottom=" + doubleTapBottomBinding +
                ", Double Tap Left=" + doubleTapLeftBinding + ", Double Tap Right=" + doubleTapRightBinding +
                ", Distance=" + gestureDistanceThreshold + ", Velocity=" + gestureVelocityThreshold +
                ", Fullscreen=" + fullscreenEnabled + ", URL=" + currentUrl);

        if (currentUrl == null || currentUrl.isEmpty()) {
            showConfigPrompt();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFullscreenMode();
        if (currentUrl == null || currentUrl.isEmpty()) {
            showConfigPrompt();
        }
    }
}