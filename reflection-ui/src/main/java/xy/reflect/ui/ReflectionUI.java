package xy.reflect.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.app.DefaultApplicationInfo;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

public class ReflectionUI {

	protected static ReflectionUI defaultInstance;

	protected Map<Object, ITypeInfo> precomputedTypeInfoByObject = CacheBuilder.newBuilder().weakKeys()
			.<Object, ITypeInfo>build().asMap();

	public ReflectionUI() {
	}

	public static ReflectionUI getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new ReflectionUI();
		}
		return defaultInstance;
	}

	public void registerPrecomputedTypeInfoObject(Object object, ITypeInfo type) {
		precomputedTypeInfoByObject.put(object, type);
	}

	public void unregisterPrecomputedTypeInfoObject(Object object) {
		precomputedTypeInfoByObject.remove(object);
	}

	public ITypeInfoSource getTypeInfoSource(Object object) {
		ITypeInfo precomputedType = precomputedTypeInfoByObject.get(object);
		if (precomputedType != null) {
			return new PrecomputedTypeInfoSource(precomputedType, null);
		} else {
			return new JavaTypeInfoSource(object.getClass(), null);
		}
	}

	public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
		return typeInfoSource.getTypeInfo(this);
	}

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

	public static void main(String[] args) throws Exception {
		Class<?> clazz = Object.class;
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + SystemProperties.describe();
		if (args.length == 0) {
			clazz = Object.class;
		} else if (args.length == 1) {
			if (args[0].equals("--help")) {
				System.out.println(usageText);
				return;
			} else {
				clazz = Class.forName(args[0]);
			}
		} else {
			throw new IllegalArgumentException(usageText);
		}
		Object object = SwingRenderer.getDefault().onTypeInstanciationRequest(null,
				ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(clazz, null)), null);
		if (object == null) {
			return;
		}
		SwingRenderer.getDefault().openObjectFrame(object);
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
