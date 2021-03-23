package com.alibaba.gov.android.chinaoly.meeting.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.alibaba.gov.android.chinaoly.meeting.R;


public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView webView =findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true); // 是否开启JS支持
        webSettings.setUseWideViewPort(true); // 缩放至屏幕大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕大小
        webSettings.setDefaultTextEncodingName("UTF-8"); // 设置编码格式
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.loadUrl("http://10.10.14.72");


    }

    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     *   <script type="text/javascript">
     *         function showAndroidToast(toast) {
     *             Android.showToast(toast);
     *         }
     *     </script>
     *
     */


}