package de.esserjan.edu.imbecile.test.git_https_backend

import io.undertow.Undertow
import io.undertow.security.api.AuthenticationMode
import io.undertow.security.handlers.AuthenticationCallHandler
import io.undertow.security.handlers.AuthenticationMechanismsHandler
import io.undertow.security.handlers.SecurityInitialHandler
import io.undertow.security.idm.IdentityManager
import io.undertow.security.impl.BasicAuthenticationMechanism
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * @see org.http4k.server.Undertow
 */
class HttpsUndertow(
    val host: String,
    val port: Int,
    private val keystorePassword: CharArray,
    private val identityManager: IdentityManager?
) {

    fun toServer(
        multiProtocolHandler: io.undertow.server.HttpHandler
    ): Undertow.Builder {
        val serverKeys = createServerKeys(host)

        return Undertow.builder()
            .setHandler(wrapSecurity(multiProtocolHandler, identityManager))
            .addHttpsListener(port, host, createSslContext(serverKeys))
    }

    /**
     * https://github.com/undertow-io/undertow/blob/main/examples/src/main/java/io/undertow/examples/security/basic/BasicAuthServer.java
     */
    private fun wrapSecurity(
        handler: io.undertow.server.HttpHandler,
        identityManager: IdentityManager?
    ): io.undertow.server.HttpHandler {
        val authenticationCallHandler = AuthenticationCallHandler(handler)
        val authenticationConstraintHandler = GitAuthenticationConstraint(authenticationCallHandler)
        val authenticationMechanismsHandler = AuthenticationMechanismsHandler(
            authenticationConstraintHandler,
            listOf(BasicAuthenticationMechanism("${this.host} realm"))
        )
        val securityInitialHandler = SecurityInitialHandler(
            AuthenticationMode.CONSTRAINT_DRIVEN,
            identityManager,
            authenticationMechanismsHandler
        )

        return securityInitialHandler
    }

    /**
     * https://stackoverflow.com/questions/27906682/enabling-https-in-undertow
     */
    private fun createSslContext(keyStore: KeyStore): SSLContext? {
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, keystorePassword)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, SecureRandom())
        return sslContext
    }

    private fun createServerKeys(host: String): KeyStore {
        val kpGenerator = KeyPairGenerator.getInstance("RSA")
        val keyPair = kpGenerator.genKeyPair()

        val dnName = X500Name("CN=$host")
        val contentSigner = JcaContentSignerBuilder("SHA256WithRSA")
            .build(keyPair.private)

        val cal = Calendar.getInstance()
        cal.add(1, Calendar.YEAR)
        val notAfter = cal.time

        val certificateHolder = X509v3CertificateBuilder(
            dnName,
            BigInteger.valueOf(System.currentTimeMillis()),
            Date.from(Instant.now()),
            notAfter,
            dnName,
            SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
        ).build(contentSigner)

        val certificate = JcaX509CertificateConverter().getCertificate(certificateHolder)

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        // https://stackoverflow.com/questions/22761847/keystore-is-not-initialized-exception
        keyStore.load(null, null)
        keyStore.setKeyEntry(
            "undertow-tls-self-signed",
            keyPair.private,
            keystorePassword,
            listOf(certificate).toTypedArray()
        )

        return keyStore
    }
}

