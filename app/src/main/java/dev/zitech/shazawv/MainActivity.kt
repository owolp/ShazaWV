package dev.zitech.shazawv

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.processphoenix.ProcessPhoenix

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val FAKE_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36"
        private const val SHAZAM_URL = "https://shazam.com/"
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = findViewById(R.id.webView)

        deleteHistoryData()
        setWebViewClient()
        setWebChromeClient()
        setWebSettings()

        webView.loadUrl(SHAZAM_URL)
    }

    override fun onDestroy() {
        deleteHistoryData()
        webView.destroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.application_restart, Toast.LENGTH_SHORT)
                        .show()
                    ProcessPhoenix.triggerRebirth(this)
                } else {
                    Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun deleteHistoryData() {
        with(webView) {
            clearCache(true)
            clearFormData()
            clearHistory()
            clearMatches()
            clearSslPreferences()
        }
        with(CookieManager.getInstance()) {
            removeSessionCookies {}
            removeAllCookies {}
            flush()
        }
        WebStorage.getInstance().deleteAllData()
    }

    private fun setWebViewClient() {
        webView.webViewClient = object : WebViewClient() {}
    }

    private fun setWebChromeClient() {
        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.resources?.forEach { requestedResource ->
                        if (requestedResource == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                            request.grant(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE))
                        }
                    }

                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebSettings() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = FAKE_USER_AGENT
        }
    }
}
