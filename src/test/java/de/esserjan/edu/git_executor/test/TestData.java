package de.esserjan.edu.git_executor.test;

import java.io.File;

interface TestData {

	final File GIT_PATH = new File("/usr/bin/git");

	final File GIT_REPO = new File("/home/jan/projs/egit");
	final String GIT_REPO_REMOTE = "https://github.com/eclipse-egit/egit.git";
	final File GIT_FILE = new File(GIT_REPO, "pom.xml");
	final File GIT_FOLDER = new File(GIT_REPO, "icons");
	final String GIT_HISTORICAL_COMMIT_ID = "e90d864edca6eb34d0b7a1f0dcc767bcd4970bb5";
	final String GIT_HISTORICAL_ONTO_COMMIT_ID = "cd8c66d521371cbd1163b136f991a9598055d84a";

	final String SSH_TEST_KEY = "testKey";
	final String SSH_TEST_KEY_PWD = "testKeyPassword";
	final String SSH_TEST_ASKPASS = "testAskPass.sh";
	final String GIT_MOCK_REMOTE = "mockRemote";
	final String SSH_TEST_PRINCIPAL = "testPrincipal";

	final int SSH_MOCK_PORT = 61333; // auto select
	final String SSH_SERVER = "localhost";
	final String GIT_REMOTE_PROJECT = "egit.mocked";
	final String GIT_MOCK_REMOTE_URL = String.format("ssh://%s@%s:%d/%s", //
			SSH_TEST_PRINCIPAL, SSH_SERVER, SSH_MOCK_PORT, GIT_REMOTE_PROJECT);

}