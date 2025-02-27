package de.esserjan.edu.git_executor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

@Component
public class GitCredentials {

	private static final String[] GIT_CREDENTIAL_FILL = Stream.of("git", "credential", "fill").toArray(size -> new String[size]);
	private static final String[] GIT_CREDENTIAL_REJECT = Stream.of("git", "credential", "reject").toArray(size -> new String[size]);

	public GitExecutionResult store(String protocol, String host, String username, String password)
			throws GitExecutionException {
		try {
			return gitCredentialExec(GIT_CREDENTIAL_FILL, protocol, host, username, password);
		} catch (IOException | InterruptedException e) {
			throw new GitExecutionException(e);
		}
	}

	public GitExecutionResult erase(String protocol, String host, String username) throws GitExecutionException {
		try {
			return gitCredentialExec(GIT_CREDENTIAL_REJECT, protocol, host, username, null);
		} catch (IOException | InterruptedException e) {
			throw new GitExecutionException(e);
		}
	}

	private GitExecutionResult gitCredentialExec(String[] commandStrings, String protocol, String host,
			String username, String password) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(commandStrings);
		pb.redirectErrorStream(true);

		Process p = pb.start();

		BufferedWriter writer = p.outputWriter();

		Thread.sleep(100L);

		writer.write("protocol=" + protocol);
		writer.write(System.lineSeparator());
		writer.write("host=" + host);
		writer.write(System.lineSeparator());
		writer.write("username=" + username);
		writer.write(System.lineSeparator());
		if (password != null) {
			writer.write("password=" + password);
			writer.write(System.lineSeparator());
		}

		writer.write(System.lineSeparator());
		writer.flush();

		ConsoleOutputCollector collector = ConsoleOutputCollector.collect(p);
		return new GitExecutionResult(p.waitFor(), collector.toString());
	}

}
