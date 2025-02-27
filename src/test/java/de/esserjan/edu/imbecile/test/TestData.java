package de.esserjan.edu.imbecile.test;

import java.io.File;

interface TestData {

	File GIT_PATH = new File("/usr/bin/git");

	File GIT_REPO = new File("/home/jan/projs/egit");
	String GIT_REPO_REMOTE = "https://github.com/eclipse-egit/egit.git";
	File GIT_FILE = new File(GIT_REPO, "pom.xml");
	File GIT_FOLDER = new File(GIT_REPO, "icons");
	String GIT_HISTORICAL_COMMIT_ID = "e90d864edca6eb34d0b7a1f0dcc767bcd4970bb5";
	String GIT_HISTORICAL_ONTO_COMMIT_ID = "cd8c66d521371cbd1163b136f991a9598055d84a";

	String GIT_MOCK_REMOTE = "mockRemote";
	String GIT_REMOTE_PROJECT = "egit.git";

	static interface SshData {
		String SSH_TEST_KEY = "testKey";
		String SSH_TEST_KEY_PWD = "testKeyPassword";
		String SSH_TEST_ASKPASS = "testAskPass.sh";
		String SSH_TEST_PRINCIPAL = "testPrincipal";
		int SSH_MOCK_PORT = 61333; // auto select
		String SSH_SERVER = "localhost";
		String GIT_REMOTE_URL = String.format("ssh://%s@%s:%d/%s", //
				SSH_TEST_PRINCIPAL, SSH_SERVER, SSH_MOCK_PORT, TestData.GIT_REMOTE_PROJECT);
	}

	static interface HttpsData {
		String HTTPS_PROTOCOL = "https";
		int HTTPS_PORT = 62345;
		String HTTPS_HOST = "localhost";
		String HTTPS_USERNAME = "user";
		String HTTPS_PASSWORD = "pass";
		String GIT_REMOTE_URL = String.format("https://%s@%s:%d/%s", //
				HTTPS_USERNAME, HTTPS_HOST, HTTPS_PORT, TestData.GIT_REMOTE_PROJECT);
		String HTTPS_GIT_FOLDER = "test_git_https";
		String HTTPS_CERT_PASSWORD = "mockedSecret";
	}

}