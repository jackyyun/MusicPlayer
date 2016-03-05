package com.yeren.codingkeplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.yeren.codingkeplayer.Utils.MusicUtils;
import com.yeren.codingkeplayer.vo.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener{
    private MediaPlayer mPlayer;
    private int currentPosition;

    public ArrayList<Mp3Info> mp3Infos;
    private MusicUpdateListener musicUpdateListener;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private boolean isPause = false;

    public static final int MY_MUSIC_LIST = 1;
    public static final int LIKE_MUSIC_LIST =2;
    public static final int PLAY_RECORD_MUSIC_LIST =3;

    private int ChangePlayList = MY_MUSIC_LIST;

    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;

    public PlayService() {
    }

    //play mode
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getPlay_mode() {
        return play_mode;
    }


    // ChangePlayList
    public int getChangePlayList() {
        return ChangePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        ChangePlayList = changePlayList;
    }

    //设定播放信息列表
    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    //得到播放列表中歌曲的list
    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    //返回当前播放位置
    public int getCurrentPosition(){
        return currentPosition;
    }



    //播放完歌曲后，根据播放模式来播放下一首歌曲
    private Random random = new Random();

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode){
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                currentPosition = random.nextInt(mp3Infos.size());
                play(currentPosition);
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();;
        return false;
    }

    //绑定的时候返回PlayService实例
    class PlayBinder extends Binder {
        public PlayService getPlayService(){
            return PlayService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return  new PlayBinder();
    }

    //
    @Override
    public void onCreate() {
        super.onCreate();

        CodingkeplayerApp app = (CodingkeplayerApp) getApplication();
        currentPosition = app.sp.getInt("currentPositon", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);


        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = (ArrayList<Mp3Info>) MusicUtils.getMp3Infos(this);
        es.execute(updateStatusRunnable);
    }

    //销毁activity的时候回收进程
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(es!=null &&es.isShutdown()){
            es.shutdown();
            es = null;
        }

        mPlayer = null;
        mp3Infos = null;
        musicUpdateListener = null;
    }

    //更改进度runnable()类
    Runnable updateStatusRunnable = new Runnable(){

        @Override
        public void run() {
            while(true){
                if(musicUpdateListener!=null&&mPlayer != null && mPlayer.isPlaying()){
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    //判断是否在播放
    public boolean isPlaying(){
        if(mPlayer!=null){
            return mPlayer.isPlaying();
        }
        return false;
    }

    //播放按钮
    public void play(int position) {
        Mp3Info mp3Info;
        if(position< 0 || position>mp3Infos.size()) {
           position = 0;
        }
        mp3Info = mp3Infos.get(position);

            try {
                mPlayer.reset();
                mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mPlayer.prepare();
                mPlayer.start();
                currentPosition = position;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(musicUpdateListener!=null){
                musicUpdateListener.onChange(currentPosition);
            }

    }

    //返回是否在暂停状态
    public boolean isPause(){
        return  isPause;
    }

    //暂停
    public void pause(){
        if(mPlayer.isPlaying()){
            mPlayer.pause();
            isPause = true;
        }
    }


    //下一首
    public void next(){
        if(currentPosition+1> mp3Infos.size()-1){
            currentPosition = 0;
        }else{
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev(){
        if(currentPosition-1<0){
            currentPosition = mp3Infos.size()-1;
        }else{
            currentPosition--;
        }
        play(currentPosition);
    }

    //暂停后重新开始播放
    public void start(){
        if(mPlayer != null && !mPlayer.isPlaying()){
            mPlayer.start();
        }
    }



    //得到播放进度
    public int getCurrentProgress(){
        if(mPlayer!=null&&mPlayer.isPlaying()){
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    //返回播放进度
    public int getDuration(){
        return mPlayer.getDuration();
    }

    //手动更改进度
    public void seekTo(int msec){
        mPlayer.seekTo(msec);
    }

    //更改界面状态接口
    public interface MusicUpdateListener{
        public void onPublish (int progress);
        public void onChange(int position);
    }

    //设定接口
    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
