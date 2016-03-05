package com.yeren.codingkeplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.yeren.codingkeplayer.adapter.MyMusicListAdapter;
import com.yeren.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/22.
 */
public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private ListView listView_play_record;
    private CodingkeplayerApp app;
    private MyMusicListAdapter adapter;
    private ArrayList<Mp3Info> mp3Infos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);

        listView_play_record = (ListView) findViewById(R.id.listView_near_play);
        listView_play_record.setAdapter(adapter);
        app = (CodingkeplayerApp) getApplication();
        listView_play_record.setOnItemClickListener(this);
        initData();
    }

    protected void onResume(){
        super.onResume();
        bindPlayService();
    }

    protected void onPause(){
        super.onPause();
        unbindPlayService();
    }

    private void initData(){
        try {
            //查询最近播放的五首歌曲
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime","!=",0).orderBy("playTime",true).limit(5));
            System.out.println(list);
            if(list == null || list.size()==0){
                listView_play_record.setVisibility(View.GONE);
            }else{
                listView_play_record.setVisibility(View.VISIBLE);
                mp3Infos = (ArrayList<Mp3Info>)list;
                adapter = new MyMusicListAdapter(this,mp3Infos);
                listView_play_record.setAdapter(adapter);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int progress) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(playService.getChangePlayList()!=PlayService.PLAY_RECORD_MUSIC_LIST){
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }
        playService.play(position);
    }
}
