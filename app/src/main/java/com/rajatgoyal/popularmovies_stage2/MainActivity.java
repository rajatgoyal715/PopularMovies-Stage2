package com.rajatgoyal.popularmovies_stage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rajatgoyal.popularmovies_stage2.data.MovieContract;
import com.rajatgoyal.popularmovies_stage2.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MovieItemClickListener {

    private RecyclerView rv_movies;
    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager layoutManager;

    private ProgressBar mProgressLoading;

    private static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular";
    private static final String TOP_RATED_MOVIES_URL = "https://api.themoviedb.org/3/movie/top_rated";

    private static final String lang = "en-US";
    private static final int page = 1;

    private final static String API_PARAM = "api_key";
    private final static String LANG_PARAM = "language";
    private final static String PAGE_PARAM = "page";

    public static final String TAG = "MainActivity";

    // 0 for popular movies and 1 for top rated movies and 2 for favourites
    private static int POPULAR_OR_TOP_RATED;

    private static SharedPreferences sharedPref;

    public static final String MY_PREFS = "popular";

    public static Movie[] moviesList;

    Parcelable mListState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getChoice();

        mProgressLoading = (ProgressBar) findViewById(R.id.pb_loading);

        rv_movies = (RecyclerView) findViewById(R.id.rv_movies);

        layoutManager = new GridLayoutManager(this, 2);
        rv_movies.setLayoutManager(layoutManager);

        mMoviesAdapter = new MoviesAdapter(this);
        rv_movies.setAdapter(mMoviesAdapter);

        if (savedInstanceState == null) {

            loadMoviesData();

        } else {

            int ids[] = savedInstanceState.getIntArray("id");
            String posters[] = savedInstanceState.getStringArray("poster");

            moviesList = new Movie[ids.length];
            for (int i = 0; i < ids.length; i++) {
                Log.d(TAG, "onCreate: " + ids[i] + " and " + posters[i]);
                moviesList[i] = new Movie(ids[i], posters[i]);
            }

            mMoviesAdapter.setMovieData(moviesList);
            mMoviesAdapter.notifyDataSetChanged();
            showMoviesData();

            mListState = savedInstanceState.getParcelable("position");
            if (mListState != null) {
                layoutManager.onRestoreInstanceState(mListState);
            }
        }
    }

    public void getChoice() {
        Context context = MainActivity.this;
        sharedPref = context.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);

        POPULAR_OR_TOP_RATED = sharedPref.getInt("choice", 0);
    }

    @Override
    public void onClick(int id) {
        Intent intent = new Intent(this, DisplayActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    private void loadMoviesData() {

        showLoadingIndicator();

        if (POPULAR_OR_TOP_RATED != 2) {
            URL moviesUrl = buildUrl();
            new MoviesFetchTask().execute(moviesUrl);
        } else {
            new FavouriteMoviesFetchTask().execute();
        }
    }

    public class FavouriteMoviesFetchTask extends AsyncTask<Void, Void, Void> {

        Movie[] movies;

        @Override
        protected Void doInBackground(Void... params) {
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            Log.d(TAG, "doInBackground: cursor: " + cursor.getCount());

            int count = cursor.getCount();
            movies = new Movie[cursor.getCount()];
            if (count == 0) {
                return null;
            }

            if (cursor.moveToFirst()) {
                do {
                    int movie_id = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                    String name = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME));
                    String poster_path = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));

                    Log.d(TAG, "doInBackground: " + name + " and " + poster_path);

                    movies[cursor.getPosition()] = new Movie(movie_id, poster_path);
                } while (cursor.moveToNext());
            }

            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mMoviesAdapter.setMovieData(movies);
            mMoviesAdapter.notifyDataSetChanged();
            showMoviesData();
        }
    }

    public void showLoadingIndicator() {
        mProgressLoading.setVisibility(View.VISIBLE);
        rv_movies.setVisibility(View.INVISIBLE);
    }

    public void showMoviesData() {
        mProgressLoading.setVisibility(View.INVISIBLE);
        rv_movies.setVisibility(View.VISIBLE);
    }

    public URL buildUrl() {
        String BASE_URL = (POPULAR_OR_TOP_RATED == 0) ? POPULAR_MOVIES_URL : TOP_RATED_MOVIES_URL;

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(API_PARAM, getString(R.string.api_key))
                .appendQueryParameter(LANG_PARAM, lang)
                .appendQueryParameter(PAGE_PARAM, String.valueOf(page))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public class MoviesFetchTask extends AsyncTask<URL, Void, Movie[]> {

        @Override
        protected Movie[] doInBackground(URL... params) {
            if (params.length == 0) {
                return null;
            }

            URL moviesRequestUrl = params[0];
            Log.d(TAG, "doInBackground: moviesRequestUrl: " + moviesRequestUrl.toString());

            try {
                RequestQueue mRequestQueue = Volley.newRequestQueue(MainActivity.this);
                mRequestQueue.start();

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        moviesRequestUrl.toString(),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    JSONArray results = response.getJSONArray("results");

                                    int id;
                                    String poster_path;

                                    Movie[] movies = new Movie[results.length()];

                                    for (int i = 0; i < results.length(); i++) {
                                        id = results.getJSONObject(i).getInt("id");
                                        poster_path = results.getJSONObject(i).getString("poster_path");

                                        movies[i] = new Movie(id, poster_path);
                                    }

                                    mMoviesAdapter.setMovieData(movies);
                                    mMoviesAdapter.notifyDataSetChanged();
                                    showMoviesData();

                                    rv_movies.scrollToPosition(0);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                            }
                        }
                );

                mRequestQueue.add(jsonObjectRequest);

            } catch (Exception e) {
                Log.d(TAG, "doInBackground: error: " + e.getMessage());
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem popularMenuItem = menu.findItem(R.id.action_popular);
        MenuItem topRatedMenuItem = menu.findItem(R.id.action_top_rated);
        MenuItem favoriteMenuItem = menu.findItem(R.id.action_favourites);

        if (POPULAR_OR_TOP_RATED == 0) {
            popularMenuItem.setChecked(true);
        } else if (POPULAR_OR_TOP_RATED == 1) {
            topRatedMenuItem.setChecked(true);
        } else {
            favoriteMenuItem.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        int earlier = POPULAR_OR_TOP_RATED;

        if (id == R.id.action_popular) {
            item.setChecked(true);
            POPULAR_OR_TOP_RATED = 0;
        }

        if (id == R.id.action_top_rated) {
            item.setChecked(true);
            POPULAR_OR_TOP_RATED = 1;
        }

        if (id == R.id.action_favourites) {
            item.setChecked(true);
            POPULAR_OR_TOP_RATED = 2;
        }

        updateSharedPref();

        if (earlier != POPULAR_OR_TOP_RATED) {
            loadMoviesData();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateSharedPref() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("choice", POPULAR_OR_TOP_RATED);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mListState = layoutManager.onSaveInstanceState();
        outState.putParcelable("position", mListState);

        moviesList = mMoviesAdapter.getMovieData();
        int[] moviesId = new int[moviesList.length];
        String[] moviesPosters = new String[moviesList.length];
        for (int i = 0; i < moviesList.length; i++) {
            moviesId[i] = moviesList[i].getId();
            moviesPosters[i] = moviesList[i].getPoster_path();
        }
        outState.putIntArray("id", moviesId);
        outState.putStringArray("poster", moviesPosters);

        super.onSaveInstanceState(outState);
    }
}
