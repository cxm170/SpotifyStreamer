package com.kiwi.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import java.util.ArrayList;

public class RetainedFragment extends Fragment {

    public static final String RETAINED_FRAGMENT = "RetainedFragment";

    // data to retain
    public ArrayList<String> artistNs;
    public ArrayList<String> artistImageURLs;

    public static RetainedFragment findOrCreateRetainFragment(FragmentManager fm){

        RetainedFragment fragment = (RetainedFragment)fm.findFragmentByTag(RETAINED_FRAGMENT);

        if(fragment == null){

            fragment = new RetainedFragment();
            fm.beginTransaction().add(fragment, RETAINED_FRAGMENT).commit();
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }
}