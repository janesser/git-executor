package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.Imbecile.ImbecileException
import de.esserjan.edu.imbecile.test.TestData.HttpsData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class ImbecileHttpsTest(serverHolder: ServerHolder) : GitHttpsTestSupport(serverHolder) {

    @Test
    @Order(0)
    @Throws(ImbecileException::class)
    fun canAddUserPassPair() {
        credentials.fill(
            HttpsData.HTTPS_PROTOCOL,
            HttpsData.HTTPS_HOST,
            HttpsData.HTTPS_USERNAME,
            HttpsData.HTTPS_PASSWORD
        )
    }

    @Test
    @Order(0)
    @Throws(ImbecileException::class)
    fun canRejectUserPassPair() {
        credentials.reject(
            HttpsData.HTTPS_PROTOCOL,
            HttpsData.HTTPS_HOST,
            HttpsData.HTTPS_USERNAME
        )
    }

    @Test
    @Order(1)
    fun canAccessAnonymously() {
        assertTrue(gitFolder.deleteRecursively())

        val cloneResult = executor.clone(
            HttpsData.GIT_REMOTE_URL,
            gitFolder
        )
        assertExitCodeZero(cloneResult)

        val filesCloned = gitFolder.list()?.asList()
        assertThat(filesCloned, hasItems(".git", "pom.xml"))
    }

    @Test
    @Order(2)
    @Throws(ImbecileException::class)
    fun canPushWithCredentials() {
        // arrange
        canAccessAnonymously()

        executor.config("credential.helper", "store --file " + File(gitFolder, ".git-credentials"))

        // act & assert
        val host = HttpsData.HTTPS_HOST + ":" + HttpsData.HTTPS_PORT
        val fillResult = credentials.fill(
            HttpsData.HTTPS_PROTOCOL,
            host,
            HttpsData.HTTPS_USERNAME,
            HttpsData.HTTPS_PASSWORD
        )
        assertExitCodeZero(fillResult)

        val commitResult = executor.commit("empty commit", allowEmpty = true)
        assertExitCodeZero(commitResult)

        val pushResult = executor.push(HttpsData.GIT_REMOTE_URL, false)
        assertExitCodeZero(pushResult)

        val eraseResult = credentials.reject(HttpsData.HTTPS_PROTOCOL, host, HttpsData.HTTPS_USERNAME)
        assertExitCodeZero(eraseResult)
    }

    @Test
    @Order(2)
    @Throws(ImbecileException::class)
    fun cannotPushWithWrongCredentials() {
        // arrange
        executor.config("credential.helper", "store --file " + File(gitFolder, ".git-credentials"))

        // act & assert
        val host = HttpsData.HTTPS_HOST + ":" + HttpsData.HTTPS_PORT
        val storeResult = credentials.fill(
            HttpsData.HTTPS_PROTOCOL,
            host,
            HttpsData.HTTPS_USERNAME,
            "wrong" + HttpsData.HTTPS_PASSWORD
        )
        assertExitCodeZero(storeResult)

        executor.commit("empty commit", allowEmpty = true)
        val pushResult = executor.push(HttpsData.GIT_REMOTE_URL)
        Assertions.assertEquals(128, pushResult.exitCode)

        val eraseResult = credentials.reject(HttpsData.HTTPS_PROTOCOL, host, HttpsData.HTTPS_USERNAME)
        assertExitCodeZero(eraseResult)
    }
}