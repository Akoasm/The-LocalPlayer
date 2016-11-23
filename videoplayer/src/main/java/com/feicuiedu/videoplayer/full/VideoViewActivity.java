package com.feicuiedu.videoplayer.full;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.feicuiedu.videoplayer.R;

import java.util.Locale;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * 使用VideoView进行视频播放的Activity,自定义了缓冲时的视图, MediaController是自定义的
 * 使用open方法,传入视频path,启动此Activity
 */
public class VideoViewActivity extends AppCompatActivity {

    private static final String KEY_VIDEO_PATH = "KEY_VIDEO_PATH";

    /**
     * 开启当前Activity，传入视频播放路径
     */
    public static void open(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoViewActivity.class);
        intent.putExtra(KEY_VIDEO_PATH, videoPath);
        context.startActivity(intent);
    }

    private MediaPlayer mediaPlayer;
    private VideoView videoView;

    private ImageView ivLoading; // 缓冲信息(图像)
    private TextView tvBufferInfo; // 缓冲信息(文本信息,显示78kb/s, 67%)
    private int downloadSpeed; // 当前缓冲速度
    private int bufferPercent; // 当前缓冲百分比

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 设置窗口的背景色
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        // 设置当前内容视图
        setContentView(R.layout.activity_video_view);
    }

    @Override public void onContentChanged() {
        super.onContentChanged();
        // 1. 我们要初始化视图
        ininBufferViews();
        // 2. 我们要初始化VideoView
        initVideoView();
    }

    @Override protected void onResume() {
        super.onResume();
        videoView.setVideoPath(getIntent().getStringExtra(KEY_VIDEO_PATH));
    }

    @Override protected void onPause() {
        super.onPause();
        videoView.stopPlayback();
    }

    private void ininBufferViews() {
        tvBufferInfo = (TextView) findViewById(R.id.tvBufferInfo);
        ivLoading = (ImageView) findViewById(R.id.ivLoading);
        tvBufferInfo.setVisibility(View.INVISIBLE);
        ivLoading.setVisibility(View.INVISIBLE);
    }

    private void initVideoView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        Vitamio.isInitialized(this);
        // 控制(暂停,播放,快进等)
//        videoView.setMediaController(new MediaController(this));
        videoView.setMediaController(new CustomMediaController(this));
        videoView.setKeepScreenOn(true);
        videoView.requestFocus();
        // 资源准备监听处理
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                // 设置缓冲区大小(缓冲区填充完后，才会开始播放),默认值就是1M
                mediaPlayer.setBufferSize(512 * 1024);
            }
        });
        // 缓冲更新监听
        videoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
                // percent:当前缓冲的百分比
                bufferPercent = percent;
                updateBufferViewInfo();
            }
        });
        // "状态"信息监听
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: // 开始缓冲
                        showBufferViews();
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: // 结束缓冲
                        hideBufferViews();
                        videoView.start(); // 开始播放视频
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED: // 缓冲时下载速率
                        downloadSpeed = extra;
                        updateBufferViewInfo();
                        break;
                }
                return true;
            }
        });
    }

    // 在开始缓冲时调用的
    private void showBufferViews() {
        tvBufferInfo.setVisibility(View.VISIBLE);
        ivLoading.setVisibility(View.VISIBLE);
        downloadSpeed = 0;
        bufferPercent = 0;
    }

    // 在结束缓冲时调用的
    private void hideBufferViews() {
        tvBufferInfo.setVisibility(View.INVISIBLE);
        ivLoading.setVisibility(View.INVISIBLE);
    }

    // 缓冲时，速度变化时调用的
    private void updateBufferViewInfo() {
        String info = String.format(Locale.CHINA, "%d%%,%dkb/s", bufferPercent, downloadSpeed);
        tvBufferInfo.setText(info);
    }
}
