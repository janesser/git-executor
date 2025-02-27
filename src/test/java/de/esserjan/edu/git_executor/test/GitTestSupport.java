package de.esserjan.edu.git_executor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.esserjan.edu.git_executor.GitExecutionResult;

abstract class GitTestSupport {
	final Logger log = LoggerFactory.getLogger(getClass());

	void assertExitCodeZero(GitExecutionResult result) {
		log.debug(result.outputText());
		assertEquals(0, result.exitCode(), result.outputText());
	}
}
