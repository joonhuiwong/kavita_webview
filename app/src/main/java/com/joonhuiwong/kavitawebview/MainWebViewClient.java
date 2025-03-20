package com.joonhuiwong.kavitawebview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainWebViewClient extends WebViewClient {

    private final Context context;
    private final MainHelper helper;

    public MainWebViewClient(Context context, MainHelper helper) {
        this.context = context;
        this.helper = helper;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (helper.getShouldClearHistory()) {
            view.clearHistory();
            helper.setShouldClearHistory(false);
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        view.loadData("<html><body><h1>Error: " + error.getDescription() + "</h1></body></html>",
                "text/html", "UTF-8");
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
        new AlertDialog.Builder(context)
                .setTitle("SSL Security Warning")
                .setMessage("Certificate error: " + getSslErrorMessage(error) + "\nProceed?")
                .setPositiveButton("Yes", (dialog, which) -> handler.proceed())
                .setNegativeButton("No", (dialog, which) -> {
                    handler.cancel();
                    view.loadData("<html><body><h1>SSL Error</h1></body></html>", "text/html", "UTF-8");
                })
                .setCancelable(false)
                .show();
    }

    private String getSslErrorMessage(android.net.http.SslError error) {
        switch (error.getPrimaryError()) {
            case android.net.http.SslError.SSL_UNTRUSTED: return "Untrusted certificate";
            case android.net.http.SslError.SSL_EXPIRED: return "Expired certificate";
            case android.net.http.SslError.SSL_IDMISMATCH: return "Hostname mismatch";
            case android.net.http.SslError.SSL_NOTYETVALID: return "Not yet valid";
            case android.net.http.SslError.SSL_DATE_INVALID: return "Invalid date";
            case android.net.http.SslError.SSL_INVALID: return "Invalid certificate";
            default: return "Unknown SSL error";
        }
    }
}