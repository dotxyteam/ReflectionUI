package xy.reflect.ui.info.type;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.ColorControl;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.PolymorphicEmbeddedForm;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorMethodInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationTypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.PrimitiveUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class DefaultTypeInfo implements ITypeInfo {

	protected Class<?> javaType;
	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> fields;
	protected List<IMethodInfo> methods;
	protected List<IMethodInfo> constructors;

	public DefaultTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		if (javaType == null) {
			throw new ReflectionUIError();
		}
		this.reflectionUI = reflectionUI;
		this.javaType = javaType;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	@Override
	public boolean isConcrete() {
		if ((!javaType.isPrimitive()) && Modifier.isAbstract(javaType.getModifiers())) {
			return false;
		}
		if (javaType == Object.class) {
			return false;
		}
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		if (constructors == null) {
			constructors = new ArrayList<IMethodInfo>();
			if (!isConcrete()) {
				return Collections.emptyList();
			}
			for (Constructor<?> javaConstructor : javaType.getConstructors()) {
				if (!DefaultConstructorMethodInfo.isCompatibleWith(javaConstructor)) {
					continue;
				}
				constructors.add(new DefaultConstructorMethodInfo(reflectionUI, this, javaConstructor));
			}
		}
		return constructors;
	}

	@Override
	public String getName() {
		return javaType.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(javaType.getSimpleName());
	}

	@Override
	public String toString() {
		return getCaption();
	}

	
	@Override
	public List<IFieldInfo> getFields() {
		if (fields == null) {
			fields = new ArrayList<IFieldInfo>();
			for (Field javaField : javaType.getFields()) {
				if (!PublicFieldInfo.isCompatibleWith(javaField)) {
					continue;
				}
				fields.add(new PublicFieldInfo(reflectionUI, javaField, javaType));
			}
			for (Method javaMethod : javaType.getMethods()) {
				if (!GetterFieldInfo.isCompatibleWith(javaMethod, javaType)) {
					continue;
				}
				GetterFieldInfo getterFieldInfo = new GetterFieldInfo(reflectionUI, javaMethod, javaType);
				if (ReflectionUIUtils.findInfoByName(fields, getterFieldInfo.getName()) != null) {
					continue;
				}
				fields.add(getterFieldInfo);
			}
			ReflectionUIUtils.sortFields(fields);
		}		
		return fields;
	}

	@Override
	public List<IMethodInfo> getMethods() {
		if (methods == null) {
			methods = new ArrayList<IMethodInfo>();
			for (Method javaMethod : javaType.getMethods()) {
				if (!DefaultMethodInfo.isCompatibleWith(javaMethod, javaType)) {
					continue;
				}
				methods.add(new DefaultMethodInfo(reflectionUI, javaMethod));
			}
			ReflectionUIUtils.sortMethods(methods);
		}
		return methods;
	}

	@Override
	public boolean supportsInstance(Object object) {
		if (javaType.isPrimitive()) {
			return PrimitiveUtils.primitiveToWrapperType(javaType).isInstance(object);
		} else {
			return javaType.isInstance(object);
		}
	}

	@Override
	public int hashCode() {
		return javaType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!javaType.equals(((DefaultTypeInfo) obj).javaType)) {
			return false;
		}
		return true;
	}

	@Override
	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return null;
	}

	@Override
	public String toString(Object object) {
		if (object == null) {
			return null;
		} else {
			String result = object.toString();
			String objectClassName = object.getClass().getName();
			String objectClassCaption = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object)).getCaption();
			result = result.replaceAll(
					objectClassName.replace(".", "\\.").replace("$", "\\$").replace("[", "\\[") + "@([0-9a-z]+)",
					objectClassCaption + " ($1)");
			result = result.replace(objectClassName, objectClassCaption);
			return result;
		}
	}

	@Override
	public String getOnlineHelp() {
		return ReflectionUIUtils.getAnnotatedInfoOnlineHelp(javaType);
	}

	@Override
	public void validate(Object object) throws Exception {
		for (Method method : ReflectionUIUtils.getAnnotatedValidatingMethods(javaType)) {
			try {
				method.invoke(object);
			} catch (InvocationTargetException e) {
				throw new ReflectionUIError(e.getCause());
			}
		}
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public Component createFieldControl(final Object object, final IFieldInfo field) {
		if (field.getType().getPolymorphicInstanceSubTypes() != null) {
			return new PolymorphicEmbeddedForm(reflectionUI, object, field);
		} else {
			if (field.isNullable()) {
				return new NullableControl(reflectionUI, object, field, new Accessor<Component>() {
					@Override
					public Component get() {
						return createNonNullFieldValueControl(object, field);
					}
				});
			} else {
				return createNonNullFieldValueControl(object, field);
			}
		}
	}

	protected Component createNonNullFieldValueControl(Object object, IFieldInfo field) {
		if (field.getValueOptions(object) != null) {
			return createOptionsControl(object, field);
		} else {
			Component customFieldControl = createCustomNonNullFieldValueControl(object, field);
			if (customFieldControl != null) {
				return customFieldControl;
			} else {
				field = SwingRendererUtils.prepareEmbeddedFormCreation(reflectionUI, object, field);
				if (SwingRendererUtils.isEmbeddedFormCreationForbidden(field)) {
					return new DialogAccessControl(reflectionUI, object, field);
				} else {
					return new EmbeddedFormControl(reflectionUI, object, field);
				}
			}
		}
	}

	protected Component createCustomNonNullFieldValueControl(Object object, IFieldInfo field) {
		if (javaType == Color.class) {
			return new ColorControl(reflectionUI, object, field);
		} else {
			return null;
		}
	}

	protected Component createOptionsControl(final Object object, final IFieldInfo field) {
		return new EnumerationControl(reflectionUI, object, new FieldInfoProxy(field) {

			@Override
			public ITypeInfo getType() {
				return new ArrayAsEnumerationTypeInfo(reflectionUI, field.getValueOptions(object),
						field.getCaption() + " Value Options");
			}

		});
	}

	@Override
	public boolean hasCustomFieldControl(Object object, IFieldInfo field) {
		if (field.getType().getPolymorphicInstanceSubTypes() != null) {
			return true;
		} else if (field.getValueOptions(object) != null) {
			return true;
		} else {
			if (javaType == Color.class) {
				return true;
			} else {
				return false;
			}
		}
	}

}
