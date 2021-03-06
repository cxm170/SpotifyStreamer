package com.kiwi.spotifystreamer;

import android.app.Activity;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by jiweili on 5/6/15.
 */
public class ArtistFragment extends ListFragment {
    public ArrayList<String> artistImageURLs = new ArrayList<>();
    public ArrayList<String> artistNs = new ArrayList<>();
    private ArrayList<String> spotifyIDs = new ArrayList<>();

    private ArtistArrayAdapter artistAdapter;

    private String artistName;



    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String spotifyIDForArtist, String artistName);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String spotifyIDForArtist, String artistName) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        artistName = getArguments().getString("artistName");


//        Log.e("error", "artistName = " + artistName);






//        if(savedInstanceState != null)
//        Log.e("error", "savenInstanceState: " +savedInstanceState.toString());



//        else{
//            artistNs = savedInstanceState.getStringArrayList("artistNs");
//            artistImageURLs = savedInstanceState.getStringArrayList("artistImageURLs");
//            spotifyIDs = savedInstanceState.getStringArrayList("spotifyIDs");
//            Log.e("error", "savenInstanceState: nonnull");
//            Log.e("error", "artistNs:" + artistNs.toString());
//        }


            queryArtists(artistName);

        artistAdapter = new ArtistArrayAdapter(getActivity(), artistNs, artistImageURLs);
        setListAdapter(artistAdapter);



    }




    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




//        Log.e("error", "Artist Name:" + artistNs);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(spotifyIDs.get(position), artistNs.get(position));

        CurrentPlaybackHelper.previousArtist = CurrentPlaybackHelper.currentArtist;

        CurrentPlaybackHelper.currentArtist = artistNs.get(position);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);



            outState.putStringArrayList("artistNs", artistNs);
            outState.putStringArrayList("artistImageURLs", artistImageURLs);
            outState.putStringArrayList("spotifyIDs", spotifyIDs);

            Log.e("error", "Saved artistNs:" + artistNs.toString());
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public void queryArtists(String artistName){

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {


                        artistAdapter.notifyDataSetChanged();

                    }
                });
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "No artists are found to match the name.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}


