package com.kalodata.kalodata_android;

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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


class BillingActivity : ComponentActivity() {
    private lateinit var billingClient: BillingClient
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases!= null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // 处理用户取消支付的情况
        } else {
            // 处理其他支付错误情况
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // 处理消耗成功的情况
            }
        }
        if (billingClient!= null) {
            billingClient.consumeAsync(consumeParams, listener)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_billing)

//         初始化 Google 支付客户端
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 支付客户端初始化成功
                    Log.d("success","支付客户端初始化成功");
                    // 从Intent中获取传递的商品ID
                    val intent = intent
                    if (intent != null && intent.hasExtra("PRODUCT_ID")) {
                        val productId = intent.getStringExtra("PRODUCT_ID") as String
                        toGooglePay(productId) // 调用支付方法并传递商品ID
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // 处理支付服务断开的情况
                Log.d("error","处理支付服务断开的情况");
            }
        })

        //添加按钮并设置点击事件
//        val button = Button(this)
//        button.text = "进行 Google 支付"
//        button.setOnClickListener {
//            toGooglePay("test1")
//        }
//        setContentView(button)
        // 从 Intent 中获取传递过来的商品 ID

    }

    private fun toGooglePay(productId:String) {
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
                                .build()
                            billingClient.launchBillingFlow(this@BillingActivity, billingFlowParams)
                        } else {
                            Toast.makeText(this@BillingActivity, "商品 ID 无效", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    companion object {
        // 假设这个方法被MainActivity调用以启动支付流程
        public fun startBillingFlow(context: Context, productId: String?) {
            val intent = Intent(context, BillingActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            context.startActivity(intent)
        }

        public fun init(context: Context, productId: String?) {
            val intent = Intent(context, BillingActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            context.startActivity(intent)
        }
    }
}