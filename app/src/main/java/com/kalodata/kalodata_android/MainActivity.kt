package com.kalodata.kalodata;
import java.util.Base64
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.gson.Gson

// 定义一个回调接口
interface MainActionCallback {
    fun toGooglePay(productId: String, userId: String)
}

fun base64Encode(input: String): String {
    val encodedBytes = Base64.getEncoder().encode(input.toByteArray(Charsets.UTF_8))
    return String(encodedBytes, Charsets.UTF_8)
}

fun purchaseInfoToJson(purchase: Purchase): String {
    val gson = Gson()
    val purchase = mapOf(
        "data" to purchase.originalJson,
        "signature" to purchase.signature
    )
    return base64Encode(gson.toJson(purchase))
}

class MainActivity : ComponentActivity(),MainActionCallback {
    private lateinit var webView: WebView
    private lateinit var billingClient: BillingClient;
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases!= null) {
            Log.d("支付成功", "Payment successful: " + billingResult.responseCode);
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("支付取消", "User canceled the payment: " + billingResult.responseCode);
            //  用户取消提示
            //  Toast.makeText(this, "User canceled the payment: ${billingResult.responseCode}", Toast.LENGTH_LONG).show()

        } else {
            Log.d("支付失败", "Payment failed: " + billingResult.responseCode);
            // 根据具体的响应码处理不同的错误情况
            Toast.makeText(this, "Payment failed: ${billingResult.responseCode}", Toast.LENGTH_LONG).show()
        }
    }
    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // 将 Purchase 对象转换为 PurchaseInfo
//                val purchaseInfo = purchaseToPurchaseInfo(parsePurchaseJson(purchase.originalJson), purchase.signature)
                // 将 PurchaseInfo 转换为 JSON 字符串
                val json = purchaseInfoToJson(purchase)
                // 处理消耗成功的情况
                // 1. 验证购买
                // 你可以在这里添加服务器端的购买验证，确保购买有效
                // 2. 解锁应用内的功能或内容
                // 例如，如果购买的是游戏内货币，你可以在这里增加用户的余额
                // 假设有一个函数updateUserBalance用来更新用户的余额
                // updateUserBalance(purchase.sku, purchase.quantity)
                // 3. 记录事务
                // 记录用户购买事务到你的应用数据库或远程服务器
                // logPurchaseTransaction(purchase)
                // 4. 通知用户
                // 显示一个Toast消息或更新UI来告知用户他们的购买已成功
//                Toast.makeText(this, "购买成功", Toast.LENGTH_LONG).show()
                callJsFromAndroid("channelMessage","buysuccess",json)
                // 5. 发放物品或权限
                // 根据购买的项目，发放相应的物品或权限给用户
                // grantItemOrPermissionToUser(purchase)
            }else{
                callJsFromAndroid("channelMessage","buyfail", "Purchase consumption failed, please try again")
                // 消耗失败，可以处理错误或重试
                // 例如，你可以提示用户消耗失败，并尝试再次消耗
//                Toast.makeText(this, "Purchase consumption failed, please try again", Toast.LENGTH_LONG).show()
            }
        }
        if (billingClient!= null) {
            billingClient.consumeAsync(consumeParams, listener)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // webview
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.settings?.javaScriptEnabled = true   // 支持 JavaScript
        // 设置是否启用 DOM 存储
        // DOM 存储是一种在 Web 应用程序中存储数据的机制，它使用 JavaScript 对象和属性来存储和检索数据
        webView.settings?.domStorageEnabled = true
        // 设置 WebView 是否可以获取焦点 ( 自选 非必要 )
        webView?.isFocusable = true
        // 设置 WebView 中的滚动条样式 ( 自选 非必要 )
        // SCROLLBARS_INSIDE_OVERLAY - 在内容上覆盖滚动条 ( 默认 )
        webView?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        // 设置页面自适应
        // Viewport 元标记是指在 HTML 页面中的 <meta> 标签 , 可以设置网页在移动端设备上的显示方式和缩放比例
        // 设置是否支持 Viewport 元标记的宽度
        webView.settings?.useWideViewPort = true

        // WebViewClient 是一个用于处理 WebView 页面加载事件的类
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // 页面加载完成后可以在这里执行一些操作，例如调用JavaScript函数
                // webView.evaluateJavascript("javascript:testDat2('buyFail');") { value ->
                //    Toast.makeText(this@MainActivity, "JavaScript returned" + value, Toast.LENGTH_SHORT).show()
                // }
            }
        }

        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.loadUrl("https://develop.m.kalodata.com/")
//         webView.loadUrl("http://192.168.31.131:5173")
        // 初始化账单信息
        initializeBillingClient()
    }

    /**
     * h5 内发送给 native 消息，在此返回 h5 时需要判断是否在主线程
     */
    fun callJsFromAndroid(funName: String, eventType: String, message:String?="") {
        // 确保在主线程中调用evaluateJavascript
        if (Looper.myLooper() == Looper.getMainLooper()) {
            handleEvaluateJs(funName, eventType,  message)
        } else {
            // 如果不在主线程，使用Handler切换到主线程
            Handler(Looper.getMainLooper()).post {
                handleEvaluateJs(funName, eventType, message)
            }
        }
    }

    fun handleEvaluateJs (funName: String, eventType: String, message:String?){
        // 隐藏原生的提示
        //  if(eventType =="buyfail"){
        //     Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        //  }
        webView.evaluateJavascript("javascript:$funName('$eventType', '$message')") { value ->
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()

        } else {
            super.onBackPressed()
        }
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 支付客户端初始化成功
                    Toast.makeText( this@MainActivity, "The payment client was initialized successfully.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onBillingServiceDisconnected() {
                // 处理支付服务断开的情况
                callJsFromAndroid("channelMessage","buyfail", "Handle payment service disconnection")
            }
    })
    }

    override fun toGooglePay(productId:String, userId:String) {
        // 调用H5中的一个方法
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        if (billingClient!= null) {
            billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                object : ProductDetailsResponseListener {
                    override fun onProductDetailsResponse(billingResult: BillingResult, productDetailsList: List<ProductDetails>) {
                        if (productDetailsList.isNotEmpty()) {
                            val productDetails = productDetailsList[0]
                            val productDetailsParamsList = listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                            )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .setObfuscatedAccountId(userId)
                                .build()

                            billingClient.launchBillingFlow(this@MainActivity, billingFlowParams)
                        } else {
                            callJsFromAndroid("channelMessage","buyfail", "Invalid product ID")
                        }
                    }
                }
            )
        }
    }


    // WebAppInterface
    inner class WebAppInterface(private val activity:ComponentActivity) {
        /**
         * js 调用弹窗提示
         */
        @JavascriptInterface
        fun showToast(toast: String) {
            Toast.makeText(this@MainActivity, toast, Toast.LENGTH_SHORT).show()
        }

        /**
         * js 调用去购买
         */
        @JavascriptInterface
        fun handleBuy(productAndUserId:String) {
            val parts = productAndUserId.split('&')
            toGooglePay(parts[0], parts[1])
        }
    }
}
