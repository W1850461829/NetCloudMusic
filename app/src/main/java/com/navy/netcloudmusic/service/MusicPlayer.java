package com.navy.netcloudmusic.service;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.service.media.MediaBrowserService;

import com.wm.remusic.MediaAidlInterface;

import java.util.WeakHashMap;

/**
 * Created by Administrator on 2017/11/27.
 */

public class MusicPlayer {
    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;
    private static final long[] sEmptyList;
    private static MediaAidlInterface mService = null;
    private static ContentValues[] mContentValuesCache = null;

    static {
        mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
        sEmptyList = new Long[0];

    }

    public static final ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;

        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MediaService.class));
        final ServiceBinder binder = new ServiceBinder(callback, contextWrapper.getApplicationContext());
        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MediaService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;

        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;

        }
        mContextWrapper.unbindService(mBinder);
        if (mContextWrapper.isEmpty) {
            mService = null;

        }
    }

    public static final boolean isPlaybackServiceConnected() {
        return mService != null;
    }

    public static void next() {
        if (mService != null) {
            try {
                mService.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initPlaybackServiceWithSettings(final Context context) {
        setShowAlbumArtOnLockscreen(true);
    }

    private static void setShowAlbumArtOnLockscreen(boolean enabled) {
        if (mService != null) {
            try {
                mService.setLockscreenAlbumArt(enabled);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    public static void asyncNext(final Context context,) {
        final Intent previous = new Intent(context, MediaService.class);
        previous.setAction(MediaService.NEXT_ACTION);
        context.startService(previous);
    }

    public static void previous(final Context context, final boolean force) {
        final Intent previous = new Intent(context, MediaService.class);
        if (force) {
            previous.setAction(MediaService.PREVIOUS_FORCE_ACTION);
        } else {
            previous.setAction(MediaService.PREVIOUS_ACTION);
        }
        context.startService(previous);
    }

    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();

                } else {
                    mService.play();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public static boolean isTrackLocal() {

        try {
            if (mService != null) {
                return mService.isTrackLocal();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;


    }
}
