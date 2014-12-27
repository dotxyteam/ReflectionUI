package xy.reflect.ui.info.annotation;
 
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 


@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Property { 
	public String key();
	public String value();
 
}