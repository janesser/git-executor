package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.util.SubProcessBuilder
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Path

object GitHttpsBackendDispatcher : Dispatcher() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val projectRoot = TestData.GIT_REPO.path

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.requestUrl?.encodedPath?.substring(1 + TestData.GIT_REMOTE_PROJECT.length)
        logger.debug("accessing: {}", path)

        if (path?.startsWith("/objects") == true) {
            val objectPath = Path.of(projectRoot, ".git", path)
            return if (objectPath.toFile().exists())
                MockResponse().setBody(Buffer().write(Files.readAllBytes(objectPath)))
            else
                MockResponse().setResponseCode(404)
        }

        val pb = SubProcessBuilder(
            listOf("/usr/lib/git-core/git-http-backend"),
            null,
            mapOf(
                "GIT_PROJECT_ROOT" to projectRoot,
                "GIT_HTTP_EXPORT_ALL" to "",
                "PATH_INFO" to path.orEmpty(),
                "REQUEST_METHOD" to request.method.orEmpty(),
                "GIT_PROTOCOL" to request.getHeader("Git-Protocol").orEmpty(),
                //"QUERY_STRING" to request.requestUrl?.query.orEmpty(),
                //"CONTENT_TYPE" to request.getHeader("Content-Type").orEmpty(),
            )
        )

        val p = pb.start()
        val collectedOutput = p.collectedOutput()

        logger.debug("git-http-backend returnCode {}", p.waitFor())

        val response = MockResponse()
        val outputSplit = collectedOutput.split("\n\n", limit = 2)
        outputSplit[0].split("\n")
            .forEach { header: String ->
                val headerSplit = header.split(": ", limit = 2)
                response.setHeader(headerSplit[0], headerSplit[1])
            }
        val body = outputSplit[1]
        response.setBody(body)

        return response
    }
}