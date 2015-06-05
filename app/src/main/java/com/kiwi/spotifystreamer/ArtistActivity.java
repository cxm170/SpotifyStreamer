package com.kiwi.spotifystreamer;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


public class ArtistActivity extends AppCompatActivity implements ArtistFragment.Callbacks, TopTrackFragment.Callbacks{

    EditText editText;


    private final String ARTIST_FRAGMENT = "com.kiwi.artist_fragment";
    private final String PLAPBACK_FRAGMENT_TAG = "PLAYBACK_FRAGMENT";

    ArtistFragment artistFragment;

    private Button nowplaying;

    private boolean mTwoPane = false;

    Toolbar toolbar;

    private ArrayList<String> artistImageURLs ;
    private ArrayList<String> artistNs;

    private ArtistArrayAdapter artistAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_activity);

        if(findViewById(R.id.track_list_view_container) != null){
            mTwoPane = true;
        }

        Log.e("error", "mTwoPane = " + mTwoPane);

         toolbar = (Toolbar) findViewById(R.id.toolbar_artist); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);


        nowplaying = (Button) findViewById(R.id.nowplaying);


            nowplaying.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CurrentPlaybackHelper.playingNow) {

                        if(mTwoPane){
                            PlaybackFragment playbackFragment = PlaybackFragment.newInstance();

                            // In case this activity was started with special instructions from an
                            // Intent, pass the Intent's extras to the fragment as arguments

                            Bundle bundle = new Bundle();

                            bundle.putStringArrayList("spotifyIDsForTracks", CurrentPlaybackHelper.spotifyIDsForTracks);

                            bundle.putInt("position", CurrentPlaybackHelper.currentPosition);


                                bundle.putBoolean("isSameTrack", true);


                            bundle.putBoolean("showDialog", true);

                            playbackFragment.setArguments(bundle);



                            playbackFragment.show(getFragmentManager(), PLAPBACK_FRAGMENT_TAG);
                        }else {
                            Intent appInfo = new Intent(ArtistActivity.this, PlaybackActivity.class);
                            appInfo.putStringArrayListExtra("spotifyIDsForTracks", CurrentPlaybackHelper.spotifyIDsForTracks);

                            appInfo.putExtra("position", CurrentPlaybackHelper.currentPosition);


                            startActivity(appInfo);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No songs are playing now.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });



        editText = (EditText) findViewById(R.id.search);


        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                        replaceArtistFragment(editText.getText().toString());

                    handled = true;
                }
                return handled;
            }
        });

        RetainedFragment retainFragment = RetainedFragment.findOrCreateRetainFragment(getFragmentManager());

        artistNs = retainFragment.artistNs;
        artistImageURLs = retainFragment.artistImageURLs;

        if(artistNs==null){

            artistNs = new ArrayList<>();
            artistImageURLs = new ArrayList<>();

            retainFragment.artistNs = artistNs;
            retainFragment.artistImageURLs = artistImageURLs;
        }

        // create fragments

            artistFragment = (ArtistFragment) getFragmentManager().findFragmentByTag(ARTIST_FRAGMENT);


        artistAdapter = new ArtistArrayAdapter(this, artistNs, artistImageURLs);


        if(artistFragment!=null)
            artistFragment.setListAdapter(artistAdapter);








    }


    @Override
    protected void onStart() {

        // TESTING: If device orientation has changed List<ImageBean> was saved
        // with a RetainedFragment. Seed the adapter with the retained
        // List.
        artistAdapter.notifyDataSetChanged();
        super.onStart();
    }



//    private void startArtistFragment(String artistName){
//        // Create a new Fragment to be placed in the activity layout
//        ArtistFragment artistNewFragment = new ArtistFragment();
//
//        // In case this activity was started with special instructions from an
//        // Intent, pass the Intent's extras to the fragment as arguments
//
//        Bundle bundle = new Bundle();
//
//        bundle.putString("artistName", artistName);
//
//        artistNewFragment.setArguments(bundle);
//
//        // Add the fragment to the 'fragment_container' FrameLayout
//        getFragmentManager().beginTransaction()
//                .add(R.id.artist_list_view_container, artistNewFragment, ARTIST_FRAGMENT).commit();
//    }

    private void replaceArtistFragment(String artistName){
        // Create a new Fragment to be placed in the activity layout
        ArtistFragment artistNewFragment = new ArtistFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments

        Bundle bundle = new Bundle();

        bundle.putString("artistName", artistName);

        artistNewFragment.setArguments(bundle);

        // Add the fragment to the 'fragment_container' FrameLayout
        getFragmentManager().beginTransaction()
                .replace(R.id.artist_list_view_container, artistNewFragment, ARTIST_FRAGMENT).commit();


    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Android automatically saves visible fragments here. (?)

        super.onSaveInstanceState(outState);



        artistFragment = (ArtistFragment) getFragmentManager().findFragmentByTag(ARTIST_FRAGMENT);

        if(artistFragment!=null) {
            RetainedFragment retainFragment = RetainedFragment.findOrCreateRetainFragment(getFragmentManager());
            retainFragment.artistNs = artistFragment.artistNs;
            retainFragment.artistImageURLs = artistFragment.artistImageURLs;
//            Log.e("error", "retain fragment: " + retainFragment.artistNs);
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
    public void onItemSelected(String spotifyIDForArtist, String artistName){


        if(mTwoPane){
            Bundle bundle = new Bundle();

            bundle.putString("spotifyIDForArtist", spotifyIDForArtist);


            TopTrackFragment firstFragment = new TopTrackFragment();

            firstFragment.setArguments(bundle);

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .replace(R.id.track_list_view_container, firstFragment).addToBackStack(null).commit();

            toolbar.setSubtitle(artistName);
        }else {


            Intent appInfo = new Intent(ArtistActivity.this, TopTrackActivity.class);
            appInfo.putExtra("spotifyIDForArtist", spotifyIDForArtist);
            appInfo.putExtra("artistName", artistName);
            startActivity(appInfo);
        }
    }


    @Override
    public void onItemSelectedTrack(ArrayList<String> spotifyIDsForTracks, int position){


        // Create a new Fragment to be placed in the activity layout
        PlaybackFragment playbackFragment = PlaybackFragment.newInstance();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments

        Bundle bundle = new Bundle();

        bundle.putStringArrayList("spotifyIDsForTracks", spotifyIDsForTracks);

        bundle.putInt("position", position);

        if (CurrentPlaybackHelper.currentPosition == position &&
                (CurrentPlaybackHelper.previousArtist==null ||
                        CurrentPlaybackHelper.previousArtist.equals(CurrentPlaybackHelper.currentArtist))){
            bundle.putBoolean("isSameTrack", true);
        } else {
            bundle.putBoolean("isSameTrack", false);

        }

        bundle.putBoolean("showDialog", true);

        playbackFragment.setArguments(bundle);



        playbackFragment.show(getFragmentManager(), PLAPBACK_FRAGMENT_TAG);

    }

}


