package git_executor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Test;

import git_executor.GitExecutionException;
import git_executor.GitExecutionResult;
import git_executor.GitExecutor;

public class GitExecutorTest {

	private GitExecutor underTest;

	public GitExecutorTest() throws GitExecutionException {
		underTest = new GitExecutor();
		if (TestData.GIT_PATH.exists())
			underTest.setGitExecutable(TestData.GIT_PATH);
		underTest.setGitRepo(TestData.GIT_REPO);
	}

	@Test
	public void canFindGitOnPath() throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(new String[] { "which", "git" });
		assertEquals(0, process.waitFor());
	}

	@Test
	public void canGitVersion() throws GitExecutionException {
		GitExecutionResult res = underTest.version();
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitReset() throws GitExecutionException {
		GitExecutionResult res = underTest.reset(true, TestData.GIT_HISTORICAL_COMMIT_ID);
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitClean() throws GitExecutionException, IOException {
		File f = new File(TestData.GIT_FOLDER, "cleanMe");
		f.createNewFile();
		assertTrue(f.exists());

		GitExecutionResult res = underTest.clean();
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
	public void canGitFetchHttps() throws GitExecutionException {
		GitExecutionResult res = underTest.fetch();
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitCommit() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult res = underTest.commit("empty commit", false, true, false);
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitPull() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult res = underTest.pull(); // FF_ONLY
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitPullDetectConflict() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult resCommit = underTest.commit("empty commit", false, true, false);
		assertEquals(0, resCommit.exitCode(), resCommit.outputText());

		GitExecutionResult res = underTest.pull();
		assertEquals(128, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitPullRebase() throws GitExecutionException {
		resetTestScenario(); // arrange

		GitExecutionResult resCommit = underTest.commit("empty commit", false, true, false);
		assertEquals(0, resCommit.exitCode(), resCommit.outputText());

		GitExecutionResult res = underTest.pull(GitExecutor.PullMode.REBASE_MERGE);
		assertEquals(0, res.exitCode(), res.outputText());
	}

	@Test
	public void canGitRebaseAbort() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		File targetF = Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()).toFile();
		Files.move(TestData.GIT_FILE.toPath(), targetF.toPath(), StandardCopyOption.REPLACE_EXISTING);

		GitExecutionResult resAdd = underTest.add(TestData.GIT_FILE);
		assertEquals(0, resAdd.exitCode());

		GitExecutionResult resCommit = underTest.commit("file deleted accidentaly oopsy");
		assertEquals(0, resCommit.exitCode(), resCommit.outputText());

		GitExecutionResult res = underTest.rebase(TestData.GIT_HISTORICAL_ONTO_COMMIT_ID);
		assertEquals(1, res.exitCode(), res.outputText());

		GitExecutionResult abortResult = underTest.rebaseAbort();
		assertEquals(0, abortResult.exitCode(), abortResult.outputText());
	}

	@Test
	public void canDetectUncommittedFile() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		assertFalse(underTest.hasChanges());

		Files.move(TestData.GIT_FILE.toPath(), Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()),
				StandardCopyOption.REPLACE_EXISTING);

		assertTrue(underTest.hasChanges());
	}

	@Test
	public void canStageAllFiles() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		Files.move(TestData.GIT_FILE.toPath(), Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()),
				StandardCopyOption.REPLACE_EXISTING);

		assertEquals(0, underTest.addAll().exitCode());
	}

	@Test
	public void canStageFile() throws GitExecutionException, IOException {
		resetTestScenario(); // arrange

		File targetF = Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()).toFile();
		assertTrue(targetF.createNewFile());

		assertEquals(0, underTest.add(targetF).exitCode());

		File nonExistentF = Paths.get(TestData.GIT_FOLDER.getPath(), "nonExistentFile").toFile();
		assertFalse(nonExistentF.exists());

		assertNotEquals(0, underTest.add(nonExistentF).exitCode());
	}
}
