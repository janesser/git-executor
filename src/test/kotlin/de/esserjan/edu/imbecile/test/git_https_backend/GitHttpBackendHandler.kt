package de.esserjan.edu.imbecile.test.git_https_backend

import de.esserjan.edu.imbecile.util.SubProcess
import de.esserjan.edu.imbecile.util.SubProcessBuilder
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.nio.file.Path

class GitHttpBackendHandler(private val webRoot: String, private val projectRoot: Path) : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.toSubProcess()
    }

    private fun HttpServerExchange.toSubProcess(): SubProcess {
        val pb = SubProcessBuilder(
            listOf("/usr/lib/git-core/git-http-backend"),
            null,
            mapOf(
                "GIT_PROJECT_ROOT" to projectRoot.toAbsolutePath().toString(),
                "GIT_HTTP_EXPORT_ALL" to "",
                "PATH_INFO" to requestPath.substring(1 + webRoot.length),
                "REQUEST_METHOD" to requestMethod.toString(),
                "QUERY_STRING" to queryString,
                "CONTENT_TYPE" to requestHeaders.get("Content-Type")?.first().orEmpty(),
                "REMOTE_USER" to securityContext.authenticatedAccount?.principal?.name.orEmpty(),
                "GIT_PROTOCOL" to requestHeaders.get("Git-Protocol")?.first().orEmpty(),
            )
        )

        return runBlocking {
            val p = pb.start()

            startBlocking()
            launch {
                while (!isComplete) {
                    inputStream.transferTo(p.outputStream())
                    yield()
                }
            }

            launch {
                while (!isComplete) {
                    p.inputStream().transferTo(outputStream)
                    yield()
                }
            }

            return@runBlocking p
        }
    }

}