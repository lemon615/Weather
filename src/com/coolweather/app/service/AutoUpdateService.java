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
		// AlarmManager是Android用来实现一个闹钟的服务，它一般不实例化，而是通过Context.getSystemService(Context.ALARM_SERVICE)方法获得
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
		//8小时
		int anHour=8*60*60*1000;
		//设置开始计时时间，注意elapsedRealtime()多用于计算时间间隔，且在sleep的情况下也会继续工作（当前时间+8小时）
		long triggerAtTime=SystemClock.elapsedRealtime()+anHour;
		//指定启动AutoUpdateReceiver服务
		Intent i=new Intent(this, AutoUpdateReceiver.class);
		// 创建PendingIntent对象  
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		// 注册一个新的闹铃
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
