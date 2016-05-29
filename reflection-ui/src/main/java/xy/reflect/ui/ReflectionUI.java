package xy.reflect.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldAsListListTypeInfo.MultipleFieldAsListItem;
import xy.reflect.ui.info.field.MultipleFieldAsListListTypeInfo.MultipleFieldAsListItemTypeInfo;
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
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.util.HiddenNullableFacetsInfoProxyGenerator;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

public class ReflectionUI {

	protected Map<ITypeInfoSource, ITypeInfo> typeInfoBySource = CacheBuilder.newBuilder().maximumSize(1000)
			.<ITypeInfoSource, ITypeInfo> build().asMap();
	protected SwingRenderer swingRenderer;	
	protected InfoCustomizations infoCustomizations;
	protected String infoCustomizationsFilePath;

	public ReflectionUI() {
		initializeInfoCustomizations();
		swingRenderer = createSwingRenderer();
	}

	public static void main(String[] args) {
		ReflectionUI reflectionUI = new ReflectionUI();
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
			Object object = reflectionUI.getSwingRenderer().onTypeInstanciationRequest(null,
					reflectionUI.getTypeInfo(new JavaTypeInfoSource(clazz)), false);
			if (object == null) {
				return;
			}
			reflectionUI.getSwingRenderer().openObjectFrame(object, reflectionUI.getObjectTitle(object),
					reflectionUI.getSwingRenderer().getIconImage(object));
		} catch (Throwable t) {
			reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(null, t);
		}
	}

	protected void initializeInfoCustomizations() {
		try {
			infoCustomizationsFilePath = getInfoCustomizationsFilePath();
			if (infoCustomizationsFilePath != null) {
				infoCustomizations = new InfoCustomizations(this);
				File file = new File(infoCustomizationsFilePath);
				if (file.exists()) {
					infoCustomizations.loadFromFile(file);
				}
			}
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
	}

	public InfoCustomizations getInfoCustomizations() {
		return infoCustomizations;
	}

	public String getInfoCustomizationsFilePath() {
		return System.getProperty(SystemProperties.INFO_CUSTOMIZATIONS_FILE);
	}

	protected SwingRenderer createSwingRenderer() {
		return new SwingRenderer(this);
	}

	public final SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public boolean canCopy(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof Serializable) {
			return true;
		}
		return false;
	}

	public Object copy(Object object) {
		if (object == null) {
			return null;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object copy = ois.readObject();
			return copy;
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not copy object: " + t.toString());
		}
	}

	public boolean equals(Object value1, Object value2) {
		return ReflectionUIUtils.equalsOrBothNull(value1, value2);
	}

	public String toString(Object object) {
		if (object == null) {
			return null;
		}
		return getTypeInfo(getTypeInfoSource(object)).toString(object);
	}

	public ITypeInfoSource getTypeInfoSource(Object object) {
		if (object instanceof PrecomputedTypeInfoInstanceWrapper) {
			return ((PrecomputedTypeInfoInstanceWrapper) object).getPrecomputedTypeInfoSource();
		} else if (object instanceof MultipleFieldAsListItem) {
			return new PrecomputedTypeInfoSource(
					new MultipleFieldAsListItemTypeInfo(this, (MultipleFieldAsListItem) object));
		} else {
			return new JavaTypeInfoSource(object.getClass());
		}
	}

	public void logError(Throwable t) {
		t.printStackTrace();
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
			} else {
				throw new ReflectionUIError();
			}
			typeInfoBySource.put(typeSource, result);
		}
		if (SystemProperties.hideNullablefacets()) {
			result = new HiddenNullableFacetsInfoProxyGenerator(this).get(result);
		}
		if (infoCustomizations != null) {
			result = infoCustomizations.get(result);
		}
		return result;
	}

	public String prepareStringToDisplay(String string) {
		return string;
	}

	public String getObjectTitle(Object object) {
		if (object == null) {
			return "(Missing Value)";
		}
		if (object instanceof MultipleFieldAsListItem) {
			return ((MultipleFieldAsListItem) object).toString();
		}
		if (object instanceof StandardMapEntry<?, ?>) {
			Object key = ((StandardMapEntry<?, ?>) object).getKey();
			if (key == null) {
				return "";
			} else {
				return toString(key);
			}
		}
		return getTypeInfo(getTypeInfoSource(object)).getCaption();
	}

	public String getFieldTitle(Object object, IFieldInfo field) {
		String result = ReflectionUIUtils.composeTitle(getObjectTitle(object), field.getCaption());
		Object fieldValue = field.getValue(object);
		if (fieldValue != null) {
			String fieldValueKind = getObjectTitle(field.getValue(object));
			if (!field.getCaption().equals(fieldValueKind)) {
				result = ReflectionUIUtils.composeTitle(result, fieldValueKind);
			}
		}
		return result;
	}

	public String getMethodTitle(Object object, IMethodInfo method, Object returnValue, String context) {
		String result = method.getCaption();
		if (object != null) {
			result = ReflectionUIUtils.composeTitle(getObjectTitle(object), result);
		}
		if (context != null) {
			result = ReflectionUIUtils.composeTitle(result, context);
		}
		if (returnValue != null) {
			result = ReflectionUIUtils.composeTitle(result, getObjectTitle(returnValue));
		}
		return result;
	}

	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

}
