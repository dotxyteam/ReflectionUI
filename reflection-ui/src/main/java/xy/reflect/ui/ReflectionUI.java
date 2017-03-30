package xy.reflect.ui;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.enumeration.StandardEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.ArrayTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapAsListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntryTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

public class ReflectionUI {

	protected static ReflectionUI defaultInstance;

	protected Map<ITypeInfoSource, ITypeInfo> typeInfoBySource = CacheBuilder.newBuilder().maximumSize(1000)
			.<ITypeInfoSource, ITypeInfo>build().asMap();
	protected Map<Object, ITypeInfo> precomputedTypeInfoByObject = CacheBuilder.newBuilder().weakKeys()
			.<Object, ITypeInfo>build().asMap();

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "ReflectionUI.DEFAULT";
		} else {
			return super.toString();
		}
	}

	public static ReflectionUI getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new ReflectionUI() {

				@Override
				public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
					ITypeInfo result = super.getTypeInfo(typeSource);
					if (SystemProperties.areDefaultInfoCustomizationsActive()) {
						result = InfoCustomizations.getDefault().get(this, result);
					}
					return result;
				}

			};
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
			return new PrecomputedTypeInfoSource(precomputedType);
		} else {
			return new JavaTypeInfoSource(object.getClass());
		}
	}

	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		ITypeInfo result = typeInfoBySource.get(typeSource);
		if (result == null) {
			if (typeSource instanceof PrecomputedTypeInfoSource) {
				result = ((PrecomputedTypeInfoSource) typeSource).getPrecomputedType();
			} else if (typeSource instanceof JavaTypeInfoSource) {
				JavaTypeInfoSource javaTypeSource = (JavaTypeInfoSource) typeSource;
				if (StandardCollectionTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					Class<?> itemClass = ReflectionUIUtils.getJavaGenericTypeParameter(javaTypeSource, Collection.class,
							0);
					ITypeInfo itemType;
					if (itemClass == null) {
						itemType = null;
					} else {
						itemType = getTypeInfo(new JavaTypeInfoSource(itemClass));
					}
					result = new StandardCollectionTypeInfo(this, javaTypeSource.getJavaType(), itemType);
				} else if (StandardMapAsListTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					Class<?> keyClass = ReflectionUIUtils.getJavaGenericTypeParameter(javaTypeSource, Map.class, 0);
					Class<?> valueClass = ReflectionUIUtils.getJavaGenericTypeParameter(javaTypeSource, Map.class, 1);
					result = new StandardMapAsListTypeInfo(this, javaTypeSource.getJavaType(), keyClass, valueClass);
				} else if (StandardMapEntryTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					Class<?> keyClass = null;
					Class<?> valueClass = null;
					Class<?>[] genericParams = javaTypeSource.getGenericTypeParameters();
					if (genericParams != null) {
						keyClass = genericParams[0];
						valueClass = genericParams[1];
					}
					result = new StandardMapEntryTypeInfo(this, keyClass, valueClass);
				} else if (javaTypeSource.getJavaType().isArray()) {
					result = new ArrayTypeInfo(this, javaTypeSource.getJavaType());
				} else if (javaTypeSource.getJavaType().isEnum()) {
					result = new StandardEnumerationTypeInfo(this, javaTypeSource.getJavaType());
				} else if (BooleanTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new BooleanTypeInfo(this, javaTypeSource.getJavaType());
				} else if (TextualTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new TextualTypeInfo(this, javaTypeSource.getJavaType());
				} else if (FileTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new FileTypeInfo(this);
				} else {
					result = new DefaultTypeInfo(this, javaTypeSource.getJavaType());
				}
				typeInfoBySource.put(typeSource, result);
			} else {
				throw new ReflectionUIError();
			}
		}
		return result;
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

	public void logError(String msg) {
		System.err.println(formatLogMessage("ERROR - " + msg));
	}

	public void logError(Throwable t) {
		logError(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	public static void main(String[] args) {
		try {
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
					ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(clazz)), false);
			if (object == null) {
				return;
			}
			SwingRenderer.getDefault().openObjectFrame(object);
		} catch (Throwable t) {
			SwingRenderer.getDefault().handleExceptionsFromDisplayedUI(null, t);
		}
	}
}
