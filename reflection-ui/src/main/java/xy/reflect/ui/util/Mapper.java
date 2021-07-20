


package xy.reflect.ui.util;

/**
 * Simple generic mapper interface.
 * 
 * @author olitank
 *
 * @param <I> The input type.
 * @param <O> The output type.
 */
public interface Mapper<I, O> {

	/**
	 * @param i The input value.
	 * @return the output value corresponding to the input.
	 */
	public O get(I i);
}
