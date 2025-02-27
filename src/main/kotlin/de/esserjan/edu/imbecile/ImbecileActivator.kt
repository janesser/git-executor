package de.esserjan.edu.imbecile

import org.osgi.framework.*
import org.osgi.framework.BundleActivator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class ImbecileActivator : BundleActivator {

	private val logger: Logger = LoggerFactory.getLogger(this::class.java)
	private var registration: ServiceRegistration<Imbecile>? = null

	object ImbecileServiceFactory : ServiceFactory<Imbecile> {
		private val logger: Logger = LoggerFactory.getLogger(this::class.java)
		
		override fun getService(bundle: Bundle, registration: ServiceRegistration<Imbecile>): Imbecile =
			Imbecile()


		override fun ungetService(bundle: Bundle, registration: ServiceRegistration<Imbecile>, service: Imbecile?) {
			logger.debug("unget {}", service)
		}
	}

	override fun start(context: BundleContext) {
		context.registerService(
			Imbecile::class.java,
			ImbecileServiceFactory,
			Hashtable<String, Any>(0)
		)
	}

	override fun stop(context: BundleContext) {
		context.ungetService(registration?.reference)
		logger.debug("stopped {}", context)
	}
}