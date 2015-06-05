package com.kiwi.spotifystreamer;




import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTrackActivity extends AppCompatActivity implements TopTrackFragment.Callbacks {


    Button nowplaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.top_track_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_track); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        String subtitle = intent.getStringExtra("artistName");

        if (null != toolbar) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            toolbar.setSubtitle(subtitle);
            toolbar.setSubtitleTextColor(0xFFBDBDBD);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(TopTrackActivity.this);

                }
            });
        }

        nowplaying = (Button) findViewById(R.id.nowplaying);

        if(nowplaying != null) {
            nowplaying.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CurrentPlaybackHelper.playingNow) {
                        Intent appInfo = new Intent(TopTrackActivity.this, PlaybackActivity.class);
                        appInfo.putStringArrayListExtra("spotifyIDsForTracks", CurrentPlaybackHelper.spotifyIDsForTracks);

                        appInfo.putExtra("position", CurrentPlaybackHelper.currentPosition);


                        startActivity(appInfo);
                    } else {
                        Toast.makeText(getApplicationContext(), "No songs are playing now.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }



        if(findViewById(R.id.track_list_view_container) != null){
            if(savedInstanceState != null) {


                return;

            }


            // Create a new Fragment to be placed in the activity layout
            TopTrackFragment firstFragment = new TopTrackFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments

            Bundle bundle = getIntent().getExtras();


            firstFragment.setArguments(bundle);

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.track_list_view_container, firstFragment).addToBackStack(null).commit();


        }



    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelectedTrack(ArrayList<String> spotifyIDsForTracks, int position){
        Intent appInfo = new Intent(TopTrackActivity.this, PlaybackActivity.class);
        appInfo.putStringArrayListExtra("spotifyIDsForTracks", spotifyIDsForTracks);

        appInfo.putExtra("position", position);


        startActivity(appInfo);
    }


}
