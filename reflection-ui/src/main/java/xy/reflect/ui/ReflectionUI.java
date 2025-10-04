
package xy.reflect.ui;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.app.ApplicationInfoProxy;
import xy.reflect.ui.info.app.DefaultApplicationInfo;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntry;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.ValidationErrorRegistry;

/**
 * This class reads and interprets the metadata (usually the class) of objects
 * in order to propose an abstract UI model (ITypeInfo) that a renderer can use
 * to generate a working UI.
 * 
 * @author olitank
 *
 */
public class ReflectionUI {

	protected static ReflectionUI defaultInstance;

	protected static final Class<?> CACHED_CLASS_NOT_FOUND = (new Object() {
		@Override
		public String toString() {
			return "CACHED_CLASS_NOT_FOUND";
		}
	}).getClass();

	protected Map<ITypeInfoSource, ITypeInfo> typeInfoCache = createTypeInfoCache();
	protected final Object typeInfoCacheMutex = new Object();
	protected Map<String, Class<?>> classCache = createClassCache();
	protected final Object classCacheMutex = new Object();

	protected ValidationErrorRegistry validationErrorRegistry = createValidationErrorRegistry();

	/**
	 * @return the default instance of this class.
	 */
	public static ReflectionUI getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new ReflectionUI() {

				@Override
				public IApplicationInfo getApplicationInfo() {
					return new ApplicationInfoProxy(super.getApplicationInfo()) {

						@Override
						public boolean isSystemIntegrationCrossPlatform() {
							return true;
						}

						@Override
						public ColorSpecification getTitleBackgroundColor() {
							return SwingRendererUtils.getColorSpecification(Color.LIGHT_GRAY);
						}

						@Override
						public ColorSpecification getTitleForegroundColor() {
							return SwingRendererUtils.getColorSpecification(Color.DARK_GRAY);
						}

					};
				}

			};
		}
		return defaultInstance;
	}

	protected ValidationErrorRegistry createValidationErrorRegistry() {
		return new ValidationErrorRegistry();
	}

	protected Map<ITypeInfoSource, ITypeInfo> createTypeInfoCache() {
		return MiscUtils.newStandardCache(true);
	}

	protected Map<String, Class<?>> createClassCache() {
		return new HashMap<String, Class<?>>() {
			private static final long serialVersionUID = 1L;

			{
				for (Class<?> c : new Class[] { void.class, boolean.class, byte.class, char.class, short.class,
						int.class, float.class, double.class, long.class })
					put(c.getName(), c);
			}
		};
	}

	/**
	 * @param name
	 * @return The class corresponding to the given name. The result is loaded and
	 *         cached for subsequent calls
	 * @throws ClassNotFoundException If the target class is not found.
	 */
	public Class<?> loadClassThroughCache(String name) throws ClassNotFoundException {
		synchronized (classCacheMutex) {
			Class<?> c = classCache.get(name);
			if (c == null) {
				try {
					c = ClassUtils.forNameEvenIfPrimitive(name);
				} catch (ClassNotFoundException e) {
					c = CACHED_CLASS_NOT_FOUND;
				}
				classCache.put(name, c);
			}
			if (c == CACHED_CLASS_NOT_FOUND) {
				throw new ClassNotFoundException(name);
			}
			return c;
		}
	}

	/**
	 * @return the cache that stores {@link ITypeInfo} instances obtained from this
	 *         {@link ReflectionUI} instance. Its access should be synchronized on
	 *         the monitor returned by {@link #getTypeCacheMutex()}. Developers that
	 *         implement new {@link ITypeInfo} classes are encouraged to use this
	 *         map to store their instances in order to improve performances.
	 */
	public Map<ITypeInfoSource, ITypeInfo> getTypeInfoCache() {
		return typeInfoCache;
	}

	/**
	 * @return the {@link ValidationErrorRegistry} used by this {@link ReflectionUI}
	 *         instance.
	 */
	public ValidationErrorRegistry getValidationErrorRegistry() {
		return validationErrorRegistry;
	}

	/**
	 * @return the mutual-exclusion object (also called monitor) that should be used
	 *         to synchronize accesses and updates of the cache returned by
	 *         {@link #getTypeInfoCache()}. Note that using alternate monitors may
	 *         lead to dead-lock situations.
	 */
	public Object getTypeCacheMutex() {
		return typeInfoCacheMutex;
	}

	/**
	 * @param object Any object from which a UI needs to be generated.
	 * @return a source from which the UI-oriented type information of the given
	 *         object will be extracted.
	 */
	public ITypeInfoSource getTypeInfoSource(Object object) {
		if (object instanceof PrecomputedTypeInstanceWrapper) {
			return ((PrecomputedTypeInstanceWrapper) object).getTypeInfoSource();
		}
		if (object instanceof StandardMapEntry) {
			return new JavaTypeInfoSource(object.getClass(), ((StandardMapEntry) object).getGenericTypeParameters(),
					null);
		}
		return new JavaTypeInfoSource(object.getClass(), null);
	}

	/**
	 * @param typeInfoSource The source object needed to generate the UI-oriented
	 *                       type information.
	 * @return an object containing the UI-oriented type information extracted from
	 *         the given source and maybe customized. Note that calling
	 *         {@link ITypeInfo#getSource()} on the result returns an object equals
	 *         to the given typeInfoSource parameter.
	 */
	public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
		ITypeInfo result = typeInfoSource.buildTypeInfo(this);
		if (!result.getSource().equals(typeInfoSource)) {
			throw new ReflectionUIError("Calling " + ITypeInfo.class.getSimpleName()
					+ "#getSource() on the following instance does not return an object equals to the source object: "
					+ result);
		}
		return result;
	}

	/**
	 * @return the UI-oriented application (global) information.
	 */
	public IApplicationInfo getApplicationInfo() {
		return new DefaultApplicationInfo();
	}

	/**
	 * Formats the given message (used for logging).
	 * 
	 * @param msg The message to format.
	 * @return A formatted message.
	 */
	protected String formatLogMessage(String msg) {
		msg = MiscUtils.truncateNicely(msg, 20000);
		return SimpleDateFormat.getDateTimeInstance().format(new Date()) + " [" + ReflectionUI.class.getSimpleName()
				+ "] " + msg;
	}

	/**
	 * Logs the given message (to the console output stream by default) if the debug
	 * mode is active (see {@link SystemProperties#isDebugModeActive()}).
	 * 
	 * @param msg The message.
	 */
	public void logDebug(String msg) {
		if (!SystemProperties.isDebugModeActive()) {
			return;
		}
		System.out.println(formatLogMessage("DEBUG - " + msg));
	}

	/**
	 * Logs the given exception (to the console output stream by default) if the
	 * debug mode is active (see {@link SystemProperties#isDebugModeActive()}).
	 * 
	 * @param t The exception.
	 */
	public void logDebug(Throwable t) {
		logDebug(MiscUtils.getPrintedStackTrace(t));
	}

	/**
	 * Logs the given error message (to the console error stream by default). If the
	 * error is already displayed on the screen then {@link #logDebug(String)}
	 * should be used instead.
	 * 
	 * @param msg The message.
	 */
	public void logError(String msg) {
		System.err.println(formatLogMessage("ERROR - " + msg));
	}

	/**
	 * Logs the given exception (to the console error stream by default). If the
	 * error is already displayed on the screen then {@link #logDebug(Throwable)}
	 * should be used instead.
	 * 
	 * @param t The exception.
	 */
	public void logError(Throwable t) {
		logError(MiscUtils.getPrintedStackTrace(t));
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "ReflectionUI.DEFAULT";
		} else {
			return super.toString();
		}
	}

}
