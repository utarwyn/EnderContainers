package fr.utarwyn.endercontainers.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation Configurable.
 * Used to fill attributes in {@link Configuration} class.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Configurable {

    /**
     * Custom key in the config YML file
     *
     * @return The config key
     */
    String key() default "";

}
