package de.esserjan.edu.git_executor;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class GitExecutorActivator implements BundleActivator {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private ServiceRegistration<GitExecutor> registration;

	@Override
	public void start(BundleContext context) throws Exception {
		registration = context.registerService(GitExecutor.class, new GitExecutor(), new Hashtable<String, Object>(0));
		logger.debug("Service(s) registered.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		registration.unregister();
		logger.debug("Service(s) unregistered.");
	}

}
