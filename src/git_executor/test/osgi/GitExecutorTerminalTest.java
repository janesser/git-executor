package git_executor.test.osgi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * https://vogella.com/blog/getting-started-with-osgi-declarative-services-2024/ 
 * (especially "2. IDE Setup")
 */
@ExtendWith(ServiceExtension.class)
public class GitExecutorTerminalTest extends GitExecutorServiceTest {
	
	// FIXME no junit5 extension for PlatformUI
	ITerminalService terminalService = TerminalServiceFactory.getService();

	@Test
	public void canInjectTerminalService() {
		assertNotNull(terminalService);
	}
}