package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.Imbecile
import de.esserjan.edu.imbecile.ImbecileCredentials
import de.esserjan.edu.imbecile.test.TestData.HttpsData
import de.esserjan.edu.imbecile.test.git_https_backend.GitHttpBackendHandler
import de.esserjan.edu.imbecile.test.git_https_backend.HttpsUndertow
import de.esserjan.edu.imbecile.test.git_https_backend.MapIdentityManager
import io.undertow.UndertowOptions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals

sealed interface ServerHolder {
    fun startServer()
    fun stopServer()
}

data object GitUndertowMock : ServerHolder {
    private val server = HttpsUndertow(
        HttpsData.HTTPS_HOST,
        HttpsData.HTTPS_PORT,
        HttpsData.HTTPS_CERT_PASSWORD.toCharArray(),
        MapIdentityManager(mapOf(HttpsData.HTTPS_USERNAME to HttpsData.HTTPS_PASSWORD))
    ).toServer(
        GitHttpBackendHandler(TestData.GIT_REMOTE_PROJECT, TestData.GIT_REPO.toPath())
    ).setServerOption(
        UndertowOptions.ENABLE_HTTP2, true
    ) // If disabled: Can not multiplex, even if we wanted to with GIT_CURL_VERBOSE
        .build()

    override fun startServer() {
        server.start()
    }

    override fun stopServer() {
        server.stop()
    }


}

abstract class GitHttpsTestSupport(private val serverHolder: ServerHolder) : GitTestSupport() {

    @BeforeEach
    fun startMockWebServer() {
        serverHolder.startServer()
    }

    @AfterEach
    fun stopMockWebServer() {
        serverHolder.stopServer()
    }

    companion object {
        var GIT_TRACE_ENABLED = false

        val executor: Imbecile = Imbecile()
        val gitFolder: File = Files.createTempDirectory(HttpsData.HTTPS_GIT_FOLDER).toFile()
        val credentials: ImbecileCredentials = ImbecileCredentials(gitFolder)

        @JvmStatic
        @BeforeAll
        fun setupGitPlayground() {
            // accept self-signed certificates
            executor.extraEnvVars["GIT_SSL_NO_VERIFY"] = "true"

            if (GIT_TRACE_ENABLED) {
                executor.extraEnvVars["GIT_TRACE"] = ""
                executor.extraEnvVars["GIT_TRACE_PACKET"] = ""
                executor.extraEnvVars["GIT_TRACE_PACK_ACCESS"] = ""
                executor.extraEnvVars["GIT_CURL_VERBOSE"] = ""
            }

            // tmp_dir for cloning working-tree
            gitFolder.deleteOnExit()
        }

        @JvmStatic
        @BeforeAll
        fun prepareBareServerGitRepo() {
            TestData.GIT_REPO.deleteRecursively()

            val serverRepo = Imbecile()
            val cloneResult = serverRepo.clone(TestData.GIT_REPO_REMOTE, TestData.GIT_REPO, bare = true)
            assertEquals(0, cloneResult.exitCode, cloneResult.outputText)

            val configResult = serverRepo.config("http.receivepack", "true")
            assertEquals(0, configResult.exitCode, configResult.outputText)
        }
    }
}