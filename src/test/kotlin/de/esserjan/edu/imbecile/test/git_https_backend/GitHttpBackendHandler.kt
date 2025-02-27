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

/**
 * tested against git version 2.43.0 (ubuntu noble)
 */
class GitHttpBackendHandler(private val webRoot: String, private val projectRoot: Path) : HttpHandler {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.respondWithHttpBackend()
    }

    /**
     * https://git-scm.com/docs/http-protocol
     *
     * https://git-scm.com/docs/pack-protocol
     */
    private fun HttpServerExchange.respondWithHttpBackend() {
        val reqPath = requestPath.substring(1 + webRoot.length)
        val contentLength = requestHeaders.get("Content-Length")?.first().orEmpty()
        val contentEncoding = requestHeaders.get("Content-Encoding")?.first().orEmpty()
        val remoteUser = securityContext.authenticatedAccount?.principal?.name.orEmpty()

        logger.info(
            "request user: {} method: {} path: {} query: {} content-encoding: {} content-length: {}",
            remoteUser,
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
                "CONTENT_TYPE" to requestHeaders.get("Content-Type")?.first().orEmpty(),
                "CONTENT_ENCODING" to contentEncoding,
                "CONTENT_LENGTH" to contentLength,
                "REMOTE_USER" to remoteUser,
                "REMOTE_ADDR" to requestHeaders.get("Host")?.first().orEmpty(),
                "GIT_PROTOCOL" to requestHeaders.get("Git-Protocol")?.first().orEmpty(),
                "HTTPS" to "on"
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
            val message = inputStream.readAllBytes()
            // cond-breakpoint: requestMethod.toString().equals("POST") && responseHeaders["Content-Type"]?.first()?.contains("x-git-receive-pack")==true
            logger.debug("request body:\n{}", String(message))
            message
        }

        val p = pb.start()
        with(p.outputStream()) {
            write(payload)
            flush()
        }

        val reader = p.inputReader()
        do {
            val header = reader.readLine()
            if (header.isNotEmpty()) {
                val headerName = header.takeWhile { c -> c != ':' }
                val headerValue = header.dropWhile { c -> c != ':' }.trimStart(':', ' ').trimEnd()
                responseHeaders.add(HttpString(headerName), headerValue)
            } else
                break
        } while (true)
        logger.debug("response headers:\n{}", responseHeaders)

        if (responseHeaders["Content-Type"]?.first() == "application/x-git-receive-pack-advertisement") {
            logger.info("$remoteUser is pushing")

            val response = p.inputStream().readAllBytes()
            logger.debug("x-git-receive-pack-advertisement: {}", String(response))

            /*
                client support for v2 isn't yet ready
                    "fatal: support for protocol v2 not implemented yet"
             */

            /*
                fatal: invalid server response; got 'a16492293d6879d193b475d77540399014190693 refs/heads/master'

                REMOTE_USER=user PWD=/home/jan/projs/git-executor QUERY_STRING=service=git-receive-pack GIT_HTTP_EXPORT_ALL= REQUEST_METHOD=GET LANGUAGE=de_DE LANG=de_DE.UTF-8 REMOTE_ADDR=localhost:62345 CONTENT_LENGTH= PATH_INFO=/info/refs HTTPS=on GIT_PROTOCOL="version=2" CONTENT_ENCODING= GIT_PROJECT_ROOT=/home/jan/projs/egit CONTENT_TYPE= /usr/lib/git-core/git-http-backend

                see 'src/test/resources/x-git-receive-pack-advertisement.bin'
                    receiveHeader skipped ?
                    0000 opening skipped?

                https://mincong.io/2018/05/04/git-and-http/
             */

            outputStream.write("001f# service=git-receive-pack\n".toByteArray(CS))
            outputStream.write(FLUSH_MARKER)
            outputStream.write(response)
            outputStream.flush()
        } else {
            val response = p.inputStream().readAllBytes()
            logger.debug(String(response))
            outputStream.write(response)
            outputStream.flush()
        }

        val processExitCode = p.waitFor()
        logger.debug("process terminated with: {}", processExitCode)
        if (processExitCode != 0)
            logger.warn("process exited with exitCode {} output {}", processExitCode, p.collectedOutput())
    }

    companion object {
        val CS = Charsets.UTF_8

        val FLUSH_MARKER = "0000".toByteArray(CS)
    }
}