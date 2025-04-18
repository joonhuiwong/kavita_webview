package com.joonhuiwong.kavitawebview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class MainActivity extends ComponentActivity {

    private WebView webView;
    private MainHelper helper;
    private ActivityResultLauncher<Intent> configLauncher;
    private AlertDialog configDialog, backOptionsDialog;
    private boolean isBackOptionsShown = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(MainConstants.PREFS_NAME, MODE_PRIVATE);
        webView = findViewById(R.id.webView);
        helper = new MainHelper(this, webView, prefs, this::showBackOptionsDialog);

        configLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        helper.handleConfigResult(result.getData());
                        helper.applyFullscreenMode(getWindow());
                    }
                });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.clearCache(true);

        webView.setWebViewClient(new MainWebViewClient(this, helper));
        helper.setupWebView();
        helper.applyFullscreenMode(getWindow());

        if (helper.getCurrentUrl() != null && !helper.getCurrentUrl().isEmpty()) {
            webView.loadUrl(helper.getCurrentUrl());
        } else {
            showConfigPrompt();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, this::handleBack);
        } else {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    handleBack();
                }
            });
        }
    }

    private void showConfigPrompt() {
        if (configDialog != null && configDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configuration Required")
                .setPositiveButton("Configure", (dialog, which) -> {
                    configLauncher.launch(helper.createConfigIntent());
                    if (configDialog != null) {
                        configDialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    if (configDialog != null) {
                        configDialog.dismiss();
                    }
                    finish();
                })
                .setCancelable(false);

        configDialog = builder.create();
        configDialog.show();
    }

    private void showBackOptionsDialog() {
        if (backOptionsDialog != null && backOptionsDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_back_options, null);
        builder.setView(dialogView);

        MaterialButton buttonConfig = dialogView.findViewById(R.id.button_config);
        MaterialButton buttonExit = dialogView.findViewById(R.id.button_exit);
        MaterialButton buttonClose = dialogView.findViewById(R.id.button_close);

        backOptionsDialog = builder.create();
        Objects.requireNonNull(backOptionsDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        buttonConfig.setOnClickListener(v -> {
            if (backOptionsDialog != null) {
                backOptionsDialog.dismiss();
            }
            isBackOptionsShown = false;
            configLauncher.launch(helper.createConfigIntent());
        });
        buttonExit.setOnClickListener(v -> {
            if (backOptionsDialog != null) {
                backOptionsDialog.dismiss();
            }
            isBackOptionsShown = false;
            finish();
        });
        buttonClose.setOnClickListener(v -> {
            if (backOptionsDialog != null) {
                backOptionsDialog.dismiss();
            }
            isBackOptionsShown = false;
        });

        backOptionsDialog.show();
        isBackOptionsShown = true;
    }

    private void handleBack() {
        if (helper.canGoBack()) {
            helper.goBack();
        } else {
            showBackOptionsDialog();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return helper.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        outState.putString(MainConstants.PREF_KEYS[1], helper.volumeUpBinding);
        outState.putString(MainConstants.PREF_KEYS[2], helper.volumeDownBinding);
        outState.putString(MainConstants.PREF_KEYS[3], helper.swipeLeftBinding);
        outState.putString(MainConstants.PREF_KEYS[4], helper.swipeRightBinding);
        outState.putString(MainConstants.PREF_KEYS[5], helper.swipeUpBinding);
        outState.putString(MainConstants.PREF_KEYS[6], helper.swipeDownBinding);
        outState.putString(MainConstants.PREF_KEYS[7], helper.doubleTapTopBinding);
        outState.putString(MainConstants.PREF_KEYS[8], helper.doubleTapBottomBinding);
        outState.putString(MainConstants.PREF_KEYS[9], helper.doubleTapLeftBinding);
        outState.putString(MainConstants.PREF_KEYS[10], helper.doubleTapRightBinding);
        outState.putFloat(MainConstants.PREF_KEYS[11], helper.gestureDistanceThreshold);
        outState.putFloat(MainConstants.PREF_KEYS[12], helper.gestureVelocityThreshold);
        outState.putString(MainConstants.PREF_KEYS[0], helper.getCurrentUrl());
        outState.putBoolean(MainConstants.PREF_KEYS[13], helper.hideStatusBar);
        outState.putBoolean(MainConstants.PREF_KEYS[14], helper.hideNavigationBar);
        outState.putBoolean(MainConstants.PREF_KEYS[15], helper.disableTextSelection);
        outState.putBoolean("shouldClearHistory", helper.shouldClearHistory);
        outState.putBoolean("isBackOptionsShown", isBackOptionsShown);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        helper.loadPreferences();
        helper.currentUrl = savedInstanceState.getString(MainConstants.PREF_KEYS[0]);
        helper.shouldClearHistory = savedInstanceState.getBoolean("shouldClearHistory");
        helper.hideStatusBar = savedInstanceState.getBoolean(MainConstants.PREF_KEYS[13]);
        helper.hideNavigationBar = savedInstanceState.getBoolean(MainConstants.PREF_KEYS[14]);
        helper.disableTextSelection = savedInstanceState.getBoolean(MainConstants.PREF_KEYS[15]);
        isBackOptionsShown = savedInstanceState.getBoolean("isBackOptionsShown", false);
        helper.applyFullscreenMode(getWindow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.applyFullscreenMode(getWindow());
        if (helper.getCurrentUrl() == null || helper.getCurrentUrl().isEmpty()) {
            showConfigPrompt();
        } else if (isBackOptionsShown && (backOptionsDialog == null || !backOptionsDialog.isShowing())) {
            showBackOptionsDialog();
        }
    }
}