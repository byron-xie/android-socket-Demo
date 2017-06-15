package com.example.wxs.androidwebsocketdemo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

import static com.example.wxs.androidwebsocketdemo.Const.ACTION_HEARTBEAT;
import static com.example.wxs.androidwebsocketdemo.Const.HEARTBEAT_INTERVAL;

/**
 * Created by wxs on 16/8/17.
 */
public class WebSocketService extends Service {

    private static final String TAG = WebSocketService.class.getSimpleName();

    public static final String WEBSOCKET_ACTION = "WEBSOCKET_ACTION";

    private static WebSocketConnection webSocketConnection;
    private static WebSocketOptions options = new WebSocketOptions();
    private static boolean isExitApp = false;
    private static String websocketHost = "ws://121.40.165.18:8088"; //websocket服务端的url,,,ws是协议,和http一样,我写的时候是用的我们公司的服务器所以这里不能贴出来


    private static PendingIntent mPendingIntent;
    private static Context applicationContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(applicationContext==null){
            applicationContext = WebSocketService.this.getApplicationContext();
        }
        initPendingIntent();
        return super.onStartCommand(intent, flags, startId);
    }



    private static void initPendingIntent(){
        if(mPendingIntent==null){
            AlarmManager    mAlarmManager = (AlarmManager)applicationContext. getSystemService(ALARM_SERVICE);
            mPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, new Intent(
                    ACTION_HEARTBEAT), PendingIntent.FLAG_UPDATE_CURRENT);
            // 启动心跳定时器
            long triggerAtTime = SystemClock.elapsedRealtime() + HEARTBEAT_INTERVAL;
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    triggerAtTime, HEARTBEAT_INTERVAL, mPendingIntent);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void closeWebsocket(boolean exitApp) {
        isExitApp = exitApp;
        if (webSocketConnection != null && webSocketConnection.isConnected()) {
            webSocketConnection.disconnect();
            webSocketConnection = null;
        }

    }

    public static void webSocketConnect(){
        webSocketConnection = new WebSocketConnection();
        options.setMaxFramePayloadSize(10000000);
        options.setMaxMessagePayloadSize(10000000);
        try {
            webSocketConnection.connect(websocketHost,new WebSocketHandler(){
                //websocket启动时候的回调
                @Override
                public void onOpen() {
                    Log.d(TAG,"open");
                }


                //websocket接收到消息后的回调
                @Override
                public void onTextMessage(String payload) {
                    if(!payload.contains("心跳开始")){
                        notify_normal(payload);
                    }
                    Log.d(TAG, "payload = " + payload);
                }

                //websocket关闭时候的回调
                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "code = " + code + " reason = " + reason);
                    switch (code) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3://手动断开连接
//                            if (!isExitApp) {
//                                webSocketConnect();
//                            }
                            break;
                        case 4:
                            break;
                        /**
                         * 由于我在这里已经对网络进行了判断,所以相关操作就不在这里做了
                         */
                        case 5://网络断开连接
//                            closeWebsocket(false);
//                            webSocketConnect();
                            break;
                    }
                }
            } , options);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }


    public static void notify_normal(String messgae) {
        Intent intent = new Intent(applicationContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(applicationContext,
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int largeIcon = R.mipmap.ic_launcher;
        int smallIcon = R.mipmap.ic_launcher;
        String ticker = "您有一条新通知";
        String title = "服务器收到-消息如下:";
        String substring = messgae.substring(19, messgae.length());
        ArrayList<String> messageList = new ArrayList<String>();

            messageList.add(substring);

        String content = "[" + messageList.size() + "条]"   + messageList.get(0);
        //实例化工具类，并且调用接口
        NotifyUtil notify3 = new NotifyUtil(applicationContext, 3);
        notify3.notify_mailbox(pIntent, smallIcon, largeIcon, messageList, ticker,
                title, content, true, true, false);

    }
    public static void sendMsg(String s) {
        Log.d(TAG, "sendMsg = " + s);
        if (!TextUtils.isEmpty(s))
            if (webSocketConnection != null) {
                webSocketConnection.sendTextMessage(s);
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPendingIntent!=null){
            AlarmManager    mAlarmManager = (AlarmManager)applicationContext. getSystemService(ALARM_SERVICE);
            mAlarmManager.cancel(mPendingIntent);
            mPendingIntent=null;
        }

    }
    public static class HeartReceiver extends BroadcastReceiver {

        private static final String TAG = "HeartReceiver";
        private int count=0;
        Intent messageIntent = new Intent(WebSocketService.WEBSOCKET_ACTION);

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Const.ACTION_START_HEART.equals(action)) {
                Log.e(TAG,"ACTION_START_HEART");
                if(webSocketConnection==null||!webSocketConnection.isConnected()){
                    Log.e("HeartReceiver","onReceive webSocketConnect initPendingIntent");
                    if (webSocketConnection != null) {
                        try {
                            webSocketConnection.disconnect();
                        }catch (IllegalStateException e){
                        }
                    }
                    webSocketConnect();
                }
                initPendingIntent();

            } else if (ACTION_HEARTBEAT.equals(action)) {
                Log.d(TAG, "Heartbeat");
                //在此完成心跳需要完成的工作，比如请求远程服务器……
                ConnectivityManager systemService = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = systemService.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isAvailable()) {
                    Log.e("HeartReceiver","心跳检测 网络已断开");
                } else {
                  if (webSocketConnection==null||!webSocketConnection.isConnected()) {
                        Log.e("HeartReceiver","心跳检测 swocket ,心跳");
                        if (webSocketConnection != null) {
                            webSocketConnection.disconnect();
                        }
                        webSocketConnect();
                    }else {
                        Log.e("HeartReceiver","心跳开始");
                        messageIntent.putExtra("message","心跳开始___心跳次数为:"+count++);
                        context.sendBroadcast(messageIntent);
                        webSocketConnection.sendTextMessage("心跳开始___心跳次数为:"+count++);

                    }

                }
            } else if (Const.ACTION_STOP_HEART.equals(action)) {
                Log.d(TAG, "Stop heart");
                try{
                    AlarmManager    mAlarmManager = (AlarmManager)applicationContext. getSystemService(ALARM_SERVICE);
                    mAlarmManager.cancel(mPendingIntent);

                }catch (Exception e){}
                mPendingIntent=null;

            }
        }

        public HeartReceiver() {
        }
    }
}
