package com.yeren.codingkeplayer;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.yeren.codingkeplayer.Utils.MusicUtils;
import com.yeren.codingkeplayer.Utils.TimeUtils;
import com.yeren.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/18.
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{
    //private ArrayList<Mp3Info> mp3Infos;

    private static final int UPDATTE_TIME = 0X1;

    private TextView tv_play_name;
    //private ImageView im_play_music;
    private SeekBar seekbar_play;
    private TextView tv_playtime_start;
    private TextView tv_playtime_end;
    private ImageView iv_pattern;
    private ImageView iv_like;

    private ImageView iv_play_prev;
    private ImageView iv_play;
    private ImageView iv_play_next;

    private ArrayList<View> views = new ArrayList<>();
    private ImageView iv_album;
    private ViewPager viewPager;
    private CodingkeplayerApp app;

    private static MyHanlder myHanlder;

    public PlayActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        app = (CodingkeplayerApp) getApplication();
        tv_play_name = (TextView) findViewById(R.id.tv_play_name);
        //im_play_music = (ImageView) findViewById(R.id.im_play_music);
        seekbar_play = (SeekBar) findViewById(R.id.seekbar_play);
        tv_playtime_start = (TextView) findViewById(R.id.tv_playtime_start);
        tv_playtime_end = (TextView) findViewById(R.id.tv_playtime_end);
        iv_pattern = (ImageView) findViewById(R.id.iv_pattern);
        iv_like = (ImageView) findViewById(R.id.iv_like);
        iv_play_prev = (ImageView) findViewById(R.id.iv_play_prev);
        iv_play = (ImageView) findViewById(R.id.iv_play);
        iv_play_next = (ImageView) findViewById(R.id.iv_play_next);

        viewPager = (ViewPager) findViewById(R.id.vp_play_music);
        initViewPager();

        iv_play.setOnClickListener(this);
        iv_play_prev.setOnClickListener(this);
        iv_play_next.setOnClickListener(this);
        iv_pattern.setOnClickListener(this);
        seekbar_play.setOnSeekBarChangeListener(this);
        iv_like.setOnClickListener(this);

        //mp3Infos = (ArrayList<Mp3Info>) MusicUtils.getMp3Infos(this);
        //bindPlayService();
        myHanlder = new MyHanlder(this);

    }

    //绑定PlayService
    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    //解绑PlayService
    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbindPlayService();
    }

    // 初始化viewPager
      private void initViewPager(){
        View album_image_layout = getLayoutInflater().inflate(R.layout.layout_play_music_album,null);
        iv_album = (ImageView) album_image_layout.findViewById(R.id.iv_abum);
        views.add(iv_album);
        views.add(getLayoutInflater().inflate(R.layout.layout_play_music_lrc, null));
        viewPager.setAdapter(new MyPagerAdapter(views));
    }

    //viewPager的Adapter
    public class MyPagerAdapter extends PagerAdapter {
        private ArrayList<View> list;
        public MyPagerAdapter(ArrayList<View> list){
            this.list = list;
        }

        @Override
        public int getCount() {
            if(list != null && list.size() > 0){
                return list.size();
            }else{
                return 0;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(list.get(position));
            return list.get(position);
            //return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }



    //手动拖动来改变播放进度
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //更新播放时间
    static class MyHanlder extends Handler {
         private PlayActivity playActivity;

         public MyHanlder(PlayActivity playActivity){
             this.playActivity = playActivity;
         }


         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             if(playActivity!=null){
                 switch (msg.what){
                     case UPDATTE_TIME:
                         playActivity.tv_playtime_start.setText(TimeUtils.getTime(msg.arg1));
                         break;
                     default:
                         break;
                 }
             }
         }
     }



    //更换播放进步和播放时间
    @Override
    public void publish(int progress) {
        //tv_playtime_end.setText(TimeUtils.getTime(progress));
        Message msg = myHanlder.obtainMessage(UPDATTE_TIME);
        msg.arg1 = progress;
        myHanlder.sendMessage(msg);
        seekbar_play.setProgress(progress);
    }


    //得到播放音乐的ID；
    public long getId(Mp3Info mp3Info){
        long id = 0;
        switch (playService.getChangePlayList()){
            case PlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id= mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;

    }

    //更改播放界面状态
    @Override
    public void change(int position) {
       // if(this.playService.isPlaying()){
            Mp3Info mp3Info = playService.mp3Infos.get(position);
            tv_play_name.setText(mp3Info.getTitle());
            Bitmap albumBitmap = MusicUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true);
            //im_play_music.setImageBitmap(albumBitmap);
            iv_album.setImageBitmap(albumBitmap);
            tv_playtime_end.setText(TimeUtils.getTime(mp3Info.getDuration()));
            iv_play.setImageResource(R.mipmap.a2);
            seekbar_play.setProgress(0);
            seekbar_play.setMax((int) mp3Info.getDuration());
            if(playService.isPlaying()){
                iv_play.setImageResource(R.mipmap.a2);
            }else{
                iv_play.setImageResource(R.mipmap.a1);
            }

            switch (playService.getPlay_mode()){
                case PlayService.ORDER_PLAY:
                    iv_pattern.setImageResource(R.mipmap.order_play);
                   // iv_pattern.setTag(PlayService.ORDER_PLAY);
                    break;
                case PlayService.RANDOM_PLAY:
                    iv_pattern.setImageResource(R.mipmap.random);
                   // iv_pattern.setTag(PlayService.RANDOM_PLAY);
                    break;
                case PlayService.SINGLE_PLAY:
                    iv_pattern.setImageResource(R.mipmap.single);
                   // iv_pattern.setTag(PlayService.SINGLE_PLAY);
                    break;
            }
        //}
        try {
            Mp3Info likeMp3Info =  app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
            if(likeMp3Info!=null){
                int isLike = likeMp3Info.getIsLike();
                if(isLike==1){
                    iv_like.setImageResource(R.mipmap.button_like);
                }
            }else{
                iv_like.setImageResource(R.mipmap.button_unlike);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //播放按钮，上一首，下一首，播放模式点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_play_prev:
                playService.prev();
                break;
            case R.id.iv_play:
                if(playService.isPlaying()){
                    iv_play.setImageResource(R.mipmap.a1);
                    playService.pause();
                }else{
                    if(playService.isPause()){
                        iv_play.setImageResource(R.mipmap.a2);
                        playService.start();
                    }else{
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            case R.id.iv_play_next:
                playService.next();
                break;

            case R.id.iv_pattern:
                //int mode = (int) iv_pattern.getTag();

                switch (playService.getPlay_mode()){
                    case PlayService.ORDER_PLAY:
                        iv_pattern.setImageResource(R.mipmap.random);
                       // iv_pattern.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this,getString(R.string.random_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        iv_pattern.setImageResource(R.mipmap.single);
                     //   iv_pattern.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play),Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        iv_pattern.setImageResource(R.mipmap.order_play);
                      //  iv_pattern.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case R.id.iv_like:

                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                //System.out.println(mp3Info);
                Toast.makeText(this,"this is like",Toast.LENGTH_LONG).show();
//                System.out.println(mp3Info.getId());
//                System.out.println("abc");
//                Log.w("yeren", "efg");

                try {
                    Mp3Info likeMp3Info =  app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
                    System.out.println(likeMp3Info);
                    if(likeMp3Info == null){
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        System.out.println(mp3Info);
                        app.dbUtils.save(mp3Info);
                        System.out.println("save");
                        iv_like.setImageResource(R.mipmap.button_like);
                    }else{
                        int isLike = likeMp3Info.getIsLike();
                        if(isLike==1){
                            likeMp3Info.setIsLike(0);
                            iv_like.setImageResource(R.mipmap.button_unlike);
                        }else{
                            likeMp3Info.setIsLike(1);
                            iv_like.setImageResource(R.mipmap.button_like);
                        }
                        app.dbUtils.update(likeMp3Info,"isLike");
                        System.out.println("update");
                        //app.dbUtils.deleteById(Mp3Info.class, likeMp3Info.getId());
                        //System.out.println("delete");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error");
                }
                break;
            default:
                break;
        }
    }
}
