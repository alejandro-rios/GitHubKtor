package com.alejandrorios.githubktor

import io.ktor.auth.OAuthServerSettings
import io.ktor.http.HttpMethod
import java.io.File

var accessToken: String? = null
val twitterOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "github",
    authorizeUrl = "https://github.com/login/oauth/authorize",
    accessTokenUrl = "https://github.com/login/oauth/access_token",
    requestMethod = HttpMethod.Post,
    clientId = OAUTH_CLIENT_ID,
    clientSecret = OAUTH_CLIENT_SECRET,
    defaultScopes = listOf("repo"),
    accessTokenRequiresBasicAuth = false
)

fun saveOAuthToken(token: String) {
    accessToken = token
    File(OAUTH_TOKEN_STORAGE_URI).writeText(token)
}

fun loadStoredOAuthToken(): String? {
    return try {
        val fileText = File(OAUTH_TOKEN_STORAGE_URI).readText()
        when {
            fileText.isNotEmpty() -> fileText
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}