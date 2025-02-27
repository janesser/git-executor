package de.esserjan.edu.imbecile.test.git_https_backend.http4k

import de.esserjan.edu.imbecile.util.SubProcessBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

sealed class GenericResponse(val headers: Map<String, String>, val body: ByteArray, val responseCode: Int) {
    class NotFoundResponse : GenericResponse(emptyMap(), ByteArray(0), 404)
    class FileResponse(filePath: Path) : GenericResponse(emptyMap(), Files.readAllBytes(filePath), 200)
    class ForwardedResponse(headers: Map<String, String>, body: ByteArray, responseCode: Int) :
        GenericResponse(headers, body, responseCode)
}

/**
 * https://git-scm.com/book/de/v2/Git-auf-dem-Server-Smart-HTTP
 */
class GitHttpBackendProxy(private val projectRoot: String) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun serveGitBackend(path: String, method: String, query:String?, contentType: String?, gitProtocol: String = "v2"): GenericResponse {
        if (path.startsWith("/objects")) {
            val objectPath = Path.of(projectRoot, ".git", path)
            return if (objectPath.toFile().exists())
                GenericResponse.FileResponse(objectPath)
            else
                GenericResponse.NotFoundResponse()
        }

        val pb = SubProcessBuilder(
            listOf("/usr/lib/git-core/git-http-backend"),
            null,
            mapOf(
                "GIT_PROJECT_ROOT" to projectRoot,
                "GIT_HTTP_EXPORT_ALL" to "",
                "PATH_INFO" to path,
                "REQUEST_METHOD" to method,
                //"QUERY_STRING" to query.orEmpty(),
                //"CONTENT_TYPE" to contentType.orEmpty(),
                //"REMOTE_USER" to "user", // FIXME
                "GIT_PROTOCOL" to gitProtocol,
            )
        )

        val p = pb.start()
        val collectedOutput = p.collectedOutput()

        logger.debug("git-http-backend returnCode {}", p.waitFor())

        val outputSplit = collectedOutput.split("\n\n", limit = 2)
        val headers = mutableMapOf<String, String>()
        outputSplit[0].split("\n")
            .forEach { header: String ->
                val headerSplit = header.split(": ", limit = 2)
                headers[headerSplit[0]] = headerSplit[1]
            }
        val body = outputSplit[1]

        return GenericResponse.ForwardedResponse(headers, body.encodeToByteArray(), 200)
    }
}