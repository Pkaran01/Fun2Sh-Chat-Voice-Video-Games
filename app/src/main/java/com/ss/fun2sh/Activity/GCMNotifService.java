package com.ss.fun2sh.Activity;

import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;


public class GCMNotifService extends Service {

    Context context;
    String message;
    String sub;
    String url;
    String imageName;

    private final static AtomicInteger notificationCounter = new AtomicInteger(
            1);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    private Target target = new Target() {
        @Override
        public void onPrepareLoad(Drawable arg0) {
//			Logger.log("notif::::onPrepareLoad ");
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//			Logger.log("notif::::onBitmapLoaded "+from);
            generateNotification(bitmap);

        }

        @Override
        public void onBitmapFailed(Drawable arg0) {
//			Logger.log("notif::::onBitmapFailed ");
            Bitmap placeholderBitmap = decodeBitmapFromResource(
                    context.getResources(), R.drawable.notif_placeholder, 96, 96);
            generateNotification(placeholderBitmap);
        }
    };

    @Override
    public void onStart(Intent intent, int startid) {

        context = getApplicationContext();

        if (intent != null) {
            String msg = intent.getStringExtra("message");

//			Logger.log("notif msg::::" + msg);
            M.E(msg);
            if (msg != null) {
                try {
                    JSONObject jsonObject = new JSONObject(msg);
                    message = jsonObject.getString("MSG");
                    sub = jsonObject.getString("SUB");
                    url = jsonObject.getString("URL");
                    imageName = jsonObject.getString("IMG");
//					String image = Constants.NOTIF_IMGPATH + imageName;
                    String image = "http://image.dabank.co.uk/" + imageName;


                    if (!imageName.equals("")) {
                        Picasso.with(getApplicationContext()).load(image)
                                .placeholder(R.drawable.notif_placeholder)
                                .error(R.drawable.notif_error).into(target);
                    } else {
                        generateNotification(null);
                    }

                } catch (JSONException e) {
                }
            }

        } else {
//			Logger.log("notif msg:::: intent else ");
        }

    }

    public void generateNotification(Bitmap bitmap) {

        Uri alarmSound = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//		int smallIcon = R.drawable.notification_img;

        int smallIcon = R.drawable.ic_appicon;


        int statusID = notificationCounter.incrementAndGet();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        Intent intent = new Intent(context, GCMMessageView.class);

        if (!imageName.equals("")) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap));
            // builder.setLargeIcon(largeIconBitmap);
        }

        intent.putExtra("sub", sub);
        intent.putExtra("message", message);
        intent.putExtra("imageName", imageName);
        intent.putExtra("url", url);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, statusID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentText(message);
        builder.setTicker(message);
        builder.setContentTitle(sub);
        builder.setSmallIcon(smallIcon);
        builder.setAutoCancel(true);
        builder.setSound(alarmSound);
        builder.setContentIntent(pIntent);

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(statusID, builder.build());
    }

    public static Bitmap decodeBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode previewImage with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    @Override
    public void onDestroy() {

    }

}


// String imgName="Notification201563419870.jpg";
// image=StringValues.NOTIF_IMGPATH + imgName;
// message = "body message";
// sub = "subject";
// url = "http://www.google.com";

// Handler uiHandler = new Handler(Looper.getMainLooper());
// uiHandler.post(new Runnable() {
// @Override
// public void run() {

//
// }
// });
