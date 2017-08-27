package com.rajatgoyal.popularmovies_stage2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rajatgoyal.popularmovies_stage2.model.Review;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DisplayActivity extends AppCompatActivity {

    private TextView mTitle, mOverview, mDuration, mRating, mReleaseDate;
    private ImageView mPoster;
    private ProgressBar mLoading;
    private TextView mErrorMessage;
    private LinearLayout mContent;
    private ToggleButton mFavoriteButton;
    private RecyclerView mReviewsList;
    private ReviewsAdapter mReviewsAdapter;

    public static int MOVIE_ID;
    public static String TRAILER_LINK;
    public static Review[] reviews;

    public static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    public static final String MOVIE_DETAIL_BASE_URL = "https://api.themoviedb.org/3/movie/";
    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mTitle = (TextView) findViewById(R.id.tv_title);
        mOverview = (TextView) findViewById(R.id.tv_overview);
        mDuration = (TextView) findViewById(R.id.tv_duration);
        mRating = (TextView) findViewById(R.id.tv_rating);
        mReleaseDate = (TextView) findViewById(R.id.tv_release_date);

        mPoster = (ImageView) findViewById(R.id.iv_poster);

        mLoading = (ProgressBar) findViewById(R.id.pb_loading);
        mErrorMessage = (TextView) findViewById(R.id.tv_error_message);
        mContent = (LinearLayout) findViewById(R.id.ll_content);

        mFavoriteButton = (ToggleButton) findViewById(R.id.favorite_button);

        Intent intent = getIntent();

        MOVIE_ID = intent.getIntExtra("id", 211672);
        TRAILER_LINK = null;

        boolean isFavorite = intent.getBooleanExtra("favorite", false);
        mFavoriteButton.setChecked(isFavorite);

        mReviewsList = (RecyclerView) findViewById(R.id.reviews_list);
        mReviewsList.setLayoutManager(new LinearLayoutManager(this));

        mReviewsAdapter = new ReviewsAdapter(this);
        mReviewsAdapter.setReviewsList(reviews);

        mReviewsList.setAdapter(mReviewsAdapter);

        if (!isOnline()) {
            showErrorMessage();
        } else {
            showLoadingIndicator();
            loadMovieData();
            loadTrailerData();
            loadReviewData();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showLoadingIndicator() {
        mLoading.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
        mContent.setVisibility(View.INVISIBLE);
    }

    public void showErrorMessage() {
        mErrorMessage.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
        mContent.setVisibility(View.INVISIBLE);
    }

    public void showContent() {
        mContent.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    public String buildUrl() {
        Uri builtUri = Uri.parse(MOVIE_DETAIL_BASE_URL + MOVIE_ID).buildUpon()
                .appendQueryParameter("api_key", getString(R.string.api_key))
                .appendQueryParameter("language", "en-US")
                .build();
        return builtUri.toString();
    }

    public void loadMovieData() {

        String url = buildUrl();
        new MovieDataFetchTask().execute(url);

    }

    public void storeMovieData(String title, String overview, String release_date, String rating, String poster_path, int duration) {
        mTitle.setText(title);
        mOverview.setText(overview);
        mReleaseDate.setText(release_date);
        mRating.setText(rating);

        String runtime = String.valueOf(duration) + " minutes";
        mDuration.setText(runtime);

        String full_poster_path = POSTER_BASE_URL + poster_path;
        Picasso.with(DisplayActivity.this).load(full_poster_path).into(mPoster);

        showContent();
    }

    private class MovieDataFetchTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            String url = params[0];

            try {
                RequestQueue requestQueue = Volley.newRequestQueue(DisplayActivity.this);

                JsonObjectRequest request = new JsonObjectRequest(
                        url,
                        null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String overview = response.getString("overview");
                                    String title = response.getString("title");
                                    String release_date = response.getString("release_date");
                                    String rating = response.getString("vote_average");
                                    String poster_path = response.getString("poster_path");
                                    int duration = response.getInt("runtime");

                                    storeMovieData(title, overview, release_date, rating, poster_path, duration);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                );

                requestQueue.add(request);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void loadTrailerData() {
        String url = buildTrailerFetchUrl();
        new TrailerFetchTask().execute(url);
    }

    public String buildTrailerFetchUrl() {
        Uri builtUri = Uri.parse(MOVIE_DETAIL_BASE_URL + MOVIE_ID + "/videos").buildUpon()
                .appendQueryParameter("api_key", getString(R.string.api_key))
                .appendQueryParameter("language", "en-US")
                .build();
        return builtUri.toString();
    }

    private class TrailerFetchTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];

            try {
                RequestQueue requestQueue = Volley.newRequestQueue(DisplayActivity.this);

                JsonObjectRequest request = new JsonObjectRequest(
                        url,
                        null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray results = response.getJSONArray("results");
                                    TRAILER_LINK = YOUTUBE_BASE_URL + results.getJSONObject(0).getString("key");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                );

                requestQueue.add(request);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void loadReviewData() {
        String url = buildReviewFetchUrl();
        new ReviewsFetchTask().execute(url);
    }

    public String buildReviewFetchUrl() {
        Uri builtUri = Uri.parse(MOVIE_DETAIL_BASE_URL + MOVIE_ID + "/reviews").buildUpon()
                .appendQueryParameter("api_key", getString(R.string.api_key))
                .appendQueryParameter("language", "en-US")
                .build();
        return builtUri.toString();
    }

    class ReviewsFetchTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];

            try {
                RequestQueue requestQueue = Volley.newRequestQueue(DisplayActivity.this);

                JsonObjectRequest request = new JsonObjectRequest(
                        url,
                        null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray results = response.getJSONArray("results");
                                    Review[] reviews = new Review[results.length()];
                                    for (int i = 0; i < results.length(); i++) {
                                        JSONObject review = results.getJSONObject(i);
                                        reviews[i] = new Review(review.getString("author"), review.getString("content"));
                                    }
                                    mReviewsAdapter.setReviewsList(reviews);
                                    mReviewsAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DisplayActivity.this, "error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );

                requestQueue.add(request);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void playTrailer(View view) {
        if (TRAILER_LINK != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TRAILER_LINK));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.google.android.youtube");
            startActivity(intent);
        } else {
            loadTrailerData();
        }
    }

    public void shareTrailer(View view) {
        if (TRAILER_LINK != null) {
            ShareCompat.IntentBuilder.from(this)
                    .setChooserTitle("Share with..")
                    .setText(TRAILER_LINK)
                    .setType("text/plain")
                    .startChooser();
        } else {
            loadTrailerData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        reviews = mReviewsAdapter.getReviewsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReviewsAdapter.setReviewsList(reviews);
        mReviewsAdapter.notifyDataSetChanged();
    }
}
