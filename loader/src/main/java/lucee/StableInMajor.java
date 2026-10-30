package lucee;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface of the loader API that extensions implement or call. Such an interface must
 * only be changed (methods added, removed or modified) with a new major version of Lucee, otherwise
 * extensions built for an older release of the same major version break.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StableInMajor {
}
