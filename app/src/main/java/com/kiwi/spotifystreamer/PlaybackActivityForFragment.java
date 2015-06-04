package com.kiwi.spotifystreamer;



import android.app.DialogFragment;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PlaybackActivityForFragment extends AppCompatActivity{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playback_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playback); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        if(findViewById(R.id.playback_fragment_container) != null){
            if(savedInstanceState != null)
                return;

            // Create a new Fragment to be placed in the activity layout
            DialogFragment firstFragment = PlaybackFragment.newInstance();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.playback_fragment_container, firstFragment).commit();


        }



    }
}
