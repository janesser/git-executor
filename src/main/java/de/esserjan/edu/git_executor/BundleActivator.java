package de.esserjan.edu.git_executor;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BundleActivator implements org.osgi.framework.BundleActivator {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private ServiceRegistration<GitExecutor> registration;
	
	@Override
	public void start(BundleContext context) throws Exception {
		registration = context.registerService(GitExecutor.class, new ServiceFactory<>() {

			@Override
			public GitExecutor getService(Bundle bundle, ServiceRegistration<GitExecutor> registration) {
				return new GitExecutor();
			}

			@Override
			public void ungetService(Bundle bundle, ServiceRegistration<GitExecutor> registration,
					GitExecutor service) {
				logger.debug("unget instance: {}", service);
			}
		}, null);
		logger.debug(GitExecutor.class.getClass() + " registered.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		context.ungetService(registration.getReference());

	}

}
