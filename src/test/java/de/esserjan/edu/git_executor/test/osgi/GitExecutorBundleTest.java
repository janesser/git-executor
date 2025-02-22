package de.esserjan.edu.git_executor.test.osgi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.launch.Framework;
import org.osgi.test.common.annotation.InjectService;

import de.esserjan.edu.git_executor.GitExecutor;
import de.laeubisoft.osgi.junit5.framework.annotations.EmbeddedFramework;
import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle;
import de.laeubisoft.osgi.junit5.framework.extension.FrameworkExtension;
import de.laeubisoft.osgi.junit5.framework.services.FrameworkEvents;

@ExtendWith(FrameworkExtension.class)
@UseFelixServiceComponentRuntime
@UseDynamicBundle
@UseSlf4j
@WithBundle(value = "git-executor", start = true)
public class GitExecutorBundleTest {
	@InjectService
	FrameworkEvents frameworkEvents;

	@BeforeEach
	public void checkService() {
		frameworkEvents.assertErrorFree();
	}

	@BeforeEach
	public void printFrameworkInfo(@EmbeddedFramework Framework framework) {
		FrameworkExtension.printBundles(framework, System.out::println);
		FrameworkExtension.printServices(framework, System.out::println);
	}

	@InjectService
	GitExecutor underTest;

	@Test
	public void gotInjected() {
		assertNotNull(underTest);
	}

}
