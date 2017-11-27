package com.navy.netcloudmusic.activity;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.navy.netcloudmusic.R;
import com.navy.netcloudmusic.service.MusicPlayer;
import com.wm.remusic.MediaAidlInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/27.
 */

public class BaseActivity extends AppCompatActivity implements ServiceConnection {
    private MusicPlayer.ServiceToken mToken;
    private PlaybackStatus mPlaybackStatus;
    private QuickControlsFragment fragment;
    private String TAG = "BaseActivity";
    private ArrayList<MusicStateListener> mMusicListner = new ArrayList<>();


    /**
     * 更新播放队列
     */
    public void updateQueue() {

    }

    /**
     * 更新歌曲状态信息
     */
    public void updataTrackInfo() {
        for (final MusicStateListener listener : mMusicListner) {
            if (listener != null) {
                listener.reloadAdapter();
                listener.updateTrackInfo();

            }

        }
    }

    /**
     * fragment界面刷新
     */
    public void refreshUI() {
        for (final MusicStateListener listener : mMusicListner) {
            if (listener != null) {
                listener.reloadAdapter();

            }

        }
    }

    public void updateTime() {
        for (final MusicStateListener listener : mMusicListner) {
            if (listener != null) {
                listener.updateTime();

            }

        }
    }

    /**
     * 歌曲切换
     */
    public void updateTrack() {

    }

    public void updateLrc() {

    }

    /**
     * @param p 更新歌曲缓冲进度值，p取值从0~100
     */
    public void updateBuffer(int p) {

    }

    public void changeTheme() {
        for (final MusicStateListener listener : mMusicListner) {
            if (listener != null) {
                listener.changeTheme();
            }

        }
    }

    /**
     * @param l 歌曲是否加载中
     */
    public void loading(boolean l) {

    }


    /**
     * @param outState 取消保存状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    /**
     * @param savedInstanceState 取消保存状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * @param show 显示或关闭底部播放控制栏
     */
    protected void showQuickControl(boolean show) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (show) {
            if (fragment == null) {
                fragment = QuickControlsFragment.newInstance();
                ft.add(R.id.bottom_container, fragment).commitAllowingStateLoss();

            } else {
                ft.show(fragment).commitAllowingStateLoss();
            }

        } else {
            if (fragment != null) {
                ft.hide(fragment).commitAllowingStateLoss();

            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = MusicPlayer.bindToService(this, this);
        mPlaybackStatus = new PlaybackState(this);
        IntentFilter f = new IntentFilter();
        f.addAction(MediaService.PLAYSTATE_CHANGED);
        f.addAction(MediaService.META_CHANGED);
        f.addAction(MediaService.QUEUE_CHANGED);
        f.addAction(IConstants.MUSIC_COUNT_CHANGED);
        f.addAction(MediaService.TRACK_PREPARED);
        f.addAction(MediaService.BUFFER_UP);
        f.addAction(IConstants.EMPTY_LIST);
        f.addAction(MediaService.MUSIC_CHANGED);
        f.addAction(MediaService.LRC_UPDATED);
        f.addAction(IConstants.PLAYLIST_COUNT_CHANGED);
        f.addAction(MediaService.MUSIC_LODING);
        registerReceiver(mPlaybackStatus, new IntentFilter(f));
        showQuickControl(true);

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mService = MediaAidlInterface.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mService = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();

        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMusicListner.clear();

    }

    private void unbindService() {
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }

    }

    public void setMusicStateListenerListener(final MusicStateListener status) {
        if (status == this) {
            throw new UnsupportedOperationException("Override the method, don't add a listener");

        }
        if (status != null) {
            mMusicListner.add(status);
        }
    }

    public void removeMusicStateListenerListener(final MusicStateListener status) {
        if (status != null) {
            mMusicListener.remove(status);
        }
    }

    private final static class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<BaseActivity> mReference;


        public PlaybackStatus(final BaseActivity activity) {
            mReference = new WeakReference<>(activity);
        }


        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            BaseActivity baseActivity = mReference.get();
            if (baseActivity != null) {
                if (action.equals(MediaService.META_CHANGED)) {
                    baseActivity.updateTrackInfo();

                } else if (action.equals(MediaService.PLAYSTATE_CHANGED)) {

                } else if (action.equals(MediaService.TRACK_PREPARED)) {
                    baseActivity.updateTime();
                } else if (action.equals(MediaService.BUFFER_UP)) {
                    baseActivity.updateBuffer(intent.getIntExtra("progress", 0));
                } else if (action.equals(MediaService.MUSIC_LODING)) {
                    baseActivity.loading(intent.getBooleanExtra("isloading", false));
                } else if (action.equals(MediaService.REFRESH)) {

                } else if (action.equals(IConstants.MUSIC_COUNT_CHANGED)) {
                    baseActivity.refreshUI();
                } else if (action.equals(IConstants.PLAYLIST_COUNT_CHANGED)) {
                    baseActivity.refreshUI();
                } else if (action.equals(MediaService.QUEUE_CHANGED)) {
                    baseActivity.updateQueue();
                } else if (action.equals(MediaService.TRACK_ERROR)) {
                    final String errorMsg = context.getString(R.string.exit,
                            intent.getStringExtra(MediaService.TrackErrorExtra.TRACK_NAME));
                    Toast.makeText(baseActivity, errorMsg, Toast.LENGTH_SHORT).show();
                } else if (action.equals(MediaService.MUSIC_CHANGED)) {
                    baseActivity.updateTrack();
                } else if (action.equals(MediaService.LRC_UPDATED)) {
                    baseActivity.updateLrc();
                }

            }
        }
    }
}


