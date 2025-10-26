package sions.json.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, CONSTRUCTOR, FIELD})
@Retention(RUNTIME)
public @interface JsonColumn {

	String name() default "";
	
}
