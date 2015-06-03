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

public class ArtistArrayAdapter extends ArrayAdapter<String> {
    private  Context context;
    private  List<String> artistNames;
    private  List<String> artistImageURLs;

    public ArtistArrayAdapter(Context context, List<String> artistNames, List<String> artistImageURLs) {
        super(context, -1, artistNames);
        this.context = context;
        this.artistNames = artistNames;
        this.artistImageURLs = artistImageURLs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.artist, parent, false);
        TextView artistNameTextView = (TextView) rowView.findViewById(R.id.artist_name);
        ImageView artistImageView = (ImageView) rowView.findViewById(R.id.artist_image);
        artistNameTextView.setText(artistNames.get(position));
        if (artistImageURLs.get(position) != null) Picasso.with(context).load(artistImageURLs.get(position)).into(artistImageView);

        return rowView;
    }
}
