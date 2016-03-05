package com.yeren.codingkeplayer;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.lidroid.xutils.db.sqlite.Selector;
import com.yeren.codingkeplayer.Utils.MusicUtils;
import com.yeren.codingkeplayer.adapter.MyMusicListAdapter;
import com.yeren.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;


/**
 * Created by Administrator on 2016/2/16.
 */
public class MyMusicListFragment extends Fragment implements OnItemClickListener, View.OnClickListener {

    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter myMusicListAdapter;
    private MainActivity mainActivity;
    //private int position;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //mainActivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment f = new MyMusicListFragment();
        return f;
    }

    private ListView lv_my_music_list;
    private ImageView iv_music_picture;
    private TextView tv_music_name;
    private TextView tv_singer_name;
    private ImageView player_btn_play_normal;
    private ImageView player_btn_next_normal;
    private QuickScroll quickscroll;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout,null);
        iv_music_picture = (ImageView) view.findViewById(R.id.iv_music_picture);
        tv_music_name = (TextView) view.findViewById(R.id.tv_music_name);
        tv_singer_name = (TextView) view.findViewById(R.id.tv_singer_name);
        player_btn_play_normal = (ImageView) view.findViewById(R.id.iv_music_start);
        player_btn_next_normal = (ImageView) view.findViewById(R.id.iv_music_next);
        lv_my_music_list = (ListView) view.findViewById(R.id.lv_my_music_list);
        quickscroll = (QuickScroll) view.findViewById(R.id.quickscroll);

        lv_my_music_list.setOnItemClickListener(this);
        player_btn_play_normal.setOnClickListener(this);
        player_btn_next_normal.setOnClickListener(this);
        iv_music_picture.setOnClickListener(this);


        Log.v("yeren", "point00");
        mainActivity = (MainActivity) getActivity();
        //loadData();
        //mainActivity.bindPlayService();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("myMusicListFragment onResume...");
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("myMusicListFragment onPause...");
        mainActivity.unbindPlayService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mainActivity.unbindPlayService();
    }

    public void loadData(){
        mp3Infos = (ArrayList<Mp3Info>) MusicUtils.getMp3Infos(mainActivity);
        //mp3Infos = mainActivity.playService.mp3Infos;
        myMusicListAdapter = new MyMusicListAdapter(mainActivity,mp3Infos);
        lv_my_music_list.setAdapter(myMusicListAdapter);
        initQuickscroll();
    }

    private void initQuickscroll(){
        quickscroll.init(QuickScroll.TYPE_POPUP_WITH_HANDLE,lv_my_music_list,myMusicListAdapter,QuickScroll.STYLE_HOLO);
        quickscroll.setFixedSize(1);
        quickscroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
        quickscroll.setPopupColor(QuickScroll.BLUE_LIGHT,QuickScroll.BLUE_LIGHT_SEMITRANSPARENT,1, Color.WHITE,1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mainActivity.playService.getChangePlayList()!=PlayService.MY_MUSIC_LIST){
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        mainActivity.playService.play(position);

        //保存播放时间
      savePlayRecord();
    }

    private void savePlayRecord(){
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try{
            Mp3Info playRecordMp3Info = mainActivity.app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getId()));
            if(playRecordMp3Info == null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前时间
                mainActivity.app.dbUtils.save(mp3Info);
            }else{
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.update(playRecordMp3Info,"playTime");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void changeUIStatusOnPlay(int position){
        if(position>=0&&position<mainActivity.playService.mp3Infos.size()){
            Mp3Info mp3Info = mainActivity.playService.mp3Infos.get(position);
            tv_music_name.setText(mp3Info.getTitle());
            tv_singer_name.setText(mp3Info.getArtist());

            if(mainActivity.playService.isPlaying()){
                player_btn_play_normal.setImageResource(R.mipmap.player_btn_pause_normal);
            }else{
                player_btn_play_normal.setImageResource(R.mipmap.player_btn_play_normal);
            }


            Bitmap albumBitmap = MusicUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true);
            iv_music_picture.setImageBitmap(albumBitmap);
           // this.position = position;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_music_start:
                if(mainActivity.playService.isPlaying()){
                    player_btn_play_normal.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();
                }else{
                    if (mainActivity.playService.isPause()){
                        player_btn_play_normal.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                    }else{
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }

                }
                break;

            case R.id.iv_music_next:
                mainActivity.playService.next();
                break;

            case R.id.iv_music_picture:
                Intent intent = new Intent(mainActivity,PlayActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
