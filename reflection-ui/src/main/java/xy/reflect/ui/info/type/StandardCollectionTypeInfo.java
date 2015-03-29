package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ListControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardCollectionTypeInfo extends DefaultTypeInfo implements
		IListTypeInfo {

	protected Class<?> itemJavaType;

	public StandardCollectionTypeInfo(ReflectionUI reflectionUI,
			Class<?> javaType, Class<?> itemJavaType) {
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
					if (javaType.isAssignableFrom(ArrayList.class)) {
						return new ArrayList<Object>();
					} else if (javaType.isAssignableFrom(HashSet.class)) {
						return new HashSet<Object>();
					} else {
						throw new ReflectionUIError();
					}
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
	public Object fromListValue(Object[] listValue) {
		IMethodInfo constructor = ReflectionUIUtils
				.getZeroParameterConstrucor(this);
		Collection result = (Collection) constructor.invoke(null,
				Collections.<String, Object> emptyMap());
		for (Object item : listValue) {
			if (result instanceof Set) {
				if (result.contains(item)) {
					throw new ReflectionUIError("Duplicate item: '"
							+ reflectionUI.toString(item) + "'");
				}
			}
			result.add(item);
		}
		return result;
	}

	@Override
	public Object[] toListValue(Object value) {
		return ((Collection<?>) value).toArray();
	}

	@Override
	public IListStructuralInfo getStructuralInfo() {
		ITypeInfo itemType = getItemType();
		try {
			TabularTreetStructuralInfo tabularInfo = new TabularTreetStructuralInfo(
					reflectionUI, itemType);
			if (tabularInfo.getColumnFields().size() >= 3) {
				return tabularInfo;
			}
		} catch (Exception ignore) {
		}
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
				((StandardCollectionTypeInfo) obj).itemJavaType)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isOrdered() {
		return List.class.isAssignableFrom(javaType);
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Collection.class.isAssignableFrom(javaType)) {
			if (ReflectionUIUtils
					.getZeroParameterConstrucor(new DefaultTypeInfo(
							new ReflectionUI(), javaType)) != null) {
				return true;
			}
			if (javaType.isAssignableFrom(ArrayList.class)) {
				return true;
			}
			if (javaType.isAssignableFrom(HashSet.class)) {
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
