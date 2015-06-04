package com.kiwi.spotifystreamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener  {
    private static final String ACTION_PLAY = "com.kiwi.action.PLAY";
    private static final String ACTION_PAUSE = "com.kiwi.action.PAUSE";
    private static final String ACTION_PREVIOUS = "com.kiwi.action.PREVIOUS";
    private static final String ACTION_NEXT = "com.kiwi.action.NEXT";
    private static final String ACTION_FIRSTTIME = "com.kiwi.action.FIRSTTIME";
    private static final String ACTION_PLAYBACK_START = "com.kiwi.action.PLAYBACK_START";
    private static final String ACTION_PLAYBACK_END = "com.kiwi.action.PLAYBACK_END";

    private MediaPlayer mMediaPlayer = null;
    /** interface for clients that bind */
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    AudioManager audioManager;


    private String presentUrl;


    private Handler mHandler;

    private String url;


    @Override
    public void onCreate(){

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        presentUrl = null;

        mHandler = new Handler(Looper.getMainLooper());



    }


    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }

    public class LocalBinder extends Binder {
        PlaybackService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()

//        url = intent.getStringExtra("url"); // your URL here
//
//        if (url == null) {
//            url = presentUrl;
//        }
//
//        presentUrl = url;
//        Log.e("error", "URL = " + url);




        return mBinder;
    }


    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

    private void initMediaPlayer(){
            mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    sendMessage(ACTION_PLAYBACK_END); // finish current activity
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        sendMessage(ACTION_PLAYBACK_START);

        player.start();



    }


    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.e("error", "audio focus gain is OK");
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    public void controlPlayback(String action, String url){
        this.url = url;
        Log.e("error", "URL = "+url);

        switch(action){
            case ACTION_PREVIOUS:
            case ACTION_NEXT:
                if(mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                initMediaPlayer();
                sendMessage(ACTION_PLAYBACK_END);
                break;
            case ACTION_FIRSTTIME:
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }

                    mMediaPlayer.release();
                }

                initMediaPlayer();

        }

    }

    public void pausePlayback(){

        if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();

        sendMessage(ACTION_PLAYBACK_END);

    }

    public void startOrResumePlayback(){

        if (mMediaPlayer!=null){
            mMediaPlayer.start();
            sendMessage(ACTION_PLAYBACK_START);
        }
        else
            initMediaPlayer();



    }




    private void sendMessage(String action) {

        Intent intent = new Intent(action);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();

    }

    public void seekTo(int progress){
        mMediaPlayer.seekTo(progress * 1000);
    }
}
