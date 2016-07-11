package com.ss.fun2sh.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rahul on 09-Jun-16.
 */
public class SupportTicketsAdapter extends BaseAdapter {
    Context context;
    int size;
    HashMap<String,ArrayList<String>> support_map=new  HashMap<String,ArrayList<String>>();

    public SupportTicketsAdapter(Context context,int size,HashMap support_map) {
        this.context = context;
        this.size=size;
        this.support_map=support_map;
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {

        TextView txtTitle;
        TextView txtDesc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        //  if (convertView == null) {
        convertView = mInflater.inflate(R.layout.support_tickets_list_row, null);
        //  holder = new ViewHolder();
        //  holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);

        TextView txt_head= (TextView) convertView.findViewById(R.id.txt_head);
        TextView txt_msg= (TextView) convertView.findViewById(R.id.txt_msg);
        TextView txt_attachment= (TextView) convertView.findViewById(R.id.txt_attachment);

        Log.d("ddd adapter",""+support_map.size());


        ArrayList<String> support_head_list = support_map.get(Constants.HEADING);
        ArrayList<String> support_msg_list = support_map.get(Constants.MESSAGE);
        ArrayList<String> support_attach_list =support_map.get(Constants.ATTACHMENT);
//        final String ur=support_map.get(Constants.ATTCHEMENT).toString();

        Log.d("ddd adapter",""+support_head_list.size());
        Log.d("ddd adapter",""+support_msg_list.size());
        Log.d("ddd adapter",""+support_attach_list.size());
//
        txt_head.setText(support_head_list.get(position).toString());
        txt_msg.setText(Html.fromHtml(support_msg_list.get(position).toString()));

        final String ur=support_attach_list.get(position).toString();
//        final String ur=support_attach_list.get(position).toString();


        if(ur.length()>0)
        {
            txt_attachment.setVisibility(View.VISIBLE);

            String mm="http://dabank.co.uk/MailAttachements/pBjfsaennh.pdf";
            Log.d("ddd len",""+ur.length());
            Log.d("ddd ur",""+ur);
            String s=ur.substring(ur.length()-3);
            Log.d("ddd",s);
//            String[] array=mm.split(".");
//            int size=array.length;
//            Log.d("ddd size",""+size);
//            Log.d("ddd ele",""+array[size-1]);
//            for(int i=0;i<array.length;i++)
//            {
//                Log.d("ddd",array[i]);
//            }


//            String ext=ur.split(".")[1];
//            if(ext.equalsIgnoreCase("pdf"))
//            {
//                txt_attachment.setText("Download Attachment");
//            }
//            else
//            {
//                txt_attachment.setText("View Attachment");
//            }

        }
        else
        {
            txt_attachment.setVisibility(View.GONE);
        }

        final View finalConvertView = convertView;

        txt_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Uri uri = Uri.parse("http://www.google.com/intl/en_ALL/images/srpr/logo1w.png");

//                Uri uri = Uri.parse(ur);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                ((Activity) finalConvertView.getContext()).startActivity(intent);

//                final Dialog dialog = new Dialog(finalConvertView.getContext(), android.R.style.Theme_Translucent);
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog.setCancelable(true);
//                dialog.setContentView(R.layout.webview);
//
//                WebView web_preview = (WebView) finalConvertView.findViewById(R.id.web_preview);
//                ProgressBar progressBar = (ProgressBar) finalConvertView.findViewById(R.id.progressBar1);
//                LinearLayout progress_layout= (LinearLayout)finalConvertView.findViewById(R.id.progress_layout);
//
//                web_preview.getSettings().setJavaScriptEnabled(true);
//                web_preview.setWebViewClient(new HelloWebViewClient());
//                web_preview.loadUrl("http://www.google.com");
//
//                dialog.show();

                String s=ur.substring(ur.length()-3);
                Log.d("ddd-",s);
                if(s.contains("pdf"))
                {

                    Dialog dialog = new Dialog(finalConvertView.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.webview);
                    WebView wb = (WebView) dialog.findViewById(R.id.web_preview);
                    wb.getSettings().setJavaScriptEnabled(true);
                     wb.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url="+ur);

                    wb.setWebViewClient(new HelloWebViewClient());
                    dialog.setCancelable(true);
                    dialog.show();
                }
                else
                {
                    Dialog dialog = new Dialog(finalConvertView.getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.webview);
                    WebView wb = (WebView) dialog.findViewById(R.id.web_preview);
                    wb.getSettings().setJavaScriptEnabled(true);
                    wb.loadUrl(ur);
                    wb.setWebViewClient(new HelloWebViewClient());
                    dialog.setCancelable(true);
                    dialog.show();

                }

//                Dialog dialog = new Dialog(finalConvertView.getContext());
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog.setContentView(R.layout.webview);
//                WebView wb = (WebView) dialog.findViewById(R.id.web_preview);
//                wb.getSettings().setJavaScriptEnabled(true);
////                wb.loadUrl("http://www.google.com");
////                wb.loadUrl(ur);
//
//                wb.loadUrl("http://dabank.co.uk/MailAttachements/pBjfsaennh.pdf");
//
////                wb.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + "http://dabank.co.uk/MailAttachements/pBjfsaennh.pdf");
//
//                wb.setWebViewClient(new HelloWebViewClient());
//                dialog.setCancelable(true);
//                dialog.show();
            }
        });

        return convertView;
    }

    public class HelloWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            progressBar.setVisibility(View.VISIBLE);
//            progress_layout.setVisibility(View.VISIBLE);

            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

//            progressBar.setVisibility(View.GONE);
//            progress_layout.setVisibility(View.GONE);
//            webView.setVisibility(View.VISIBLE);
        }

    }

}
