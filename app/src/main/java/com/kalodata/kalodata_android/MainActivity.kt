//package com.kalodata.kalodata_android;
//import android.os.Bundle
//import android.webkit.WebView
//import androidx.activity.ComponentActivity
//
//class MainActivity : ComponentActivity() {
//    private lateinit var webView: WebView
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        webView = findViewById(R.id.webview)
//        webView.settings.javaScriptEnabled = true // 启用JavaScript
//        webView.settings.domStorageEnabled = true // 启用 DOM 存储 API (这对于一些以 AJAX 技术构建的网页很重要)
//        // 加载网页
//        webView.loadUrl("https://m.kalodata.com")
//    }
//}

package com.kalodata.kalodata_android;
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 隐藏状态栏和导航栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 设置窗口全屏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )



        webView = findViewById<WebView>(R.id.webview)

        // 获取并设置 Web 设置
        val settings = webView?.settings

        settings?.javaScriptEnabled = true   // 支持 JavaScript
        // 设置是否启用 DOM 存储
        // DOM 存储是一种在 Web 应用程序中存储数据的机制，它使用 JavaScript 对象和属性来存储和检索数据
        settings?.domStorageEnabled = true
        // 设置 WebView 是否启用内置缩放控件 ( 自选 非必要 )
        //settings.builtInZoomControls = true

        // 5.0 以上需要设置允许 http 和 https 混合加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        } else {
            // 5.0 以下不用考虑  http 和 https 混合加载 问题
            settings?.mixedContentMode = WebSettings.LOAD_NORMAL
        }

        // 设置页面自适应
        // Viewport 元标记是指在 HTML 页面中的 <meta> 标签 , 可以设置网页在移动端设备上的显示方式和缩放比例
        // 设置是否支持 Viewport 元标记的宽度
        settings?.useWideViewPort = true

        // 设置 WebView 是否使用宽视图端口模式
        // 宽视图端口模式下 , WebView 会将页面缩小到适应屏幕的宽度
        // 没有经过移动端适配的网页 , 不要启用该设置
        settings?.loadWithOverviewMode = true

        // 设置 WebView 是否可以获取焦点 ( 自选 非必要 )
        webView?.isFocusable = true
        // 设置 WebView 是否启用绘图缓存 位图缓存可加速绘图过程 ( 自选 非必要 )
        webView?.isDrawingCacheEnabled = true
        // 设置 WebView 中的滚动条样式 ( 自选 非必要 )
        // SCROLLBARS_INSIDE_OVERLAY - 在内容上覆盖滚动条 ( 默认 )
        webView?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        // WebViewClient 是一个用于处理 WebView 页面加载事件的类
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // 4.0 之后必须添加该设置
                // 只能加载 http:// 和 https:// 页面 , 不能加载其它协议链接
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url)
                    return true
                }
                return false
            }

            // SSL 证书校验出现异常
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                when (error.primaryError) {
                    SslError.SSL_INVALID, SslError.SSL_UNTRUSTED -> {
                        handler.proceed()
                    }
                    else -> handler.cancel()
                }
            }
        }

        // WebChromeClient 是一个用于处理 WebView 界面交互事件的类
        webView?.webChromeClient =  MyWebChromeClient()

        // 加载网页
        webView.loadUrl("https://m.kalodata.com")

        // js调用安卓方法支持（第二个参数是js代码中调用APP中的交互桥类定义的名，需保持一致）
        webView?.addJavascriptInterface(JoAppObject(),"joApp")

        // 原生调用js中的方法（不带参数版）
        // 这里joAppJs与H5 web端中定义的被原生调用JS类new的变量名一致，方便统一调用
        joAppJs("joAppJs.test")
        // 原生调用js中的方法（带参数版）
        joAppJs("joAppJs.testData","一只可爱的对号")
    }
}


class MyWebChromeClient: WebChromeClient(){


    // 显示 网页加载 进度条
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        Log.d("JoApp","${newProgress}")
        super.onProgressChanged(view, newProgress)

        if (newProgress == 100) {
            //加载100%
            Log.d(TAG, "onProgressChanged: " + "webView---100%");

            //执行加载完成调用js，如：传入token等
            onLoagJs()
//                if (!isWebViewloadError && View.VISIBLE == btnRetry.getVisibility()){
//                    btnRetry.setVisibility(View.GONE);//重新加载按钮
//                }
        }
    }
}