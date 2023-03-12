package org.avvento.apps.onlineradio.api;

import org.avvento.apps.onlineradio.explore.Adverts;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("Muta-Jonathan/AvventoRadio-android-Apk/UPDATE-02/adverts")
    Call<Adverts> getAdverts(
            @Query("country") String country);
}
