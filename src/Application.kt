package com.alejandrorios.githubktor

import com.google.gson.Gson
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.html.respondHtml
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.html.*
import org.apache.commons.text.StringEscapeUtils

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    checkRequiredConstants()

    accessToken = loadStoredOAuthToken()

    install(DefaultHeaders)
    install(CallLogging)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Authentication) {
        oauth("github-oauth") {
            client = HttpClient(Apache)
            providerLookup = { twitterOauthProvider }
            urlProvider = { redirectUrl("/login") }
        }
    }

    install(Routing) {
        handleStaticResources()
        handleGetRoutes()
        handleAuthenticatedRoutes()
    }
}

fun checkRequiredConstants() {
    if (OAUTH_TOKEN_STORAGE_URI.isEmpty() ||
        OAUTH_CLIENT_ID.isEmpty() ||
        OAUTH_CLIENT_SECRET.isEmpty()
    ) {
        throw IllegalStateException("You need to setup some basic constants first. Please see Constants.kt")
    }
}

private fun handleError(error: String) {
    println(error)
}

private fun getGitHubData(): List<GitHubRepo>? {
    var repos: List<GitHubRepo>? = null

    runBlocking {
        launch {
            try {
                repos = withContext(Dispatchers.IO) {
                    getGitHubServicesInstance()
                        .getGitHubRepos("public")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                handleError("Oops!, something went wrong")
            }
        }
    }

    return repos
}

private fun Route.handleGetRoutes() {
    get("/") {
        call.respondHtml {
            head {
                styleLink("/static-resources/styles.css")
                title {
                    +"GitHub Dashboard"
                }
            }
            body {
                imageInput {
                    width = "400px";
                    src = "https://github.githubassets.com/images/modules/explore/social.jpg"
                }
                p {
                    ul {
                        li {
                            +"Is authenticated: ${accessToken?.isNotEmpty() ?: false}"
                        }
                        li {
                            a(href = "/login") {
                                +"Login"
                            }
                        }
                        li {
                            a(href = "/dashboard") {
                                +"GitHub Dashboard HTML"
                            }
                        }
                        li {
                            a(href = "/prettier_dashboard") {
                                +"GitHub Prettier Dashboard"
                            }
                        }
                        li {
                            a(href = "/dashboard.json") {
                                +"GitHub Dashboard RESTful API"
                            }
                        }
                    }
                }
            }
        }
    }

    get("/dashboard") {
        val gitHubData = getGitHubData()

        call.respondHtml {
            head {
                styleLink("/static-resources/styles.css")
                title {
                    +"GitHub Dashboard"
                }
            }
            body {
                imageInput {
                    width = "400px"
                    src = "https://i.kym-cdn.com/photos/images/original/001/704/393/8d2.png"
                }
                p {
                    h1 { +"GitHub Dashboard" }
                    h2 { +"Repos:" }
                    h3 { a("/repos/new") { +"Create repo" } }
                    ul {
                        gitHubData?.forEach {
                            li {
                                p {
                                    +StringEscapeUtils.unescapeHtml4(it.fullName)
                                    br {
                                        it.description?.let { desc ->
                                            +StringEscapeUtils.unescapeHtml4(desc)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/dashboard.json") {
        val gitHubData = getGitHubData()
        val gson = Gson()

        call.respondText {
            gson.toJson(gitHubData)
        }
    }

    get("/prettier_dashboard") {
        val gitHubData = getGitHubData()

        call.respond(
            FreeMarkerContent(
                "index.ftl",
                mapOf("GitHubRepos" to gitHubData),
                "e"
            )
        )
    }

    get("/repos/new") {
        call.respondHtml {
            head {
                styleLink("/static-resources/styles.css")
            }
            body {
                imageInput {
                    width = "400px";
                    src =
                        "https://sdtimes.com/wp-content/uploads/2017/10/29682337-83f3017e-88bf-11e7-846c-138e9639b87f.png"
                }
                form("/repos/new", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                    acceptCharset = "utf-8"
                    p {
                        label { +"Repo name: " }
                        textInput { name = "name" }
                    }
                    p {
                        label { +"Description: " }
                        textInput { name = "description" }
                    }
                    p {
                        submitInput { value = "create repo" }
                    }
                }
                footer {
                    h6 { +"https://sdtimes.com/wp-content/uploads/2017/10/29682337-83f3017e-88bf-11e7-846c-138e9639b87f.png" }
                }
            }
        }
    }

    post("/repos/new") {
        val params = call.receiveParameters()
        val repoParams = RepoParams(name = params["name"]!!, description = params["description"]!!, private = false)

        runBlocking {
            launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        getGitHubServicesInstance().createRepo(repoParams)
                    }

                    if (response.isSuccessful) {
                        call.respondRedirect("/dashboard")
                    } else {
                        handleError(response.errorBody().toString())
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    handleError("Oops!, something went wrong")
                }
            }
        }
    }
}

private fun Route.handleAuthenticatedRoutes() {
    authenticate("github-oauth") {
        route("/login") {
            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: error("No principal")

                saveOAuthToken(principal.accessToken)

                call.respondRedirect("/")
            }
        }
    }
}

private fun Route.handleStaticResources() {
    static("static-resources") {
        resources("css")
    }
}

fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}
