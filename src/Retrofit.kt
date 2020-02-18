package com.alejandrorios.githubktor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GitHubServices {
    @GET("/user/repos")
    suspend fun getGitHubRepos(@Query("visibility") visibility: String): List<GitHubRepo>

    @POST("/user/repos")
    suspend fun createRepo(@Body repoParams: RepoParams): retrofit2.Response<Unit>
}

fun getGitHubServicesInstance(): GitHubServices {
    val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    return Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
        .create()
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val request = original.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(request)
    }
}