package de.esserjan.edu.git_executor.test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;

import de.esserjan.edu.git_executor.GitCredentials;
import de.esserjan.edu.git_executor.GitExecutionException;
import de.esserjan.edu.git_executor.GitExecutionResult;
import de.esserjan.edu.git_executor.GitExecutor;
import de.esserjan.edu.git_executor.test.TestData.HttpsData;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = { HttpsData.HTTPS_PORT })
public class GitExecutorHttpsTest extends GitTestSupport {

	private GitExecutor executor = new GitExecutor();
	private GitCredentials credentials = new GitCredentials();

	@Test
	public void canAddUserPassPair() throws GitExecutionException {
		credentials.store(HttpsData.HTTPS_PROTOCOL, HttpsData.HTTPS_HOST, HttpsData.HTTPS_USERNAME,
				HttpsData.HTTPS_PASSWORD);
	}

	@Test
	public void canRejectUserPassPair() throws GitExecutionException {
		credentials.erase(HttpsData.HTTPS_PROTOCOL, HttpsData.HTTPS_HOST, HttpsData.HTTPS_USERNAME);
	}

	@Test
	public void canAuthWithCredentials() throws GitExecutionException, IOException {
		File gitFolder = Files.createTempDirectory(HttpsData.HTTPS_GITFOLDER).toFile();
		gitFolder.deleteOnExit();
		executor.setGitRepo(gitFolder);

		File sslCertFolder = Files.createTempDirectory(HttpsData.HTTPS_SSLFOLDER).toFile();
		sslCertFolder.deleteOnExit();

		ConfigurationProperties.dynamicallyCreateCertificateAuthorityCertificate(true);
		ConfigurationProperties.directoryToSaveDynamicSSLCertificate(sslCertFolder.getAbsolutePath());
		ConfigurationProperties.forwardProxyAuthenticationUsername(HttpsData.HTTPS_USERNAME);
		ConfigurationProperties.forwardProxyAuthenticationPassword(HttpsData.HTTPS_PASSWORD);

		GitExecutionResult storeResult = credentials.store(HttpsData.HTTPS_PROTOCOL, HttpsData.HTTPS_HOST,
				HttpsData.HTTPS_USERNAME, HttpsData.HTTPS_PASSWORD);
		assertExitCodeZero(storeResult);

		GitExecutionResult cloneResult = executor.clone(HttpsData.GIT_REMOTE_URL, gitFolder, Optional.empty());
		assertExitCodeZero(cloneResult);

		GitExecutionResult eraseResult = credentials.erase(HttpsData.HTTPS_PROTOCOL, HttpsData.HTTPS_HOST,
				HttpsData.HTTPS_USERNAME);
		assertExitCodeZero(eraseResult);

		GitExecutionResult fetchResult = executor.fetch();
		assertNotEquals(0, fetchResult.exitCode());
	}
}
