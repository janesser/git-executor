package git_executor.test.osgi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

import git_executor.GitExecutor;

@ExtendWith(ServiceExtension.class)
public class GitExecutorServiceTest {
	
	@InjectService
	GitExecutor executor;

	@Test
	public void canInjectService() {
		assertNotNull(executor);
	}
	
}