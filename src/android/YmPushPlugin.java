package org.apache.cordova.ympush;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.umeng.common.message.UmengMessageDeviceConfig;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.IUmengUnregisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.UmengRegistrar;
import com.umeng.message.entity.UMessage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhangchong on 16/4/15.
 */
public class YmPushPlugin extends CordovaPlugin {

    CallbackContext callbackContext;
    private PushAgent mPushAgent;
    CordovaInterface cordovaInterface;
    List<String> ymKey=new ArrayList<String>();


    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        cordovaInterface=this.cordova;
        mPushAgent = PushAgent.getInstance(cordovaInterface.getActivity());
        if ("ympush".equals(action)){
            Log.e("x",""+args.get(0));
            if(args!=null){
                JSONArray jsonArray=args.getJSONArray(0);
                for(int i=0;i<jsonArray.length();i++){
                    Log.e("x", "" + jsonArray.get(i));
                    ymKey.add(jsonArray.getString(i));
                }
                mPushAgent.setAppkeyAndSecret(ymKey.get(0), ymKey.get(1));
            }
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Log.e("x", "zl");
                    printKeyValue();
                    ymPush();
                    getDeviceToken();
                }
            });

            return true;
        }
        else {
            return false;
        }
    }
    public void getDeviceToken(){
        String device_token = UmengRegistrar.getRegistrationId(cordovaInterface.getActivity());
        PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                "{\""+"device_token\""+":"+"\""+device_token+"\"}");
        mPlugin.setKeepCallback(true);
        callbackContext.sendPluginResult(mPlugin);
    }

    public void ymPush(){
        mPushAgent.setDebugMode(true);
        mPushAgent.setMessageHandler(messageHandler);
        mPushAgent.setNotificationClickHandler(notificationClickHandler);
        String device_token = UmengRegistrar.getRegistrationId(cordovaInterface.getActivity());
        Log.e("device_token",""+device_token);
//		mPushAgent.setPushCheck(true);    //默认不检查集成配置文件
//		mPushAgent.setLocalNotificationIntervalLimit(false);  //默认本地通知间隔最少是10分钟

        //sdk开启通知声音
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
        // sdk关闭通知声音
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        // 通知声音由服务端控制
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);
//		mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//		mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);

        //应用程序启动统计
        //参考集成文档的1.5.1.2
        //http://dev.umeng.com/push/android/integration#1_5_1
        mPushAgent.onAppStart();

        //开启推送并设置注册的回调处理
        mPushAgent.enable(mRegisterCallback);

    }

    private void printKeyValue() {
        //获取自定义参数

        Bundle bun = cordovaInterface.getActivity().getIntent().getExtras();
        if (bun != null)
        {
            Set<String> keySet = bun.keySet();
            for (String key : keySet) {
                String value = bun.getString(key);
                Log.i("YmPlugin", key + ":" + value);
            }
        }

    }

    private void updateStatus() {
        String pkgName = cordovaInterface.getActivity().getPackageName();
        String info = String.format("enabled:%s\nisRegistered:%s\nDeviceToken:%s\n" +
                        "SdkVersion:%s\nAppVersionCode:%s\nAppVersionName:%s",
                mPushAgent.isEnabled(), mPushAgent.isRegistered(),
                mPushAgent.getRegistrationId(), MsgConstant.SDK_VERSION,
                UmengMessageDeviceConfig.getAppVersionCode(cordovaInterface.getActivity()), UmengMessageDeviceConfig.getAppVersionName(cordovaInterface.getActivity()));
        // tvStatus.setText("应用包名：" + pkgName + "\n" + info);

        //btnEnable.setImageResource(mPushAgent.isEnabled() ? R.drawable.open_button : R.drawable.close_button);
        // copyToClipBoard();

        Log.i("YmPlugin", "updateStatus:" + String.format("enabled:%s  isRegistered:%s",
                mPushAgent.isEnabled(), mPushAgent.isRegistered()));
        Log.i("YmPlugin", "=============================");
        //btnEnable.setClickable(true);
    }





    public Handler handler = new Handler();

    //此处是注册的回调处理
    //参考集成文档的1.7.10
    //http://dev.umeng.com/push/android/integration#1_7_10
    public IUmengRegisterCallback mRegisterCallback = new IUmengRegisterCallback() {

        @Override
        public void onRegistered(String registrationId) {
            // TODO Auto-generated method stub
            handler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    updateStatus();
                }
            });
        }
    };

    //此处是注销的回调处理
    //参考集成文档的1.7.10
    //http://dev.umeng.com/push/android/integration#1_7_10
    public IUmengUnregisterCallback mUnregisterCallback = new IUmengUnregisterCallback() {

        @Override
        public void onUnregistered(String registrationId) {
            // TODO Auto-generated method stub
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    updateStatus();
                }
            }, 2000);
        }
    };

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
                        UTrack.getInstance(cordovaInterface.getActivity().getApplicationContext()).trackMsgClick(msg);
                    } else {
                        //自定义消息的忽略统计
                        UTrack.getInstance(cordovaInterface.getActivity().getApplicationContext()).trackMsgDismissed(msg);
                    }
                    Log.e("xxx", "z");
                    PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                            "{\""+"msg\""+":"+"\""+msg.custom+"\"}");
                    mPlugin.setKeepCallback(true);
                    callbackContext.sendPluginResult(mPlugin);
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
//					return builder.build();
//
//				default:
//					//默认为0，若填写的builder_id并不存在，也使用默认。
//					return super.getNotification(context, msg);
//				}
            return super.getNotification(context, msg);
        }
    };
    UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
        @Override
        public void dealWithCustomAction(Context context, UMessage msg) {
            Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
        }
    };


}
