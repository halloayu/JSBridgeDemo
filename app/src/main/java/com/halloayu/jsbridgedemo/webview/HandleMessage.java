package com.halloayu.jsbridgedemo.webview;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONObject;

public class HandleMessage {

    private final String TAG = "HandleMessage";

    WebView webView;
    Context context;

    public HandleMessage(WebView webView, Context context) {
        this.webView = webView;
        this.context = context;
        webView.addJavascriptInterface(this, "androidPlatform"); // 注册命名空间
    }

    /**
     * 接收来自H5应用的请求
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void postString(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            Toast.makeText(context, jsonObject.getString("data"), Toast.LENGTH_LONG).show();
            send(jsonObject.getString("callbackId"));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void send(String callbackId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("callbackId", callbackId);
            jsonObject.put("data", "我是原生响应的返回数据");
            final String script = "javascript:window.JSBridge.postMsg("+jsonObject.toString()+")";
            webView.post(() -> webView.evaluateJavascript(script, null));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
