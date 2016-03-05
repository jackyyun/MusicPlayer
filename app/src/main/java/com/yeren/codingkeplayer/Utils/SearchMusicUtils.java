package com.yeren.codingkeplayer.Utils;

import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;
import java.util.logging.LogRecord;

import javax.xml.parsers.ParserConfigurationException;


public class SearchMusicUtils {
    private static final int SIZE = 20;
    private static final String URL = Constant.BAIDU_URL + Constant.BAIDU_SEARCH;
    private static SearchMusicUtils sInstance;
    private OnSearchResultListener mListener;

    private ExecutorService mThreadPool;

    public synchronized static SearchMusicUtils getInstance(){
        if(sInstance == null){
            try{
                sInstance = new SearchMusicUtils();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return sInstance;
    }

    private SearchMusicUtils() throws ParserConfigurationException {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    public SearchMusicUtils setListener(OnSearchResultListener mListener){
        this.mListener = mListener;
        return  this;
    }

    public void search(final String key,final int page){

        final Handler handler = new Handler() {
            public void handleMessage(Message msg){
                switch (msg.what){
                    case Constant.SUCCESS:
                        if(mListener != null) mListener.onSearchResult((ArrayList<SearchResult>)msg.obj);
                        break;
                    case Constant.FAILED:
                        if(mListener != null)mListener.onSearchResult(null);
                        break;
                }
            }
        };

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<SearchResult> results = getMusicList(key,page);
                if(results == null){
                    handler.sendEmptyMessage(Constant.FAILED);
                    return;
                }
                handler.obtainMessage(Constant.SUCCESS,results).sendToTarget();
            }
        });
    }






    private ArrayList<SearchResult> getMusicList(final String key,final int page){
        final String start = String.valueOf((page-1)*SIZE);

        try {
            Document doc = Jsoup.connect(URL)
                    .data("key", key, "start", start, "size", String.valueOf(SIZE))
                    .userAgent(Constant.USER_AGENT)
                    .timeout(60 * 1000).get();
            System.out.println(doc);
            Elements songTitles = doc.select("div.song-item.clearFix");
            Elements songInfos;
            ArrayList<SearchResult> searchResults = new ArrayList<>();

            TAG:
            for(Element song: songTitles){
                songInfos = song.getElementsByTag("a");
                SearchResult searchResult = new SearchResult();
                for(Element info:songInfos){
                    //收费歌曲
                    if(info.attr("href").startsWith("http://y.biadu.com/song/")){
                        continue TAG;
                    }
                    //跳转到百度音乐盒的歌曲
                    if(info.attr("href").equals("#")&&!TextUtils.isEmpty(info.attr("data-songdata"))){
                        continue TAG;
                    }
                    //歌曲链接
                    if(info.attr("href").startsWith("/song")){
                        searchResult.setMusicName(info.text());
                        searchResult.setUrl(info.attr("href"));
                    }
                    //歌手链接
                    if(info.attr("href").startsWith("/data")){
                        searchResult.setArtist(info.text());
                    }
                    //专辑链接
                    if(info.attr("href").startsWith("/album")){
                        searchResult.setAlbum(info.text().replace("《|》",""));
                    }
                }
                searchResults.add(searchResult);
            }
            System.out.println(searchResults);
            return searchResults;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;

    }

    public interface OnSearchResultListener {
       public void onSearchResult(ArrayList<SearchResult> results);
    }
}
