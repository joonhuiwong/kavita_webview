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
    private AlertDialog configDialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(MainConstants.PREFS_NAME, MODE_PRIVATE);
        webView = findViewById(R.id.webView);
        helper = new MainHelper(this, webView, prefs);

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

        if (helper.getCurrentUrl() == null || helper.getCurrentUrl().isEmpty()) {
            showConfigPrompt();
        } else if (savedInstanceState == null) {
            webView.loadUrl(helper.getCurrentUrl());
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
                .setMessage("Please enter your Kavita URL")
                .setPositiveButton("Configure", (dialog, which) -> {
                    configLauncher.launch(helper.createConfigIntent());
                    if (configDialog != null) {
                        configDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_back_options, null);
        builder.setView(dialogView);

        MaterialButton buttonConfig = dialogView.findViewById(R.id.button_config);
        MaterialButton buttonExit = dialogView.findViewById(R.id.button_exit);
        MaterialButton buttonClose = dialogView.findViewById(R.id.button_close);

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        buttonConfig.setOnClickListener(v -> {
            configLauncher.launch(helper.createConfigIntent());
            dialog.dismiss();
        });
        buttonExit.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        buttonClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void handleBack() {
        if (helper.canGoBack()) helper.goBack();
        else showBackOptionsDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (helper.onKeyDown(keyCode)) return true;
        return super.onKeyDown(keyCode, event);
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
        outState.putBoolean(MainConstants.PREF_KEYS[13], helper.fullscreenEnabled);
        outState.putBoolean("shouldClearHistory", helper.shouldClearHistory);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
        helper.loadPreferences();
        helper.currentUrl = savedInstanceState.getString(MainConstants.PREF_KEYS[0]);
        helper.shouldClearHistory = savedInstanceState.getBoolean("shouldClearHistory");
        helper.fullscreenEnabled = savedInstanceState.getBoolean(MainConstants.PREF_KEYS[13]);
        helper.applyFullscreenMode(getWindow());
        if (helper.getCurrentUrl() == null || helper.getCurrentUrl().isEmpty()) {
            showConfigPrompt();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.applyFullscreenMode(getWindow());
        if (helper.getCurrentUrl() == null || helper.getCurrentUrl().isEmpty()) {
            showConfigPrompt();
        }
    }
}