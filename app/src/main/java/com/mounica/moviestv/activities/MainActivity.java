package com.mounica.moviestv.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import com.mounica.moviestv.R;
import com.mounica.moviestv.adapter.NowShowingAdapter;
import com.mounica.moviestv.adapter.PaginationListener;
import com.mounica.moviestv.adapter.PopularMoviesAdapter;
import com.mounica.moviestv.dataobjects.GenreList;
import com.mounica.moviestv.dataobjects.NowShowingMovies;
import com.mounica.moviestv.dataobjects.NowShowingMoviesResults;
import com.mounica.moviestv.helper.GenreMap;
import com.mounica.moviestv.network.MoviesApi;
import com.mounica.moviestv.network.MoviesApiClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  private List<NowShowingMovies> mNowShowingMovies;
  private List<NowShowingMovies> mPopularMovies;
  private ScrollView mScrollView;
  private RecyclerView mRvNowShowing, mRvPopular;
  private NowShowingAdapter mNowShowingAdapter;
  private PopularMoviesAdapter mPopularMoviesAdapter;
  private ProgressBar mProgressBar;
  private LinearLayoutManager mLinearLayoutManager;
  private int mTotalPages = 5;
  private int mCurrentPage = 1;
  private boolean isLastPage = false;
  private boolean isLoading = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Now showing
    mNowShowingMovies = new ArrayList<>();
    mRvNowShowing = findViewById(R.id.rvnowshowing);
    mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    mRvNowShowing.setLayoutManager(mLinearLayoutManager);
    mNowShowingAdapter = new NowShowingAdapter(this, mNowShowingMovies);
    mRvNowShowing.setAdapter(mNowShowingAdapter);
    mRvNowShowing.addOnScrollListener(new PaginationListener(mLinearLayoutManager) {
      @Override
      protected void loadMoreItems() {
        isLoading = true;
        mCurrentPage += mCurrentPage;
        loadNextPage();
      }

      @Override
      public int getTotalPageCount() {
        return mTotalPages;
      }

      @Override
      public boolean isLastPage() {
        return isLastPage;
      }

      @Override
      public boolean isLoading() {
        return isLoading;
      }
    });

    //Popular movies
    mPopularMovies = new ArrayList<>();
    mPopularMoviesAdapter = new PopularMoviesAdapter(this, mPopularMovies);
    mRvPopular = findViewById(R.id.rvpopular);
    mRvPopular.setAdapter(mPopularMoviesAdapter);
    mRvPopular
        .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    //Load Genres
    if (!GenreMap.isGenreListLoaded()) {
      loadGenres();
    }
    //loads first page of Now showing
    loadFirstPage();
    //loads first page of popular movies
    loadFirstPageOfPopularMovies();
  }

  private void loadFirstPageOfPopularMovies() {
    MoviesApiClient.getClient().create(MoviesApi.class)
        .getPopularMovies(getResources().getString(R.string.API_KEY), 1).enqueue(
        new Callback<NowShowingMoviesResults>() {
          @Override
          public void onResponse(Call<NowShowingMoviesResults> call,
              Response<NowShowingMoviesResults> response) {
            if (response.isSuccessful()) {
              for (NowShowingMovies popularMovie : response.body().getResults()) {
                if (popularMovie != null && popularMovie.getBackdropPath() != null) {
                  mPopularMovies.add(popularMovie);
                }
              }
              mPopularMoviesAdapter.notifyDataSetChanged();
            }
          }

          @Override
          public void onFailure(Call<NowShowingMoviesResults> call, Throwable t) {
            Log.i(TAG, t.getMessage());
          }
        });
  }

  private Call<NowShowingMoviesResults> makeAPICall() {
    return MoviesApiClient.getClient().create(MoviesApi.class)
        .getNowShowingMovies(getResources().getString(R.string.API_KEY), mCurrentPage, "US");
  }

  private void loadFirstPage() {
    makeAPICall().enqueue(new Callback<NowShowingMoviesResults>() {
      @Override
      public void onResponse(Call<NowShowingMoviesResults> call,
          Response<NowShowingMoviesResults> response) {
        if (response.isSuccessful()) {
          //total pages declared as 5
//          mTotalPages = response.body().getTotalPages();
          for (NowShowingMovies movieBrief : response.body().getResults()) {
            if (movieBrief != null && movieBrief.getBackdropPath() != null) {
              mNowShowingMovies.add(movieBrief);
            }
          }
          if (mCurrentPage > mTotalPages) {
            isLastPage = true;
          }
          mNowShowingAdapter.notifyDataSetChanged();
          //TODO implement looping through all pages
        }
      }

      @Override
      public void onFailure(Call<NowShowingMoviesResults> call, Throwable t) {
        Log.i(TAG, t.getMessage());
        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG)
            .show();
      }
    });
  }

  private void loadNextPage() {
    makeAPICall().enqueue(new Callback<NowShowingMoviesResults>() {
      @Override
      public void onResponse(Call<NowShowingMoviesResults> call,
          Response<NowShowingMoviesResults> response) {
        if (response.isSuccessful()) {
          Log.i(TAG, "onResponse: " + mCurrentPage);
          isLoading = false;
          for (NowShowingMovies movieBrief : response.body().getResults()) {
            if (movieBrief != null && movieBrief.getBackdropPath() != null) {
              mNowShowingMovies.add(movieBrief);
            }
          }
          if (mCurrentPage == mTotalPages) {
            isLastPage = true;
          }
          mNowShowingAdapter.notifyDataSetChanged();
        }
      }

      @Override
      public void onFailure(Call<NowShowingMoviesResults> call, Throwable t) {
        Log.i(TAG, "onFailure: " + t.getMessage());

      }
    });
  }

  private void loadGenres() {
    MoviesApi genres = MoviesApiClient.getClient().create(MoviesApi.class);
    Call<GenreList> genreList = genres.getGenreList(getResources().getString(R.string.API_KEY));
    genreList.enqueue(new Callback<GenreList>() {
      @Override
      public void onResponse(Call<GenreList> call, Response<GenreList> response) {
        if (response.isSuccessful()) {
          GenreMap.loadGenresList(response.body().getGenres());
        }
      }

      @Override
      public void onFailure(Call<GenreList> call, Throwable t) {
        Log.i(TAG, t.getMessage());
      }
    });
  }
}
