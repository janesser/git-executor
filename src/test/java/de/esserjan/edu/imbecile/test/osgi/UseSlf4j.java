package de.esserjan.edu.imbecile.test.osgi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle;

@Inherited
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@WithBundle("slf4j.api")
@WithBundle(value = "slf4j.simple", start = true)
public @interface UseSlf4j {

}