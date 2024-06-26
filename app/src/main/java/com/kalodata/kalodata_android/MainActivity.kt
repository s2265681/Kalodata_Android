package com.kalodata.app
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.kalodata.kalodata_android.R

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true // 启用JavaScript
        webView.settings.domStorageEnabled = true // 启用 DOM 存储 API (这对于一些以 AJAX 技术构建的网页很重要)

        // 设置是否允许 WebView 使用 File 协议
        webView.settings.allowFileAccess = true

        // 设置 WebView 的 UserAgent 字符串
        webView.settings.userAgentString = "MyCustomUserAgent"

        // 设置是否自动加载图片
        webView.settings.loadsImagesAutomatically = true

        // 设置编码格式
        webView.settings.defaultTextEncodingName = "utf-8"
        // 加载网页
        webView.loadUrl("https://www.baidu.com")
    }
}