package jp.co.pannacotta.lunch_app

import jp.co.pannacotta.lunch_app.model.Gourmet
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Hotpepperapi {
    @GET("/hotpepper/gourmet/v1/")
    fun webservice(
            @Query("key") key: String?,
            @Query("lat") lat: String?,
            @Query("lng") lng: String?,
            @Query("range") range: String?,
            @Query("lunch") lunch: String?,
            @Query("count") count: String?,
            @Query("format") format: String?
            //            @Query("name") String name,
            //            @Query("genre") String genre,
            //            @Query("free_food") String free_food,
            //            @Query("private_room") String private_room,
            //            @Query("title") String title
    ): Call<Gourmet?>?
}