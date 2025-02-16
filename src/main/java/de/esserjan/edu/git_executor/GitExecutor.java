package de.esserjan.edu.git_executor;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GitExecutor allows direct access to git-cli.
 * 
 * Main inspiration form
 * https://github.com/JetBrains/intellij-community/tree/8b6e5ebc0cfccaad14323e47337ddac47c8347aa/plugins/git4idea/src/git4idea
 * 
 * @author Jan Esser <jesser@gmx.de>
 */
@Component
public class GitExecutor {

	public static enum PullMode {
		FF_ONLY, REBASE_MERGE
	}

	public static final String ENV_SSH_ASKPASS = "SSH_ASKPASS";
	private static final String SETSID = "setsid";

	private static final String[] prepend(String command, String... commandArgs) {
		return Stream.concat(Stream.of(command), Stream.of(commandArgs)).toArray(size -> new String[size]);
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private File gitExecutable;
	private File gitRepo = null;
	private Map<String, String> extraEnvs = new HashMap<>(0);

	/**
	 * defaults
	 */
	public GitExecutor() {
		this.gitExecutable = new File("git");
		this.extraEnvs = new HashMap<String, String>(0);
	}

	public void setGitRepo(File gitRepo) {
		this.gitRepo = gitRepo;
	}

	public void setGitExecutable(File gitExecutable) {
		this.gitExecutable = gitExecutable;
	}

	public Map<String, String> getExtraEnvs() {
		return extraEnvs;
	}

	private GitExecutionResult gitExec(String gitCommand, List<String> gitCommandCall) throws GitExecutionException {
		return gitExec(gitCommand, gitCommandCall.toArray(size -> new String[size]));
	}

	private GitExecutionResult gitExec(String gitCommand, String... gitCommandArgs) throws GitExecutionException {
		try {
			return gitExec(gitRepo, prepend(gitCommand, gitCommandArgs));
		} catch (IOException | InterruptedException ex) {
			throw new GitExecutionException(ex);
		}
	}

	private GitExecutionResult gitExec(File workingDir, String[] gitCommands) throws IOException, InterruptedException {
		String[] commandStrings = prepend(gitExecutable.getPath(), gitCommands);

		if (this.extraEnvs.containsKey(ENV_SSH_ASKPASS)) {
			// https://stackoverflow.com/questions/38354773/how-to-pass-an-ssh-key-passphrase-via-environment-variable
			commandStrings = prepend(SETSID, commandStrings);
			log.info("Detected {} pointing to {}", ENV_SSH_ASKPASS, this.extraEnvs.get(ENV_SSH_ASKPASS));
		}

		final ProcessBuilder pb = new ProcessBuilder(commandStrings);

		if (workingDir.exists())
			pb.directory(workingDir);

		pb.redirectErrorStream(true);
		pb.redirectInput(Redirect.PIPE);
		pb.redirectOutput(Redirect.PIPE);

		Map<String, String> environment = pb.environment();
		environment.putAll(this.extraEnvs);

		log.debug("Executing: {}", (Object) commandStrings);
		log.debug("With extraEnvs: {}", this.extraEnvs);
		log.debug("In workingDir: {}", workingDir);
		Process p = pb.start();

		StringBuilder sb = new StringBuilder();
		// openTerminalOverProcess(p, sb);

		try (

				Scanner scanner = new Scanner(p.getInputStream())) {
			for (; scanner.hasNextLine();) {
				String nextLine = scanner.nextLine();
				sb.append(nextLine);
				sb.append('\n');
			}

			int waitFor = p.waitFor();
			return new GitExecutionResult(waitFor, sb.toString());
		}
	}

//	// FIXME rework this
//	private boolean openTerminalOverProcess(Process p, StringBuilder sb) {
//		ITerminalService terminalService = TerminalServiceFactory.getService();
//		if (terminalService == null)
//			return false;
//
//		Map<String, Object> terminalProperties = new HashMap<>();
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_TITLE, "GitExecutor");
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, true);
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_ENCODING, StandardCharsets.UTF_8.name());
//
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
//				"org.eclipse.tm.terminal.connector.process.launcher.process");
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_PROCESS_OBJ, p);
//
//		ITerminalServiceOutputStreamMonitorListener[] listeners = new ITerminalServiceOutputStreamMonitorListener[] {
//				(byteBuffer, bytesRead) -> sb.append(new String(byteBuffer, StandardCharsets.UTF_8)) //
//		};
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS, listeners);
//		terminalProperties.put(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS, listeners);
//
////		terminalProperties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
////				"org.eclipse.tm.terminal.connector.streams.StreamsConnector");
////		terminalProperties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDIN, p.getOutputStream());
////		terminalProperties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT, p.getInputStream());
////		terminalProperties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDERR, p.getErrorStream());
//
//		terminalService.openConsole(terminalProperties, new Done() {
//
//			@Override
//			public void done(IStatus status) {
//				if (!status.isOK())
//					throw new RuntimeException("terminal failed to complete OK. " + status);
//			}
//
//		});
//
//		return true;
//	}

	public GitExecutionResult version() throws GitExecutionException {
		return gitExec("--version");
	}

	public GitExecutionResult fetch() throws GitExecutionException {
		return gitExec("fetch");
	}

	public GitExecutionResult fetch(String remote) throws GitExecutionException {
		return gitExec("fetch", remote);
	}

	public GitExecutionResult pull() throws GitExecutionException {
		return pull(PullMode.FF_ONLY);
	}

	public GitExecutionResult pull(PullMode mode) throws GitExecutionException {
		String pullArg;
		switch (mode) {
		case FF_ONLY:
			pullArg = "--ff-only";
			break;
		case REBASE_MERGE:
			pullArg = "--rebase=merges";
			break;
		default:
			throw new RuntimeException("NOT IMPLEMENTED");
		}

		return gitExec("pull", pullArg);
	}

	public GitExecutionResult reset(boolean hard, String commitId) throws GitExecutionException {
		return gitExec("reset", hard ? "--hard" : "", commitId);
	}

	public GitExecutionResult commit(String message) throws GitExecutionException {
		return commit(message, false, false, false);
	}

	public GitExecutionResult commit(String message, boolean amend, boolean allowEmpty, boolean commitAll)
			throws GitExecutionException {
		if (commitAll)
			this.addAll();

		List<String> commitArgs = new ArrayList<String>();
		commitArgs.add("-m");
		commitArgs.add("\"" + message + "\"");

		if (amend)
			commitArgs.add("--amend");

		if (allowEmpty)
			commitArgs.add("--allow-empty");

		return gitExec("commit", commitArgs);
	}

	public GitExecutionResult rebase(String onto) throws GitExecutionException {
		return gitExec("rebase", "--onto", onto);
	}

	public GitExecutionResult rebaseAbort() throws GitExecutionException {
		return gitExec("rebase", "--abort");
	}

	/**
	 * @see https://stackoverflow.com/a/3879077
	 * @return
	 * @throws GitExecutionException
	 */
	public boolean hasChanges() throws GitExecutionException {
		GitExecutionResult refreshIndexResult = gitExec("update-index", "--refresh");
		if (refreshIndexResult.exitCode() != 0)
			return true;

		GitExecutionResult diffIndexResult = gitExec("diff-index", "--quiet", "HEAD");
		if (diffIndexResult.exitCode() != 0)
			return true;

		return false; // otherwise
	}

	public GitExecutionResult addAll() throws GitExecutionException {
		return gitExec("add", "-A", ":/");
	}

	public GitExecutionResult add(File f) throws GitExecutionException {
		return gitExec("add", "-A", this.gitRepo.toPath().relativize(f.toPath()).toString());
	}

	public GitExecutionResult clean() throws GitExecutionException {
		return gitExec("clean", "-fd", ":/");
	}

	public GitExecutionResult addRemote(String gitRemote, String gitRemoteUrl) throws GitExecutionException {
		return gitExec("remote", "add", gitRemote, gitRemoteUrl);
	}

	public GitExecutionResult removeRemote(String gitRemote) throws GitExecutionException {
		return gitExec("remote", "remove", gitRemote);
	}

	public GitExecutionResult clone(String gitRepoRemote, File gitRepoDir, Optional<Integer> depth)
			throws GitExecutionException {
		if (depth.isPresent())
			return gitExec("clone", "--depth", depth.get().toString(), gitRepoRemote, gitRepoDir.getPath());
		else
			return gitExec("clone", gitRepoRemote, gitRepoDir.getPath());
	}

}