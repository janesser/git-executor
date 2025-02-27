package de.esserjan.edu.imbecile.test.git_https_backend.http4k

import de.esserjan.edu.imbecile.test.git_https_backend.HttpsUndertow
import de.esserjan.edu.imbecile.test.git_https_backend.MapIdentityManager
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.*
import java.io.ByteArrayInputStream
import java.nio.file.Path


fun getServer(
    projectRoot: Path,
    servletPath: String,
    serverHost: String,
    serverPort: Int,
    serverKeyStorePassword: String,
    userMap: Map<String, String> = emptyMap()
): Http4kServer {
    val gitHttpBackendProxy = GitHttpBackendProxy(projectRoot.toAbsolutePath().toString())

    val appHttp: HttpHandler = { req: Request ->
        val genericResponse =
            gitHttpBackendProxy.serveGitBackend(
                req.uri.path.substring(1 + servletPath.length),
                req.method.name,
                req.uri.query,
                req.header("Content-Type")
            )

        val status = Status.fromCode(genericResponse.responseCode)
        val headers = genericResponse.headers.toList()
        val bodyInputStream = ByteArrayInputStream(genericResponse.body)

        if (status != null)
            Response(status)
                .headers(headers)
                .body(bodyInputStream)
        else
            Response(Status.INTERNAL_SERVER_ERROR)
    }

    return appHttp.asServer(
        HttpsUndertow4kServerConfig(
            HttpsUndertow(
                serverHost,
                serverPort,
                serverKeyStorePassword.toCharArray(),
                MapIdentityManager(userMap)
            )
        )
    )
}
