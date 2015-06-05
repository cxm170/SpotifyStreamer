package com.kiwi.spotifystreamer;




import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PlaybackFragment extends DialogFragment {

    private static final String ACTION_PLAY = "com.kiwi.action.PLAY";
    private static final String ACTION_PAUSE = "com.kiwi.action.PAUSE";
    private static final String ACTION_PREVIOUS = "com.kiwi.action.PREVIOUS";
    private static final String ACTION_NEXT = "com.kiwi.action.NEXT";
    private static final String ACTION_FIRSTTIME = "com.kiwi.action.FIRSTTIME";
    private static final String ACTION_PLAYBACK_START = "com.kiwi.action.PLAYBACK_START";
    private static final String ACTION_PLAYBACK_END = "com.kiwi.action.PLAYBACK_END";


    private ArrayList<String> spotifyIDsForTracks;
    private int position;


    private TextView playbackArtist;
    private TextView playbackAlbum;
    private TextView playbackTrack;
    private ImageView playbackImage;
    private SeekBar seekBar;
    private ImageButton playbackLast;
    private ImageButton playbackPause;
    private ImageButton playbackNext;
    private TextView playbackPosition;
    private TextView playbackFinalTime;

    private boolean isPause = true;

    private SpotifyApi api;

    private SpotifyService spotify;


    private int timeElapsed = 0;

    private PlaybackService mService;
    private boolean mBound = false;

    private TimeZone timeZone ;
    private SimpleDateFormat dataFormat;

    private Handler durationHandler = new Handler();

    private Boolean isSameTrack;


    public Track currentTrack;

    private boolean showDialog;

    static PlaybackFragment newInstance() {
        return new PlaybackFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playback_fragment, container, false);

        if(showDialog)
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);



        Log.e("error", "Track: " + currentTrack);

        Intent intentPlay = new Intent(getActivity(), PlaybackService.class);
        getActivity().startService(intentPlay);
        getActivity().bindService(intentPlay, mConnection, Context.BIND_AUTO_CREATE);


        if(savedInstanceState!=null){
            isSameTrack = savedInstanceState.getBoolean("isSameTrack");
        }


            playbackArtist = (TextView) view.findViewById(R.id.playback_artist_name);
            playbackAlbum = (TextView) view.findViewById(R.id.playback_album_name);
            playbackTrack = (TextView) view.findViewById(R.id.playback_track_name);
            playbackImage = (ImageView) view.findViewById(R.id.playback_image);
            seekBar = (SeekBar) view.findViewById(R.id.playback_seekbar);
            playbackLast = (ImageButton) view.findViewById(R.id.playback_last);
            playbackPause = (ImageButton) view.findViewById(R.id.playback_pause);
            playbackNext = (ImageButton) view.findViewById(R.id.playback_next);
            playbackPosition = (TextView) view.findViewById(R.id.playback_position);
            playbackFinalTime = (TextView) view.findViewById(R.id.playback_finaltime);



        playbackNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (position == spotifyIDsForTracks.size() - 1) position = -1;
                downloadAndPlayTrack(++position, ACTION_NEXT);
                CurrentPlaybackHelper.currentPosition++;

            }
        });

        playbackLast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (position == 0) position = spotifyIDsForTracks.size();
                downloadAndPlayTrack(--position, ACTION_PREVIOUS);
                CurrentPlaybackHelper.currentPosition--;
            }
        });

        playbackPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBound) {
                    if (!isPause) {
                        mService.pausePlayback();
                        isPause = true;

                    } else {
                        mService.startOrResumePlayback();
                        isPause = false;

                    }
                }
            }

        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mBound && fromUser) {
                    mService.seekTo(seekBar.getProgress());
                }
            }
        });





        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        CurrentPlaybackHelper.playingNow = true;
        CurrentPlaybackHelper.previousArtist = CurrentPlaybackHelper.currentArtist;

        showDialog = getArguments().getBoolean("showDialog");

        timeZone = TimeZone.getTimeZone("UTC");
        dataFormat = new SimpleDateFormat("mm:ss");
        dataFormat.setTimeZone(timeZone);



        spotifyIDsForTracks = getArguments().getStringArrayList("spotifyIDsForTracks");

        CurrentPlaybackHelper.spotifyIDsForTracks = spotifyIDsForTracks;

        position = getArguments().getInt("position", 0);

        CurrentPlaybackHelper.currentPosition = position;

        isSameTrack = getArguments().getBoolean("isSameTrack");

        api = new SpotifyApi();

        spotify = api.getService();










        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYBACK_START);
        intentFilter.addAction(ACTION_PLAYBACK_END);


        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                intentFilter);








    }



    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }

        durationHandler.removeCallbacks(updateSeekbarTime);

    }

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        // Unbind from the service



        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isSameTrack", isSameTrack);

    }



    private void changeUI(final String action, Track track){
        if (track.artists.size() > 0)
            playbackArtist.setText(track.artists.get(0).name);
        playbackAlbum.setText(track.album.name);
        playbackTrack.setText(track.name);
        if (track.album.images.size() > 0)
            Picasso.with(getActivity()).load(track.album.images.get(0).url).into(playbackImage);

        seekBar.setMax(30);



        String time = dataFormat.format(new Date(29000));


        playbackFinalTime.setText(time);


        seekBar.setClickable(false);


        if (action != null) {
            //Music starts to play once this activity is created.
            seekBar.setProgress(0);
            playbackPosition.setText("00:00");
            if (mBound) {
                mService.controlPlayback(action, track.preview_url);

            }
        }
    }



    private void downloadAndPlayTrack(int position, final String action){

        spotify.getTrack(spotifyIDsForTracks.get(position), new Callback<Track>() {
            @Override
            public void success(final Track track, Response response) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        currentTrack = track;
                        changeUI(action, track);


                    }
                });


            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity().getApplicationContext(), "The top_track_view is not found.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;



            if (isSameTrack){
                int currentPosition = mService.getCurrentPosition();
                playbackPosition.setText(dataFormat.format(new Date(currentPosition * 1000)));
                seekBar.setProgress(currentPosition);
                if(currentTrack!=null) changeUI(null, currentTrack);
                else downloadAndPlayTrack(position, null);
                durationHandler.postDelayed(updateSeekbarTime, 1000);
            }
            else {
                downloadAndPlayTrack(position, ACTION_FIRSTTIME);
                isSameTrack = true;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case ACTION_PLAYBACK_START:
                    playbackPause.setImageResource(R.drawable.ic_pause_black_24dp);
                    isPause = false;
                    durationHandler.postDelayed(updateSeekbarTime, 1000);
                    break;
                case ACTION_PLAYBACK_END:
                    playbackPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    isPause = true;
                    durationHandler.removeCallbacks(updateSeekbarTime);
                    break;
            }
        }
    };

    private Runnable updateSeekbarTime = new Runnable() {
        @Override
        public void run() {
            if(mBound) {
                timeElapsed = mService.getCurrentPosition();
                seekBar.setProgress(timeElapsed);
                playbackPosition.setText(dataFormat.format(new Date(timeElapsed * 1000)));
//                Log.e("error", "time =" + dataFormat.format(new Date(timeElapsed*1000)));
                durationHandler.postDelayed(this, 1000);

            }
        }
    };
}
