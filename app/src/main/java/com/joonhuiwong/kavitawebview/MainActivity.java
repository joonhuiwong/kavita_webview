package com.joonhuiwong.kavitawebview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends ComponentActivity {

    private WebView webView;
    private static final String TAG = "MainActivity";
    private String volumeUpBinding = "Page Up";
    private String volumeDownBinding = "Page Down";
    private String swipeLeftBinding = "Right";
    private String swipeRightBinding = "Left";
    private float gestureDistanceThreshold = 100f;
    private float gestureVelocityThreshold = 100f;
    private String currentUrl = null;
    private boolean shouldClearHistory = false;
    private boolean fullscreenEnabled = true; // Default to true
    private GestureDetectorCompat gestureDetector;

    private final ActivityResultLauncher<Intent> configLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String newVolumeUp = result.getData().getStringExtra(ConfigActivity.EXTRA_VOLUME_UP);
                    String newVolumeDown = result.getData().getStringExtra(ConfigActivity.EXTRA_VOLUME_DOWN);
                    String newSwipeLeft = result.getData().getStringExtra(ConfigActivity.EXTRA_SWIPE_LEFT);
                    String newSwipeRight = result.getData().getStringExtra(ConfigActivity.EXTRA_SWIPE_RIGHT);
                    float newGestureDistance = result.getData().getFloatExtra(ConfigActivity.EXTRA_GESTURE_DISTANCE, 100f);
                    float newGestureVelocity = result.getData().getFloatExtra(ConfigActivity.EXTRA_GESTURE_VELOCITY, 100f);
                    String newUrl = result.getData().getStringExtra(ConfigActivity.EXTRA_URL);
                    boolean newFullscreen = result.getData().getBooleanExtra(ConfigActivity.EXTRA_FULLSCREEN, true);

                    volumeUpBinding = newVolumeUp;
                    volumeDownBinding = newVolumeDown;
                    swipeLeftBinding = newSwipeLeft;
                    swipeRightBinding = newSwipeRight;
                    gestureDistanceThreshold = newGestureDistance;
                    gestureVelocityThreshold = newGestureVelocity;
                    fullscreenEnabled = newFullscreen;
                    Log.d(TAG, "Updated bindings: Volume Up=" + volumeUpBinding + ", Volume Down=" + volumeDownBinding +
                            ", Swipe Left=" + swipeLeftBinding + ", Swipe Right=" + swipeRightBinding +
                            ", Distance=" + gestureDistanceThreshold + ", Velocity=" + gestureVelocityThreshold +
                            ", Fullscreen=" + fullscreenEnabled);

                    applyFullscreenMode(); // Apply fullscreen setting immediately

                    if (newUrl != null && !newUrl.equals(currentUrl)) {
                        currentUrl = newUrl;
                        shouldClearHistory = true;
                        webView.loadUrl(currentUrl);
                        Log.d(TAG, "URL changed, will clear history after load: " + currentUrl);
                    }
                }
                if (currentUrl == null || currentUrl.isEmpty()) {
                    showConfigPrompt();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyFullscreenMode(); // Apply on startup

        webView = findViewById(R.id.webView);

        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) &&
                        Math.abs(diffX) > gestureDistanceThreshold &&
                        Math.abs(velocityX) > gestureVelocityThreshold) {
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
                return false;
            }
        });

        WebView.setWebContentsDebuggingEnabled(true);

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
                        "console.log('Angular app loaded: ' + (document.querySelector('app-root').innerHTML.length > 0));" +
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

            @Override
            public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
                Log.e(TAG, "SSL Error: " + error.toString());
                handler.proceed();
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
            Log.d(TAG, "Loading URL: " + currentUrl);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                    Log.d(TAG, "Navigated back");
                } else {
                    showBackOptionsDialog();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void applyFullscreenMode() {
        if (fullscreenEnabled) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            Log.d(TAG, "Fullscreen mode enabled");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0); // Reset to default
            Log.d(TAG, "Fullscreen mode disabled");
        }
    }

    private void showConfigPrompt() {
        new AlertDialog.Builder(this)
                .setMessage("Please enter your Kavita URL")
                .setPositiveButton("Configure", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                    intent.putExtra(ConfigActivity.EXTRA_VOLUME_UP, volumeUpBinding);
                    intent.putExtra(ConfigActivity.EXTRA_VOLUME_DOWN, volumeDownBinding);
                    intent.putExtra(ConfigActivity.EXTRA_SWIPE_LEFT, swipeLeftBinding);
                    intent.putExtra(ConfigActivity.EXTRA_SWIPE_RIGHT, swipeRightBinding);
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
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        buttonConfig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
            intent.putExtra(ConfigActivity.EXTRA_VOLUME_UP, volumeUpBinding);
            intent.putExtra(ConfigActivity.EXTRA_VOLUME_DOWN, volumeDownBinding);
            intent.putExtra(ConfigActivity.EXTRA_SWIPE_LEFT, swipeLeftBinding);
            intent.putExtra(ConfigActivity.EXTRA_SWIPE_RIGHT, swipeRightBinding);
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
            case "None":
                return KeyEvent.KEYCODE_UNKNOWN;
            case "Page Up":
                return KeyEvent.KEYCODE_PAGE_UP;
            case "Page Down":
                return KeyEvent.KEYCODE_PAGE_DOWN;
            case "Left":
                return KeyEvent.KEYCODE_DPAD_LEFT;
            case "Right":
                return KeyEvent.KEYCODE_DPAD_RIGHT;
            default:
                return KeyEvent.KEYCODE_UNKNOWN;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        outState.putString("volumeUpBinding", volumeUpBinding);
        outState.putString("volumeDownBinding", volumeDownBinding);
        outState.putString("swipeLeftBinding", swipeLeftBinding);
        outState.putString("swipeRightBinding", swipeRightBinding);
        outState.putFloat("gestureDistanceThreshold", gestureDistanceThreshold);
        outState.putFloat("gestureVelocityThreshold", gestureVelocityThreshold);
        outState.putString("currentUrl", currentUrl);
        outState.putBoolean("shouldClearHistory", shouldClearHistory);
        outState.putBoolean("fullscreenEnabled", fullscreenEnabled);
        Log.d(TAG, "State saved");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
        volumeUpBinding = savedInstanceState.getString("volumeUpBinding", "Page Up");
        volumeDownBinding = savedInstanceState.getString("volumeDownBinding", "Page Down");
        swipeLeftBinding = savedInstanceState.getString("swipeLeftBinding", "Right");
        swipeRightBinding = savedInstanceState.getString("swipeRightBinding", "Left");
        gestureDistanceThreshold = savedInstanceState.getFloat("gestureDistanceThreshold", 100f);
        gestureVelocityThreshold = savedInstanceState.getFloat("gestureVelocityThreshold", 100f);
        currentUrl = savedInstanceState.getString("currentUrl");
        shouldClearHistory = savedInstanceState.getBoolean("shouldClearHistory", false);
        fullscreenEnabled = savedInstanceState.getBoolean("fullscreenEnabled", true);
        applyFullscreenMode(); // Apply restored state
        Log.d(TAG, "State restored: Volume Up=" + volumeUpBinding + ", Volume Down=" + volumeDownBinding +
                ", Swipe Left=" + swipeLeftBinding + ", Swipe Right=" + swipeRightBinding +
                ", Distance=" + gestureDistanceThreshold + ", Velocity=" + gestureVelocityThreshold +
                ", Fullscreen=" + fullscreenEnabled + ", URL=" + currentUrl);
    }
}