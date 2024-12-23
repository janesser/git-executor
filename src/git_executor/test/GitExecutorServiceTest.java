package git_executor.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

import git_executor.GitExecutor;

/**
 * https://vogella.com/blog/getting-started-with-osgi-declarative-services-2024/ 
 * (especially "2. IDE Setup")
 */
@ExtendWith(ServiceExtension.class)
@Requirement(namespace="", name = "")
public class GitExecutorServiceTest {
	
	@InjectService
	GitExecutor executor;

	@Test
	public void canInjectService() {
		assertNotNull(executor);
	}
}