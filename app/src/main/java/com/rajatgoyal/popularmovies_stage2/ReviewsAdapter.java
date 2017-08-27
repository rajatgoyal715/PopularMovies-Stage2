package com.rajatgoyal.popularmovies_stage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rajatgoyal.popularmovies_stage2.model.Review;

/**
 * Created by rajat on 28/8/17.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private Review[] reviewsList;
    private Context context;

    public ReviewsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.review_list_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.mAuthor.setText(reviewsList[position].getAuthor());
        holder.mContent.setText(reviewsList[position].getContent());
    }

    @Override
    public int getItemCount() {
        if (reviewsList == null) return 0;
        return reviewsList.length;
    }

    public void setReviewsList(Review[] reviews) {
        reviewsList = reviews;
    }

    public Review[] getReviewsList() {
        return reviewsList;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder{

        TextView mAuthor, mContent;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            mAuthor = (TextView) itemView.findViewById(R.id.review_author);
            mContent = (TextView) itemView.findViewById(R.id.review_content);
        }
    }
}
