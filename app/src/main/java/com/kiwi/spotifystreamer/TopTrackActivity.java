package com.kiwi.spotifystreamer;




import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTrackActivity extends AppCompatActivity {

    String spotifyIDForArtist;
    ListView topTracksListView;
    TrackArrayAdapter trackAdapter;

    ArrayList<String> trackNames = new ArrayList<>();
    ArrayList<String> albumNames = new ArrayList<>();
    ArrayList<String> albumImageURLs = new ArrayList<>();

    ArrayList<String> spotifyIDsForTracks = new ArrayList<>();



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


        spotifyIDForArtist = intent.getStringExtra("spotifyIDForArtist");
        topTracksListView = (ListView) findViewById(R.id.track_list_view);

        if(savedInstanceState == null){


            new QueryTopTracksTask().execute(spotifyIDForArtist);


        }else{
            trackNames = savedInstanceState.getStringArrayList("trackNames");
            albumNames = savedInstanceState.getStringArrayList("albumNames");
            albumImageURLs = savedInstanceState.getStringArrayList("albumImageURLs");
            spotifyIDsForTracks = savedInstanceState.getStringArrayList("spotifyIDsForTracks");
        }

        trackAdapter = new TrackArrayAdapter(this, trackNames, albumNames, albumImageURLs);
        topTracksListView.setAdapter(trackAdapter);

        topTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent appInfo = new Intent(TopTrackActivity.this, PlaybackActivity.class);
                appInfo.putExtra("spotifyIDsForTrack", spotifyIDsForTracks.get(position));
                startActivity(appInfo);
            }
        });


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("trackNames", trackNames);
        outState.putStringArrayList("albumNames", albumNames);
        outState.putStringArrayList("albumImageURLs", albumImageURLs);
        outState.putStringArrayList("spotifyIDsForTracks", spotifyIDsForTracks);
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


    private class QueryTopTracksTask extends AsyncTask<String, Integer, Tracks> {
        protected Tracks doInBackground(String ... sportifyID) {
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            Map<String, Object> country = new TreeMap<>();
            country.put("country","US");

            return spotify.getArtistTopTrack(sportifyID[0], country);
        }



        protected void onPostExecute(Tracks results) {
            trackNames.clear();
            albumNames.clear();
            albumImageURLs.clear();

            for(Track track:results.tracks){
                if(track.album.images.size()>0){
                    albumImageURLs.add(track.album.images.get(track.album.images.size()-1).url);
                }else
                    albumImageURLs.add(null);

                trackNames.add(track.name);
                albumNames.add(track.album.name);
                spotifyIDsForTracks.add(track.id);
            }

            if(trackNames.size()>0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        trackAdapter.notifyDataSetChanged();
                    }
                });
            }else{
                Toast.makeText(getApplicationContext(), "No tracks are found for this artist.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
