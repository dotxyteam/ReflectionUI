
package xy.reflect.ui.util;

/**
 * Useful utilities intended to be called through reflection.
 * 
 * @author olitank
 *
 */
public class Functions {

	public static boolean negate(boolean b) {
		return !b;
	}

	public static int increment(int i) {
		return i + 1;
	}

	public static int decrement(int i) {
		return i - 1;
	}

	public static int ratioToPercentage(float ratio) {
		return Math.round(ratio * 100);
	}

	public static long ratioToPercentage(double ratio) {
		return Math.round(ratio * 100);
	}

	public static float percentageToRatio(int percentage) {
		return percentage / 100f;
	}

	public static double percentageToRatio(long percentage) {
		return percentage / 100d;
	}

}
