

package xy.reflect.ui;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.app.ApplicationInfoProxy;
import xy.reflect.ui.info.app.DefaultApplicationInfo;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SystemProperties;

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

	/**
	 * Constructs an instance of this class.
	 */
	public ReflectionUI() {
	}

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

	/**
	 * @param object Any object from which a UI needs to be generated.
	 * @return an object from which the UI-oriented type information of the given
	 *         object will be extracted.
	 */
	public ITypeInfoSource getTypeInfoSource(Object object) {
		if (object instanceof PrecomputedTypeInstanceWrapper) {
			return ((PrecomputedTypeInstanceWrapper) object).getTypeInfoSource();
		}
		return new JavaTypeInfoSource(this, object.getClass(), null);
	}

	/**
	 * @param typeInfoSource The source object needed to generate the UI-oriented
	 *                       type information.
	 * @return an object containing the UI-oriented type information extracted from
	 *         the given source and maybe customized. Note that calling
	 *         {@link ITypeInfo#getSource()} on the result returns an object equals
	 *         to the given source.
	 */
	public ITypeInfo buildTypeInfo(ITypeInfoSource typeInfoSource) {
		ITypeInfo result = typeInfoSource.getTypeInfo();
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
