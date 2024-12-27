package git_executor.test.osgi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;

@ExtendWith(BundleContextExtension.class)
public class GitExecutorBundleTest {
	@InjectBundleContext
	BundleContext bundleContext;

	@Test
	public void canActivatePlugin() {
		assertEquals("git-executor", bundleContext.getBundle().getSymbolicName());
		assertEquals(Bundle.ACTIVE, bundleContext.getBundle().getState());
	}
}