package com.kalodata.kalodata_android;
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true // 启用JavaScript
        webView.settings.domStorageEnabled = true // 启用 DOM 存储 API (这对于一些以 AJAX 技术构建的网页很重要)
        // 加载网页
        webView.loadUrl("https://m.kalodata.com")
    }
}