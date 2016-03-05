package com.yeren.codingkeplayer;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Administrator on 2016/2/17.
 */
public abstract class BaseActivity extends FragmentActivity {

    protected PlayService playService;
    private Boolean isBound = false;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    private ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
                playService = playBinder.getPlayService();
                playService.setMusicUpdateListener(musicUpdateListener);
                musicUpdateListener.onChange(playService.getCurrentPosition());
            }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
        }
    };

    private PlayService.MusicUpdateListener musicUpdateListener = new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);
    public abstract void change(int progress);

    public void bindPlayService(){
        if(!isBound) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    public void unbindPlayService(){
        if(isBound) {
            unbindService(conn);
            isBound = false;
        }
    }
}
