package com.rajatgoyal.popularmovies_stage2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rajatgoyal.popularmovies_stage2.model.Trailer;

import static com.rajatgoyal.popularmovies_stage2.DisplayActivity.YOUTUBE_BASE_URL;

/**
 * Created by rajat on 29/8/17.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

    private Trailer[] trailersList;
    private Context context;

    public TrailersAdapter(Context context) {
        this.context = context;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.trailerName.setText(trailersList[position].getName());

        final String youtube_url = YOUTUBE_BASE_URL + trailersList[position].getKey();

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtube_url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.google.android.youtube");
                context.startActivity(intent);
            }
        });

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, youtube_url);
                context.startActivity(Intent.createChooser(sharingIntent, "Share with.."));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (trailersList == null) return 0;
        return trailersList.length;
    }

    public void setTrailersList(Trailer[] trailers) {
        trailersList = trailers;
    }

    public Trailer[] getTrailersList() {
        return trailersList;
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {

        TextView trailerName;
        ImageButton playButton, shareButton;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            trailerName = (TextView) itemView.findViewById(R.id.trailer_name_text_view);
            playButton = (ImageButton) itemView.findViewById(R.id.playButton);
            shareButton = (ImageButton) itemView.findViewById(R.id.shareButton);
        }
    }
}
