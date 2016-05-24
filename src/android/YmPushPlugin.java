package org.apache.cordova.ympush;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.umeng.common.message.UmengMessageDeviceConfig;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.IUmengUnregisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
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
    List<String> tags=new ArrayList<String>();
    String device_token;


    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext=callbackContext;
        cordovaInterface=this.cordova;
        mPushAgent = PushAgent.getInstance(cordovaInterface.getActivity());
        if ("startWork".equals(action)){
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


                }
            });

            return true;
        }else if("setTag".equals(action)){
            if(args!=null){
                JSONArray jsonArray=args.getJSONArray(0);
                setTags(jsonArray.getString(0));
            }
            return true;
        }
        else if("deleteTag".equals(action)){
            if(args!=null){
                JSONArray jsonArray=args.getJSONArray(0);
                deleteTags(jsonArray.getString(0));
            }
            return true;
        }
        else {
            return false;
        }
    }
    public void deleteTags(String tag){
        device_token = UmengRegistrar.getRegistrationId(cordovaInterface.getActivity());
        Log.e("device_token",""+device_token);
        PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                "删除TAG失败");
        if(device_token!=""){
            try {
                mPushAgent.getTagManager().delete(tag);
                mPlugin = new PluginResult(PluginResult.Status.OK,
                        "删除TAG成功");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPlugin.setKeepCallback(true);
        callbackContext.sendPluginResult(mPlugin);
    }


    public void setTags(String tag){
        device_token = UmengRegistrar.getRegistrationId(cordovaInterface.getActivity());
        Log.e("device_token",""+device_token);
        PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                "设置TAG失败");
        if(device_token!=""){
            try {
                mPushAgent.getTagManager().add(tag);
                mPlugin = new PluginResult(PluginResult.Status.OK,
                        "设置TAG成功");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPlugin.setKeepCallback(true);
        callbackContext.sendPluginResult(mPlugin);
    }

    public void getDeviceToken(){
        device_token= UmengRegistrar.getRegistrationId(cordovaInterface.getActivity());

        PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                "{\""+"device_token\""+":"+"\""+device_token+"\"}");
        mPlugin.setKeepCallback(true);
        callbackContext.sendPluginResult(mPlugin);

    }

    public void ymPush(){
        mPushAgent.setDebugMode(true);
//        mPushAgent.setMessageHandler(messageHandler);
        mPushAgent.setNotificationClickHandler(notificationClickHandler);
        device_token = UmengRegistrar.getRegistrationId(cordovaInterface.getActivity());
        Log.e("device_token",""+device_token);
        if(device_token!=""){
            PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                    "{\""+"device_token\""+":"+"\""+device_token+"\"}");
            mPlugin.setKeepCallback(true);
            callbackContext.sendPluginResult(mPlugin);
        }
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
        mPushAgent.setAppkeyAndSecret(ymKey.get(0), ymKey.get(1));
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
                //PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                //   {"msg": { "key": " value"}}
                //"{\""+"msg\""+":"+"{\""+key+"\""+":"+"\""+value+"\"}}");
                PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                        "{\""+"msg\""+":"+"\""+value+"\"}");
                mPlugin.setKeepCallback(true);
                callbackContext.sendPluginResult(mPlugin);

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
        getDeviceToken();
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

    UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
        @Override
        public void dealWithCustomAction(Context context, UMessage msg) {

            //用于判断app状态
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                    .getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                    Log.i(context.getPackageName(), "此appimportace ="
                            + appProcess.importance
                            + ",context.getClass().getName()="
                            + context.getClass().getName());
                    if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        Log.i(context.getPackageName(), "处于后台"
                                + appProcess.processName);
                        Log.e("x","处于后台");
                    } else {
                        Log.i(context.getPackageName(), "处于前台"
                                + appProcess.processName);
                        Intent intent = new Intent(context, cordovaInterface.getActivity().getClass());
                        //将MainAtivity的launchMode设置成SingleTask, 或者在下面flag中加上Intent.FLAG_CLEAR_TOP,
                        //如果Task栈中有MainActivity的实例，就会把它移到栈顶，把在它之上的Activity都清理出栈，
                        //如果Task栈不存在MainActivity实例，则在栈顶创建
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        Log.e("x","处于前台");
                    }
                }
            }
            Log.e("msg",msg.custom);
            PluginResult mPlugin = new PluginResult(PluginResult.Status.OK,
                    "{\""+"msg\""+":"+"\""+msg.custom+"\"}");
            mPlugin.setKeepCallback(true);
            callbackContext.sendPluginResult(mPlugin);
            Toast.makeText(cordovaInterface.getActivity().getApplicationContext(), msg.custom, Toast.LENGTH_LONG).show();
        }
    };


}
