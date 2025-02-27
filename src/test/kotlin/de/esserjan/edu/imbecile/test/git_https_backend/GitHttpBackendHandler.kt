package de.esserjan.edu.imbecile.test.git_https_backend

import de.esserjan.edu.imbecile.util.SubProcessBuilder
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.zip.GZIPInputStream

class GitHttpBackendHandler(private val webRoot: String, private val projectRoot: Path) : HttpHandler {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.respondWithHttpBackend()
    }

    private fun HttpServerExchange.respondWithHttpBackend() {
        val reqPath = requestPath.substring(1 + webRoot.length)
        val contentLength = requestHeaders.get("Content-Length")?.first.orEmpty()
        val contentEncoding = requestHeaders.get("Content-Encoding")?.first.orEmpty()

        logger.debug(
            "request method: {} path: {} query: {} content-encoding: {} content-length: {}",
            requestMethod,
            reqPath,
            queryString,
            contentEncoding,
            contentLength
        )
        logger.debug("request headers: {}", requestHeaders)


        val pb = SubProcessBuilder(
            listOf("/usr/lib/git-core/git-http-backend"),
            null,
            mapOf(
                "GIT_PROJECT_ROOT" to projectRoot.toAbsolutePath().toString(),
                "GIT_HTTP_EXPORT_ALL" to "",
                "PATH_INFO" to reqPath,
                "REQUEST_METHOD" to requestMethod.toString(),
                "QUERY_STRING" to queryString,
                "CONTENT_TYPE" to requestHeaders.get("Content-Type")?.first.orEmpty(),
                "CONTENT_ENCODING" to contentEncoding,
                "CONTENT_LENGTH" to contentLength,
                "REMOTE_USER" to securityContext.authenticatedAccount?.principal?.name.orEmpty(),
                //"REMOTE_ADDR" to "remoteAddress",
                "GIT_PROTOCOL" to requestHeaders.get("Git-Protocol")?.first.orEmpty(),
            )
        )

        startBlocking()
        val payload: ByteArray = if (contentEncoding == "gzip") {
            val decodedMessage =
                GZIPInputStream(inputStream).bufferedReader(Charset.defaultCharset())
                    .readText()
            logger.debug("request body (decoded):\n{}", decodedMessage)
            pb.envVars()["CONTENT_LENGTH"] = "${decodedMessage.length}"
            decodedMessage.toByteArray()
        } else {
            val message = inputStream.bufferedReader(Charset.defaultCharset()).readText()
            logger.debug("request body:\n{}", message)
            message.toByteArray()
        }

        val p = pb.start()
        with(p.outputStream()) {
            write(payload)
            flush()
        }


        val response: StringBuilder = StringBuilder()
        do {
            val chunk = p.inputReader().readText()
            response.append(chunk)
        } while (!chunk.endsWith("0000") && !chunk.contains("fatal:"))

        logger.debug("response:\n{}", response)
        val (headers, body) = response.split("\r\n\r\n")
        for (header in headers.split("\n")) {
            val headerName = header.takeWhile { c -> c != ':' }
            val headerValue = header.dropWhile { c -> c != ':' }.drop(1)
            responseHeaders.add(HttpString(headerName), headerValue)
        }

        responseSender.send(body)
        logger.info("process terminated with: {}", p.waitFor())
    }
}