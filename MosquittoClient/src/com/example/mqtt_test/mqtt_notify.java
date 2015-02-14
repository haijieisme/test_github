package com.example.mqtt_test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class mqtt_notify extends Service {
	/* 最重要的三个部分host 地址，client_name,主题：Topic */
	private String host = "tcp://192.168.1.137:1883";
	String client_name = "client";
	private String myTopic = "newline-1"; /* 主题 */
	private MqttClient client;
	private ScheduledExecutorService scheduler;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		

		init();
		startReconnect();
	}

	private void set_notification(String msg) {
		// TODO Auto-generated method stub
		NotificationManager notificationManager = (NotificationManager) mqtt_notify.this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(R.drawable.ic_launcher,
				"新年快乐", System.currentTimeMillis());

		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(mqtt_notify.this, MainActivity.class);
		PendingIntent activity = PendingIntent.getActivity(mqtt_notify.this, 0,
				intent, 0);
		notification.setLatestEventInfo(mqtt_notify.this, "newline",
				"主题 是：" + msg, activity);
		notification.number += 1;
		notificationManager.notify(0, notification);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			scheduler.shutdown();
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	private void init() {
		try {
			// host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
			client = new MqttClient(host, client_name, new MemoryPersistence());
			// MQTT的连接设置
			// options = new MqttConnectOptions();
			// //
			// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
			// options.setCleanSession(true);
			// // 设置连接的用户名
			// options.setUserName(userName);
			// // 设置连接的密码
			// options.setPassword(passWord.toCharArray());
			// // 设置超时时间 单位为秒
			// options.setConnectionTimeout(10);
			// // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
			// options.setKeepAliveInterval(20);
			// 设置回调
			client.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					// 连接丢失后，一般在这里面进行重连
					System.out.println("connectionLost----------");
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// publish后会执行到这里
					System.out.println("deliveryComplete---------"
							+ token.isComplete());
				}

				/* 收到信息 */
				@Override
				public void messageArrived(String topicName, MqttMessage message)
						throws Exception {
					// subscribe后得到的消息会执行到这里面
					System.out.println("messageArrived----------");
					String msg_get = new String(message.getPayload());/* message里面是byte [],要转换一下*/
					Log.d("mqtt", "收到 " + topicName + ":" + msg_get);
					set_notification(msg_get);
				}
			});
			// connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startReconnect() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (!client.isConnected()) {
					connect(); /* 这里进行连接 */
				}
			}
		}, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS); /*
														 * 0秒后开始，每隔10秒后等任务结束开始第二个
														 * ，不会同时执行
														 */
	}

	private void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// client.connect(options);
					client.connect();
					client.subscribe(myTopic, 1); /* 这里订阅了主题 */
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("mqtt", "连接失败");
				}
			}
		}).start();
	}
}
