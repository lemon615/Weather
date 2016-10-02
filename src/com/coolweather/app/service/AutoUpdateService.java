package com.coolweather.app.service;

import com.coolweather.app.activity.WeatherActivity;
import com.coolweather.app.receiver.AutoUpdateReceiver;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWeather();
			}
		}).start();
		// AlarmManager��Android����ʵ��һ�����ӵķ�����һ�㲻ʵ����������ͨ��Context.getSystemService(Context.ALARM_SERVICE)�������
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
		//8Сʱ
		int anHour=8*60*60*1000;
		//���ÿ�ʼ��ʱʱ�䣬ע��elapsedRealtime()�����ڼ���ʱ����������sleep�������Ҳ�������������ǰʱ��+8Сʱ��
		long triggerAtTime=SystemClock.elapsedRealtime()+anHour;
		//ָ������AutoUpdateReceiver����
		Intent i=new Intent(this, AutoUpdateReceiver.class);
		// ����PendingIntent����  
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		// ע��һ���µ�����
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode=prefs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			@Override
			public void onError(Exception e) {
				Log.v("crb", "e="+e);
			}
		});
	}
}
