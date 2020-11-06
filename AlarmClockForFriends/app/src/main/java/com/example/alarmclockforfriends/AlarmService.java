package com.example.alarmclockforfriends;


import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AlarmService extends Service {

    public static AlarmBroadcastReceiver receiver=null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //restartAlarmManager();
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.start();
        return Service.START_STICKY;
    }

    private void restartAlarmManager() {
        if(receiver!=null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
        receiver = new AlarmBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("AlarmService");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        AlarmBroadcastReceiver.startAlarm(this);
    }

    @Override
    public void onDestroy()
    {
        AlarmBroadcastReceiver.stopAlarm(this);
        super.onDestroy();
    }
}
