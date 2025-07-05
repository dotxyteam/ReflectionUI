
package xy.reflect.ui.info.type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Type information extracted from the Java type encapsulated in the given type
 * information source.
 * 
 * @author olitank
 *
 */
public class DefaultTypeInfo extends AbstractInfo implements ITypeInfo {

	protected JavaTypeInfoSource source;
	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> fields;
	protected List<IMethodInfo> methods;
	protected List<IMethodInfo> constructors;
	protected final Object mutex = new Object();

	public DefaultTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source) {
		if (source == null) {
			throw new ReflectionUIError();
		}
		this.reflectionUI = reflectionUI;
		this.source = source;
	}

	@Override
	public ITypeInfoSource getSource() {
		return source;
	}

	@Override
	public ITransaction createTransaction(Object object) {
		return null;
	}

	@Override
	public void onFormRefresh(Object object) {
	}

	@Override
	public Runnable getLastFormRefreshStateRestorationJob(Object object) {
		return null;
	}

	@Override
	public CategoriesStyle getCategoriesStyle() {
		return CategoriesStyle.getDefault();
	}

	@Override
	public ResourcePath getFormBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getFormBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormBorderColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormButtonBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormButtonForegroundColor() {
		return null;
	}

	@Override
	public ResourcePath getFormButtonBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getFormButtonBorderColor() {
		return null;
	}

	@Override
	public ColorSpecification getCategoriesBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getCategoriesForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormEditorForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormEditorBackgroundColor() {
		return null;
	}

	@Override
	public int getFormPreferredWidth() {
		return -1;
	}

	@Override
	public int getFormPreferredHeight() {
		return -1;
	}

	@Override
	public int getFormSpacing() {
		return ITypeInfo.DEFAULT_FORM_SPACING;
	}

	@Override
	public boolean onFormVisibilityChange(Object object, boolean visible) {
		return false;
	}

	public Class<?> getJavaType() {
		return source.getJavaType();
	}

	@Override
	public FieldsLayout getFieldsLayout() {
		return FieldsLayout.VERTICAL_FLOW;
	}

	@Override
	public MethodsLayout getMethodsLayout() {
		return MethodsLayout.HORIZONTAL_FLOW;
	}

	@Override
	public MenuModel getMenuModel() {
		return new MenuModel();
	}

	@Override
	public boolean isPrimitive() {
		return getJavaType().isPrimitive();
	}

	@Override
	public boolean isImmutable() {
		return ClassUtils.isKnownAsImmutableClass(getJavaType());
	}

	@Override
	public boolean isConcrete() {
		if ((!getJavaType().isPrimitive()) && Modifier.isAbstract(getJavaType().getModifiers())) {
			return false;
		}
		if (getJavaType().isInterface()) {
			return false;
		}
		if (getJavaType() == Object.class) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isModificationStackAccessible() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		synchronized (mutex) {
			if (constructors == null) {
				constructors = new ArrayList<IMethodInfo>();
				if (ClassUtils.isPrimitiveClassOrWrapper(getJavaType())) {
					final Class<?> primitiveClass;
					final Class<?> wrapperClass;
					if (ClassUtils.isPrimitiveClass(getJavaType())) {
						primitiveClass = getJavaType();
						wrapperClass = ClassUtils.primitiveToWrapperClass(primitiveClass);
					} else {
						wrapperClass = getJavaType();
						primitiveClass = ClassUtils.wrapperToPrimitiveClass(wrapperClass);
					}
					Constructor<?> primitiveParamWrapperCtor;
					try {
						primitiveParamWrapperCtor = wrapperClass.getConstructor(primitiveClass);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
					constructors
							.add(new DefaultConstructorInfo(reflectionUI, primitiveParamWrapperCtor, getJavaType()) {

								@Override
								public List<IParameterInfo> getParameters() {
									return Collections.emptyList();
								}

								@Override
								public Object invoke(Object ignore, InvocationData invocationData) {
									invocationData.getProvidedParameterValues().put(0,
											ClassUtils.getDefaultPrimitiveValue(primitiveClass));
									return super.invoke(ignore, invocationData);
								}

							});
				} else if (String.class == getJavaType()) {
					try {
						constructors.add(
								new DefaultConstructorInfo(reflectionUI, String.class.getConstructor(), getJavaType()));
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				} else {
					for (Constructor<?> javaConstructor : getJavaType().getConstructors()) {
						if (!DefaultConstructorInfo.isCompatibleWith(javaConstructor, getJavaType())) {
							continue;
						}
						constructors.add(new DefaultConstructorInfo(reflectionUI, javaConstructor, getJavaType()));
					}
				}
			}
			return constructors;
		}
	}

	@Override
	public String getName() {
		return getJavaType().getName();
	}

	@Override
	public String getCaption() {
		if (String.class.equals(getJavaType())) {
			return "Text";
		} else if (getJavaType().isPrimitive()) {
			return ClassUtils.primitiveToWrapperClass(getJavaType()).getSimpleName();
		} else {
			return ReflectionUIUtils.identifierToCaption(getJavaType().getSimpleName());
		}
	}

	@Override
	public List<IFieldInfo> getFields() {
		synchronized (mutex) {
			if (fields == null) {
				fields = new ArrayList<IFieldInfo>();
				Field[] javaFields = getJavaType().getFields();
				ReflectionUIUtils.sortFields(javaFields);
				for (Field javaField : javaFields) {
					if (!PublicFieldInfo.isCompatibleWith(javaField)) {
						continue;
					}
					fields.add(new PublicFieldInfo(reflectionUI, javaField, getJavaType()));
				}
				Method[] javaMethods = getJavaType().getMethods();
				ReflectionUIUtils.sortMethods(javaMethods);
				for (Method javaMethod : javaMethods) {
					if (!GetterFieldInfo.isCompatibleWith(javaMethod, getJavaType())) {
						continue;
					}
					GetterFieldInfo getterFieldInfo = new GetterFieldInfo(reflectionUI, javaMethod, getJavaType());
					fields.add(getterFieldInfo);
				}
			}
			return fields;
		}
	}

	@Override
	public List<IMethodInfo> getMethods() {
		synchronized (mutex) {
			if (methods == null) {
				methods = new ArrayList<IMethodInfo>();
				Method[] javaMethods = getJavaType().getMethods();
				ReflectionUIUtils.sortMethods(javaMethods);
				for (Method javaMethod : javaMethods) {
					if (!DefaultMethodInfo.isCompatibleWith(javaMethod, getJavaType())) {
						continue;
					}
					methods.add(new DefaultMethodInfo(reflectionUI, javaMethod, getJavaType()));
				}
			}
			return methods;
		}
	}

	@Override
	public boolean supports(Object object) {
		if (getJavaType().isPrimitive()) {
			if (object == null) {
				return false;
			}
			return ClassUtils.primitiveToWrapperClass(getJavaType()).isInstance(object);
		} else {
			if (object == null) {
				return true;
			}
			return getJavaType().isInstance(object);
		}
	}

	@Override
	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return Collections.emptyList();
	}

	@Override
	public String toString(Object object) {
		ReflectionUIUtils.checkInstance(this, object);
		if (object instanceof String) {
			return (String) object;
		} else if (ClassUtils.isPrimitiveClassOrWrapper(getJavaType())) {
			return ReflectionUIUtils.primitiveToString(object);
		} else {
			String result = object.toString();
			if (result == null) {
				result = "";
			}
			result = result
					.replaceAll(getJavaType().getName().replace(".", "\\.").replace("$", "\\$").replace("[", "\\[")
							+ "@([0-9a-z]+)", getCaption() + " [id=$1]");
			result = result.replace(getJavaType().getName(), getCaption());
			return result;
		}
	}

	@Override
	public ResourcePath getIconImagePath(Object object) {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public void validate(Object object, ValidationSession session) throws Exception {
		ReflectionUIUtils.checkInstance(this, object);
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public boolean canPersist() {
		if (Serializable.class.isAssignableFrom(getJavaType())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isValidationRequired() {
		return false;
	}

	@Override
	public void save(Object object, File outputFile) {
		try (FileOutputStream out = new FileOutputStream(outputFile)) {
			IOUtils.serialize(object, out);
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public void load(Object object, File inputFile) {
		Object loaded;
		try (FileInputStream in = new FileInputStream(inputFile)) {
			loaded = IOUtils.deserialize(in);
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
		try {
			ReflectionUIUtils.copyFieldValuesAccordingInfos(reflectionUI, loaded, object, true);
		} catch (Throwable t) {
			throw new ReflectionUIError("Deserializion error: Deep copy failure: " + t.toString(), t);
		}
	}

	@Override
	public boolean canCopy(Object object) {
		if (object == null) {
			return true;
		}
		ReflectionUIUtils.checkInstance(this, object);
		if (object instanceof Serializable) {
			return true;
		}
		return false;
	}

	@Override
	public Object copy(Object object) {
		if (object == null) {
			return null;
		}
		ReflectionUIUtils.checkInstance(this, object);
		if (object instanceof Serializable) {
			return IOUtils.copyThroughSerialization((Serializable) object);
		}
		throw new ReflectionUIError();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultTypeInfo other = (DefaultTypeInfo) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultTypeInfo [source=" + source + "]";
	}

}
