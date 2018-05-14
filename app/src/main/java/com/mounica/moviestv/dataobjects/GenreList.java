package com.mounica.moviestv.dataobjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by mounicachikkam on 5/10/18.
 */

public class GenreList {

  @SerializedName("genres")
  @Expose
  private List<Genre> genres = null;

  public List<Genre> getGenres() {
    return genres;
  }

  public void setGenres(List<Genre> genres) {
    this.genres = genres;
  }

  public GenreList withGenres(List<Genre> genres) {
    this.genres = genres;
    return this;
  }

}
