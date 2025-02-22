package de.esserjan.edu.git_executor.test.osgi;

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
@WithBundle("org.objectweb.asm")
@WithBundle("org.objectweb.asm.commons")
@WithBundle("org.objectweb.asm.tree")
@WithBundle("org.objectweb.asm.tree.analysis")
@WithBundle("org.objectweb.asm.util")
@WithBundle(value = "org.apache.aries.spifly.dynamic.bundle", start = true)
public @interface UseDynamicBundle {

}