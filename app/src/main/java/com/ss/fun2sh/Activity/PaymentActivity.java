package com.ss.fun2sh.Activity;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ss.fun2sh.R;

public class PaymentActivity extends AppCompatActivity {

    ProgressBar progressBar;
    LinearLayout progress_layout;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

//        String url = getArguments().getString("paymentDetail");
        Bundle b=getIntent().getExtras();
        String url=b.getString("paymentDetail");
        webView = (WebView) findViewById(R.id.web);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progress_layout= (LinearLayout) findViewById(R.id.progress_layout);

        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(true);
//        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        webView.setScrollbarFadingEnabled(false);
//        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebViewClient(new HelloWebViewClient());
//        webView.setWebViewClient(new WebViewClient() {
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
////                showToastMsg(description);
//            }
//
//
//        });
        webView.loadUrl(url);

    }
    private class HelloWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            progressBar.setVisibility(View.VISIBLE);
            progress_layout.setVisibility(View.VISIBLE);

            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

//            progressBar.setVisibility(View.GONE);
            progress_layout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }

    }

}
