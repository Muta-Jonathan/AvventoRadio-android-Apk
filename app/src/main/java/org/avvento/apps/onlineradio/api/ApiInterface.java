package org.avvento.apps.onlineradio.api;

import org.avvento.apps.onlineradio.explore.Adverts;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("Muta-Jonathan/Test/main/news")
    Call<Adverts> getAdverts(
    );
}
