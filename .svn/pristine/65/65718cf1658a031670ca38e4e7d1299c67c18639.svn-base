package com.example.admin.localplayer.local;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import android.widget.GridView;

import com.example.admin.localplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by admin on 2016/11/22.
 */

public class LocalVideoActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

//    @BindView(R.id.gridView)
//    GridView gridView;
    GridView gridView;
    private Unbinder unbinder;


    private LocalVideoAdapter localVideoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local);
        gridView = (GridView) findViewById(R.id.gridView);
        ButterKnife.bind(this);
        localVideoAdapter = new LocalVideoAdapter(this);
        // 初始当前页面的Loader
        getLoaderManager().initLoader(0, null, this);
        gridView.setAdapter(localVideoAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localVideoAdapter.release();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Video.Media._ID, // 视频Id
                MediaStore.Video.Media.DATA, // 视频文件路径
                MediaStore.Video.Media.DISPLAY_NAME, // 视频名称
        };

        return  new android.content.CursorLoader(this,MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        localVideoAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        localVideoAdapter.swapCursor(null);
    }
}
