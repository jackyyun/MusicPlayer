package com.yeren.codingkeplayer;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.yeren.codingkeplayer.Utils.DownloadUtils;
import com.yeren.codingkeplayer.Utils.SearchResult;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Administrator on 2016/2/24.
 */
public class DownloadDialogFragment extends DialogFragment {
    private SearchResult searchResult;//当前要下载的歌曲；
    private MainActivity mainActivity;

    public  static DownloadDialogFragment newInstance(SearchResult searchResult){
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.searchResult = searchResult;
        return downloadDialogFragment;
    }

    private  String[] items;

    public void onAttach(Context context){
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        items = new String[]{"下载","取消"};

    }

    //创建对话框
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        downloadMusic();
                        break;
                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();
    }


    public void downloadMusic() {
        Toast.makeText(mainActivity,"正在下载"+searchResult.getMusicName(),Toast.LENGTH_SHORT).show();
        DownloadUtils.getInstance().setListener(new DownloadUtils.OnDownloadListener(){
            public void onDownload(String mp3Url){
                Toast.makeText(mainActivity,"下载成功",Toast.LENGTH_SHORT).show();
                //扫描新下载的歌曲
                Uri contentUri = Uri.fromFile(new File(mp3Url));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
                getContext().sendBroadcast(mediaScanIntent);
            }

            public void onFailed(String error){
                Toast.makeText(mainActivity,error,Toast.LENGTH_SHORT).show();
        }
        }).download(searchResult);
    }


}
