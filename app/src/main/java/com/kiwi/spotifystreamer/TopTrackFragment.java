package com.kiwi.spotifystreamer;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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


public class TopTrackFragment extends ListFragment {

    private String spotifyIDForArtist;
    private ListView topTracksListView;
    private TrackArrayAdapter trackAdapter;

    private ArrayList<String> trackNames = new ArrayList<>();
    private ArrayList<String> albumNames = new ArrayList<>();
    private ArrayList<String> albumImageURLs = new ArrayList<>();

    private ArrayList<String> spotifyIDsForTracks = new ArrayList<>();



    private Button nowplaying;



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
        public void onItemSelectedTrack(ArrayList<String> spotifyIDsForTracks, int position);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelectedTrack(ArrayList<String> spotifyIDsForTracks, int position) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TopTrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spotifyIDForArtist = getArguments().getString("spotifyIDForArtist");


        if(savedInstanceState == null){


            new QueryTopTracksTask().execute(spotifyIDForArtist);
            Log.e("error", "savenInstanceState: null");

        }else{
            trackNames = savedInstanceState.getStringArrayList("trackNames");
            albumNames = savedInstanceState.getStringArrayList("albumNames");
            albumImageURLs = savedInstanceState.getStringArrayList("albumImageURLs");
            spotifyIDsForTracks = savedInstanceState.getStringArrayList("spotifyIDsForTracks");
            Log.e("error", "savenInstanceState: nonnull");
        }


        // TODO: replace with a real list adapter.

        trackAdapter = new TrackArrayAdapter(getActivity(), trackNames, albumNames, albumImageURLs);
        setListAdapter(trackAdapter);




    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        mCallbacks.onItemSelectedTrack(spotifyIDsForTracks, position);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }

        outState.putStringArrayList("trackNames", trackNames);
        outState.putStringArrayList("albumNames", albumNames);
        outState.putStringArrayList("albumImageURLs", albumImageURLs);
        outState.putStringArrayList("spotifyIDsForTracks", spotifyIDsForTracks);

        Log.e("error", "artistNs:" + trackNames.toString());

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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        trackAdapter.notifyDataSetChanged();
                    }
                });
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "No tracks are found for this artist.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }



}
