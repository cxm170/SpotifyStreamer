package com.kiwi.spotifystreamer;




import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class PlaybackActivity extends AppCompatActivity{

    private int currentPosition;


    private PlaybackFragment playbackFragment;

    private final String PLAPBACK_FRAGMENT_TAG = "PLAYBACK_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playback_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playback); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        currentPosition = getIntent().getIntExtra("position", -1);


        Log.e("error", "current position = " + currentPosition);
        Log.e("error", "previous position = " + CurrentPlaybackHelper.currentPosition);





        if(findViewById(R.id.playback_fragment_container) != null){
            if(savedInstanceState != null) {
                return;
//                playbackFragment = (PlaybackFragment)
//                        getSupportFragmentManager().findFragmentByTag(PLAPBACK_FRAGMENT_TAG);

            }else {
                // Create a new Fragment to be placed in the activity layout
                playbackFragment = PlaybackFragment.newInstance();

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments

                Bundle bundle = getIntent().getExtras();


                Log.e("error", "first chosen artist:" + (CurrentPlaybackHelper.previousArtist.equals("placeholder")));
                Log.e("error", "same artist +" + (CurrentPlaybackHelper.previousArtist.equals(CurrentPlaybackHelper.currentArtist)));

                if (CurrentPlaybackHelper.currentPosition == currentPosition &&
                        (CurrentPlaybackHelper.previousArtist.equals("placeholder") ||
                                CurrentPlaybackHelper.previousArtist.equals(CurrentPlaybackHelper.currentArtist))) {
                    bundle.putBoolean("isSameTrack", true);
                } else {
                    bundle.putBoolean("isSameTrack", false);

                }

                bundle.putBoolean("showDialog", false);

                playbackFragment.setArguments(bundle);


                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                        .add(R.id.playback_fragment_container, playbackFragment, PLAPBACK_FRAGMENT_TAG).commit();

            }
        }

    }



}
