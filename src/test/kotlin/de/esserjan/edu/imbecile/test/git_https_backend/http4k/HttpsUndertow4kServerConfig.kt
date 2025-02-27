package de.esserjan.edu.imbecile.test.git_https_backend.http4k

import de.esserjan.edu.imbecile.test.git_https_backend.HttpsUndertow
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kServer
import org.http4k.server.PolyServerConfig
import org.http4k.server.buildHttp4kUndertowServer
import org.http4k.server.buildUndertowHandlers
import org.http4k.sse.SseHandler
import org.http4k.websocket.WsHandler

class HttpsUndertow4kServerConfig(private val httpsUndertow : HttpsUndertow) : PolyServerConfig {
    override fun toServer(http: HttpHandler?, ws: WsHandler?, sse: SseHandler?): Http4kServer {
        val (httpHandler, multiProtocolHandler) = buildUndertowHandlers(http, ws, sse, stopMode)

        return httpsUndertow.toServer(multiProtocolHandler)
            .buildHttp4kUndertowServer(httpHandler, stopMode, httpsUndertow.port)
    }

}