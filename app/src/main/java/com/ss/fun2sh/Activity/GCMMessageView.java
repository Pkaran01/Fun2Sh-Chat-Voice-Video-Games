package com.ss.fun2sh.Activity;

import com.squareup.picasso.Picasso;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.R;

public class GCMMessageView extends Activity implements OnClickListener {
	
	TextView txtmsg;
	TextView txtHeader;
	ImageView notifImage;
	TextView notif_url;
	String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messageview);
		txtmsg = (TextView) findViewById(R.id.notif_message);
		txtHeader = (TextView) findViewById(R.id.notif_header);
		notifImage = (ImageView) findViewById(R.id.notif_image);
		notif_url = (TextView) findViewById(R.id.notif_url);
		notif_url.setOnClickListener(this);

		String sub = getIntent().getStringExtra("sub");
		String message = getIntent().getStringExtra("message");
		String imageName = getIntent().getStringExtra("imageName");
		url = getIntent().getStringExtra("url");
		String image = Const.NOTIF_IMGPATH + imageName;

		txtHeader.setText(sub.toUpperCase());
		
		if (!message.equals("")) {
			txtmsg.setVisibility(View.VISIBLE);
		    txtmsg.setText(message);
		
		}

		if (!imageName.equals("")) {
			notifImage.setVisibility(View.VISIBLE);
			Picasso.with(this).load(image)
					.placeholder(R.drawable.notif_placeholder)
					.error(R.drawable.notif_error).into(notifImage);
//			notifImage.setScaleType(ScaleType.FIT_XY);
		}
		
		if (!url.equals("")) {
			notif_url.setVisibility(View.VISIBLE);
			notif_url.setPaintFlags(notif_url.getPaintFlags()
					| Paint.UNDERLINE_TEXT_FLAG);
			notif_url.setText("Click here");
		}
	}

	@Override
	public void onClick(View view) {

		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
			startActivity(browserIntent);
		} catch (ActivityNotFoundException exception) {
			exception.printStackTrace();
		}

	}

}












//import android.os.Bundle;
//import android.widget.TextView;
//
//public class GCMMessageView extends DaBankActivity {
//	String message;
//	TextView txtmsg;
//	TextView txtHeader;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.messageview);
//		txtmsg = (TextView) findViewById(R.id.notif_message);
//		txtHeader = (TextView) findViewById(R.id.notif_header);
//		String message = getIntent().getStringExtra("message");
//		String subject = getIntent().getStringExtra("subject");
//		txtmsg.setText(message);
//		txtHeader.setText(subject);
//		
//	}
//}
