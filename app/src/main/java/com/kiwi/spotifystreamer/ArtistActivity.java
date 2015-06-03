package com.kiwi.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


public class ArtistActivity extends AppCompatActivity {

    EditText editText;
    ListView artistListView;

    ArrayList<String> artistImageURLs = new ArrayList<>();
    ArrayList<String> artistNs = new ArrayList<>();
    ArrayList<String> spotifyIDs = new ArrayList<>();

    ArtistArrayAdapter artistAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_artist); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.search);



        artistListView = (ListView) findViewById(R.id.artist_list_view);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    queryArtists(editText.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });


        if(savedInstanceState != null) {
            artistNs = savedInstanceState.getStringArrayList("artistNs");
            artistImageURLs = savedInstanceState.getStringArrayList("artistImageURLs");
            spotifyIDs = savedInstanceState.getStringArrayList("spotifyIDs");
        }

        artistAdapter = new ArtistArrayAdapter(this, artistNs, artistImageURLs);
        artistListView.setAdapter(artistAdapter);


        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent appInfo = new Intent(ArtistActivity.this, TopTrackActivity.class);
                appInfo.putExtra("spotifyIDForArtist", spotifyIDs.get(position));
                appInfo.putExtra("artistName", artistNs.get(position));
                startActivity(appInfo);
            }
        });

    }







    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("artistNs", artistNs);
        outState.putStringArrayList("artistImageURLs", artistImageURLs);
        outState.putStringArrayList("spotifyIDs", spotifyIDs);

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


    private void queryArtists(String artistName){

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);


        new QueryArtistsTask().execute(artistName);




    }



    private class QueryArtistsTask extends AsyncTask<String, Integer, ArtistsPager> {
        protected ArtistsPager doInBackground(String ... artistName) {
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();
            return spotify.searchArtists(artistName[0]);
        }



        protected void onPostExecute(ArtistsPager results) {
           artistImageURLs.clear();
            artistNs.clear();
            spotifyIDs.clear();

            for(Artist artist:results.artists.items){
                String spotifyID = artist.id;
                Image image;
                if(artist.images.size()>0){
                    image = artist.images.get(artist.images.size()-1);
                    String imageURL = image.url;
                    artistImageURLs.add(imageURL);
                }
                else artistImageURLs.add(null);
                String artistN = artist.name;


                artistNs.add(artistN);
                spotifyIDs.add(spotifyID);

            }

            if(artistNs.size()>0) {
                runOnUiThread(new Runnable() {
                    public void run() {


                        artistAdapter.notifyDataSetChanged();

                    }
                });
            }else{
                Toast.makeText(getApplicationContext(), "No artists are found to match the name.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



}
