package com.yeren.codingkeplayer;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yeren.codingkeplayer.Utils.AppUtils;
import com.yeren.codingkeplayer.Utils.Constant;
import com.yeren.codingkeplayer.Utils.SearchMusicUtils;
import com.yeren.codingkeplayer.Utils.SearchResult;
import com.yeren.codingkeplayer.adapter.NetMusicAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/2/16.
 */
public class NetMusicListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private MainActivity mainActivity;
    private ListView listView_net_music;
    private LinearLayout load_layout;
    private LinearLayout ll_search_btn_container;
    private LinearLayout ll_search_container;
    private EditText et_search_content;
    private ImageButton ib_search_btn;

    private ArrayList<SearchResult> searchResults = new ArrayList<>();
    private NetMusicAdapter netMusicAdapter;
    private int page =1;

    public static NetMusicListFragment newInstance() {
        NetMusicListFragment f = new NetMusicListFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.net_music_list_layout,null);
        mainActivity = (MainActivity) getActivity();
        listView_net_music = (ListView) view.findViewById(R.id.listView_net_music);
        load_layout = (LinearLayout) view.findViewById(R.id.load_layout);
        ll_search_btn_container =(LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ll_search_container = (LinearLayout) view.findViewById(R.id.ll_search_container);
        et_search_content = (EditText) view.findViewById(R.id.et_search_content);
        ib_search_btn = (ImageButton) view.findViewById(R.id.ib_search_btn);

        listView_net_music.setOnItemClickListener(this);

        //ll_search_container.setOnClickListener(this);
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);

        mainActivity = (MainActivity) getActivity();
        loadNetData();//加载网络音乐数据；
        System.out.println("yeren step0");
        return view;
    }

    private void  loadNetData(){
        load_layout.setVisibility(View.VISIBLE);

        System.out.println("yeren step1");

        new LoadNetDataTask().execute(Constant.BAIDU_URL+Constant.BAIDU_DAYHOT);

    }

    class LoadNetDataTask extends AsyncTask<String,Integer,Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            load_layout.setVisibility(View.VISIBLE);
            listView_net_music.setVisibility(View.VISIBLE);
            searchResults.clear();
            System.out.println("yeren step2");
            System.out.println(searchResults);
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];

            try {
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6*1000).get();
                System.out.println("yeren step3");
                System.out.println(doc);

                Elements songTitles = doc.select("span.song-title");
                System.out.println("yeren step4");
                Elements artists = doc.select("span.author_list");
                System.out.println("yeren step5");

                for(int i = 0; i< songTitles.size();i++){
                    SearchResult searchResult = new SearchResult();

                    Elements urls = songTitles.get(i).getElementsByTag("a");
                    searchResult.setUrl(urls.get(0).attr("href"));
                    searchResult.setMusicName(urls.get(0).text());

                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    searchResult.setArtist(artistElements.get(0).text());

                    searchResult.setAlbum("热歌榜");
                    searchResults.add(searchResult);
                }
                System.out.println("yeren step6");
                System.out.println(searchResults);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return  1;

        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(result == 1){
                System.out.println("yeren step7");
                netMusicAdapter = new NetMusicAdapter(mainActivity,searchResults);
                System.out.println(searchResults);

                listView_net_music.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout, null));
                listView_net_music.setAdapter(netMusicAdapter);
            }
            load_layout.setVisibility(View.GONE);
            listView_net_music.setVisibility(View.VISIBLE);
            System.out.println("yeren step8");

        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ll_search_btn_container:

                ll_search_btn_container.setVisibility(View.GONE);
                ll_search_container.setVisibility(View.VISIBLE);
                break;

            case R.id.ib_search_btn:
                searchMusic();
                break;

            default:
                break;
        }
    }

    private void searchMusic(){
        //隐藏输入软键盘
        AppUtils.hideInputMethod(et_search_content);
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        String key = et_search_content.getText().toString();
        if(TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity,"请输入关键词",Toast.LENGTH_SHORT).show();
            return;
        }
        load_layout.setVisibility(View.VISIBLE);
        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener(){
            public void onSearchResult(ArrayList<SearchResult> results){
                ArrayList<SearchResult> sr = netMusicAdapter.getSearchResults();
                sr.clear();
                sr.addAll(results);
                netMusicAdapter.notifyDataSetChanged();
                load_layout.setVisibility(View.GONE);
            }

        }).search(key, page);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position>=netMusicAdapter.getSearchResults().size()||position<0) return;
        showDownloadDialog(position);
    }

    private void showDownloadDialog(final int position){
        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(searchResults.get(position));
        downloadDialogFragment.show(getFragmentManager(),"download");
    }


}
