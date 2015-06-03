package com.kiwi.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrackArrayAdapter extends ArrayAdapter<String> {
    private  Context context;
    private  List<String> trackNames;
    private  List<String> albumNames;
    private  List<String> albumImageURLs;

    public TrackArrayAdapter(Context context, List<String> trackNames, List<String> albumNames, List<String> albumImageURLs) {
        super(context, -1, trackNames);
        this.context = context;
        this.trackNames = trackNames;
        this.albumNames = albumNames;
        this.albumImageURLs = albumImageURLs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        TrackViewHolder trackViewHolder;

        if (vi == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.track, parent, false);
            trackViewHolder = new TrackViewHolder();
            trackViewHolder.trackName = (TextView) vi.findViewById(R.id.track_name);
            trackViewHolder.albumName = (TextView) vi.findViewById(R.id.album_name);
            trackViewHolder.albumImage = (ImageView) vi.findViewById(R.id.album_image);
            vi.setTag(trackViewHolder);
        }else{
            trackViewHolder = (TrackViewHolder) vi.getTag();
        }


        trackViewHolder.trackName.setText(trackNames.get(position));
        trackViewHolder.albumName.setText(albumNames.get(position));
        if (albumImageURLs.get(position) != null) Picasso.with(context).load(albumImageURLs.get(position)).into(trackViewHolder.albumImage);

        return vi;
    }


    static class TrackViewHolder {
        TextView trackName;
        TextView albumName;
        ImageView albumImage;

    }
}
