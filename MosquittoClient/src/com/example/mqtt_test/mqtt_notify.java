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
	/* ����Ҫ����������host ��ַ��client_name,���⣺Topic */
	private String host = "tcp://192.168.1.137:1883";
	String client_name = "client";
	private String myTopic = "newline-1"; /* ���� */
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
				"�������", System.currentTimeMillis());

		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(mqtt_notify.this, MainActivity.class);
		PendingIntent activity = PendingIntent.getActivity(mqtt_notify.this, 0,
				intent, 0);
		notification.setLatestEventInfo(mqtt_notify.this, "newline",
				"���� �ǣ�" + msg, activity);
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
			// hostΪ��������testΪclientid������MQTT�Ŀͻ���ID��һ���Կͻ���Ψһ��ʶ����ʾ��MemoryPersistence����clientid�ı�����ʽ��Ĭ��Ϊ���ڴ汣��
			client = new MqttClient(host, client_name, new MemoryPersistence());
			// MQTT����������
			// options = new MqttConnectOptions();
			// //
			// �����Ƿ����session,�����������Ϊfalse��ʾ�������ᱣ���ͻ��˵����Ӽ�¼����������Ϊtrue��ʾÿ�����ӵ������������µ��������
			// options.setCleanSession(true);
			// // �������ӵ��û���
			// options.setUserName(userName);
			// // �������ӵ�����
			// options.setPassword(passWord.toCharArray());
			// // ���ó�ʱʱ�� ��λΪ��
			// options.setConnectionTimeout(10);
			// // ���ûỰ����ʱ�� ��λΪ�� ��������ÿ��1.5*20���ʱ����ͻ��˷��͸���Ϣ�жϿͻ����Ƿ����ߣ������������û�������Ļ���
			// options.setKeepAliveInterval(20);
			// ���ûص�
			client.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					// ���Ӷ�ʧ��һ�����������������
					System.out.println("connectionLost----------");
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// publish���ִ�е�����
					System.out.println("deliveryComplete---------"
							+ token.isComplete());
				}

				/* �յ���Ϣ */
				@Override
				public void messageArrived(String topicName, MqttMessage message)
						throws Exception {
					// subscribe��õ�����Ϣ��ִ�е�������
					System.out.println("messageArrived----------");
					String msg_get = new String(message.getPayload());/* message������byte [],Ҫת��һ��*/
					Log.d("mqtt", "�յ� " + topicName + ":" + msg_get);
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
					connect(); /* ����������� */
				}
			}
		}, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS); /*
														 * 0���ʼ��ÿ��10�������������ʼ�ڶ���
														 * ������ͬʱִ��
														 */
	}

	private void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// client.connect(options);
					client.connect();
					client.subscribe(myTopic, 1); /* ���ﶩ�������� */
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("mqtt", "����ʧ��");
				}
			}
		}).start();
	}
}
