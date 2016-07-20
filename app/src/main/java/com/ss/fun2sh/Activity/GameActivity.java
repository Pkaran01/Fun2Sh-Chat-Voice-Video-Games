package com.ss.fun2sh.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.CRUD.NetworkUtil;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.R;

/**
 * Created by ajaybabup on 6/14/2016.
 */
public class GameActivity  extends AppCompatActivity {

    WebView web;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_game);

        web=(WebView)findViewById(R.id.web);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        if (NetworkUtil.getConnectivityStatus(this)) {

            WebView web=(WebView)findViewById(R.id.web);
            String user= PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.userId);
            String pwd=PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.pwd);
            String web_URL="http://game.fun-joy.co.uk?IDNO="+user+"&PWD="+pwd;

            web.getSettings().setJavaScriptEnabled(true);
            web.setWebViewClient(new HelloWebViewClient());

            web.loadUrl(web_URL);

        } else {
            M.dError(this, "Unable to connect internet !");
        }
    }
    private class HelloWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            progressBar.setVisibility(View.VISIBLE);
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            progressBar.setVisibility(View.GONE);
        }
    }



}
