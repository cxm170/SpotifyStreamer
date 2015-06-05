package com.kiwi.spotifystreamer;

import java.util.ArrayList;

/**
 * Created by jiweili on 5/6/15.
 */
public class CurrentPlaybackHelper {
    public static int currentPosition = -1;
    public static ArrayList<String> spotifyIDsForTracks;
    public static String currentArtist = "placeholder";
    public static String previousArtist;
    public static boolean playingNow = false;
}
