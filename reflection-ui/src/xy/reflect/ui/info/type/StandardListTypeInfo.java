package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ListControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardListTypeInfo extends DefaultTypeInfo implements
		IListTypeInfo {

	protected Class<?> itemJavaType;

	public StandardListTypeInfo(ReflectionUI reflectionUI, Class<?> javaType,
			Class<?> itemJavaType) {
		super(reflectionUI, javaType);
		this.itemJavaType = itemJavaType;
	}

	@Override
	public ITypeInfo getItemType() {
		if (itemJavaType == null) {
			return null;
		}
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(itemJavaType));
	}

	@Override
	public String getName() {
		return javaType.getName() + "(item: "
				+ ((itemJavaType == null) ? "?" : itemJavaType.getName()) + ")";
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getCaption() {
		if (itemJavaType == null) {
			return "List";
		} else {
			return "List of " + getItemType().getCaption();
		}
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	public Class<?> getItemJavaType() {
		return itemJavaType;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		if (ReflectionUIUtils.getNParametersMethod(super.getConstructors(), 0) != null) {
			return super.getConstructors();
		} else {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>(
					super.getConstructors());
			result.add(new AbstractConstructorMethodInfo(this) {

				@Override
				public Object invoke(Object object,
						Map<String, Object> valueByParameterName) {
					return new ArrayList<Object>();
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return Collections.emptyList();
				}
			});
			return result;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromStandardList(List<?> list) {
		IMethodInfo constructor = ReflectionUIUtils
				.getZeroParameterConstrucor(this);
		List result = (List) constructor.invoke(null,
				Collections.<String, Object> emptyMap());
		result.addAll(list);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<?> toStandardList(Object value) {
		return new ArrayList((List<?>) value);
	}

	@Override
	public IListStructuralInfo getStructuralInfo() {
		ITypeInfo itemType = getItemType();
		return new DefaultListStructuralInfo(reflectionUI, itemType);
	}

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new ListControl(reflectionUI, object, field);
	}

	@Override
	public int hashCode() {
		return javaType.hashCode() + itemJavaType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!javaType.equals(((DefaultTypeInfo) obj).javaType)) {
			return false;
		}
		if (!ReflectionUIUtils.equalsOrBothNull(itemJavaType,
				((StandardListTypeInfo) obj).itemJavaType)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isOrdered() {
		return true;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (List.class.isAssignableFrom(javaType)) {
			if (ReflectionUIUtils
					.getZeroParameterConstrucor(new DefaultTypeInfo(
							new ReflectionUI(), javaType)) != null) {
				return true;
			}
			if (javaType.isAssignableFrom(ArrayList.class)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isImmutable() {
		return false;
	}

	@Override
	public boolean hasCustomFieldControl() {
		return true;
	}

}
