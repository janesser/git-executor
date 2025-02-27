package de.esserjan.edu.imbecile.test;

import de.esserjan.edu.imbecile.Imbecile;
import de.esserjan.edu.imbecile.ImbecileCredentials;
import de.esserjan.edu.imbecile.ImbecileResult;
import de.esserjan.edu.imbecile.test.TestData.HttpsData;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GitExecutorHttpsTest extends GitTestSupport {

    static MockWebServer mockWebServer;

    @BeforeEach
    public void startMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.setProtocolNegotiationEnabled(true);
        mockWebServer.setDispatcher(GitHttpsBackendDispatcher.INSTANCE);
        mockWebServer.start(HttpsData.HTTPS_PORT);
    }

    @AfterEach
    public void stopMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    private final Imbecile executor = new Imbecile();
    private final ImbecileCredentials credentials = new ImbecileCredentials();
    private final File gitFolder;

    public GitExecutorHttpsTest() throws IOException {
        super();

        // accept self-signed certificates
        executor.getExtraEnvVars().put("GIT_SSL_NO_VERIFY", "true");

        // tmp_dir for cloning working-tree
        gitFolder = Files.createTempDirectory(HttpsData.HTTPS_GIT_FOLDER).toFile();
        gitFolder.deleteOnExit();
    }

    @Test
    @Order(0)
    public void canAddUserPassPair() throws Imbecile.ImbecileException {
        credentials.store(HttpsData.HTTPS_PROTOCOL, HttpsData.HTTPS_HOST, HttpsData.HTTPS_USERNAME,
                HttpsData.HTTPS_PASSWORD);
    }

    @Test
    @Order(0)
    public void canRejectUserPassPair() throws Imbecile.ImbecileException {
        credentials.erase(HttpsData.HTTPS_PROTOCOL, HttpsData.HTTPS_HOST, HttpsData.HTTPS_USERNAME);
    }

    @Test
    @Order(1)
    public void canAccessAnonymously() {
        ImbecileResult cloneResult = executor.clone(mockWebServer.url(TestData.GIT_REMOTE_PROJECT).toString(), gitFolder, null);
        assertExitCodeZero(cloneResult);
    }

    @Disabled
    @Test
    @Order(2)
    public void canAuthWithCredentials() throws Imbecile.ImbecileException {
        // arrange
        mockWebServer.requireClientAuth();

        // act & assert
        final String host = HttpsData.HTTPS_HOST + ":" + HttpsData.HTTPS_PORT;
        ImbecileResult storeResult = credentials.store(HttpsData.HTTPS_PROTOCOL, host,
                HttpsData.HTTPS_USERNAME, HttpsData.HTTPS_PASSWORD + "wrong"); // FIxME enable credential.helpers on clone ?
        assertExitCodeZero(storeResult);

        ImbecileResult pushResult = executor.push(HttpsData.GIT_REMOTE_URL, false);
        assertExitCodeZero(pushResult);

        ImbecileResult eraseResult = credentials.erase(HttpsData.HTTPS_PROTOCOL, host,
                HttpsData.HTTPS_USERNAME);
        assertExitCodeZero(eraseResult);
    }
}
