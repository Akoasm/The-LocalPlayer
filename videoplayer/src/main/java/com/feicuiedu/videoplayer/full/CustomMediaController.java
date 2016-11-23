package com.feicuiedu.videoplayer.full;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.feicuiedu.videoplayer.R;

import io.vov.vitamio.widget.MediaController;

/**
 * 继承{@link MediaController}，实现自定义的视频播放控制器。
 * 重写{@link #makeControllerView()}方法，提供自定义的视图，视图规则如下：
 * SeekBar的id必须是mediacontroller_seekbar
 * 播放/暂停按钮的id必须是mediacontroller_play_pause
 * 当前时间的id必须是mediacontroller_time_current
 * 总时间的id必须是mediacontroller_time_total
 * 视频名称的id必须是mediacontroller_file_name
 * drawable资源中必须有pause_button和play_button
 */
public class CustomMediaController extends MediaController {

    private MediaPlayerControl mediaPlayerControl;

    private final AudioManager audioManager; // 用来调整音量的
    private Window window; // 用来调整亮度的

    private final int maxVolume; // 最大音量(获取到的)
    private int currentVolume;// 当前音量(在开始滑动手势时的音量)
    private float currentBrightness;// 当前亮度(在开始滑动手势时的亮度(0.0f - 1.0f,如果是负代表自动调整))

    public CustomMediaController(Context context) {
        super(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        window = ((Activity) context).getWindow();
        // 初始设置一个默认音量 50%
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, AudioManager.FLAG_SHOW_UI);
        // 初始设置一个默认亮度 50%
        // WindowManager.LayoutParams layoutParams = window.getAttributes();
        // layoutParams.screenBrightness = 0.5f;
        // window.setAttributes(layoutParams);
    }

    // 重写这个方法(vitamio MediaController的)，来自定义MediaController的视图
    @Override protected View makeControllerView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_custom_video_controller, this);
        initView(view);
        return view;
    }

    // 父类的MediaPlayerControl是私有的,重写这个方法，就是为了将player保存一份，方便我们使用
    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        this.mediaPlayerControl = player;
    }

    private void initView(View view) {
        // 设置forward快进
        ImageButton btnFastForward = (ImageButton) view.findViewById(R.id.btnFastForward);
        btnFastForward.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                // 获取当前进度
                long position = mediaPlayerControl.getCurrentPosition();
                position += 10000;
                mediaPlayerControl.seekTo(position);
            }
        });
        // 设置rewind快退
        ImageButton btnFastRewind = (ImageButton) view.findViewById(R.id.btnFastRewind);
        btnFastRewind.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                long position = mediaPlayerControl.getCurrentPosition();
                position -= 10000;
                mediaPlayerControl.seekTo(position);
            }
        });
        // 调整屏幕亮度(左边)和音量(右边)
        // 1. 拿到View（整个视频播放区我们故意放了一个空的view）
        // 2. 对View进行touch监听
        // 3. 在touch监听里用手势处理
        // 4. 完成屏幕左侧和右侧的判断
        // 5. 完成我们的业务
        final View adjustView = view.findViewById(R.id.adjustView);
        final GestureDetector gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onScroll(
                            MotionEvent e1,
                            MotionEvent e2,
                            float distanceX,
                            float distanceY) {
                        float startX = e1.getX(); // scroll起始位置
                        float startY = e1.getY();
                        float endX = e2.getX();
                        float endY = e2.getY();
                        float width = adjustView.getWidth();
                        float height = adjustView.getHeight();
                        // 垂直移动距离占整个视图高度的比例(用来调整亮度和音量的)
                        float percentage = (startY - endY) / height;
                        // 如果是在屏幕左侧的1/5，调亮度
                        if (startX < width / 5) {
                            adjustBrightness(percentage);
                            return true;
                        }
                        // 如果是在屏幕右侧的1/5，调音量
                        else if (startX > width * 4 / 5) {
                            adjustVolume(percentage);
                            return true;
                        }
                        return false;
                    }
                });
        // 对View进行touch监听,但我们自己不去判断各种touch动作了（我们用系统提供的手势处理）
        adjustView.setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                // 按下事件时(也代码着马上将开始手势处理)获取到当前音量及亮度
                // 使用ACTION_MASK是为了过滤掉多点触屏事件

                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentBrightness = window.getAttributes().screenBrightness;
                }
                gestureDetector.onTouchEvent(event);
                // 为了在调整过程中，不消失
                CustomMediaController.this.show();
                return true;
            }
        });
    }

    private void adjustVolume(float percentage) {
        // 计算出目标音量
        int targetVolume = (int) (percentage * maxVolume) + currentVolume;
        targetVolume = targetVolume > maxVolume ? maxVolume : targetVolume;
        targetVolume = targetVolume < 0 ? 0 : targetVolume;
        // 设置音量
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI);
    }

    private void adjustBrightness(float percentage) {
        // 计算出目标亮度
        float targetBrightness = percentage + currentBrightness;
        targetBrightness = targetBrightness > 1.0f ? 1.0f : targetBrightness;
        targetBrightness = targetBrightness < 0.01f ? 0.01f : targetBrightness;
        // 设置亮度
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = targetBrightness;
        window.setAttributes(layoutParams);
    }
}