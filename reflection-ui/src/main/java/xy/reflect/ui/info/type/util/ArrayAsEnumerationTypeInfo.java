package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

@SuppressWarnings("unused")
public class ArrayAsEnumerationTypeInfo implements IEnumerationTypeInfo {
	protected ReflectionUI reflectionUI;
	protected Object[] array;
	protected String typeCaption;

	public ArrayAsEnumerationTypeInfo(ReflectionUI reflectionUI, Object[] array, String typeCaption) {
		super();
		this.reflectionUI = reflectionUI;
		this.array = array;
		this.typeCaption = typeCaption;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getCaption() {
		return typeCaption;
	}

	@Override
	public void validate(Object object) throws Exception {
	}

	@Override
	public String toString(Object object) {
		return object.toString();
	}

	@Override
	public boolean supportsInstance(Object object) {
		return Arrays.asList(array).contains(object);
	}

	@Override
	public boolean isConcrete() {
		return true;
	}

	@Override
	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return null;
	}

	@Override
	public List<IMethodInfo> getMethods() {
		return Collections.emptyList();
	}

	@Override
	public List<IFieldInfo> getFields() {
		return Collections.emptyList();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		if (array.length == 0) {
			return Collections.emptyList();
		} else {
			return Collections
					.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(ArrayAsEnumerationTypeInfo.this) {

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							return array[0];
						}

						@Override
						public List<IParameterInfo> getParameters() {
							return Collections.emptyList();
						}
					});
		}
	}

	@Override
	public Object[] getPossibleValues() {
		return array;
	}

	@Override
	public IEnumerationItemInfo getValueInfo(final Object object) {
		return new IEnumerationItemInfo() {

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
			}

			@Override
			public String getOnlineHelp() {
				return null;
			}

			@Override
			public String getName() {
				return reflectionUI.toString(object);
			}

			@Override
			public String getCaption() {
				return reflectionUI.toString(object);
			}
		};
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(array);
		result = prime * result + ((typeCaption == null) ? 0 : typeCaption.hashCode());
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
		ArrayAsEnumerationTypeInfo other = (ArrayAsEnumerationTypeInfo) obj;
		if (!Arrays.equals(array, other.array))
			return false;
		if (typeCaption == null) {
			if (other.typeCaption != null)
				return false;
		} else if (!typeCaption.equals(other.typeCaption))
			return false;
		return true;
	}

	public void unregisterArrayItem(Object resultEnumItem) {
		reflectionUI.unregisterPrecomputedTypeInfoObject(resultEnumItem);
	}

	public void registerArrayItem(Object item) {
		reflectionUI.registerPrecomputedTypeInfoObject(item, this);
	}

}
