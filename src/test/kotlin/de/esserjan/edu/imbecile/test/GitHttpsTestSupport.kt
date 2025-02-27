package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.Imbecile
import de.esserjan.edu.imbecile.ImbecileCredentials
import de.esserjan.edu.imbecile.test.TestData.HttpsData
import de.esserjan.edu.imbecile.test.git_https_backend.GitHttpBackendHandler
import de.esserjan.edu.imbecile.test.git_https_backend.HttpsUndertow
import de.esserjan.edu.imbecile.test.git_https_backend.MapIdentityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.nio.file.Files

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
    ).build()

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
                executor.extraEnvVars["GIT_TRACE"] = "1"
                executor.extraEnvVars["GIT_TRACE_PACKET"] = "1"
                executor.extraEnvVars["GIT_TRACE_PACK_ACCESS"] = "1"
                executor.extraEnvVars["GIT_CURL_VERBOSE"] = "1"
            }

            // tmp_dir for cloning working-tree
            gitFolder.deleteOnExit()
        }

        @JvmStatic
        @BeforeAll
        fun enableReceivePack(): Unit {
            val serverRepo = Imbecile(repositoryDirectory = TestData.GIT_REPO)
            serverRepo.config("http.receivepack", "true")
        }
    }

}