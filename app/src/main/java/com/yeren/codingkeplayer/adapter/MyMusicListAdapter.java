package com.yeren.codingkeplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.yeren.codingkeplayer.R;
import com.yeren.codingkeplayer.Utils.TimeUtils;
import com.yeren.codingkeplayer.vo.Mp3Info;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/2/16.
 */
public class MyMusicListAdapter extends BaseAdapter implements Scrollable {

    private Context ctx;
    private ArrayList<Mp3Info> mp3Infos;

    public MyMusicListAdapter(Context ctx,ArrayList<Mp3Info> mp3Infos){
        this.ctx = ctx;
        this.mp3Infos = mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_music_list,null);
            vh = new ViewHolder();
            vh.tv_item_music_name = (TextView) convertView.findViewById(R.id.tv_item_music_name);
            vh.tv_item_singer_name = (TextView) convertView.findViewById(R.id.tv_item_singer_name);
            vh.tv_item_music_time = (TextView) convertView.findViewById(R.id.tv_item_music_time);
            convertView.setTag(vh);
        }
        vh = (ViewHolder) convertView.getTag();
        Mp3Info mp3Info = mp3Infos.get(position);
        vh.tv_item_music_name.setText(mp3Info.getTitle());
        vh.tv_item_singer_name.setText(mp3Info.getArtist());
        vh.tv_item_music_time.setText(TimeUtils.getTime(mp3Info.getDuration()));
        return  convertView;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return Character.toString(mp3Infos.get(childposition).getTitle().charAt(0));
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }

    static  class ViewHolder {
        TextView tv_item_music_name;
        TextView tv_item_singer_name;
        TextView tv_item_music_time;
    }
}
