package com.kalodata.kalodata_android;
import android.content.Context
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true // 启用JavaScript
        webView.settings.domStorageEnabled = true // 启用 DOM 存储 API (这对于一些以 AJAX 技术构建的网页很重要)

        // 添加JavaScript接口
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        // 加载网页
        webView.loadUrl("https://staging.m.kalodata.com")
//        webView.loadUrl("file:///android_asset/jsBridge.html")
    }

    class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()

        }
    }
}
