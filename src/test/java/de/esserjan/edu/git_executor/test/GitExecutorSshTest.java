package de.esserjan.edu.git_executor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;

import de.esserjan.edu.git_executor.GitExecutionException;
import de.esserjan.edu.git_executor.GitExecutionResult;
import de.esserjan.edu.git_executor.GitExecutor;
import software.sham.git.MockGitServer;

public class GitExecutorSshTest {

	private GitExecutor underTest;

	private static File keyFilePriv;
	private static File keyFilePub;
	private static File askPassFile;

	private MockGitServer server;

	public GitExecutorSshTest() throws GitExecutionException {
		underTest = new GitExecutor();
		if (TestData.GIT_PATH.exists())
			underTest.setGitExecutable(TestData.GIT_PATH);
		underTest.setGitRepo(TestData.GIT_REPO);
	}

	@BeforeAll
	public static void generateClientKeypair() throws IOException, SshException {
		keyFilePriv = File.createTempFile(TestData.SSH_TEST_KEY, "");
		Files.setPosixFilePermissions(keyFilePriv.toPath(), PosixFilePermissions.fromString("rw-" + "------"));
		keyFilePriv.deleteOnExit();

		keyFilePub = new File(keyFilePriv.getAbsolutePath() + ".pub");
		keyFilePub.deleteOnExit();

		SshKeyPair keyPair = SshKeyPairGenerator.generateKeyPair(SshKeyPairGenerator.SSH2_RSA);
		SshKeyUtils.savePrivateKey(keyPair, TestData.SSH_TEST_KEY_PWD, "", keyFilePriv);
		SshKeyUtils.createPublicKeyFile(keyPair.getPublicKey(), "", keyFilePub);
	}

	@BeforeAll
	public static void setupAskpass() throws IOException {
		askPassFile = File.createTempFile(TestData.SSH_TEST_ASKPASS, "");
		Files.setPosixFilePermissions(askPassFile.toPath(), PosixFilePermissions.fromString("rwx" + "------"));
		askPassFile.deleteOnExit();

		try (FileWriter writer = new FileWriter(askPassFile)) {
			writer.write("#!/bin/sh");
			writer.write(System.lineSeparator());
			writer.write("echo ");
			writer.write(TestData.SSH_TEST_KEY_PWD);
			writer.write(System.lineSeparator());
			writer.flush();
		}
	}

	@BeforeEach
	public void startSshMock() throws IOException, GeneralSecurityException, InterruptedException {
		File knownHosts = new File(System.getProperty("user.home") + "/.ssh", "known_hosts");
		Process sshkeygenCleanup = Runtime.getRuntime().exec(new String[] { "/usr/bin/ssh-keygen", //
				"-f", knownHosts.getAbsolutePath(), //
				"-R", "[" + TestData.SSH_SERVER + "]:" + TestData.SSH_MOCK_PORT //
		});
		assertEquals(0, sshkeygenCleanup.waitFor(), sshkeygenCleanup.errorReader().readLine());

		server = new MockGitServer(TestData.SSH_MOCK_PORT);
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
			throws IOException, GeneralSecurityException, GitExecutionException, InterruptedException {
		// arrange 3: re-register mock remote
		underTest.removeRemote(TestData.GIT_MOCK_REMOTE);
		underTest.addRemote(TestData.GIT_MOCK_REMOTE, TestData.GIT_MOCK_REMOTE_URL);

		// arrange 4: set GIT_SSH_COMMAND with specific key
		underTest.getExtraEnvs().put("GIT_SSH_COMMAND", //
				Stream.of(sshCommand(null)).reduce("", (a, b) -> a + " " + b) //
		);
		underTest.getExtraEnvs().put(GitExecutor.ENV_SSH_ASKPASS, askPassFile.getAbsoluteFile().toString());

		// act
		GitExecutionResult res = underTest.fetch(TestData.GIT_MOCK_REMOTE);

		// assert
		assertEquals(0, res.exitCode(), res.outputText());
	}

	private String[] sshCommand(String serverUrl) {
		return sshCommand(serverUrl, null);
	}

	private String[] sshCommand(String serverUrl, String command) {
		return new String[] { //
				"/usr/bin/ssh", "-v", //
				"-i", keyFilePriv.getAbsolutePath(), //
				"-o", "IdentitiesOnly=yes", //
				"-o", "StrictHostKeyChecking=no", //
				(serverUrl != null ? serverUrl : ""), //
				(command != null ? command : "") //
		};
	}
}
