package com.alejandrorios.githubktor

import com.google.gson.annotations.SerializedName

data class GitHubRepo(
    val id: String,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val description: String
)

data class RepoParams(
    val name: String,
    val description: String,
    val private: Boolean = false
)