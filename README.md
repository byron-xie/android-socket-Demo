# androidWebsocketDemo
android使用websocket进行长链接的一个简单的demo，该项目适合嵌入到系统，也可以增加监听保活和心跳代码已经写好，适合小白阅读和理解


目前可以容纳10万字节 三四万中文字符不成问题，如果自己想要更大可以修改，这个demo可以接收报文，如果文件巨大建议用http上传下载



##基本操作都在WebSocketService 这个类中，websocketHost要填写自己服务器的，ws开头的url； 服务端的url为测试，可以顺利收发消息

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
        
        
        
        
        
        
        
        
        
        
        心跳，2个任务 保持长连接，并检测连接是否正常，不正常或者断网（断网分3种，一种断网未断开，一种已经网和连接断开，另外一种 网未断，连接段）
        
              if (Const.ACTION_START_HEART.equals(action)) {
              //启动心跳
                if(webSocketConnection==null||!webSocketConnection.isConnected()){
                    Log.e("HeartReceiver","onReceive webSocketConnect initPendingIntent");
                    //初次心跳检测连接是否存在，不存在或未连接 
                    if (webSocketConnection != null) {
                        try {
                            webSocketConnection.disconnect();//如果存在连接对象 连接不上 则取消连接
                        }catch (IllegalStateException e){
                        }
                    }
                    webSocketConnect();//初始化连接 并连接
                }
                initPendingIntent();//初始化心跳

            } else if (ACTION_HEARTBEAT.equals(action)) {
                //在此完成心跳需要完成的工作，比如请求远程服务器……
                ConnectivityManager systemService = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = systemService.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isAvailable()) {//检测网络
                //第一种情况
                } else {
                  if (webSocketConnection==null||!webSocketConnection.isConnected()) 
                        if (webSocketConnection != null) { //第二种情况
                            webSocketConnection.disconnect();
                        }
                        webSocketConnect();
                    }else {
                        context.sendBroadcast(messageIntent);
                        webSocketConnection.sendTextMessage("心跳开始___心跳次数为:"+count++);
                    }

                }
            } else if (Const.ACTION_STOP_HEART.equals(action)) {
            
             //停止心跳 ，节省电量
             
             
                Log.d(TAG, "Stop heart");
                try{
                    AlarmManager    mAlarmManager = (AlarmManager)applicationContext. getSystemService(ALARM_SERVICE);
                    mAlarmManager.cancel(mPendingIntent);

                }catch (Exception e){}
                mPendingIntent=null;

            }
        }

``
