package de.esserjan.edu.imbecile.test;

import de.esserjan.edu.imbecile.Imbecile;
import de.esserjan.edu.imbecile.ImbecileResult;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GitExecutorTest extends GitTestSupport {

    private final Imbecile underTest;

    public GitExecutorTest() {
        underTest = new Imbecile();
        if (TestData.GIT_PATH.exists())
            underTest.setExecutable(TestData.GIT_PATH);
        underTest.setRepositoryDirectory(TestData.GIT_REPO);
    }

    @Test
    @Order(1)
    public void canFindGitOnPath() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"which", "git"});
        assertEquals(0, process.waitFor());
    }

    @Test
    @Order(2)
    public void canGitVersion() {
        ImbecileResult res = underTest.version();
        assertExitCodeZero(res);
    }

    @Test
    @Order(3)
    public void canGitClone() throws IOException, InterruptedException {
        int deleteExitCode = Runtime.getRuntime().exec("rm -fR " + TestData.GIT_REPO.getPath()).waitFor();
        assertEquals(0, deleteExitCode);
        assertFalse(TestData.GIT_REPO.exists());

        ImbecileResult res = underTest.clone(TestData.GIT_REPO_REMOTE, TestData.GIT_REPO, null, null);
        assertExitCodeZero(res);
        assertTrue(TestData.GIT_REPO.exists());
    }

    @Test
    @Order(4)
    public void canGitReset() {
        ImbecileResult res = underTest.reset(true, TestData.GIT_HISTORICAL_COMMIT_ID);
        assertExitCodeZero(res);
    }

    @Test
    @Order(4)
    public void canGitClean() throws IOException {
        File f = new File(TestData.GIT_FOLDER, "cleanMe");
        f.createNewFile();
        assertTrue(f.exists());

        ImbecileResult res = underTest.clean();
        assertExitCodeZero(res);

        assertFalse(f.exists());
    }

    private void resetTestScenario() {
        try {
            canGitReset();
            canGitClean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    public void canGitFetchHttps() {
        ImbecileResult res = underTest.fetch(ORIGIN);
        assertExitCodeZero(res);
    }

    @Test
    @Order(6)
    public void canGitCommit() {
        resetTestScenario(); // arrange

        ImbecileResult res = underTest.commit("empty commit", false, true, false);
        assertExitCodeZero(res);
    }

    @Test
    @Order(6)
    public void canDetectUncommittedFile() throws IOException {
        resetTestScenario(); // arrange

        assertFalse(underTest.hasChanges());

        Files.move(TestData.GIT_FILE.toPath(), Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()),
                StandardCopyOption.REPLACE_EXISTING);

        assertTrue(underTest.hasChanges());
    }

    @Test
    @Order(6)
    public void canStageAllFiles() throws IOException {
        resetTestScenario(); // arrange

        Files.move(TestData.GIT_FILE.toPath(), Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()),
                StandardCopyOption.REPLACE_EXISTING);

        ImbecileResult res = underTest.addAll();
        assertExitCodeZero(res);
    }

    @Test
    @Order(6)
    public void canStageFileNewFile() throws IOException {
        resetTestScenario(); // arrange

        File targetF = Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()).toFile();
        assertTrue(targetF.createNewFile());

        ImbecileResult res = underTest.add(targetF);
        assertExitCodeZero(res);
    }

    @Test
    @Order(6)
    public void cannotStageNonexistentFile() throws IOException {
        resetTestScenario(); // arrange

        File nonExistentF = Paths.get(TestData.GIT_FOLDER.getPath(), "nonExistentFile").toFile();
        assertFalse(nonExistentF.exists());

        ImbecileResult res = underTest.add(nonExistentF);
        assertEquals(128, res.getExitCode(), res.getOutputText());
    }

    @Test
    @Order(7)
    public void canGitPull() {
        resetTestScenario(); // arrange

        ImbecileResult res = underTest.pull(ORIGIN, Imbecile.PullMode.FF_ONLY);
        log.debug(res.getOutputText());
        assertExitCodeZero(res);
    }

    @Test
    @Order(8)
    public void canGitPullDetectConflict() {
        resetTestScenario(); // arrange

        ImbecileResult resCommit = underTest.commit("empty commit", false, true, false);
        assertExitCodeZero(resCommit);

        ImbecileResult res = underTest.pull(ORIGIN, Imbecile.PullMode.FF_ONLY);
        log.debug(res.getOutputText());
        assertEquals(128, res.getExitCode(), res.getOutputText());
    }

    @Test
    @Order(8)
    public void canGitPullRebase() {
        resetTestScenario(); // arrange

        ImbecileResult resCommit = underTest.commit("empty commit", false, true, false);
        assertExitCodeZero(resCommit);

        ImbecileResult res = underTest.pull(ORIGIN, Imbecile.PullMode.REBASE_MERGE);
        assertExitCodeZero(res);
    }

    @Test
    @Order(8)
    public void canGitRebaseAbort() throws IOException {
        resetTestScenario(); // arrange

        File targetF = Paths.get(TestData.GIT_FOLDER.getPath(), TestData.GIT_FILE.getName()).toFile();
        Files.move(TestData.GIT_FILE.toPath(), targetF.toPath(), StandardCopyOption.REPLACE_EXISTING);

        ImbecileResult resAdd = underTest.add(TestData.GIT_FILE);
        assertExitCodeZero(resAdd);

        ImbecileResult resCommit = underTest.commit("file deleted accidentaly oopsy", false, false, false);
        assertExitCodeZero(resCommit);

        ImbecileResult res = underTest.rebase(TestData.GIT_HISTORICAL_ONTO_COMMIT_ID);
        log.debug(res.getOutputText());
        assertEquals(1, res.getExitCode(), res.getOutputText());

        ImbecileResult abortResult = underTest.rebaseAbort();
        assertExitCodeZero(abortResult);
    }
}
