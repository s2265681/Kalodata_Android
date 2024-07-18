

package com.kalodata.kalodata_android;
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import android.os.Handler
import android.content.Intent

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // 设置延时跳转到主Activity，这里设置为3秒
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}