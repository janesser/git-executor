package de.esserjan.edu.imbecile.test;

import de.esserjan.edu.imbecile.ImbecileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class GitTestSupport {
    final Logger log = LoggerFactory.getLogger(getClass());

    static final String ORIGIN = "origin";

    void assertExitCodeZero(ImbecileResult result) {
        log.debug(result.getOutputText());
        assertEquals(0, result.getExitCode(), result.getOutputText());
    }
}
