package de.esserjan.edu.imbecile.test.git_https_backend

import io.undertow.security.handlers.AuthenticationConstraintHandler
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange

class GitAuthenticationConstraint(
    next: HttpHandler,
    private val servicesAuthRequirement: List<String> = listOf("git-receive-pack")
) : AuthenticationConstraintHandler(next) {

    override fun isAuthenticationRequired(exchange: HttpServerExchange): Boolean {
        return exchange.isSecure && exchange.queryParameters["service"]?.first.let { service ->
            servicesAuthRequirement.contains(
                service
            )
        }
    }
}