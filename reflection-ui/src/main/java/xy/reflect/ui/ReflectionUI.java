package xy.reflect.ui;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.app.ApplicationInfoProxy;
import xy.reflect.ui.info.app.DefaultApplicationInfo;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

/**
 * This class reads and interprets the metadata (usually the class) of objects
 * in order to propose an abstract UI model (ITypeInfo).
 * 
 * @author olitank
 *
 */
public class ReflectionUI {

	protected static ReflectionUI defaultInstance;

	protected Map<Object, ITypeInfo> precomputedTypeInfoByObject = CacheBuilder.newBuilder().weakKeys()
			.<Object, ITypeInfo>build().asMap();

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
	 * Allows to associate an object with a predefined ITypeInfo instance.
	 * 
	 * @param object
	 *            The object to associate.
	 * @param type
	 *            The ITypeInfo instance to associate with the object.
	 */
	public void registerPrecomputedTypeInfoObject(Object object, ITypeInfo type) {
		precomputedTypeInfoByObject.put(object, type);
	}

	/**
	 * Allows to break the association between an object and a predefined ITypeInfo
	 * instance.
	 * 
	 * @param object
	 *            The object that was associated.
	 */
	public void unregisterPrecomputedTypeInfoObject(Object object) {
		precomputedTypeInfoByObject.remove(object);
	}

	/**
	 * @param object
	 *            Any object from which a UI needs to be created.
	 * @return a metadata object from which an abstract UI model will be extracted.
	 */
	public ITypeInfoSource getTypeInfoSource(Object object) {
		ITypeInfo precomputedType = precomputedTypeInfoByObject.get(object);
		if (precomputedType != null) {
			return new PrecomputedTypeInfoSource(precomputedType, null);
		} else {
			return new JavaTypeInfoSource(object.getClass(), null);
		}
	}

	/**
	 * @param typeInfoSource
	 *            The data object needed to generate the UI-oriented type
	 *            information.
	 * @return an object encapsulating UI-oriented type information.
	 */
	public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
		return typeInfoSource.getTypeInfo(this);
	}

	/**
	 * @return the common UI properties descriptor.
	 */
	public IApplicationInfo getApplicationInfo() {
		return new DefaultApplicationInfo();
	}

	protected String formatLogMessage(String msg) {
		return SimpleDateFormat.getDateTimeInstance().format(new Date()) + " [" + ReflectionUI.class.getSimpleName()
				+ "] " + msg;
	}

	public void logDebug(String msg) {
		if (!SystemProperties.isDebugModeActive()) {
			return;
		}
		System.out.println(formatLogMessage("DEBUG - " + msg));
	}

	public void logDebug(Throwable t) {
		logDebug(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	public void logError(String msg) {
		System.err.println(formatLogMessage("ERROR - " + msg));
	}

	public void logError(Throwable t) {
		logError(ReflectionUIUtils.getPrintedStackTrace(t));
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
