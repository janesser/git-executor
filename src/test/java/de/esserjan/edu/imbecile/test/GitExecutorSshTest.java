package de.esserjan.edu.imbecile.test;

import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import de.esserjan.edu.imbecile.test.TestData.SshData;
import de.esserjan.edu.imbecile.Imbecile;
import de.esserjan.edu.imbecile.ImbecileResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.sham.git.MockGitServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitExecutorSshTest extends GitTestSupport {

    private final Imbecile underTest;

    private static File keyFilePriv;
    private static File keyFilePub;
    private static File askPassFile;

    private MockGitServer server;

    public GitExecutorSshTest() {
        underTest = new Imbecile();
        if (TestData.GIT_PATH.exists())
            underTest.setExecutable(TestData.GIT_PATH);
        underTest.setRepositoryDirectory(TestData.GIT_REPO);
    }

    @BeforeAll
    public static void generateClientKeypair() throws IOException, SshException {
        keyFilePriv = File.createTempFile(SshData.SSH_TEST_KEY, "");
        Files.setPosixFilePermissions(keyFilePriv.toPath(), PosixFilePermissions.fromString("rw-" + "------"));
        keyFilePriv.deleteOnExit();

        keyFilePub = new File(keyFilePriv.getAbsolutePath() + ".pub");
        keyFilePub.deleteOnExit();

        SshKeyPair keyPair = SshKeyPairGenerator.generateKeyPair(SshKeyPairGenerator.SSH2_RSA);
        SshKeyUtils.savePrivateKey(keyPair, SshData.SSH_TEST_KEY_PWD, "", keyFilePriv);
        SshKeyUtils.createPublicKeyFile(keyPair.getPublicKey(), "", keyFilePub);
    }

    @BeforeAll
    public static void setupAskpass() throws IOException {
        askPassFile = File.createTempFile(SshData.SSH_TEST_ASKPASS, "");
        Files.setPosixFilePermissions(askPassFile.toPath(), PosixFilePermissions.fromString("rwx" + "------"));
        askPassFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(askPassFile)) {
            writer.write("#!/bin/sh");
            writer.write(System.lineSeparator());
            writer.write("echo ");
            writer.write(SshData.SSH_TEST_KEY_PWD);
            writer.write(System.lineSeparator());
            writer.flush();
        }
    }

    @BeforeEach
    public void startSshMock() throws IOException, InterruptedException {
        File knownHosts = new File(System.getProperty("user.home") + "/.ssh", "known_hosts");
        Process sshkeygenCleanup = Runtime.getRuntime().exec(new String[]{"/usr/bin/ssh-keygen", //
                "-f", knownHosts.getAbsolutePath(), //
                "-R", "[" + SshData.SSH_SERVER + "]:" + SshData.SSH_MOCK_PORT //
        });
        assertEquals(0,
                sshkeygenCleanup.waitFor(),
                sshkeygenCleanup.errorReader().readLine());

        server = new MockGitServer(SshData.SSH_MOCK_PORT);
        server.allowPublicKey(SshKeyUtils.getPublicKey(keyFilePub).getJCEPublicKey()).enableShell();

        server.prepareGitProject(TestData.GIT_REMOTE_PROJECT);

        server.start();
    }

    @AfterEach
    public void stopSshMock() throws IOException {
        server.stop();
    }

    @Test
    public void canGitFetchSsh()
            throws IOException, GeneralSecurityException, InterruptedException {
        // arrange 3: re-register mock remote
        underTest.removeRemote(TestData.GIT_MOCK_REMOTE);
        underTest.addRemote(TestData.GIT_MOCK_REMOTE, SshData.GIT_REMOTE_URL);

        // arrange 4: set GIT_SSH_COMMAND with specific key
        underTest.getExtraEnvVars().put("GIT_SSH_COMMAND", //
                Stream.of(sshCommand(null)).reduce("", (a, b) -> a + " " + b) //
        );
        underTest.getExtraEnvVars().put(Imbecile.ENV_SSH_ASK_PASS, askPassFile.getAbsoluteFile().toString());

        // act
        ImbecileResult res = underTest.fetch(TestData.GIT_MOCK_REMOTE);

        // assert
        assertExitCodeZero(res);
    }

    private String[] sshCommand(String serverUrl) {
        return sshCommand(serverUrl, null);
    }

    private String[] sshCommand(String serverUrl, String command) {
        return new String[]{ //
                "/usr/bin/ssh", "-v", //
                "-i", keyFilePriv.getAbsolutePath(), //
                "-o", "IdentitiesOnly=yes", //
                "-o", "StrictHostKeyChecking=no", //
                (serverUrl != null ? serverUrl : ""), //
                (command != null ? command : "") //
        };
    }
}
