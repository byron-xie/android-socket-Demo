package com.example.wxs.androidwebsocketdemo.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.wxs.androidwebsocketdemo.WebSocketService;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.example.wxs.androidwebsocketdemo.Const.ACTION_START_HEART;
import static com.example.wxs.androidwebsocketdemo.Const.ACTION_STOP_HEART;

/**
 * Created by root on 17-6-14.
 */

public class ConnectReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isDisConect = networkInfo == null || !networkInfo.isAvailable();
            if (isDisConect) {
                Toast.makeText(context, "网络已断开，请重新连接", Toast.LENGTH_SHORT).show();
                    Log.e("HeartReceiver","网络已断开,取消定时");
                  context.sendBroadcast(new Intent(ACTION_STOP_HEART));
                }else {
                context.startService(new Intent(context, WebSocketService.class));
                context.sendBroadcast(new Intent(ACTION_START_HEART));
            }
                }
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            context.startService(new Intent(context, WebSocketService.class));
        }
    }
}
