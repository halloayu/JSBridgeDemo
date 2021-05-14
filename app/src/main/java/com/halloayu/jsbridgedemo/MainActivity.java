package com.halloayu.jsbridgedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.halloayu.jsbridgedemo.webview.HandleMessage;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private String url  = "file:///android_asset/jsbridgetest.html";
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        webView.loadUrl(url);
        new HandleMessage(webView, this);
    }
}