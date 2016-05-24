package com.example.ympushtest2;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

public class MyApplication extends Application {
	
	private static final String TAG = MyApplication.class.getName();
	private PushAgent mPushAgent;

	@Override
	public void onCreate() {
		super.onCreate();
		mPushAgent = PushAgent.getInstance(this);
		mPushAgent.setDebugMode(true);
		
		UmengMessageHandler messageHandler = new UmengMessageHandler(){
			/**
			 * 参考集成文档的1.6.3
			 * http://dev.umeng.com/push/android/integration#1_6_3
			 * */
			@Override
			public void dealWithCustomMessage(final Context context, final UMessage msg) {
				new Handler().post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						// 对自定义消息的处理方式，点击或者忽略
						boolean isClickOrDismissed = true;
						if(isClickOrDismissed) {
							//自定义消息的点击统计
							UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
						} else {
							//自定义消息的忽略统计
							UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
						}
						Log.e("x","dealWithCustomMessage");
						Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
					}
				});
			}
			
			/**
			 * 参考集成文档的1.6.4
			 * http://dev.umeng.com/push/android/integration#1_6_4
			 * */
			@Override
			public Notification getNotification(Context context,
												UMessage msg) {
//				switch (msg.builder_id) {
//				case 1:
//					NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//					RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
//					myNotificationView.setTextViewText(R.id.notification_title, msg.title);
//					myNotificationView.setTextViewText(R.id.notification_text, msg.text);
//					myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
//					myNotificationView.setImageViewResource(R.id.notification_small_icon, getSmallIconId(context, msg));
//					builder.setContent(myNotificationView)
//                           .setSmallIcon(getSmallIconId(context, msg))
//					       .setTicker(msg.ticker)
//					       .setAutoCancel(true);
//
//					return builder.build();
//
//				default:
//					//默认为0，若填写的builder_id并不存在，也使用默认。
//					return super.getNotification(context, msg);
//				}
				return super.getNotification(context, msg);
			}
		};
		mPushAgent.setMessageHandler(messageHandler);

		/**
		 * 该Handler是在BroadcastReceiver中被调用，故
		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
		 * 参考集成文档的1.6.2
		 * http://dev.umeng.com/push/android/integration#1_6_2
		 * */
		UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
			@Override
			public void dealWithCustomAction(Context context, UMessage msg) {
				Log.e("x",context.getPackageName());
				Intent intent = context.getPackageManager().
						getLaunchIntentForPackage(context.getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
				Bundle bundle = new Bundle();
				bundle.putString("msg", msg.custom);
				intent.putExtras(bundle);
				context.startActivity(intent);
				Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
			}
		};



		//使用自定义的NotificationHandler，来结合友盟统计处理消息通知
		//参考http://bbs.umeng.com/thread-11112-1-1.html
		//CustomNotificationHandler notificationClickHandler = new CustomNotificationHandler();
		mPushAgent.setNotificationClickHandler(notificationClickHandler);
		
//		if (MiPushRegistar.checkDevice(this)) {
//            MiPushRegistar.register(this, "2882303761517400865", "5501740053865");
//		}
	}



}
