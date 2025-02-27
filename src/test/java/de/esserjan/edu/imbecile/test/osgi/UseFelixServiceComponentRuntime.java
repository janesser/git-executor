package de.esserjan.edu.imbecile.test.osgi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle;

@Inherited
@Target({ ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@WithBundle("org.osgi.util.promise")
@WithBundle("org.osgi.util.function")
@WithBundle("org.osgi.service.component")
@WithBundle("org.osgi.dto")
@WithBundle(value = "org.apache.felix.scr", start = true)
public @interface UseFelixServiceComponentRuntime {

}