package xy.reflect.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldAsOne.ListItem;
import xy.reflect.ui.info.field.MultipleFieldAsOne.ListItemTypeInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.StandardEnumerationTypeInfo;
import xy.reflect.ui.info.type.UtilitiesTypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.iterable.ArrayTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapAsListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntry;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntryTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.util.HiddenNullableFacetsTypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public class ReflectionUI {

	public static final ReflectionUI DEFAULT = createDefault();

	protected Map<ITypeInfoSource, ITypeInfo> typeInfoBySource = CacheBuilder.newBuilder().maximumSize(1000)
			.<ITypeInfoSource, ITypeInfo> build().asMap();
	protected Map<Object, ITypeInfo> precomputedTypeInfoByObject = CacheBuilder.newBuilder().weakKeys()
			.<Object, ITypeInfo> build().asMap();

	protected static ReflectionUI createDefault() {
		return new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				ITypeInfo result = super.getTypeInfo(typeSource);
				if (SystemProperties.areDefaultInfoCustomizationsActive()) {
					result = InfoCustomizations.DEFAULT.get(this, result);
				}
				return result;
			}

		};
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
					Class<?> itemType = ReflectionUIUtils.getJavaGenericTypeParameter(javaTypeSource, Collection.class,
							0);
					result = new StandardCollectionTypeInfo(this, javaTypeSource.getJavaType(), itemType);
				} else if (StandardMapAsListTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					Class<?> keyType = ReflectionUIUtils.getJavaGenericTypeParameter(javaTypeSource, Map.class, 0);
					Class<?> valueType = ReflectionUIUtils.getJavaGenericTypeParameter(javaTypeSource, Map.class, 1);
					result = new StandardMapAsListTypeInfo(this, javaTypeSource.getJavaType(), keyType, valueType);
				} else if (StandardMapEntryTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					Class<?> keyType = javaTypeSource.getGenericTypeParameters()[0];
					Class<?> valueType = javaTypeSource.getGenericTypeParameters()[1];
					result = new StandardMapEntryTypeInfo(this, keyType, valueType);
				} else if (javaTypeSource.getJavaType().isArray()) {
					Class<?> itemType = javaTypeSource.getJavaType().getComponentType();
					result = new ArrayTypeInfo(this, javaTypeSource.getJavaType(), itemType);
				} else if (javaTypeSource.getJavaType().isEnum()) {
					result = new StandardEnumerationTypeInfo(this, javaTypeSource.getJavaType());
				} else if (BooleanTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new BooleanTypeInfo(this, javaTypeSource.getJavaType());
				} else if (TextualTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new TextualTypeInfo(this, javaTypeSource.getJavaType());
				} else if (FileTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new FileTypeInfo(this);
				} else if (UtilitiesTypeInfo.isCompatibleWith(javaTypeSource.getJavaType())) {
					result = new UtilitiesTypeInfo(this, javaTypeSource.getJavaType());
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

	public void logInformation(String msg) {
		System.out.println(msg);
	}

	public void logError(String msg) {
		System.err.println(msg);
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
			Object object = SwingRenderer.DEFAULT.onTypeInstanciationRequest(null,
					ReflectionUI.DEFAULT.getTypeInfo(new JavaTypeInfoSource(clazz)), false);
			if (object == null) {
				return;
			}
			SwingRenderer.DEFAULT.openObjectFrame(object);
		} catch (Throwable t) {
			SwingRenderer.DEFAULT.handleExceptionsFromDisplayedUI(null, t);
		}
	}
}
