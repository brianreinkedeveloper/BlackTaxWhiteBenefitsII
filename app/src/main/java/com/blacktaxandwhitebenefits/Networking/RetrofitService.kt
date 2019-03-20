package com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetBlogService {

    // Gets everything after the base URL.
    @GET("/wp-json/wp/v2/posts?_embed")

    // Calls top-level object 'BlogArticles'.
    fun getAllArticles(
        @Query("page") page: String
    ): Call<List<BlogArticles>>
}
