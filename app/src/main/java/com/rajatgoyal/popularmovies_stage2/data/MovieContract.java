package com.rajatgoyal.popularmovies_stage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by rajat on 28/8/17.
 */

public class MovieContract {

    public static final String AUTHORITY = "com.rajatgoyal.popularmovies_stage2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
    }
}
