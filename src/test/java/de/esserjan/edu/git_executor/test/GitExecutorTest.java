package de.esserjan.edu.git_executor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.esserjan.edu.git_executor.GitExecutionException;
import de.esserjan.edu.git_executor.GitExecutionResult;
import de.esserjan.edu.git_executor.GitExecutor;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GitExecutorTest {

	private final Logger log = LoggerFactory.getLogger(getClass()); 
	
	private GitExecutor underTest;

	public GitExecutorTest() throws GitExecutionException {
		underTest = new GitExecutor();
		if (TestData.GIT_PATH.exists())
			underTest.setGitExecutable(TestData.GIT_PATH);
		underTest.setGitRepo(TestData.GIT_REPO);
	}

	@Test
	@Order(1)
	public void canFindGitOnPath() throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(new String[] { "which", "git" });
		assertEquals(0, process.waitFor());
	}

	@Test
	@Order(2)
	public void canGitVersion() throws GitExecutionException {
		GitExecutionResult res = underTest.version();
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(3)
	@Disabled
	public void canGitClone() throws GitExecutionException, IOException, InterruptedException {
		int deleteExitCode = Runtime.getRuntime().exec("rm -fR " + TestData.GIT_REPO.getPath()).waitFor();
		assertEquals(0, deleteExitCode);
		assertFalse(TestData.GIT_REPO.exists());

		GitExecutionResult res = underTest.clone(TestData.GIT_REPO_REMOTE, TestData.GIT_REPO, Optional.empty());
		log.debug(res.outputText());

		assertEquals(0, res.exitCode(), res.outputText());
		assertTrue(TestData.GIT_REPO.exists());
	}

	@Test
	@Order(4)
	public void canGitReset() throws GitExecutionException {
		GitExecutionResult res = underTest.reset(true, TestData.GIT_HISTORICAL_COMMIT_ID);
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(4)
	public void canGitClean() throws GitExecutionException, IOException {
		File f = new File(TestData.GIT_FOLDER, "cleanMe");
		f.createNewFile();
		assertTrue(f.exists());

		GitExecutionResult res = underTest.clean();
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());

		assertFalse(f.exists());
	}

	private void resetTestScenario() {
		try {
			canGitReset();
			canGitClean();
		} catch (GitExecutionException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@Order(5)
	public void canGitFetchHttps() throws GitExecutionException {
		GitExecutionResult res = underTest.fetch();
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(5)
	public void canGitFetchSsh() throws GitExecutionException, IOException, InterruptedException {
		// given prepared SSH key
		
		underTest.getExtraEnvs().put("GIT_SSH_COMMAND", "ssh -i " + TestData.KEY_FILE.getPath());
		underTest.getExtraEnvs().put("SSH_ASKPASS", TestData.KEY_PASSPHRASE.getPath());
		underTest.getExtraEnvs().put("SSH_ASKPASS_REQUIRE", "prefer");
		underTest.getExtraEnvs().put("SSH_TEST_PASSPHRASE", "new_passphrase");

		GitExecutionResult res = underTest.fetch("janesser");
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
		
		underTest.getExtraEnvs().clear();
	}

	@Test
	@Order(6)
	public void canGitCommit() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult res = underTest.commit("empty commit", false, true, false);
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(6)
	public void canDetectUncommittedFile() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		assertFalse(underTest.hasChanges());

		Files.move(TestData.GIT_FILE.toPath(), Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()),
				StandardCopyOption.REPLACE_EXISTING);

		assertTrue(underTest.hasChanges());
	}

	@Test
	@Order(6)
	public void canStageAllFiles() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		Files.move(TestData.GIT_FILE.toPath(), Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()),
				StandardCopyOption.REPLACE_EXISTING);

		GitExecutionResult res = underTest.addAll();
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(6)
	public void canStageFileNewFile() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		File targetF = Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()).toFile();
		assertTrue(targetF.createNewFile());

		GitExecutionResult res = underTest.add(targetF);
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(6)
	public void cannotStageNonexistentFile() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		File nonExistentF = Paths.get(TestData.GIT_FOLDER.getPath(), "nonExistentFile").toFile();
		assertFalse(nonExistentF.exists());

		GitExecutionResult res = underTest.add(nonExistentF);
		assertEquals(128, res.exitCode(), res.outputText());
	}

	@Test
	@Order(7)
	public void canGitPull() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult res = underTest.pull(); // FF_ONLY
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(8)
	public void canGitPullDetectConflict() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult resCommit = underTest.commit("empty commit", false, true, false);
		assertEquals(0, resCommit.exitCode(), resCommit.outputText());

		GitExecutionResult res = underTest.pull();
		log.debug(res.outputText());
		assertEquals(128, res.exitCode(), res.outputText());
	}

	@Test
	@Order(8)
	public void canGitPullRebase() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult resCommit = underTest.commit("empty commit", false, true, false);
		assertEquals(0, resCommit.exitCode(), resCommit.outputText());

		GitExecutionResult res = underTest.pull(GitExecutor.PullMode.REBASE_MERGE);
		log.debug(res.outputText());
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	@Order(8)
	public void canGitRebaseAbort() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		File targetF = Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()).toFile();
		Files.move(TestData.GIT_FILE.toPath(), targetF.toPath(), StandardCopyOption.REPLACE_EXISTING);

		GitExecutionResult resAdd = underTest.add(TestData.GIT_FILE);
		assertEquals(0, resAdd.exitCode());

		GitExecutionResult resCommit = underTest.commit("file deleted accidentaly oopsy");
		assertEquals(0, resCommit.exitCode(), resCommit.outputText());

		GitExecutionResult res = underTest.rebase(TestData.GIT_HISTORICAL_ONTO_COMMIT_ID);
		log.debug(res.outputText());
		assertEquals(1, res.exitCode(), res.outputText());

		GitExecutionResult abortResult = underTest.rebaseAbort();
		assertEquals(0, abortResult.exitCode(), abortResult.outputText());
	}
}
