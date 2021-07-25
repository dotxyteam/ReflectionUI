/*
 * 
 */
package xy.reflect.ui.util;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.control.swing.util.SwingRendererUtils;

/**
 * Various utilities.
 * 
 * @author olitank
 *
 */
public class MiscUtils {

	public static final String[] NEW_LINE_SEQUENCES = new String[] { "\r\n", "\n", "\r" };
	public static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

	public static String escapeRegex(String str) {
		return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
	}

	public static <BASE, C extends BASE> List<BASE> convertCollection(Collection<C> ts) {
		List<BASE> result = new ArrayList<BASE>();
		for (C t : ts) {
			result.add((BASE) t);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <BASE, C extends BASE> List<C> convertCollectionUnsafely(Collection<BASE> bs) {
		List<C> result = new ArrayList<C>();
		for (BASE b : bs) {
			result.add((C) b);
		}
		return result;
	}

	public static boolean equalsOrBothNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	public static String truncateNicely(String string, int maximumLength) {
		if (string.length() <= maximumLength) {
			return string;
		} else {
			return string.substring(0, maximumLength - 3) + "...";
		}
	}

	public static <T> Set<T> getIntersection(Set<T> s1, Set<T> s2) {
		HashSet<T> result = new HashSet<T>(s1);
		result.retainAll(s2);
		return result;
	}

	public static <K, V> List<K> getKeysFromValue(Map<K, V> map, Object value) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (MiscUtils.equalsOrBothNull(entry.getValue(), value)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public static String changeCase(String result, boolean upperElseLower, int subStringStart, int subStringEnd) {
		String subString = result.substring(subStringStart, subStringEnd);
		if (upperElseLower) {
			subString = subString.toUpperCase();
		} else {
			subString = subString.toLowerCase();
		}
		return result.substring(0, subStringStart) + subString + result.substring(subStringEnd);
	}

	public static String[] splitLines(String s) {
		if (s.length() == 0) {
			return new String[0];
		}
		return s.split(getNewLineRegex(), -1);
	}

	public static String getNewLineRegex() {
		return stringJoin(Arrays.asList(NEW_LINE_SEQUENCES), "|");
	}

	public static Object indentLines(String s, String tabulation) {
		String[] lines = splitLines(s);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				result.append("\n");
			}
			String line = lines[i];
			result.append(tabulation + line);
		}
		return result.toString();
	}

	public static <T> String stringJoin(T[] array, String separator) {
		return stringJoin(Arrays.asList(array), separator);
	}

	public static String stringJoin(List<?> list, String separator) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			Object item = list.get(i);
			if (i > 0) {
				result.append(separator);
			}
			if (item == null) {
				result.append("null");
			} else {
				result.append(item.toString());
			}
		}
		return result.toString();
	}

	public static String escapeHTML(String string, boolean preserveNewLines) {
		StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				else if (c == '\n')
					// Handle Newline
					if (preserveNewLines) {
						sb.append("<br/>");
					} else {
						sb.append(c);
					}
				else {
					int ci = 0xffff & c;
					if (ci < 160)
						// nothing special only 7 Bit
						sb.append(c);
					else {
						// Not 7 Bit use the unicode system
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
	}

	public static <T extends Comparable<T>> int compareNullables(T c1, T c2) {
		if (c1 == null) {
			if (c2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (c2 == null) {
				return 1;
			} else {
				return c1.compareTo(c2);
			}
		}
	}

	public static StackTraceElement[] createDebugStackTrace(int firstElementsToRemove) {
		StackTraceElement[] result = new Exception().getStackTrace();
		return Arrays.copyOfRange(result, 1 + firstElementsToRemove, result.length);
	}

	public static <T> boolean replaceItem(List<T> list, T t1, T t2) {
		int index = list.indexOf(t1);
		if (index == -1) {
			return false;
		}
		list.set(index, t2);
		return true;
	}

	public static String getUniqueID() {
		return new UID().toString();
	}

	public static Dimension getDefaultScreenSize() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Rectangle maximumBounds = SwingRendererUtils.getMaximumWindowBounds(gd);
		return maximumBounds.getSize();
	}

	public static String multiToSingleLine(String s) {
		return s.replaceAll("\\r\\n|\\n|\\r", " ");
	}

	public static <K, V> K getFirstKeyFromValue(Map<K, V> map, V value) {
		List<K> list = getKeysFromValue(map, value);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public static String getPrintedStackTrace(Throwable e) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(out));
		return out.toString();
	}

	public static String getPrettyErrorMessage(Throwable t) {
		return new ReflectionUIError(t).toString();
	}

	public static ExecutorService newExecutor(final String threadName, int minimumThreadCount) {
		ThreadPoolExecutor result = new ThreadPoolExecutor(minimumThreadCount, Integer.MAX_VALUE, 60000,
				TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread result = new Thread(r);
						result.setName(threadName);
						result.setDaemon(true);
						return result;
					}
				});
		return result;
	}

	public static <K, V> Map<K, V> newWeakKeysEqualityBasedMap() {
		return new WeakHashMap<K, V>();
	}

	public static <K, V> Map<K, V> newWeakValuesEqualityBasedMap() {
		return newAutoCleanUpCache(false, true, -1, 5000, "WeakValuesEqualityBasedMapCleaner");
	}

	public static <K, V> Map<K, V> newWeakKeysIdentityBasedMap() {
		return newAutoCleanUpCache(true, false, -1, 5000, "WeakKeysIdentityBasedMapCleaner");
	}

	public static <K, V> Map<K, V> newWeakKeysIdentityBasedCache(int maxSize) {
		return newAutoCleanUpCache(true, false, maxSize, 5000, "WeakKeysIdentityBasedCacheCleaner");
	}

	public static <K, V> Map<K, V> newAutoCleanUpCache(boolean weakKeys, boolean weakValues, int maxSize,
			final long cleanUpPeriodMilliseconds, String cleanUpThreadNamePrefix) {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
		if (maxSize != -1) {
			builder = builder.maximumSize(maxSize);
		}
		if (weakKeys) {
			builder = builder.weakKeys();
		}
		if (weakValues) {
			builder = builder.weakValues();
		}
		Cache<K, V> cache = builder.<K, V>build();
		Map<K, V> map = cache.asMap();
		final WeakReference<Map<K, V>> mapWeakRef = new WeakReference<Map<K, V>>(map);
		new Thread(cleanUpThreadNamePrefix + " [cache=" + cache + ", maxSize=" + maxSize + "]") {
			{
				setDaemon(true);
			}

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(cleanUpPeriodMilliseconds);
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
					Map<K, V> map = mapWeakRef.get();
					if (map == null) {
						break;
					}
					try {
						Method cleanUpMethod = map.getClass().getMethod("cleanUp");
						cleanUpMethod.setAccessible(true);
						cleanUpMethod.invoke(map);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				}
			}
		}.start();
		return map;
	}

	public static boolean isHTMLText(String text) {
		return Pattern.compile("\\s*<[hH][tT][mM][lL]>.*</[hH][tT][mM][lL]>\\s*", Pattern.DOTALL).matcher(text)
				.matches();
	}

}
